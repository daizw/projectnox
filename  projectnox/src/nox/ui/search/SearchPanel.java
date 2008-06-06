package nox.ui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.MenuElement;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import nox.net.common.NoxToolkit;
import nox.ui.common.InfiniteProgressPanel;
import nox.ui.me.Cheyenne;

public abstract class SearchPanel extends JPanel{
	int advType;
	protected InfiniteProgressPanel glassPane;
	Container infinitePane = new JPanel();
	JButton searchPeersBtn = new JButton("Search");
	AdvTable searchResultTable;
	AdvTableModel model;

	//JXTANetwork MyLogin;
	Cheyenne parent;
	long startTime;
	
	protected SearchPanel(int type, Cheyenne chy){
		advType = type;
		parent =chy;
		//this.MyLogin = NoxToolkit.getNetwork();

		String[] columns = { "Name", "Description", "UUID", "Delay/ms", "Adv" };
		Object[][] data = {};

		model = new AdvTableModel(data, columns);
	}

	protected abstract void AddMouseListener();
	
	public void StopSearching() {
		System.out.println("Stop Hunting...");
		glassPane.stop();
		searchPeersBtn.setText("Search");
		//MyLogin.StopHunting();
	}

	private void processDiscoveryResults(Enumeration<Advertisement> advEnum){
		Advertisement adv;
		long curTime = new Date().getTime();
		System.out.println(curTime);
		// let's get the responding peer's advertisement
		System.out.println(curTime + ": [Got a Discovery Response]");
		
		if (advEnum != null) {
			while (advEnum.hasMoreElements()) {
				adv = (Advertisement) advEnum.nextElement();
				//System.out.println("peer: " + ((PeerAdvertisement)adv).getPeerID());
				searchResultTable.addRow(adv, curTime - startTime);
			}
		}
	}
	protected Container buildInfinitePanel() {
		JPanel pane = new JPanel(new BorderLayout());

		glassPane = new InfiniteProgressPanel("搜索中, 请稍候...", 12);
		glassPane.setScale(0.2d);
		Dimension size = new Dimension(100, 100);
		glassPane.setSize(size);
		glassPane.setPreferredSize(size);
		glassPane.setMaximumSize(size);
		glassPane.setMinimumSize(size);

		searchPeersBtn.setSize(size);
		searchPeersBtn.setPreferredSize(size);
		searchPeersBtn.setMaximumSize(size);
		searchPeersBtn.setMinimumSize(size);
		
		searchPeersBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (searchPeersBtn.getText() == "Search") {
					glassPane.start();
					/**
					 * 搜索进度指示器
					 */
					Thread indicating = new Thread(new Runnable() {
						public void run() {
							try {
								searchPeersBtn.setText("Stop");
								Thread.sleep(2000);
							} catch (InterruptedException ie) {
							}
						}
					}, "Indicating");
					indicating.start();
					Thread hunter = new Thread(new Runnable() {
						public void run() {
							//(String peerid, int AdvType, 
							//String attribute, String value, int threshold, DiscoveryListener listener) 
							//MyLogin.GoHunting(null, advType, null, null, 100, listener);
							//本地和远程搜索广告
							DiscoveryService ds = NoxToolkit.getNetworkManager().getNetPeerGroup().getDiscoveryService();
							Enumeration<Advertisement> result = null;
							while(searchPeersBtn.getText() == "Stop"){
								//获取远程广告到本地
								ds.getRemoteAdvertisements(null, advType, null, null, 65535);
								//获取本地广告
								try {
									result = ds.getLocalAdvertisements(advType, null, null);
								} catch (IOException e) {
									e.printStackTrace();
								}
								//处理得到的广告
								processDiscoveryResults(result);
								//线程暂停5s
								try {
									Thread.sleep(5*1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}, "Hunter");
					startTime = new Date().getTime();
					hunter.start();
				} else if (searchPeersBtn.getText() == "Stop") {
					glassPane.stop();
					searchPeersBtn.setText("Search");
					//MyLogin.StopHunting();
				}
			}
		});
		pane.add(BorderLayout.CENTER, glassPane);
		pane.add(BorderLayout.EAST, searchPeersBtn);
		pane.setBackground(Color.WHITE);
		return pane;
	}
}

@SuppressWarnings("serial")
class PeerSearchPanel extends SearchPanel{
	public PeerSearchPanel(Cheyenne chy) {
		super(DiscoveryService.PEER, chy);
		searchResultTable = new PeerAdvTable(model);
		AddMouseListener();
		JScrollPane scrollPane = new JScrollPane(searchResultTable);
		
		infinitePane = buildInfinitePanel();

		this.setLayout(new BorderLayout());
		this.add(BorderLayout.NORTH, infinitePane);
		this.add(BorderLayout.CENTER, scrollPane);
	}

	@Override
	protected void AddMouseListener() {
		searchResultTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent me) {
				JPopupMenu ResultOprMenu = new JPopupMenu();
				if (me.getButton() == MouseEvent.BUTTON3) {
					/*
					 * TODO 实现右键可选取JTable 的行 有缺陷: 在已选择某行的情况下才可用;否则无效
					 */
					searchResultTable.getVisibleRect();
					int row = me.getY() / searchResultTable.getRowHeight();
					searchResultTable.getSelectionModel()
							.setLeadSelectionIndex(row);

					ResultOprMenu.add(new AbstractAction("Add to my friend list") {
						public void actionPerformed(ActionEvent e) {
							// TODO 添加到好友列表
							int[] selected = searchResultTable
									.getSelectedRows();
							for (int i = 0; i < selected.length; i++) {
								Advertisement adv = (Advertisement) searchResultTable
										.getAdvAt(selected[i]);
								//System.out.println(adv);
								// TODO 根据广告标签确定添加好友是否需要验证; 暂时直接添加
								// adv.getID();
								parent.add2PeerList((PeerAdvertisement) adv, true);
							}
						}
					});
					ResultOprMenu.add(new AbstractAction("Talk to him/her") {
						public void actionPerformed(ActionEvent e) {
							// TODO 打开聊天窗口
						}
					});
					MenuElement els[] = ResultOprMenu.getSubElements();
					for (int i = 0; i < els.length; i++)
						els[i].getComponent().setBackground(Color.WHITE);
					ResultOprMenu.setLightWeightPopupEnabled(true);
					ResultOprMenu.pack();
					ResultOprMenu.show((Component) me.getSource(), me
							.getPoint().x, me.getPoint().y);
				}
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
				// AdvTable.this.getSelectedRow();
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
	}
}

@SuppressWarnings("serial")
class GroupSearchPanel extends SearchPanel{
	public GroupSearchPanel(Cheyenne chy) {
		super(DiscoveryService.GROUP, chy);
		searchResultTable = new GroupAdvTable(model);
		AddMouseListener();
		JScrollPane scrollPane = new JScrollPane(searchResultTable);

		infinitePane = buildInfinitePanel();
		
		this.setLayout(new BorderLayout());
		this.add(BorderLayout.NORTH, infinitePane);
		this.add(BorderLayout.CENTER, scrollPane);
	}

	@Override
	protected void AddMouseListener() {
		searchResultTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent me) {
				JPopupMenu ResultOprMenu = new JPopupMenu();
				if (me.getButton() == MouseEvent.BUTTON3) {
					/*
					 * TODO 实现右键可选取JTable 的行 有缺陷: 在已选择某行的情况下才可用;否则无效
					 */
					searchResultTable.getVisibleRect();
					int row = me.getY() / searchResultTable.getRowHeight();
					searchResultTable.getSelectionModel()
							.setLeadSelectionIndex(row);

					ResultOprMenu.add(new AbstractAction("Join this group") {
						public void actionPerformed(ActionEvent e) {
							// TODO 添加到组列表
							int[] selected = searchResultTable
									.getSelectedRows();
							for (int i = 0; i < selected.length; i++) {
								Advertisement adv = (Advertisement) searchResultTable
										.getAdvAt(selected[i]);
								parent.joinThisGroup((PeerGroupAdvertisement)adv);
							}
						}
					});
					ResultOprMenu.add(new AbstractAction("Nothing") {
						public void actionPerformed(ActionEvent e) {
							// TODO nothing
						}
					});
					MenuElement els[] = ResultOprMenu.getSubElements();
					for (int i = 0; i < els.length; i++)
						els[i].getComponent().setBackground(Color.WHITE);
					ResultOprMenu.setLightWeightPopupEnabled(true);
					ResultOprMenu.pack();
					ResultOprMenu.show((Component) me.getSource(), me
							.getPoint().x, me.getPoint().y);
				}
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
				// AdvTable.this.getSelectedRow();
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
	}
}
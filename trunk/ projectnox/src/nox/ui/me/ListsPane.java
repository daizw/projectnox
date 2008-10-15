package nox.ui.me;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.MenuElement;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroupID;
import nox.db.DBTableName;
import nox.net.common.NoxToolkit;
import nox.net.group.GroupConnectionHandler;
import nox.net.peer.PeerConnectionHandler;
import nox.ui.common.GroupItem;
import nox.ui.common.NoxJListItem;
import nox.ui.common.ObjectList;
import nox.ui.common.PeerItem;
import nox.ui.common.SystemPath;

@SuppressWarnings("serial")
public class ListsPane extends JTabbedPane {
	private JPanel frdlistpane;
	private JPanel grplistpane;
	private JScrollPane frdListScrPane;
	private JScrollPane grpListScrPane;
	private JScrollPane blkListScrPane;
	
	JButton myFriends = new JButton("My Friends");
	JButton blacklist = new JButton("Blacklist");

	NoxJListItem listItem = null;
	Cheyenne parent;
	
	private int selectedIndex = 0;
	
	public ListsPane(Cheyenne par, final ObjectList flist, final ObjectList glist, final ObjectList blist) {
		frdlistpane = new JPanel();
		grplistpane = new JPanel();
		parent = par;
		
		myFriends.setSize(new Dimension(Cheyenne.WIDTH_DEFLT, 20));
		myFriends.setPreferredSize(new Dimension(Cheyenne.WIDTH_PREF, 20));
		myFriends.setMaximumSize(new Dimension(Cheyenne.WIDTH_MAX, 20));
		myFriends.setMinimumSize(new Dimension(Cheyenne.WIDTH_MIN, 20));
		blacklist.setSize(new Dimension(Cheyenne.WIDTH_DEFLT, 20));
		blacklist.setPreferredSize(new Dimension(Cheyenne.WIDTH_PREF, 20));
		blacklist.setMaximumSize(new Dimension(Cheyenne.WIDTH_MAX, 20));
		blacklist.setMinimumSize(new Dimension(Cheyenne.WIDTH_MIN, 20));

		// add to gui
		frdListScrPane = new JScrollPane(flist,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		grpListScrPane = new JScrollPane(glist,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		blkListScrPane = new JScrollPane(blist,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		frdlistpane.setLayout(new BoxLayout(frdlistpane, BoxLayout.Y_AXIS));
		frdlistpane.setAlignmentX(0.0f);
		frdlistpane.setAlignmentY(0.0f);
		
		frdlistpane.add(flist.getFilterField());
		frdlistpane.add(blist.getFilterField());
		frdlistpane.add(myFriends);
		//myFriends.setLocation(myFriends.getLocation().x - 1000, 0);
		//myFriends.setAlignmentX(LEFT_ALIGNMENT);
		frdlistpane.add(frdListScrPane);
		frdlistpane.add(blacklist);
		//blacklist.setAlignmentY(RIGHT_ALIGNMENT);
		frdlistpane.add(blkListScrPane);
		
		/**
		 * 初始不可见
		 */
		blist.getFilterField().setVisible(false);
		blkListScrPane.setVisible(false);
		
		myFriends.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				flist.getFilterField().setVisible(true);
				frdListScrPane.setVisible(true);
				blist.getFilterField().setVisible(false);
				blkListScrPane.setVisible(false);
				ListsPane.this.repaint();
				Thread playThd = new Thread(new Runnable() {
					@Override
					public void run() {
						playAudio();
					}
				}, "Beeper");
				playThd.start();
			}
		});
		blacklist.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				flist.getFilterField().setVisible(false);
				frdListScrPane.setVisible(false);
				blist.getFilterField().setVisible(true);
				blkListScrPane.setVisible(true);
				ListsPane.this.repaint();
				Thread playThd = new Thread(new Runnable() {
					@Override
					public void run() {
						playAudio();
					}
				}, "Beeper");
				playThd.start();
			}
		});
		
		/*flist.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent se) {
			}
		});*/
		flist.addMouseListener(new MouseListener(){
			@SuppressWarnings("serial")
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					//TODO 判断所点击的cell的在线状态进行对应处理, 暂时直接弹出弹出聊天窗口.
					/**
					 * TODO 应该对每一个对象只开一个窗口, 可以设定标记, 如果已经打开了一个则显示之, 否则开新窗口
					 */
					listItem = (PeerItem)flist.getSelectedValue();
					ListsPane.this.showPeerChatroom((PeerItem)listItem);
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu friendOprMenu = new JPopupMenu();
					listItem = (PeerItem)flist.getSelectedValue();
					if(listItem == null)
						return;
					//System.out.println("You just Right Click the List Item!");
					friendOprMenu.add(new AbstractAction("Talk to him/her") {
						public void actionPerformed(ActionEvent e) {
							ListsPane.this.showPeerChatroom((PeerItem)listItem);
						}
					});
					friendOprMenu.add(new AbstractAction("His/Her information") {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog((Component) null, 
									"<html>"//<BODY bgColor=#ffffff>"
									+ "<img width=64 height=64 src=\"file:/"
									+ System.getProperty("user.dir")
									+ System.getProperty("file.separator")
									+ SystemPath.PORTRAIT_RESOURCE_PATH
									+ "chat.png\"><br>"
									+"<Font color=black>昵称:</Font> <Font color=blue>"
									+ listItem.getName()
									+"<br></Font>"
									+"<Font color=black>签名档:</Font> <Font color=blue>"
									+ listItem.getDesc()
									+"<br></Font>"
									+"<Font color=black>联系方式:</Font> <Font color=blue>"
									+ "110, 119, 120, 114, 117"
									+"<br></Font>"
									+"<Font color=black>个人说明:</Font> <Font color=blue>"
									+ listItem.getName() + " owns me so much MONEY!! "
									+"<br></Font></BODY></html>",
									"User Information", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					friendOprMenu.add(new AbstractAction("Move to the blacklist") {
						public void actionPerformed(ActionEvent e) {
							//move to the blacklist
							int index = flist.getSelectedIndex();
							PeerItem temppeer = (PeerItem)flist.deleteItem(parent.getSQLConnection(), DBTableName.PEER_SQLTABLE_NAME, true, index);
							if(temppeer == null)
								return;
							try {
								blist.addItem(temppeer);
							} catch (SQLException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							ListsPane.this.repaint();
							NoxToolkit.removePeer((PeerID) temppeer.getUUID());
						}
					});
					friendOprMenu.add(new AbstractAction("Delete") {
						public void actionPerformed(ActionEvent e) {
							int index = flist.getSelectedIndex();
							PeerItem temppeer = (PeerItem)flist.deleteItem(parent.getSQLConnection(), DBTableName.PEER_SQLTABLE_NAME, true, index);
							ListsPane.this.repaint();
							if(temppeer != null)
								NoxToolkit.removePeer((PeerID) temppeer.getUUID());
						}
					});
					MenuElement els[] = friendOprMenu.getSubElements();
					for(int i = 0; i < els.length; i++)
						els[i].getComponent().setBackground(Color.WHITE);
					friendOprMenu.setLightWeightPopupEnabled(true);
					friendOprMenu.pack();
					// 位置应该是相对于源的位置
					friendOprMenu.show((Component) me.getSource(), me.getPoint().x, me.getPoint().y);
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
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		
		blist.addMouseListener(new MouseListener(){
			@SuppressWarnings("serial")
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					listItem = (PeerItem)blist.getSelectedValue();
					JOptionPane.showMessageDialog((Component) null, 
							"<html>"//<BODY bgColor=#ffffff>"
							+ "<img width=64 height=64 src=\"file:/"
							+ System.getProperty("user.dir")
							+ System.getProperty("file.separator")
							+ SystemPath.PORTRAIT_RESOURCE_PATH
							+ "chat.png\"><br>"
							+"<Font color=black>昵称:</Font> <Font color=blue>"
							+ listItem.getName()
							+"<br></Font>"
							+"<Font color=black>签名档:</Font> <Font color=blue>"
							+ listItem.getDesc()
							+"<br></Font>"
							+"<Font color=black>联系方式:</Font> <Font color=blue>"
							+ "110, 119, 120, 114, 117"
							+"<br></Font>"
							+"<Font color=black>个人说明:</Font> <Font color=blue>"
							+ listItem.getName() + " owns me so much MONEY!! "
							+"<br></Font></BODY></html>",
							"User Information", JOptionPane.INFORMATION_MESSAGE);
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu blacklistOprMenu = new JPopupMenu();
					listItem = (PeerItem)blist.getSelectedValue();
					if(listItem == null)
						return;
					blacklistOprMenu.add(new AbstractAction("His/Her information") {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog((Component) null, 
									"<html>"//<BODY bgColor=#ffffff>"
									+ "<img width=64 height=64 src=\"file:/"
									+ System.getProperty("user.dir")
									+ System.getProperty("file.separator")
									+ SystemPath.PORTRAIT_RESOURCE_PATH
									+ "chat.png\"><br>"
									+"<Font color=black>昵称:</Font> <Font color=blue>"
									+ listItem.getName()
									+"<br></Font>"
									+"<Font color=black>签名档:</Font> <Font color=blue>"
									+ listItem.getDesc()
									+"<br></Font>"
									+"<Font color=black>联系方式:</Font> <Font color=blue>"
									+ "110, 119, 120, 114, 117"
									+"<br></Font>"
									+"<Font color=black>个人说明:</Font> <Font color=blue>"
									+ listItem.getName() + " owns me so much MONEY!! "
									+"<br></Font></BODY></html>",
									"User Information", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					blacklistOprMenu.add(new AbstractAction("Add to the friendlist") {
						public void actionPerformed(ActionEvent e) {
							//move to the friendlist
							int index = blist.getSelectedIndex();
							PeerItem temppeer = (PeerItem)blist.deleteItem(parent.getSQLConnection(), DBTableName.PEER_SQLTABLE_NAME, false, index);
							if(temppeer == null)
								return;
							try {
								flist.addItem(temppeer);
							} catch (SQLException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							ListsPane.this.repaint();
						}
					});
					blacklistOprMenu.add(new AbstractAction("Delete") {
						public void actionPerformed(ActionEvent e) {
							int index = blist.getSelectedIndex();
							blist.deleteItem(parent.getSQLConnection(), DBTableName.PEER_SQLTABLE_NAME, false, index);
							ListsPane.this.repaint();
						}
					});
					MenuElement els[] = blacklistOprMenu.getSubElements();
					for(int i = 0; i < els.length; i++)
						els[i].getComponent().setBackground(Color.WHITE);
					blacklistOprMenu.setLightWeightPopupEnabled(true);
					blacklistOprMenu.pack();
					// 位置应该是相对于源的位置
					blacklistOprMenu.show((Component) me.getSource(), me.getPoint().x, me.getPoint().y);
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
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		
		grplistpane.setLayout(new BorderLayout());
		grplistpane.add(grpListScrPane, BorderLayout.CENTER);
		grplistpane.add(glist.getFilterField(), BorderLayout.NORTH);
		
		glist.addMouseListener(new MouseListener(){
			@SuppressWarnings("serial")
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					//TODO 判断所点击的cell的在线状态进行对应处理, 暂时直接弹出弹出聊天窗口.
					listItem = (GroupItem)(glist.getSelectedValue());
					ListsPane.this.showGroupChatroom((GroupItem)listItem);
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu groupOprMenu = new JPopupMenu();
					/*
					 * 怎么实现右键可选取JListItem?
					 */
					listItem = (GroupItem)glist.getSelectedValue();
					//System.out.println(flist.getComponentAt(me.getPoint()).toString());
					
					if(listItem == null)
						return;
					//System.out.println("You just Right Click the List Item!");
					groupOprMenu.add(new AbstractAction("Enter this chatroom") {
						public void actionPerformed(ActionEvent e) {
							ListsPane.this.showGroupChatroom((GroupItem)listItem);
						}
					});
					groupOprMenu.add(new AbstractAction("Group information") {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog((Component) null, 
									"<html>"//<BODY bgColor=#ffffff>"
									+ "<img width=64 height=64 src=\"file:/"
									+ System.getProperty("user.dir")
									+ System.getProperty("file.separator")
									+ SystemPath.PORTRAIT_RESOURCE_PATH
									+"chat.png\"><br>"
									+"<Font color=black>组名:</Font> <Font color=blue>"
									+ listItem.getName()
									+"<br></Font>"
									+"<Font color=black>公告:</Font> <Font color=blue>"
									+ listItem.getDesc()
									+"<br></Font>"
									+"<Font color=black>成员数量:</Font> <Font color=blue>"
									+ "110, 119, 120, 114, 117"
									+"<br></Font></BODY></html>",
									"User Information", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					groupOprMenu.add(new AbstractAction("Resign") {
						public void actionPerformed(ActionEvent e) {
							int index = glist.getSelectedIndex();
							GroupItem group = (GroupItem)glist.deleteItem(index);
							ListsPane.this.repaint();
							//删除认证书, 删除Chatroom, 删除管道监听器.
							NoxToolkit.resignGroup((PeerGroupID) group.getUUID());
						}
					});
					MenuElement els[] = groupOprMenu.getSubElements();
					for(int i = 0; i < els.length; i++)
						els[i].getComponent().setBackground(Color.WHITE);
					groupOprMenu.setLightWeightPopupEnabled(true);
					groupOprMenu.pack();
					// 位置应该是相对于源的位置
					groupOprMenu.show((Component) me.getSource(), me.getPoint().x, me.getPoint().y);
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
		});
		
		/*blklistpane.setLayout(new BorderLayout());
		blklistpane.add(blkListScrPane, BorderLayout.CENTER);
		blklistpane.add(blist.getFilterField(), BorderLayout.NORTH);*/

		this.setTabPlacement(JTabbedPane.LEFT);
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);// 滚动标签(一行)
		// tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);//多行标签
		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);

		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "chat.png"), frdlistpane);
		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "chatroom.png"), grplistpane);
		this.setToolTipTextAt(0, getHtmlText("Friends"));
		this.setToolTipTextAt(1, getHtmlText("Groups"));
		
		this.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent ce) {
				int temp = ListsPane.this.getSelectedIndex();
				//如果选择的标签是好友列表或组列表，则更新选中标签索引。
				if(temp < 2)
					selectedIndex = temp;
				//System.out.println("selectedIndex" + selectedIndex);
			}
		});
		JPanel searchPane = new JPanel();
		searchPane.setBackground(Color.WHITE);
		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "search_25.png"), searchPane);
		this.setToolTipTextAt(2, getHtmlText("Search"));
		searchPane.addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			@Override
			public void componentResized(ComponentEvent arg0) {
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
				ListsPane.this.setSelectedIndex(selectedIndex);
				parent.showSearchingFrame();
			}
		});
		JPanel configPane = new JPanel();
		configPane.setBackground(Color.WHITE);
		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "config_25.png"), configPane);
		this.setToolTipTextAt(3, getHtmlText("Configuration"));
		configPane.addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			@Override
			public void componentResized(ComponentEvent arg0) {
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
				ListsPane.this.setSelectedIndex(selectedIndex);
				parent.ShowConfigCenter();
			}
		});
		//this.addTab(null, new ImageIcon(path_blist), blklistpane);
		JPanel creatGroupPane = new JPanel();
		creatGroupPane.setBackground(Color.WHITE);
		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "new_group_25.png"), creatGroupPane);
		this.setToolTipTextAt(4, getHtmlText("Create New Group"));
		creatGroupPane.addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			@Override
			public void componentResized(ComponentEvent arg0) {
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
				ListsPane.this.setSelectedIndex(selectedIndex);
				parent.ShowCreateNewGroupDialog();
			}
		});
		
		this.setOpaque(false);
	}
	/**
	 * 返回TooltipTxt的html形式
	 * @param text
	 * @return
	 */
	public static String getHtmlText(String text) {
		return ("<html><BODY bgColor=#ffffff><Font color=black>" + text + "</Font></BODY></html>");
	}
	/**
	 * (在主界面双击好友或者组时被调用)弹出聊天窗口.
	 * @param listItem
	 */
	private void showPeerChatroom(PeerItem listItem) {
		
		PeerConnectionHandler handler = NoxToolkit.getPeerConnectionHandler((PeerID) listItem.getUUID());
		if(handler != null){
			handler.showChatroom();
		}else{
			//不存在对应的handler, 需要连接然后注册handler
			try {
				handler = new PeerConnectionHandler(listItem, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 因为组聊天室的管道实际上都是注册过的, 所以处理方法应该跟私聊有所不同!
	 * 
	 * @param listItem
	 */
	private void showGroupChatroom(GroupItem listItem) {
		PeerGroupID id = (PeerGroupID) listItem.getUUID();
		GroupConnectionHandler handler = NoxToolkit.getGroupConnectionHandler(id);
		if(handler == null)
			try {
				handler = new GroupConnectionHandler(listItem);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		
		handler.showChatroom();
	}
	/**
	 * 切换列表时播放提示音
	 * ....某些音频文件会降低速度
	 */
	public void playAudio() {
		AudioClip playsound;
		try {
			// AudioClip audioClip = Applet.newAudioClip(completeURL)
			// codeBase = new URL("file:" + System.getProperty("user.dir") +
			// "/");
			URL url = new URL("file:/" + System.getProperty("user.dir")
					+ System.getProperty("file.separator")
					+ SystemPath.AUDIO_RESOURCE_PATH
					+ "folderwpcm.wav");
			playsound = Applet.newAudioClip(url);
			// System.out.println(url);
			playsound.play();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}
}
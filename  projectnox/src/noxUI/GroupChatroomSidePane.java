package noxUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.MenuElement;
import javax.swing.ScrollPaneConstants;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.nox.NoxToolkit;
import net.nox.PeerChatroomUnit;

@SuppressWarnings("serial")
public class GroupChatroomSidePane extends JSplitPane{
	final DiscoveryListener listener = new DiscoveryListener(){
		@Override
		public void discoveryEvent(DiscoveryEvent event) {
			DiscoveryResponseMsg res = event.getResponse();

			// let's get the responding peer's advertisement
			System.out.println(" [  Got a Discovery Response ["
					+ res.getResponseCount() + " elements]  from peer : "
					+ event.getSource() + "  ]");

			long curTime = new Date().getTime();
			System.out.println(curTime);

			PeerAdvertisement adv;
			Enumeration<Advertisement> advEnum = res.getAdvertisements();

			if (advEnum != null) {
				while (advEnum.hasMoreElements()) {
					adv = (PeerAdvertisement) advEnum.nextElement();
					System.out.println("adding peer: " + ((PeerAdvertisement)adv).getName());
					PeerItem peer = new PeerItem(
							new ImageIcon(SystemPath.PORTRAIT_RESOURCE_PATH + "groupmember.png"),
							adv);
					groupmemerlist.addItem(peer);
					groupmemerlist.repaint();
					groupMemverListScrPane.repaint();
					GroupChatroomSidePane.this.repaint();
				}
			}
		}
	};
	
	JPanel groupInfoPane;
	JLabel l_groupInfo;
	JTextPane groupInfo;
	JScrollPane groupMemverListScrPane;
	JPanel groupMemberListPane;
	JPanel memberLabelPane;
	JLabel l_groupMembers;
	JButton refresh;
	boolean refreshing = false;
	GroupMemberList groupmemerlist;
	
	DiscoveryService ds;
	PeerItem listItem;
	
	GroupChatroomSidePane(final GroupChatroom parent, String grpInfo, Object[] members){
		super(JSplitPane.VERTICAL_SPLIT);
		
		l_groupInfo = new JLabel("Group Info:");
		groupInfo = new JTextPane();
		groupInfo.setText(grpInfo);
		groupInfo.setEditable(false);
		//groupInfo.setEnabled(false);
		groupInfo.setSize(new Dimension(100, 100));
		groupInfo.setPreferredSize(new Dimension(100, 100));
		groupInfo.setMaximumSize(new Dimension(10000, 100));
		groupInfo.setMinimumSize(new Dimension(20, 100));
		
		groupInfoPane = new JPanel();
		groupInfoPane.setLayout(new BoxLayout(groupInfoPane, BoxLayout.Y_AXIS));
		groupInfoPane.add(l_groupInfo);
		groupInfoPane.add(groupInfo);
		
		memberLabelPane = new JPanel();
		l_groupMembers = new JLabel("Group Members:");
		refresh = new JButton(new ImageIcon(SystemPath.BUTTONS_RESOURCE_PATH + "refresh.png"));
		refresh.setToolTipText(ListsPane.getHtmlText("Refresh this list"));
		Dimension btnsize = new Dimension(20, 20);
		refresh.setSize(btnsize);
		refresh.setPreferredSize(btnsize);
		refresh.setMaximumSize(btnsize);
		refresh.setMinimumSize(btnsize);
		
		memberLabelPane.setLayout(new BoxLayout(memberLabelPane, BoxLayout.X_AXIS));
		memberLabelPane.add(refresh);
		memberLabelPane.add(l_groupMembers);
		
		groupmemerlist = new GroupMemberList(members);
		groupMemverListScrPane = new JScrollPane(groupmemerlist,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		groupMemberListPane = new JPanel();
		groupMemberListPane.setLayout(new BoxLayout(groupMemberListPane, BoxLayout.Y_AXIS));
		groupMemberListPane.add(memberLabelPane);
		groupMemberListPane.add(groupmemerlist.getFilterField());
		groupMemberListPane.add(groupMemverListScrPane);
		
		
		this.add(groupInfoPane);
		this.add(groupMemberListPane);
		this.setDividerLocation(0.36f);
		this.setDividerSize(5);
		
		ds = parent.getDiscoveryService();
		
		refresh.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//parent.discoverMembers();
				if(refreshing){
					refreshing = false;
					System.out.println("Stop searching group members");
				}else{
					refreshing = true;
					Thread hunter = new Thread(new Runnable(){
						@Override
						public void run() {
							huntMembers();
						}
					});
					System.out.println("Begin searching group members");
					hunter.start();
				}
			}
		});
		groupmemerlist.addMouseListener(new MouseListener(){
			@SuppressWarnings("serial")
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					//TODO 判断所点击的cell的在线状态进行对应处理, 暂时直接弹出弹出聊天窗口.
					/**
					 * TODO 应该对每一个对象只开一个窗口, 可以设定标记, 如果已经打开了一个则显示之, 否则开新窗口
					 */
					listItem = (PeerItem)groupmemerlist.getSelectedValue();
					GroupChatroomSidePane.this.showPeerChatroom((PeerItem)listItem);
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu friendOprMenu = new JPopupMenu();
					listItem = (PeerItem)groupmemerlist.getSelectedValue();
					if(listItem == null)
						return;
					//System.out.println("You just Right Click the List Item!");
					friendOprMenu.add(new AbstractAction("Talk to him/her") {
						public void actionPerformed(ActionEvent e) {
							GroupChatroomSidePane.this.showPeerChatroom((PeerItem)listItem);
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
			public void mouseEntered(MouseEvent arg0) {}
			@Override
			public void mouseExited(MouseEvent arg0) {}
			@Override
			public void mousePressed(MouseEvent arg0) {}
			@Override
			public void mouseReleased(MouseEvent arg0) {}
		});
	}
	private void huntMembers(){
		try {
			while(refreshing){
				System.out.println("Sending discovery msg...");
				ds.getRemoteAdvertisements(null,
						DiscoveryService.PEER,
						null,
						null,
						100,
						listener);
				System.out.println("Sleeping for 5s ...");
				Thread.sleep(5*1000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * (在组成员列表双击组员时被调用)弹出聊天窗口.
	 * @param listItem
	 */
	private void showPeerChatroom(PeerItem listItem) {
		ID id = listItem.getUUID();
		PeerChatroomUnit roomunit = (PeerChatroomUnit) NoxToolkit.getChatroomUnit(id);
		PeerChatroom room;
		
		if(roomunit == null){
			//未注册pipe, 更无chatroom.
			//新建聊天室, 会试图连接.
			//如果连接不上....
			//如果连接成功....
			room = new PeerChatroom(listItem, null);
		}else{
			//已注册pipe
			room = roomunit.getChatroom();
			if(room == null)
			{//不存在, 开新窗口
				room = NoxToolkit.getCheyenne().setupNewChatroomOver(roomunit.getOutPipe());
				//new NoxToolkit().registerChatroom(id, room);
			}else{
				room.pack();
				room.setVisible(true);
			}
		}
	}
}

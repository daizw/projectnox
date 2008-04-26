package noxUI;
import javax.swing.ImageIcon;

import net.nox.JXTANetwork;

public class NoX {
	public static void main(String args[]) {	
		System.setProperty("sun.java2d.noddraw", "true");// 为半透明做准备
		
		JXTANetwork MyLogin;
		MyLogin = new JXTANetwork();
		MyLogin.SeekRendezVousConnection();
		
		/**
		 * 好友列表
		 */
		/*String[] flistItems = { "Chris", "Joshua", "Daniel", "Michael", "Don",
				"Kimi", "Kelly", "Keagan", "夏", "张三", "张四", "张五", "张三丰" };

		PeerItem[] friends = new PeerItem[flistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < flistItems.length; i++) {
			friends[i] = new PeerItem(new ImageIcon(
					"resrc\\portrait\\user.png"), flistItems[i], "Hi, 我是"
					+ flistItems[i], "uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx");
		}
		ObjectList flist = new ObjectList(friends);*/
		String[] flistItems = {};

		PeerItem[] friends = new PeerItem[flistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < flistItems.length; i++) {
			friends[i] = new PeerItem(new ImageIcon(
					"resrc\\portrait\\user.png"), flistItems[i], "Hi, 我是"
					+ flistItems[i], "uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx");
		}
		ObjectList flist = new ObjectList(friends);
		/**
		 * 组列表
		 */
		String[] glistItems = { "group1", "group2", "group3", "group4", "三年二班",
				"三年三班" };

		GroupItem[] groups = new GroupItem[glistItems.length];

		for (int i = 0; i < glistItems.length; i++) {
			groups[i] = new GroupItem(new ImageIcon("resrc\\icons\\chatroom.png"),
					glistItems[i], "Hi, 这是" + glistItems[i] + "的聊天室", 
					"uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx", 0, 0);
		}
		ObjectList glist = new ObjectList(groups);
		/**
		 * 黑名单
		 */
		String[] blistItems = { "Ben", "Laden", "Hitler", "Bush", "陈水扁" };

		PeerItem[] badguys = new PeerItem[blistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < blistItems.length; i++) {
			badguys[i] = new PeerItem(new ImageIcon("resrc\\icons\\blacklist.png"),
					blistItems[i], "Hi, 我是" + blistItems[i], "uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx");
		}
		ObjectList blist = new ObjectList(badguys);
		
		Cheyenne chyn = new Cheyenne(flist, glist, blist);
		chyn.pack();
		chyn.setVisible(true);
		/*Chatroom room = new Chatroom("Groupname or Friendsname here");
		room.pack();
		room.setVisible(true);*/
	}
}

package noxUI;
import javax.swing.ImageIcon;

import net.nox.JXTANetwork;

public class NoX {
	public static void main(String args[]) {	
		System.setProperty("sun.java2d.noddraw", "true");// Ϊ��͸����׼��
		System.setProperty("net.jxta.logging.Logging", "INFO");
		System.setProperty("net.jxta.level", "INFO");
		System.setProperty("java.util.logging.config.file", "logging.properties");
		 
		JXTANetwork MyLogin;
		MyLogin = new JXTANetwork();
		MyLogin.Start();
		
		/**
		 * �����б�
		 */
		/*String[] flistItems = { "Chris", "Joshua", "Daniel", "Michael", "Don",
				"Kimi", "Kelly", "Keagan", "��", "����", "����", "����", "������" };

		PeerItem[] friends = new PeerItem[flistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < flistItems.length; i++) {
			friends[i] = new PeerItem(new ImageIcon(
					"resrc\\portrait\\user.png"), flistItems[i], "Hi, ����"
					+ flistItems[i], "uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx");
		}
		ObjectList flist = new ObjectList(friends);*/
		String[] flistItems = {};

		PeerItem[] friends = new PeerItem[flistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		/*for (int i = 0; i < flistItems.length; i++) {
			friends[i] = new PeerItem(new ImageIcon(
					"resrc\\portrait\\user.png"), flistItems[i], "Hi, ����"
					+ flistItems[i], null);
		}*/
		ObjectList flist = new ObjectList(friends);
		/**
		 * ���б�
		 */
		String[] glistItems = { /*"group1", "group2", "group3", "group4", "�������",
				"��������" */};

		GroupItem[] groups = new GroupItem[glistItems.length];

		for (int i = 0; i < glistItems.length; i++) {
			groups[i] = new GroupItem(new ImageIcon("resrc\\icons\\chatroom.png"),
					glistItems[i], "Hi, ����" + glistItems[i] + "��������", 
					null, 0, 0);
		}
		ObjectList glist = new ObjectList(groups);
		/**
		 * ������
		 */
		String[] blistItems = {};

		PeerItem[] badguys = new PeerItem[blistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		/*for (int i = 0; i < blistItems.length; i++) {
			badguys[i] = new PeerItem(new ImageIcon("resrc\\icons\\blacklist.png"),
					blistItems[i], "Hi, ����" + blistItems[i], null);
		}*/
		ObjectList blist = new ObjectList(badguys);
		
		Cheyenne chyn = new Cheyenne(flist, glist, blist);
		chyn.pack();
		chyn.setVisible(true);
		/*Chatroom room = new Chatroom("Groupname or Friendsname here");
		room.pack();
		room.setVisible(true);*/
	}
}

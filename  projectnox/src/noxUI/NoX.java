package noxUI;
import javax.swing.ImageIcon;

import net.nox.JXTANetwork;

public class NoX {
	public static void main(String args[]) {	
		System.setProperty("sun.java2d.noddraw", "true");// Ϊ��͸����׼��
		
		JXTANetwork MyLogin;
		MyLogin = new JXTANetwork();
		MyLogin.SeekRendezVousConnection();
		
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

		for (int i = 0; i < flistItems.length; i++) {
			friends[i] = new PeerItem(new ImageIcon(
					"resrc\\portrait\\user.png"), flistItems[i], "Hi, ����"
					+ flistItems[i], "uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx");
		}
		ObjectList flist = new ObjectList(friends);
		/**
		 * ���б�
		 */
		String[] glistItems = { "group1", "group2", "group3", "group4", "�������",
				"��������" };

		GroupItem[] groups = new GroupItem[glistItems.length];

		for (int i = 0; i < glistItems.length; i++) {
			groups[i] = new GroupItem(new ImageIcon("resrc\\icons\\chatroom.png"),
					glistItems[i], "Hi, ����" + glistItems[i] + "��������", 
					"uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx", 0, 0);
		}
		ObjectList glist = new ObjectList(groups);
		/**
		 * ������
		 */
		String[] blistItems = { "Ben", "Laden", "Hitler", "Bush", "��ˮ��" };

		PeerItem[] badguys = new PeerItem[blistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < blistItems.length; i++) {
			badguys[i] = new PeerItem(new ImageIcon("resrc\\icons\\blacklist.png"),
					blistItems[i], "Hi, ����" + blistItems[i], "uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx");
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

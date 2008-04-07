package noxUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
/**
 * 
 * @author shinysky
 *
 */
public class Chatroom extends NoxFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7981736228268584688L;
	/**
	 * Ĭ�ϳߴ糣��
	 */
	public static final int WIDTH_DEFLT = 700;
	public static final int WIDTH_PREF = 700;
	public static final int WIDTH_MAX = 2000;
	public static final int WIDTH_MIN = 300;
	public static final int HEIGHT_DEFLT = 500;
	public static final int HEIGHT_PREF = 500;
	public static final int HEIGHT_MAX = 2000;
	public static final int HEIGHT_MIN = 200;
	
	public static final String RESOURCES_PATH = "resrc/";
	public static final String ICON_PREFIX = "/";
	public static final String ICON_SUFFIX_REGEX = "[^\\/]+\\.gif";
	public static final String ICON_RESOURCES_PATH = RESOURCES_PATH.concat("faces/");
	
	JSplitPane rootpane;
	JPanel LeftPane;
	ChatRoomPane crp;
	/**
	 * ����Ӧ�ô������ڼ̳���ɫ, ͸����
	 * ����ʵ��---------�����ںʹ�������ͬ��������ɫ��͸����
	 * ��ʵ�����������ڵ�ʱ�����ñ�����һ��Vector��, ������ɫ��͸����ʱ��
	 * Vector��ʵ�����ε��õ��ں���
	 */
	Chatroom(){
		super("NoX Chatroom", "resrc\\images\\bkgrd.png",
				"resrc\\icons\\chat2.png", "resrc\\logo\\nox.png",
				"resrc\\buttons\\minimize.png", "resrc\\buttons\\minimize_rollover.png",
				"resrc\\buttons\\maximize.png", "resrc\\buttons\\maximize_rollover.png",
				"resrc\\buttons\\normalize.png", "resrc\\buttons\\normalize_rollover.png",
				"resrc\\buttons\\close.png", "resrc\\buttons\\close_rollover.png", true);
		//���մ˴�ӦΪfalse

		this.setBounds(100, 80, WIDTH_DEFLT, HEIGHT_DEFLT);
		this.setSize(new Dimension(WIDTH_DEFLT, HEIGHT_DEFLT));
		this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
		
		LeftPane = new JPanel();
		SingleChatRoomSidePane portraits = new SingleChatRoomSidePane(
				new ImageIcon("resrc\\portrait\\portrait.png"),
				new ImageIcon("resrc\\portrait\\portrait.png"));
		
		/**
		 * �����б�
		 */
		String[] flistItems = { "Chris", "Joshua", "Daniel", "Michael", "Don",
				"Kimi", "Kelly", "Keagan", "��", "����", "����", "����", "������" };

		FriendItem[] friends = new FriendItem[flistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < flistItems.length; i++) {
			friends[i] = new FriendItem(new ImageIcon(
					"resrc\\portrait\\user.png"), flistItems[i], "(Hi, ����"
					+ flistItems[i] + ')');
		}
		MultiChatRoomSidePane groupmembers = new MultiChatRoomSidePane(
				"Hello, everyone, happy everyday!", friends
				);
		
		crp = new ChatRoomPane(this);
		//crp.setLayout(new FlowLayout());
		rootpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupmembers, crp);
		rootpane.setOneTouchExpandable(true);
		rootpane.setDividerLocation(0.25f);
		rootpane.setDividerSize(8);
		
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add (rootpane, BorderLayout.CENTER);
	}
}

package noxUI;

import java.awt.BorderLayout;
import java.awt.Dimension;

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
	
	public static final int PRIVATE_CHATROOM = 0;
	public static final int GROUP_CHATROOM = 1;
	
	public static final String RESOURCES_PATH = "resrc/";
	public static final String ICON_PREFIX = "/";
	public static final String ICON_SUFFIX_REGEX = "[^\\/]+\\.gif";
	public static final String ICON_RESOURCES_PATH = RESOURCES_PATH.concat("faces/");
	
	JSplitPane rootpane;
	ChatRoomPane crp;
	/**
	 * ����Ӧ�ô������ڼ̳���ɫ, ͸����
	 * ����ʵ��---------�����ںʹ�������ͬ��������ɫ��͸����
	 * ��ʵ�����������ڵ�ʱ�����ñ�����һ��Vector��, ������ɫ��͸����ʱ��
	 * Vector��ʵ�����ε��õ��ں���
	 * @param title �����ұ���, һ���ǶԷ����ǳ�, ��������
	 * @param type ����������: Chatroom.PRIVATE_CHATROOM(˽��);Chatroom.GROUP_CHATROOM(Ⱥ��);
	 */
	Chatroom(String title){
		super(title + " - NoX Chatroom", "resrc\\images\\bkgrd.png",
				"resrc\\icons\\chat2.png", title,
				"resrc\\buttons\\minimize.png", "resrc\\buttons\\minimize_rollover.png",
				"resrc\\buttons\\maximize.png", "resrc\\buttons\\maximize_rollover.png",
				"resrc\\buttons\\normalize.png", "resrc\\buttons\\normalize_rollover.png",
				"resrc\\buttons\\close.png", "resrc\\buttons\\close_rollover.png", false);
		//���մ˴�ӦΪfalse

		this.setBounds(100, 80, WIDTH_DEFLT, HEIGHT_DEFLT);
		this.setSize(new Dimension(WIDTH_DEFLT, HEIGHT_DEFLT));
		this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
		
		crp = new ChatRoomPane(this);
		//crp.setLayout(new FlowLayout());
		rootpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rootpane.setOneTouchExpandable(true);
		rootpane.setDividerLocation(0.25f);
		rootpane.setDividerSize(8);
		rootpane.setResizeWeight(0.16d);
		
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add (rootpane, BorderLayout.CENTER);
	}
	public Chatroom(String title, ImageIcon portrait, String friendname) {
		this(title);
		SingleChatRoomSidePane portraits = new SingleChatRoomSidePane(friendname,
				portrait,
				new ImageIcon("resrc\\portrait\\portrait.png"));
		rootpane.add(portraits);
		rootpane.add(crp);
	}
	public Chatroom(String title, FriendItem[] gmembers) {
		this(title);
		MultiChatRoomSidePane groupmembers = new MultiChatRoomSidePane(
				"Hello, everyone, happy everyday!", gmembers
				);
		rootpane.add(groupmembers);
		rootpane.add(crp);
	}
}

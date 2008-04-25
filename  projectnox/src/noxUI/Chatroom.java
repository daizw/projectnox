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
	 * 默认尺寸常量
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
	 * 最终应该从主窗口继承颜色, 透明度
	 * 考虑实现---------主窗口和从属窗口同步调节颜色和透明度
	 * 在实例化从属窗口的时候将引用保存在一个Vector中, 调节颜色及透明度时对
	 * Vector中实例依次调用调节函数
	 * @param title 聊天室标题, 一般是对方的昵称, 或者组名
	 * @param type 聊天室类型: Chatroom.PRIVATE_CHATROOM(私聊);Chatroom.GROUP_CHATROOM(群聊);
	 */
	Chatroom(String title){
		super(title + " - NoX Chatroom", "resrc\\images\\bkgrd.png",
				"resrc\\icons\\chat2.png", title,
				"resrc\\buttons\\minimize.png", "resrc\\buttons\\minimize_rollover.png",
				"resrc\\buttons\\maximize.png", "resrc\\buttons\\maximize_rollover.png",
				"resrc\\buttons\\normalize.png", "resrc\\buttons\\normalize_rollover.png",
				"resrc\\buttons\\close.png", "resrc\\buttons\\close_rollover.png", false);
		//最终此处应为false

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

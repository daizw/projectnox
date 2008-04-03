package noxUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
/**
 * 
 * @author shinysky
 *
 */
public class Cheyenne extends NoxFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 默认尺寸常量
	 */
	public static final int WIDTH_DEFLT = 300;
	public static final int WIDTH_PREF = 300;
	public static final int WIDTH_MAX = 2000;
	public static final int WIDTH_MIN = 200;
	public static final int HEIGHT_DEFLT = 600;
	public static final int HEIGHT_PREF = 600;
	public static final int HEIGHT_MAX = 2000;
	public static final int HEIGHT_MIN = 300;
	/**
	 * 各JPanel
	 */
	private MiniProfilePane profile;
	private ListsPane tabs;

	public static void main(String args[]) {
		Chatroom room = new Chatroom();
		room.pack();
		room.setVisible(true);
		
		/*Cheyenne chyn = new Cheyenne();
		chyn.pack();
		chyn.setVisible(true);*/
	}

	Cheyenne() {
		super("NoX: a IM system", "resrc\\images\\bkgrd.png", 
				"resrc\\logo\\NoXlogo_20.png", "resrc\\logo\\nox.png",
				"resrc\\buttons\\minimize.png", "resrc\\buttons\\minimize_rollover.png",
				"resrc\\buttons\\maximize.png", "resrc\\buttons\\maximize_rollover.png",
				"resrc\\buttons\\normalize.png", "resrc\\buttons\\normalize_rollover.png",
				"resrc\\buttons\\close.png", "resrc\\buttons\\close_rollover.png", true);

		JPanel contentPane = this.getContainer();
		Cheyenne.this.setBounds(600, 80, WIDTH_DEFLT, HEIGHT_DEFLT);
		Cheyenne.this.setSize(new Dimension(WIDTH_DEFLT, HEIGHT_DEFLT));
		Cheyenne.this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		Cheyenne.this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		Cheyenne.this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));

		/**
		 * mini profile 组件 含: 头像, 昵称, 状态, 签名
		 */
		profile = new MiniProfilePane("resrc\\portrait\\portrait.png",
				"Shinysky", "Hello, everyone~");
		// profile.setBackground(new Color(0, 255, 0));
		profile.setSize(new Dimension(WIDTH_DEFLT, 50));
		profile.setPreferredSize(new Dimension(WIDTH_PREF, 50));
		profile.setMaximumSize(new Dimension(WIDTH_MAX, 50));
		profile.setMinimumSize(new Dimension(WIDTH_MIN, 50));

		/**
		 * 好友列表
		 */
		String[] flistItems = { "Chris", "Joshua", "Daniel", "Michael", "Don",
				"Kimi", "Kelly", "Keagan", "夏", "张三", "张四", "张五", "张三丰" };

		FriendItem[] friends = new FriendItem[flistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < flistItems.length; i++) {
			friends[i] = new FriendItem(new ImageIcon(
					"resrc\\portrait\\user.png"), flistItems[i], "(Hi, 我是"
					+ flistItems[i] + ')');
		}
		FriendList flist = new FriendList(friends);
		/**
		 * 组列表
		 */
		String[] glistItems = { "group1", "group2", "group3", "group4", "三年二班",
				"三年三班" };

		FriendItem[] groups = new FriendItem[glistItems.length];

		for (int i = 0; i < glistItems.length; i++) {
			groups[i] = new FriendItem(new ImageIcon("resrc\\icons\\chatroom.png"),
					glistItems[i], "(Hi, 这是" + glistItems[i] + "的聊天室)");
		}
		FriendList glist = new FriendList(groups);
		/**
		 * 黑名单
		 */
		String[] blistItems = { "Ben", "Laden", "Hitler", "Bush", "陈水扁" };

		FriendItem[] badguys = new FriendItem[blistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < blistItems.length; i++) {
			badguys[i] = new FriendItem(new ImageIcon("resrc\\icons\\blacklist.png"),
					blistItems[i], "(Hi, 我是" + blistItems[i] + ')');
		}
		FriendList blist = new FriendList(badguys);

		tabs = new ListsPane(flist, glist, blist, "resrc\\icons\\chat.png",
				"resrc\\icons\\chatroom.png", "resrc\\icons\\blacklist.png");

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.add(profile);
		contentPane.add(tabs);
	}
}

class MiniProfilePane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6265273413794252382L;
	// JMapPanel myPortraitPane;
	JButton myPortrait;
	JPanel miniProfilePane;
	JComboBox myStatus;
	JTextField mySign;

	/**
	 * mini profile 组件
	 * 
	 * @param path_portrait
	 *            头像图片路径
	 * @param nickname
	 *            昵称
	 * @param sign
	 *            签名档
	 */
	MiniProfilePane(String path_portrait, String nickname, String sign) {
		myPortrait = new JButton(new ImageIcon(path_portrait));
		myPortrait.setSize(new Dimension(50, 50));
		myPortrait.setPreferredSize(new Dimension(50, 50));
		myPortrait.setMaximumSize(new Dimension(50, 50));
		myPortrait.setMinimumSize(new Dimension(50, 50));
		// myPortrait.setBorderPainted(true);

		/*
		 * myPortrait.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,
		 * Color.RED, Color.BLUE));
		 */
		myPortrait.setContentAreaFilled(false);
		myPortrait.setOpaque(false);

		/*
		 * JButton statSign = new JButton(new
		 * ImageIcon("resrc\\portrait\\busy.png")); statSign.setSize(new
		 * Dimension(50, 50)); statSign.setPreferredSize(new Dimension(50, 50));
		 * statSign.setMaximumSize(new Dimension(50, 50));
		 * statSign.setMinimumSize(new Dimension(50, 50));
		 * //statSign.setBorderPainted(false);
		 * //statSign.setContentAreaFilled(false); //statSign.setOpaque(false);
		 * 
		 * myPortrait.setLayout(new BoxLayout(myPortrait, BoxLayout.X_AXIS));
		 * myPortrait.add(statSign);
		 */

		/*
		 * myPortraitPane = new JMapPanel(myportrImg, new Point (5, 10), new
		 * Dimension(40, 40)); myPortraitPane.setSize(new Dimension(50, 50));
		 * myPortraitPane.setPreferredSize(new Dimension(50, 50));
		 * myPortraitPane.setMaximumSize(new Dimension(50, 50));
		 * myPortraitPane.setMinimumSize(new Dimension(50, 50));
		 * myPortraitPane.add(myPortrait);
		 */

		miniProfilePane = new JPanel();
		JPanel nickAndStat = new JPanel();
		JLabel nick = new JLabel(nickname);
		nick.setForeground(Color.WHITE);
		myStatus = new JComboBox();
		// myStatus.setOpaque(false);
		mySign = new JTextField(sign);
		mySign.setOpaque(false);
		mySign.setForeground(Color.WHITE);
		myStatus.addItem("Online");
		myStatus.addItem("Busy");
		myStatus.addItem("Invisible");
		myStatus.addItem("Offline");
		myStatus.setSize(new Dimension(75, 20));
		myStatus.setPreferredSize(new Dimension(75, 20));
		myStatus.setMaximumSize(new Dimension(75, 20));
		myStatus.setMinimumSize(new Dimension(75, 20));
		// myStatus.setOpaque(false);

		nickAndStat.setOpaque(false);
		// nickAndStat.setBackground(new Color(0, 255, 0));
		nickAndStat.setSize(new Dimension(150, 20));
		nickAndStat.setPreferredSize(new Dimension(150, 20));
		nickAndStat.setMaximumSize(new Dimension(1000, 20));
		nickAndStat.setMinimumSize(new Dimension(150, 20));
		// nickAndStat.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		nickAndStat.setLayout(new BoxLayout(nickAndStat, BoxLayout.X_AXIS));
		nickAndStat.add(nick);
		nickAndStat.add(Box.createHorizontalStrut(10));
		nickAndStat.add(myStatus);

		mySign.setSize(new Dimension(Cheyenne.WIDTH_DEFLT, 20));
		mySign.setPreferredSize(new Dimension(Cheyenne.WIDTH_PREF, 20));
		mySign.setMaximumSize(new Dimension(Cheyenne.WIDTH_MAX, 20));
		mySign.setMinimumSize(new Dimension(Cheyenne.WIDTH_MIN, 20));

		// miniProfilePane.setAlignmentX(JComponent.TOP_ALIGNMENT);
		miniProfilePane.setLayout(new BoxLayout(miniProfilePane,
				BoxLayout.Y_AXIS));
		miniProfilePane.add(nickAndStat);
		miniProfilePane.add(mySign);
		miniProfilePane.setOpaque(false);

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(myPortrait);
		this.add(Box.createHorizontalStrut(5));
		this.add(miniProfilePane);
		this.setOpaque(false);
	}
}

class ListsPane extends JTabbedPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7834874261118553395L;

	private JPanel frdlistpane;
	private JPanel grplistpane;
	private JPanel blklistpane;
	private JScrollPane frdListScrPane;
	private JScrollPane grpListScrPane;
	private JScrollPane blkListScrPane;

	ListsPane(FriendList flist, FriendList glist, FriendList blist,
			String path_flist, String path_glist, String path_blist) {
		frdlistpane = new JPanel();
		grplistpane = new JPanel();
		blklistpane = new JPanel();

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
		frdlistpane.setLayout(new BorderLayout());
		frdlistpane.add(frdListScrPane, BorderLayout.CENTER);
		frdlistpane.add(flist.getFilterField(), BorderLayout.NORTH);

		grplistpane.setLayout(new BorderLayout());
		grplistpane.add(grpListScrPane, BorderLayout.CENTER);
		grplistpane.add(glist.getFilterField(), BorderLayout.NORTH);

		blklistpane.setLayout(new BorderLayout());
		blklistpane.add(blkListScrPane, BorderLayout.CENTER);
		blklistpane.add(blist.getFilterField(), BorderLayout.NORTH);

		this.setTabPlacement(JTabbedPane.LEFT);
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);// 滚动标签(一行)
		// tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);//多行标签
		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);
		this.addTab(null, new ImageIcon(path_flist), frdlistpane);
		this.addTab(null, new ImageIcon(path_glist), grplistpane);
		this.addTab(null, new ImageIcon(path_blist), blklistpane);
		this.setOpaque(false);
	}
}

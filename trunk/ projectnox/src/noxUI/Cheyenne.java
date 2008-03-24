package noxUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class Cheyenne extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 默认尺寸常量
	 */
	public int WIDTH = 300;
	public int WIDTH_PREF = 300;
	public int WIDTH_MAX = 2000;
	public int WIDTH_MIN = 200;
	public int HEIGHT = 600;
	public int HEIGHT_PREF = 600;
	public int HEIGHT_MAX = 2000;
	public int HEIGHT_MIN = 300;
	/**
	 * 用来获取图片
	 */
	private Toolkit tk;
	private Image background;
	private Image img_icon;
	/**
	 * 各JPanel
	 */
	private JMapPanel rootpane;
	private JPanel titlebar;
	private JPanel profile;
	private JTabbedPane tabs;
	private FriendListPane frdlist;
	private JPanel grplist;
	private JPanel blklist;
	private JPanel footpane;
	
	JButton blogo;
	//JButton btitle;
	JLabel lab_title;
	JButton bminimize;
	//JButton bmaximize;
	JButton bclose;
	
	JButton myPortrait;
	JPanel miniProfilePane;
	JComboBox myStatus;
	JTextField mySign;
	
	JButton resizeButn;
	
	public static void main(String args[])
	{
		Cheyenne chyn = new Cheyenne();
		MoveMouseListener mml = new MoveMouseListener(chyn.getRootPane(), chyn);
		chyn.getRootPane().addMouseListener(mml);
		chyn.getRootPane().addMouseMotionListener(mml);
		chyn.setVisible(true);
	}
	Cheyenne()
    {
        super("NoX -- a IM system");
        
        tk = Toolkit.getDefaultToolkit();
		background = tk.getImage("resrc\\bkgrd.png");
		img_icon = tk.getImage("resrc\\NoXlogo_20.png");
		Cheyenne.this.setIconImage(img_icon);

		//准备图片
		this.prepareImage(background, rootpane);
		
        Container contentPane = getContentPane();
        Cheyenne.this.setBounds(500,80,WIDTH,HEIGHT);
        Cheyenne.this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
        Cheyenne.this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
        Cheyenne.this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
		//Cheyenne.this.setResizable(false);
		Cheyenne.this.setUndecorated(true); //不显示标题栏和边框
		JLabel test3 = new JLabel("what?");
		JLabel test4 = new JLabel("what?");
		JLabel test5 = new JLabel("what?");
		
		rootpane = new JMapPanel(background);
		
		titlebar = new JPanel();
		profile = new JPanel();
		tabs = new JTabbedPane();
		frdlist = new FriendListPane();
		grplist = new JPanel();
		blklist = new JPanel();
		
		blogo = new JButton(new ImageIcon(img_icon));
		blogo.setSize(new Dimension(20,20));
		blogo.setPreferredSize(new Dimension(20,20));
		blogo.setMaximumSize(new Dimension(20,20));
		blogo.setMinimumSize(new Dimension(20,20));
		blogo.setBorderPainted(false);
		blogo.setContentAreaFilled(false);
		blogo.setOpaque(false);
		blogo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//JOptionPane.showMessageDialog(null, "Hello, I am DaiZW, welcome to the NoX world!");
				AboutDialog about = new AboutDialog();
			    DialogEarthquakeCenter dec = new DialogEarthquakeCenter (about);
			    about.pack();
			    about.setModal (false);
			    about.setSize(new Dimension(500,350));
			    about.setPreferredSize(new Dimension(500,350));
				/*this.setMaximumSize(new Dimension(400,500));
				this.setMinimumSize(new Dimension(400,500));*/
			    about.setLocation(new Point(300, 150));
			    about.setVisible(true);
			    dec.startShake();//对话框必须setModal (false)才可以抖动, 否则不行
			}
		});
		
		//lab_title = new JLabel("NoX");
		lab_title = new JLabel(new ImageIcon("resrc\\nox.png"));
		
		/*btitle = new JButton(new ImageIcon("resrc\\nox.png"));
		btitle.setSize(new Dimension(60,20));
		btitle.setPreferredSize(new Dimension(60,20));
		btitle.setMaximumSize(new Dimension(60,20));
		btitle.setMinimumSize(new Dimension(60,20));
		btitle.setBorderPainted(false);
		btitle.setContentAreaFilled(false);
		btitle.setOpaque(false);*/
		
		bminimize = new JButton(new ImageIcon("resrc\\minimize.png"));
		bminimize.setRolloverIcon(new ImageIcon("resrc\\minimize_rollover.png"));
		bminimize.setSize(new Dimension(20,20));
		bminimize.setPreferredSize(new Dimension(20,20));
		bminimize.setMaximumSize(new Dimension(20,20));
		bminimize.setMinimumSize(new Dimension(20,20));
		bminimize.setBorderPainted(false);
		bminimize.setContentAreaFilled(false);
		bminimize.setOpaque(false);
		bminimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        int state = Cheyenne.this.getExtendedState();
		        
		    	// 设置图标化(iconifies)位
		        // Set the iconified bit
		        state |= JFrame.ICONIFIED;
		    
		    	// 图标化Frame
		        // Iconify the frame
		        Cheyenne.this.setExtendedState(state);
				//JOptionPane.showMessageDialog(null, "You just click the Minimize button~");
			}
		});
		
		//bmaximize = new JButton("max");
		bclose = new JButton(new ImageIcon("resrc\\close.png"));
		bclose.setRolloverIcon(new ImageIcon("resrc\\close_rollover.png"));
		bclose.setSize(new Dimension(20,20));
		bclose.setPreferredSize(new Dimension(20,20));
		bclose.setMaximumSize(new Dimension(20,20));
		bclose.setMinimumSize(new Dimension(20,20));
		bclose.setBorderPainted(false);
		bclose.setContentAreaFilled(false);
		bclose.setOpaque(false);
		bclose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Cheyenne.this.dispose();
				System.exit(0);
			}
		});
		
		titlebar.setLayout(new BoxLayout(titlebar, BoxLayout.X_AXIS));
		titlebar.setAlignmentX(JComponent.CENTER_ALIGNMENT);//设置对齐方式
		titlebar.add(blogo);
		titlebar.add(Box.createHorizontalGlue());
		titlebar.add(lab_title);
		//titlebar.add(btitle);
		titlebar.add(Box.createHorizontalGlue());
		titlebar.add(bminimize);
		titlebar.add(bclose);
		titlebar.setOpaque(false);
		//titlebar.setBackground(new Color(200, 0, 0));
		titlebar.setSize(new Dimension(WIDTH*2, 20));
		titlebar.setPreferredSize(new Dimension(WIDTH_PREF*2, 20));
		titlebar.setMaximumSize(new Dimension(WIDTH_MAX, 20));
		titlebar.setMinimumSize(new Dimension(WIDTH_MIN, 20));
		
		myPortrait = new JButton(new ImageIcon("resrc\\portrait\\portrait.png"));
		myPortrait.setSize(new Dimension(50,50));
		myPortrait.setPreferredSize(new Dimension(50,50));
		myPortrait.setMaximumSize(new Dimension(50,50));
		myPortrait.setMinimumSize(new Dimension(50,50));
		//myPortrait.setBorderPainted(false);
		myPortrait.setContentAreaFilled(false);
		myPortrait.setOpaque(false);
		
		miniProfilePane = new JPanel(); 
		myStatus = new JComboBox();
		mySign = new JTextField("Hello, everyone~");
		myStatus.addItem("Online");
		myStatus.addItem("Busy");
		myStatus.addItem("Hide");
		myStatus.addItem("Offline");
		myStatus.setSize(new Dimension(100,25));
		myStatus.setPreferredSize(new Dimension(100,25));
		myStatus.setMaximumSize(new Dimension(1000,25));
		myStatus.setMinimumSize(new Dimension(100,25));
		
		miniProfilePane.setLayout(new BoxLayout(miniProfilePane, BoxLayout.Y_AXIS));
		miniProfilePane.add(myStatus);
		miniProfilePane.add(mySign);
		
		profile.setLayout(new BoxLayout(profile, BoxLayout.X_AXIS));
		profile.add(myPortrait);
		profile.add(miniProfilePane);
		//profile.setOpaque(false);
		profile.setBackground(new Color(0, 255, 0));
		profile.setSize(new Dimension(WIDTH, 50));
		profile.setPreferredSize(new Dimension(WIDTH_PREF, 50));
		profile.setMaximumSize(new Dimension(WIDTH_MAX, 50));
		profile.setMinimumSize(new Dimension(WIDTH_MIN, 50));
		
		grplist.add(test4);
		blklist.add(test5);
		
		/**
		 * 好友列表
		 */
		JPanel flp = new JPanel();
		String[] listItems = {
	            "Chris", "Joshua", "Daniel", "Michael",
	            "Don", "Kimi", "Kelly", "Keagan", "夏", "商", "周", "张三", "张四", "张五", "张三丰"
	        };
	        flp.setLayout (new BorderLayout());
	        // populate list
	        FilteredJList list = new FilteredJList();
	        for (int i=0; i<listItems.length; i++)
	            list.addItem (listItems[i]);
	        // add to gui
	        JScrollPane pane =
	            new JScrollPane (list,
	                             ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
	                             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	        flp.add (pane, BorderLayout.CENTER);
	        flp.add (list.getFilterField(), BorderLayout.NORTH);

	        
		tabs.setTabPlacement(JTabbedPane.LEFT);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);//滚动标签(一行)
		//tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);//多行标签
		tabs.setBackground(new Color(110, 110, 110));
		tabs.addTab("", new ImageIcon("resrc\\chat.png"), flp);
		tabs.addTab("", new ImageIcon("resrc\\chatroom.png"), grplist);
		tabs.addTab("", new ImageIcon("resrc\\chat.png"), blklist);
		/*tabs.addTab("f", frdlist);
		tabs.addTab("g", grplist);
		tabs.addTab("b", blklist);*/
		tabs.setOpaque(false);
		
		/*JPanel a = new JPanel();
		JPanel b = new JPanel();
		JPanel c = new JPanel();
		JPanel d = new JPanel();
		
		tabs.addTab("friends", a);
		tabs.addTab("groups", b);
		tabs.addTab("blacklist", c);
		tabs.addTab("blacklist", d);*/
		
		footpane = new JPanel();
		resizeButn = new JButton(new AngledLinesWindowsCornerIcon());
		resizeButn.setBorderPainted(false);
		resizeButn.setContentAreaFilled(false);
		resizeButn.setSize(new Dimension(15,15));
		resizeButn.setPreferredSize(new Dimension(15,15));
		resizeButn.setMaximumSize(new Dimension(15,15));
		resizeButn.setMinimumSize(new Dimension(15,15));
		//resizeButn.setOpaque(false);
		
		ResizeListener resizer = new ResizeListener(Cheyenne.this, resizeButn);
		resizeButn.addMouseListener(resizer);
		resizeButn.addMouseMotionListener(resizer);
		
		footpane.setLayout(new BoxLayout(footpane, BoxLayout.X_AXIS));
		footpane.add(Box.createHorizontalGlue());
		footpane.add(resizeButn);
		footpane.setSize(new Dimension(WIDTH,15));
		footpane.setPreferredSize(new Dimension(WIDTH_PREF,15));
		footpane.setMaximumSize(new Dimension(WIDTH_MAX,15));
		footpane.setMinimumSize(new Dimension(WIDTH_MIN,15));
		footpane.setOpaque(false);
		
		rootpane.setLayout(new BoxLayout(rootpane, BoxLayout.Y_AXIS));
		rootpane.add(titlebar);
		rootpane.add(profile);
		rootpane.add(tabs);
		rootpane.add(footpane);
		contentPane.add(rootpane);
    }
}

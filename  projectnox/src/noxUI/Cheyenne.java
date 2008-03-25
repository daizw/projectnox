package noxUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.metal.MetalBorders;

public class Cheyenne extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Ĭ�ϳߴ糣��
	 */
	public static final int WIDTH = 300;
	public static final int WIDTH_PREF = 300;
	public static final int WIDTH_MAX = 2000;
	public static final int WIDTH_MIN = 200;
	public static final int HEIGHT = 600;
	public static final int HEIGHT_PREF = 600;
	public static final int HEIGHT_MAX = 2000;
	public static final int HEIGHT_MIN = 300;
	/**
	 * ������ȡͼƬ
	 */
	private Toolkit tk;
	private Image background;
	private Image img_icon;
	/**
	 * ��JPanel
	 */
	private JMapPanel rootpane;
	private JPanel titlebar;
	private JPanel profile;
	private JTabbedPane tabs;
	private JPanel frdlistpane;
	private JPanel grplistpane;
	private JPanel blklistpane;
	private JScrollPane frdListScrPane;
	private JScrollPane grpListScrPane;
	private JScrollPane blkListScrPane;
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
	
	//public static int counter = 0; 
	
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

		//׼��ͼƬ
		this.prepareImage(background, rootpane);
		
        Container contentPane = getContentPane();
        Cheyenne.this.setBounds(500,80,WIDTH,HEIGHT);
        Cheyenne.this.setSize(new Dimension(WIDTH, HEIGHT));
        Cheyenne.this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
        Cheyenne.this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
        Cheyenne.this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
		//Cheyenne.this.setResizable(false);
		Cheyenne.this.setUndecorated(true); //����ʾ�������ͱ߿�
		
		rootpane = new JMapPanel(background);
		
		titlebar = new JPanel();
		profile = new JPanel();
		
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
			    dec.startShake();//�Ի������setModal (false)�ſ��Զ���, ������
			}
		});
		
		/*lab_title = new JLabel("NoX");
		//Font font = new Font("����-���������ַ���", Font.BOLD, 24);
		Font font = new Font("Times New Roman", Font.BOLD, 24);
		lab_title.setForeground(Color.WHITE);
		lab_title.setFont(font);*/
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
		        
		    	// ����ͼ�껯(iconifies)λ
		        // Set the iconified bit
		        state |= JFrame.ICONIFIED;
		    
		    	// ͼ�껯Frame
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
		titlebar.setAlignmentX(JComponent.CENTER_ALIGNMENT);//���ö��뷽ʽ
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
		//myPortrait.setBorderPainted(true);
		
		/*myPortrait.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, 
				Color.RED, Color.BLUE));*/
		myPortrait.setContentAreaFilled(false);
		myPortrait.setOpaque(false);
		
		miniProfilePane = new JPanel();
		JPanel nickAndStat = new JPanel();
		JLabel nick = new JLabel("Shinysky");
		nick.setForeground(Color.WHITE);
		myStatus = new JComboBox();
		//myStatus.setOpaque(false);
		mySign = new JTextField("Hello, everyone~");
		mySign.setOpaque(false);
		mySign.setForeground(Color.WHITE);
		myStatus.addItem("Online");
		myStatus.addItem("Busy");
		myStatus.addItem("Hide");
		myStatus.addItem("Offline");
		myStatus.setSize(new Dimension(50,20));
		myStatus.setPreferredSize(new Dimension(50,20));
		myStatus.setMaximumSize(new Dimension(70,20));
		myStatus.setMinimumSize(new Dimension(50,20));
		nickAndStat.setOpaque(false);
		//nickAndStat.setBackground(new Color(0, 255, 0));
		nickAndStat.setSize(new Dimension(150,20));
		nickAndStat.setPreferredSize(new Dimension(150,20));
		nickAndStat.setMaximumSize(new Dimension(1000,20));
		nickAndStat.setMinimumSize(new Dimension(150,20));
		//nickAndStat.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		nickAndStat.setLayout(new BoxLayout(nickAndStat, BoxLayout.X_AXIS));
		nickAndStat.add(nick);
		nickAndStat.add(Box.createHorizontalStrut(10));
		nickAndStat.add(myStatus);
		
		mySign.setSize(new Dimension(WIDTH,20));
		mySign.setPreferredSize(new Dimension(WIDTH_PREF,20));
		mySign.setMaximumSize(new Dimension(WIDTH_MAX,20));
		mySign.setMinimumSize(new Dimension(WIDTH_MIN,20));
		
		//miniProfilePane.setAlignmentX(JComponent.TOP_ALIGNMENT);
		miniProfilePane.setLayout(new BoxLayout(miniProfilePane, BoxLayout.Y_AXIS));
		miniProfilePane.add(nickAndStat);
		miniProfilePane.add(mySign);
		miniProfilePane.setOpaque(false);
		
		profile.setLayout(new BoxLayout(profile, BoxLayout.X_AXIS));
		profile.add(myPortrait);
		profile.add(Box.createHorizontalStrut(5));
		profile.add(miniProfilePane);
		profile.setOpaque(false);
		//profile.setBackground(new Color(0, 255, 0));
		profile.setSize(new Dimension(WIDTH, 50));
		profile.setPreferredSize(new Dimension(WIDTH_PREF, 50));
		profile.setMaximumSize(new Dimension(WIDTH_MAX, 50));
		profile.setMinimumSize(new Dimension(WIDTH_MIN, 50));
		
		tabs = new JTabbedPane();
		frdlistpane = new JPanel();
		grplistpane = new JPanel();
		blklistpane = new JPanel();
		/**
		 * �����б�
		 */		
		String[] flistItems = {
	            "Chris", "Joshua", "Daniel", "Michael",
	            "Don", "Kimi", "Kelly", "Keagan", "��", "��", "��", "����", "����", "����", "������"
	            };
		
		// populate list
		//FilteredJList list = new FilteredJList();
	        
		FriendItem[] friends = new FriendItem[flistItems.length];
		//ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i<flistItems.length; i++)
		{
			friends[i] = new FriendItem(new ImageIcon("resrc\\portrait\\user.png"),
					flistItems[i], "(Hi, ����"+flistItems[i]+')');
		}
		FriendList flist = new FriendList(friends);
		/**
		 * ���б�
		 */		
		String[] glistItems = {
	            "group1", "group2", "group3", "group4", "�������", "��������"
	            };
	        
		FriendItem[] groups = new FriendItem[glistItems.length];

		for (int i = 0; i<glistItems.length; i++)
		{
			groups[i] = new FriendItem(new ImageIcon("resrc\\chatroom.png"),
					glistItems[i], "(Hi, ����"+glistItems[i]+"��������)");
		}
		FriendList glist = new FriendList(groups);
		/**
		 * ������
		 */		
		String[] blistItems = {
	            "Ben", "Laden", "Hitler", "Bush","��ˮ��"
	            };
		
		// populate list
		//FilteredJList list = new FilteredJList();
	        
		FriendItem[] badguys = new FriendItem[blistItems.length];
		//ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i<blistItems.length; i++)
		{
			badguys[i] = new FriendItem(new ImageIcon("resrc\\blacklist.png"),
					blistItems[i], "(Hi, ����"+blistItems[i]+')');
		}
		FriendList blist = new FriendList(badguys);
	        
		// add to gui
		frdListScrPane =
	            new JScrollPane (flist,
	                             ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
	                             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		grpListScrPane =
            new JScrollPane (glist,
                             ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		blkListScrPane =
            new JScrollPane (blist,
                             ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		frdlistpane.setLayout (new BorderLayout());
		frdlistpane.add (frdListScrPane, BorderLayout.CENTER);
		frdlistpane.add(flist.getFilterField(), BorderLayout.NORTH);
		
		grplistpane.setLayout (new BorderLayout());
		grplistpane.add (grpListScrPane, BorderLayout.CENTER);
		grplistpane.add(glist.getFilterField(), BorderLayout.NORTH);
		
		blklistpane.setLayout (new BorderLayout());
		blklistpane.add (blkListScrPane, BorderLayout.CENTER);
		blklistpane.add(blist.getFilterField(), BorderLayout.NORTH);

		tabs.setTabPlacement(JTabbedPane.LEFT);
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);//������ǩ(һ��)
		//tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);//���б�ǩ
		tabs.setBackground(Color.BLACK);
		tabs.setForeground(Color.WHITE);
		tabs.addTab(null, new ImageIcon("resrc\\chat.png"), frdlistpane);
		tabs.addTab(null, new ImageIcon("resrc\\chatroom.png"), grplistpane);
		tabs.addTab(null, new ImageIcon("resrc\\blacklist.png"), blklistpane);
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
		
		/*ImageBorder image_border = new ImageBorder(
	            new ImageIcon("resrc/upper_left.png").getImage(),
	            new ImageIcon("resrc/upper.png").getImage(),
	            new ImageIcon("resrc/upper_right.png").getImage(),

	            new ImageIcon("resrc/left_center.png").getImage(),
	            new ImageIcon("resrc/right_center.png").getImage(),

	            new ImageIcon("resrc/bottom_left.png").getImage(),
	            new ImageIcon("resrc/bottom_center.png").getImage(),
	            new ImageIcon("resrc/bottom_right.png").getImage()
	            );*/
		JPanel fakeFace = new JPanel();
		MatteBorder paneEdge = BorderFactory.createMatteBorder(2,2,2,2, Color.BLACK);
		fakeFace.setBorder(paneEdge);
		
		rootpane.setLayout(new BoxLayout(rootpane, BoxLayout.Y_AXIS));
		rootpane.add(titlebar);
		rootpane.add(profile);
		rootpane.add(tabs);
		rootpane.add(footpane);
		rootpane.setSize(new Dimension(WIDTH, HEIGHT));
		rootpane.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		rootpane.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		rootpane.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
        
		fakeFace.setLayout(new BoxLayout(fakeFace, BoxLayout.Y_AXIS));
		fakeFace.add(rootpane);

		contentPane.add(fakeFace);
    }
}

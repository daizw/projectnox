package noxUI;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.ScrollPaneConstants;

import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipe;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import net.nox.AuthenticationUtil;
import net.nox.GroupChatroomUnit;
import net.nox.PeerChatroomUnit;
import net.nox.NoxToolkit;
import net.nox.PeerGroupUtil;
import db.nox.DBTableName;
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
	
	Connection sqlconn;
	/**
	 * 好友列表/组列表/黑名单
	 */
	ObjectList friendlist, grouplist, blacklist;

	
	/**
	 * 个人/系统设置窗口
	 */
	ConfigCenterFrame ccf = new ConfigCenterFrame(this);
	/**
	 * 搜索窗口
	 */
	SearchingFrame sfrm = new SearchingFrame(Cheyenne.this);
	/**
	 * 
	 * @param flist
	 * @param glist
	 * @param blist
	 */
	Cheyenne(ObjectList flist, ObjectList glist, ObjectList blist, Connection conn) {
		super("NoX: a IM system", SystemPath.IMAGES_RESOURCE_PATH + "bkgrd.png", 
				SystemPath.LOGO_RESOURCE_PATH + "NoXlogo_20.png",
				SystemPath.LOGO_RESOURCE_PATH + "NoXlogo_48.png",
				SystemPath.LOGO_RESOURCE_PATH + "nox.png", true);

		NoxToolkit.setCheyenne(this);
		friendlist = flist;
		grouplist = glist;
		blacklist = blist;
		sqlconn = conn;
		/*try{
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }catch(Exception exe){
            exe.printStackTrace();
        }*/
        
		JPanel contentPane = this.getContainer();
		Cheyenne.this.setBounds(600, 80, WIDTH_DEFLT, HEIGHT_DEFLT);
		Cheyenne.this.setSize(new Dimension(WIDTH_DEFLT, HEIGHT_DEFLT));
		Cheyenne.this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		Cheyenne.this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		Cheyenne.this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));

		/**
		 * mini profile 组件 含: 头像, 昵称, 状态, 签名
		 */
		profile = new MiniProfilePane(this, SystemPath.PORTRAIT_RESOURCE_PATH + "portrait.png",
				NoxToolkit.getNetworkConfigurator().getName(), NoxToolkit.getNetworkManager().getNetPeerGroup().getPeerAdvertisement().getDescription());
		// profile.setBackground(new Color(0, 255, 0));
		profile.setSize(new Dimension(WIDTH_DEFLT, 50));
		profile.setPreferredSize(new Dimension(WIDTH_PREF, 50));
		profile.setMaximumSize(new Dimension(WIDTH_MAX, 50));
		profile.setMinimumSize(new Dimension(WIDTH_MIN, 50));

		tabs = new ListsPane(this, friendlist, grouplist, blacklist);

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.add(profile);
		contentPane.add(tabs);
		
		setForegroundColor();
		initTrayIcon();
		
		sfrm.setLocation(100, 60);
		sfrm.setSize(new Dimension(500, 350));
	}
	public Connection getSQLConnection(){
		return sqlconn;
	}
	/**
	 * 将广告所代表的peer添加到好友列表中
	 * @param adv 要添加的peer的广告 
	 * @return 好友的列表元素
	 */
	public PeerItem add2PeerList(PeerAdvertisement adv, boolean good){
		//TODO 将广告所代表的peer添加到好友列表中
		
		PeerItem newFriend = new PeerItem(new ImageIcon(
				SystemPath.PORTRAIT_RESOURCE_PATH + "user.png"), adv);
		
		//这样的赋值目前没有必要,
		//不过在已经存在好友, 且头像有变化的时候有用;
		try {
			if(good)
				newFriend = (PeerItem) friendlist.addItem(newFriend);
			else
				newFriend = (PeerItem) blacklist.addItem(newFriend);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tabs.repaint();
		return newFriend;
	}
	/**
	 * 将广告所代表的peer添加到好友列表中
	 * TODO 这部分可以参考JXTA Prog Guide2.5的Membership Service 一章中对InteractiveAuthenticator的介绍.
	 * 
	 * @param adv 要添加的peer的广告 
	 * @return 成功:返回false; 如果已经处于该组中: 返回true.
	 */
	public boolean joinThisGroup(PeerGroupAdvertisement adv){
		//TODO 加入到adv所代表的组中
		PeerGroup ppg = NoxToolkit.getNetworkManager().getNetPeerGroup();
		PeerGroup pg = null;
		
		//adv.get
		// Create the group itself
        try {
            pg = ppg.newGroup(adv);
        } catch (PeerGroupException pge) {
        	pge.printStackTrace();
        }
     // if the group was successfully created join it
        if (pg != null) {
        	if(AuthenticationUtil.isAuthenticated(pg)){
    			System.out.println("你已加入该组, 不需要重新加入. If you're surpried, it may because this group need no password.");
    			JOptionPane.showMessageDialog((Component) null,
    					"你已加入该组, 不需要重新加入. If you're surpried, it may because this group need no password.",
    					"Succeed!",
    					JOptionPane.INFORMATION_MESSAGE);
    			//将该组加入列表中
        		GroupItem newGroupItem = new GroupItem(new ImageIcon(
        				SystemPath.PORTRAIT_RESOURCE_PATH + "group.png"), adv);
        		try {
        			newGroupItem = (GroupItem) grouplist.addItem(newGroupItem);
        		} catch (SQLException e) {
        			e.printStackTrace();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        		tabs.repaint();
        		
        		return true;
    		}
        	
        	System.out.println("尝试加入组");
        	String password = GetPassword();
        	if(password == null || password.trim().equals("")){
        		//TODO 可以获取更多信息的密码输入窗口:
        		//1. 用户点击了OK;--密码错误
        		//2. 用户点击Cancel;--忽略
        		//3. 用户直接关闭窗口.--忽略
        		System.out.println("The user just canceled the joining process?");
        		return false;
        	}
        	//pg.getMembershipService().
        	boolean joined = PeerGroupUtil.joinPeerGroup(pg, PeerGroupUtil.MEMBERSHIP_ID, password);
        	
        	if(joined){
        		JOptionPane.showMessageDialog((Component) null,
					"您已成功加入该组. 可在组列表中查看.", "Succeed!",
					JOptionPane.INFORMATION_MESSAGE);
        		
        		//将该组加入列表中
        		GroupItem newGroupItem = new GroupItem(new ImageIcon(
        				SystemPath.PORTRAIT_RESOURCE_PATH + "group.png"), adv);
        		try {
        			newGroupItem = (GroupItem) grouplist.addItem(newGroupItem);
        		} catch (SQLException e) {
        			e.printStackTrace();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}
        		tabs.repaint();
        		
        		return true;
        	}
        	else{
        		JOptionPane.showMessageDialog((Component) null,
    					"未能成功加入该组, 密码错误?", "Failure!",
    					JOptionPane.ERROR_MESSAGE);
        		return false;
        	}
        } else {
            System.out.println("Error: failed to create new group");
            System.out.println("使用组广告创建组失败");
			JOptionPane.showMessageDialog((Component) null,
					"使用组广告创建组失败", "Phew~",
					JOptionPane.ERROR_MESSAGE);
			return false;
        }
	}
	private String GetPassword() {
		return (String) JOptionPane.showInputDialog(this, "Please enter the password:",
				"Password Needed", JOptionPane.QUESTION_MESSAGE, null, null, "");
	}
	/**
	 * 在存在对应ID-Pipe对, 而不存在对应聊天室的情况下:
	 * <li>如果是好友的消息, 则(暂时)建立聊天室显示之.
	 * (应当)提示有新消息</li>
	 * <li>如果不是好友的消息, 则(暂时)将之添加为好友建立聊天室并显示之.
	 * (应当)提示有来自陌生人的新消息</li>
	 * @param connhandler 用于管理连接的ConnectionHandler
	 * @return 新建立的chatroom, 用于注册到NoxToolkit
	 */
	public PeerChatroom setupNewChatroomOver(JxtaBiDiPipe pipe){
		//添加到好友列表
		//如果已经添加, 则在做无用功.
		PeerItem friend = add2PeerList(pipe.getRemotePeerAdvertisement(), true);
		//打开聊天室
		PeerChatroom chatroom = new PeerChatroom(friend, pipe);
		//注册之
		NoxToolkit.registerChatroom(friend.getUUID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}
	/**
	 * 在存在对应ID-Pipe对, 而不存在对应聊天室的情况下:
	 * <li>如果是好友的消息, 则(暂时)建立聊天室显示之.
	 * (应当)提示有新消息</li>
	 * <li>如果不是好友的消息, 则(暂时)将之添加为好友建立聊天室并显示之.
	 * (应当)提示有来自陌生人的新消息</li>
	 * @param connhandler 用于管理连接的ConnectionHandler
	 * @return 新建立的chatroom, 用于注册到NoxToolkit
	 */
	public GroupChatroom setupNewChatroomOver(GroupItem group, OutputPipe pipe){
		//打开聊天室
		GroupChatroom chatroom = new GroupChatroom(group, null, pipe);
		//注册之
		NoxToolkit.registerChatroom(group.getUUID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}
	public boolean setupGroupChatroom(PeerGroupAdvertisement adv){
		//打开群聊窗口
		//do something
		return true;
	}
	public void showSearchingFrame(){
		sfrm.setVisible(true);
	}
	/**
	 * 设置托盘
	 */
	private void initTrayIcon(){
        try{
            SystemTray tray=SystemTray.getSystemTray();
            Image trayImg=ImageIO.read(new File(SystemPath.LOGO_RESOURCE_PATH + "NoXlogo_16.png"));
            PopupMenu traymenu=new PopupMenu("Tray Menu");
            traymenu.add(new MenuItem("Configure")).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae){
                	ccf.setVisible(true);
                }
            });
            traymenu.add(new MenuItem("Search")).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae){
            		//sfrm.pack();
            		sfrm.setVisible(true);
                }
            });
            traymenu.add(new MenuItem("Show up")).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae){
                    Cheyenne.this.setVisible(true);
                    Cheyenne.this.setExtendedState(JFrame.NORMAL );
                }
            });
            traymenu.addSeparator();
            traymenu.add(new MenuItem("About NoX")).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae){
                	JOptionPane.showMessageDialog(Cheyenne.this,
                			"<html><Font color=red><center><h2>About</h2></center></Font>" +
                            "NoX is a P2P instant messaging system<br>base on JXTA, similar to QQ, MSN, etc.<br>" +
                            "<br>Enjoy! :)<br><br>" +
                            "If there's any problem, please contact me.<br>" +
                            "<Font color=blue>Author: Dai Zhiwei@SEU<br>" +
                            "moyueyh-net@yahoo.com.cn" +
                            "</Font></html>");
                }
            });
           // MenuItem mi = new MenuItem("Exit");
            //mi.setShortcut(new MenuShortcut(KeyEvent.VK_X, true));
            traymenu.add(new MenuItem("Exit")).addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae){
                	try {
                		Statement stmt = sqlconn.createStatement();
                		stmt.execute("SHUTDOWN");
						sqlconn.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					NoxToolkit.getNetwork().StopNetwork();
                    System.exit(0);
                }
            });
            //TODO 整个好看点的菜单?
            /*MenuElement els[] = traymenu.getSubElements();
			for(int i = 0; i < els.length; i++)
				els[i].getComponent().setBackground(Color.WHITE);*/
			
            TrayIcon trayIcon=new TrayIcon(trayImg,NoxToolkit.getNetworkConfigurator().getName() + " - NoX",traymenu);
            tray.add(trayIcon);
            trayIcon.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae){
                    Cheyenne.this.setVisible(true);
                }
            });
        }catch(Exception exe){
            exe.printStackTrace();
        }
    }
	/**
	 * 设置窗口前景颜色
	 */
	public void setForegroundColor()
	{
		super.setForegroundColor();
		Color color = super.getForegroundColor();
		profile.setForegroundColor(color);
	}
	/**
	 * 设置窗口背景颜色
	 */
	public void setBackgroundColor(Color color)
	{
		super.setBackgroundColor(color);
		if(color.equals(Color.WHITE))
			tabs.setBackground(Color.GRAY);
		else
			tabs.setBackground(color);
	}
	/**
	 * 显示个人/系统设置窗口
	 */
	public void ShowConfigCenter(){
		ccf.setVisible(true);
	}
	/**
	 * 显示创建组窗口
	 */
	public void ShowCreateNewGroupDialog(){
		CreateNewGroupDialog cngD = new CreateNewGroupDialog(this);
		cngD.setVisible(true);
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
	JPanel nickAndStat;
	JLabel nick;
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
	MiniProfilePane(final Cheyenne parent, String path_portrait, String nickname, String sign) {
		myPortrait = new JButton(new ImageIcon(path_portrait));
		myPortrait.setToolTipText(getHtmlText("This is Me"));
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

		myPortrait.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.ShowConfigCenter();
			}
		});
		
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
		nickAndStat = new JPanel();
		nick = new JLabel(nickname);
		nick.setToolTipText(getHtmlText("My nickname"));
		myStatus = new JComboBox();
		// myStatus.setOpaque(false);
		mySign = new JTextField(sign);
		
		myStatus.addItem("Online");
		myStatus.addItem("Busy");
		myStatus.addItem("Invisible");
		myStatus.addItem("Offline");
		myStatus.setToolTipText(getHtmlText("My Status"));
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

		mySign.setOpaque(false);
		mySign.setToolTipText(getHtmlText("My Description"));
		mySign.setSize(new Dimension(Cheyenne.WIDTH_DEFLT, 20));
		mySign.setPreferredSize(new Dimension(Cheyenne.WIDTH_PREF, 20));
		mySign.setMaximumSize(new Dimension(Cheyenne.WIDTH_MAX, 20));
		mySign.setMinimumSize(new Dimension(Cheyenne.WIDTH_MIN, 20));
		//mySign.setEnabled(false);
		mySign.setEditable(false);
		mySign.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				mySign.setEditable(true);
				mySign.setOpaque(true);
				if(mySign.getForeground().equals(Color.WHITE))
						mySign.setForeground(Color.BLACK);
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				mySign.setEditable(false);
				mySign.setOpaque(false);
				mySign.setForeground(nick.getForeground());
			}
		});
		
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
	public void setForegroundColor(Color color){
		nick.setForeground(color);
		mySign.setForeground(color);
	}
	/**
	 * 返回TooltipTxt的html形式
	 * @param text
	 * @return
	 */
	private String getHtmlText(String text) {
		return ("<html><BODY bgColor=#ffffff><Font color=black>" + text + "</Font></BODY></html>");
	}
}

@SuppressWarnings("serial")
class ListsPane extends JTabbedPane {
	private JPanel frdlistpane;
	private JPanel grplistpane;
	private JScrollPane frdListScrPane;
	private JScrollPane grpListScrPane;
	private JScrollPane blkListScrPane;
	
	JButton myFriends = new JButton("My Friends("+7+'/'+15+')');
	JButton blacklist = new JButton("Blacklist");

	NoxJListItem listItem = null;
	Cheyenne parent;
	
	ListsPane(Cheyenne par, final ObjectList flist, final ObjectList glist, final ObjectList blist) {
		frdlistpane = new JPanel();
		grplistpane = new JPanel();
		parent = par;
		
		myFriends.setSize(new Dimension(Cheyenne.WIDTH_DEFLT, 20));
		myFriends.setPreferredSize(new Dimension(Cheyenne.WIDTH_PREF, 20));
		myFriends.setMaximumSize(new Dimension(Cheyenne.WIDTH_MAX, 20));
		myFriends.setMinimumSize(new Dimension(Cheyenne.WIDTH_MIN, 20));
		blacklist.setSize(new Dimension(Cheyenne.WIDTH_DEFLT, 20));
		blacklist.setPreferredSize(new Dimension(Cheyenne.WIDTH_PREF, 20));
		blacklist.setMaximumSize(new Dimension(Cheyenne.WIDTH_MAX, 20));
		blacklist.setMinimumSize(new Dimension(Cheyenne.WIDTH_MIN, 20));

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
		
		frdlistpane.setLayout(new BoxLayout(frdlistpane, BoxLayout.Y_AXIS));
		frdlistpane.setAlignmentX(0.0f);
		frdlistpane.setAlignmentY(0.0f);
		
		frdlistpane.add(flist.getFilterField());
		frdlistpane.add(blist.getFilterField());
		frdlistpane.add(myFriends);
		//myFriends.setLocation(myFriends.getLocation().x - 1000, 0);
		//myFriends.setAlignmentX(LEFT_ALIGNMENT);
		frdlistpane.add(frdListScrPane);
		frdlistpane.add(blacklist);
		//blacklist.setAlignmentY(RIGHT_ALIGNMENT);
		frdlistpane.add(blkListScrPane);
		
		/**
		 * 初始不可见
		 */
		blist.getFilterField().setVisible(false);
		blkListScrPane.setVisible(false);
		
		myFriends.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				flist.getFilterField().setVisible(true);
				frdListScrPane.setVisible(true);
				blist.getFilterField().setVisible(false);
				blkListScrPane.setVisible(false);
				ListsPane.this.repaint();
				Thread playThd = new Thread(new Runnable() {
					@Override
					public void run() {
						playAudio();
					}
				}, "Beeper");
				playThd.start();
			}
		});
		blacklist.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				flist.getFilterField().setVisible(false);
				frdListScrPane.setVisible(false);
				blist.getFilterField().setVisible(true);
				blkListScrPane.setVisible(true);
				ListsPane.this.repaint();
				Thread playThd = new Thread(new Runnable() {
					@Override
					public void run() {
						playAudio();
					}
				}, "Beeper");
				playThd.start();
			}
		});
		
		/*flist.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent se) {
				// TODO Auto-generated method stub
			}
		});*/
		flist.addMouseListener(new MouseListener(){
			@SuppressWarnings("serial")
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					//TODO 判断所点击的cell的在线状态进行对应处理, 暂时直接弹出弹出聊天窗口.
					/**
					 * TODO 应该对每一个对象只开一个窗口, 可以设定标记, 如果已经打开了一个则显示之, 否则开新窗口
					 */
					listItem = (PeerItem)flist.getSelectedValue();
					ListsPane.this.showPeerChatroom((PeerItem)listItem);
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu friendOprMenu = new JPopupMenu();
					listItem = (PeerItem)flist.getSelectedValue();
					if(listItem == null)
						return;
					//System.out.println("You just Right Click the List Item!");
					friendOprMenu.add(new AbstractAction("Talk to him/her") {
						public void actionPerformed(ActionEvent e) {
							ListsPane.this.showPeerChatroom((PeerItem)listItem);
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
									+ listItem.getNick()
									+"<br></Font>"
									+"<Font color=black>签名档:</Font> <Font color=blue>"
									+ listItem.getSign()
									+"<br></Font>"
									+"<Font color=black>联系方式:</Font> <Font color=blue>"
									+ "110, 119, 120, 114, 117"
									+"<br></Font>"
									+"<Font color=black>个人说明:</Font> <Font color=blue>"
									+ listItem.getNick() + " owns me so much MONEY!! "
									+"<br></Font></BODY></html>",
									"User Information", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					friendOprMenu.add(new AbstractAction("Add to the blacklist") {
						public void actionPerformed(ActionEvent e) {
							//TODO add to the blacklist
							int index = flist.getSelectedIndex();
							PeerItem temppeer = (PeerItem)flist.deleteItem(parent.getSQLConnection(), DBTableName.PEER_SQLTABLE_NAME, true, index);
							try {
								blist.addItem(temppeer);
							} catch (SQLException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							ListsPane.this.repaint();
						}
					});
					friendOprMenu.add(new AbstractAction("Delete") {
						public void actionPerformed(ActionEvent e) {
							//TODO add to the blacklist
							int index = flist.getSelectedIndex();
							flist.deleteItem(parent.getSQLConnection(), DBTableName.PEER_SQLTABLE_NAME, true, index);
							ListsPane.this.repaint();
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
			public void mouseEntered(MouseEvent arg0) {
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		
		blist.addMouseListener(new MouseListener(){
			@SuppressWarnings("serial")
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					listItem = (PeerItem)blist.getSelectedValue();
					JOptionPane.showMessageDialog((Component) null, 
							"<html>"//<BODY bgColor=#ffffff>"
							+ "<img width=64 height=64 src=\"file:/"
							+ System.getProperty("user.dir")
							+ System.getProperty("file.separator")
							+ SystemPath.PORTRAIT_RESOURCE_PATH
							+ "chat.png\"><br>"
							+"<Font color=black>昵称:</Font> <Font color=blue>"
							+ listItem.getNick()
							+"<br></Font>"
							+"<Font color=black>签名档:</Font> <Font color=blue>"
							+ listItem.getSign()
							+"<br></Font>"
							+"<Font color=black>联系方式:</Font> <Font color=blue>"
							+ "110, 119, 120, 114, 117"
							+"<br></Font>"
							+"<Font color=black>个人说明:</Font> <Font color=blue>"
							+ listItem.getNick() + " owns me so much MONEY!! "
							+"<br></Font></BODY></html>",
							"User Information", JOptionPane.INFORMATION_MESSAGE);
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu blacklistOprMenu = new JPopupMenu();
					listItem = (PeerItem)blist.getSelectedValue();
					if(listItem == null)
						return;
					blacklistOprMenu.add(new AbstractAction("His/Her information") {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog((Component) null, 
									"<html>"//<BODY bgColor=#ffffff>"
									+ "<img width=64 height=64 src=\"file:/"
									+ System.getProperty("user.dir")
									+ System.getProperty("file.separator")
									+ SystemPath.PORTRAIT_RESOURCE_PATH
									+ "chat.png\"><br>"
									+"<Font color=black>昵称:</Font> <Font color=blue>"
									+ listItem.getNick()
									+"<br></Font>"
									+"<Font color=black>签名档:</Font> <Font color=blue>"
									+ listItem.getSign()
									+"<br></Font>"
									+"<Font color=black>联系方式:</Font> <Font color=blue>"
									+ "110, 119, 120, 114, 117"
									+"<br></Font>"
									+"<Font color=black>个人说明:</Font> <Font color=blue>"
									+ listItem.getNick() + " owns me so much MONEY!! "
									+"<br></Font></BODY></html>",
									"User Information", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					blacklistOprMenu.add(new AbstractAction("Add to the friendlist") {
						public void actionPerformed(ActionEvent e) {
							//TODO add to the blacklist
							int index = blist.getSelectedIndex();
							PeerItem temppeer = (PeerItem)blist.deleteItem(parent.getSQLConnection(), DBTableName.PEER_SQLTABLE_NAME, false, index);
							try {
								flist.addItem(temppeer);
							} catch (SQLException e1) {
								e1.printStackTrace();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							ListsPane.this.repaint();
						}
					});
					blacklistOprMenu.add(new AbstractAction("Delete") {
						public void actionPerformed(ActionEvent e) {
							//TODO add to the blacklist
							int index = blist.getSelectedIndex();
							blist.deleteItem(parent.getSQLConnection(), DBTableName.PEER_SQLTABLE_NAME, false, index);
							ListsPane.this.repaint();
						}
					});
					MenuElement els[] = blacklistOprMenu.getSubElements();
					for(int i = 0; i < els.length; i++)
						els[i].getComponent().setBackground(Color.WHITE);
					blacklistOprMenu.setLightWeightPopupEnabled(true);
					blacklistOprMenu.pack();
					// 位置应该是相对于源的位置
					blacklistOprMenu.show((Component) me.getSource(), me.getPoint().x, me.getPoint().y);
				}
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
			}
			@Override
			public void mousePressed(MouseEvent arg0) {
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		
		grplistpane.setLayout(new BorderLayout());
		grplistpane.add(grpListScrPane, BorderLayout.CENTER);
		grplistpane.add(glist.getFilterField(), BorderLayout.NORTH);

		/**
		 * 组成员列表, 测试用
		 * TODO 从数据库产生一个GroupItem[], 然后传值给objectlist构造函数
		 * 或者 群聊窗口中组成员列表可以由聊天室窗口直接从数据库获取? 无所谓..最好在这里根据GroupID来获取列表然后传值,
		 * 尽量把数据库操作集中在一个java文件中
		 */
		/*String[] flistItems = {};

		final GroupItem[] gmembers = new GroupItem[flistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		PeerGroupID groupID = null;
		//TODO 得到grouproom 的ID, 然后传值;
		for (int i = 0; i < flistItems.length; i++) {
			gmembers[i] = new GroupItem(new ImageIcon(
					SystemPath.PORTRAIT_RESOURCE_PATH + "user.png"),
					flistItems[i], "欢迎加入我们: " + flistItems[i], groupID, 0, 0);
		}*/
		
		glist.addMouseListener(new MouseListener(){
			@SuppressWarnings("serial")
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					//TODO 判断所点击的cell的在线状态进行对应处理, 暂时直接弹出弹出聊天窗口.
					listItem = (GroupItem)(glist.getSelectedValue());
					ListsPane.this.showGroupChatroom((GroupItem)listItem);
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu groupOprMenu = new JPopupMenu();
					/*
					 * 怎么实现右键可选取JListItem?
					 */
					listItem = (GroupItem)glist.getSelectedValue();
					//System.out.println(flist.getComponentAt(me.getPoint()).toString());
					
					if(listItem == null)
						return;
					//System.out.println("You just Right Click the List Item!");
					groupOprMenu.add(new AbstractAction("Enter this chatroom") {
						public void actionPerformed(ActionEvent e) {
							ListsPane.this.showGroupChatroom((GroupItem)listItem);
						}
					});
					groupOprMenu.add(new AbstractAction("Group information") {
						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog((Component) null, 
									"<html>"//<BODY bgColor=#ffffff>"
									+ "<img width=64 height=64 src=\"file:/"
									+ System.getProperty("user.dir")
									+ System.getProperty("file.separator")
									+ SystemPath.PORTRAIT_RESOURCE_PATH
									+"chat.png\"><br>"
									+"<Font color=black>组名:</Font> <Font color=blue>"
									+ listItem.getNick()
									+"<br></Font>"
									+"<Font color=black>公告:</Font> <Font color=blue>"
									+ listItem.getSign()
									+"<br></Font>"
									+"<Font color=black>成员数量:</Font> <Font color=blue>"
									+ "110, 119, 120, 114, 117"
									+"<br></Font></BODY></html>",
									"User Information", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					MenuElement els[] = groupOprMenu.getSubElements();
					for(int i = 0; i < els.length; i++)
						els[i].getComponent().setBackground(Color.WHITE);
					groupOprMenu.setLightWeightPopupEnabled(true);
					groupOprMenu.pack();
					// 位置应该是相对于源的位置
					groupOprMenu.show((Component) me.getSource(), me.getPoint().x, me.getPoint().y);
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
		
		/*blklistpane.setLayout(new BorderLayout());
		blklistpane.add(blkListScrPane, BorderLayout.CENTER);
		blklistpane.add(blist.getFilterField(), BorderLayout.NORTH);*/

		this.setTabPlacement(JTabbedPane.LEFT);
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);// 滚动标签(一行)
		// tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);//多行标签
		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);

		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "chat.png"), frdlistpane);
		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "chatroom.png"), grplistpane);
		this.setToolTipTextAt(0, getHtmlText("Friends"));
		this.setToolTipTextAt(1, getHtmlText("Groups"));
		
		JPanel searchPane = new JPanel();
		searchPane.setBackground(Color.WHITE);
		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "search_25.png"), searchPane);
		this.setToolTipTextAt(2, getHtmlText("Search"));
		searchPane.addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			@Override
			public void componentResized(ComponentEvent arg0) {
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
				parent.showSearchingFrame();
			}
		});
		JPanel configPane = new JPanel();
		configPane.setBackground(Color.WHITE);
		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "config_25.png"), configPane);
		this.setToolTipTextAt(3, getHtmlText("Configuration"));
		configPane.addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			@Override
			public void componentResized(ComponentEvent arg0) {
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
				parent.ShowConfigCenter();
			}
		});
		//this.addTab(null, new ImageIcon(path_blist), blklistpane);
		JPanel creatGroupPane = new JPanel();
		creatGroupPane.setBackground(Color.WHITE);
		this.addTab(null, new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "new_group_25.png"), creatGroupPane);
		this.setToolTipTextAt(4, getHtmlText("Create New Group"));
		creatGroupPane.addComponentListener(new ComponentListener(){
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {
			}
			@Override
			public void componentResized(ComponentEvent arg0) {
			}
			@Override
			public void componentShown(ComponentEvent arg0) {
				parent.ShowCreateNewGroupDialog();
			}
		});
		
		this.setOpaque(false);
	}
	/**
	 * 返回TooltipTxt的html形式
	 * @param text
	 * @return
	 */
	private String getHtmlText(String text) {
		return ("<html><BODY bgColor=#ffffff><Font color=black>" + text + "</Font></BODY></html>");
	}
	/**
	 * (在主界面双击好友或者组时被调用)弹出聊天窗口.
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
				room = parent.setupNewChatroomOver(roomunit.getOutPipe());
				//new NoxToolkit().registerChatroom(id, room);
			}else{
				room.pack();
				room.setVisible(true);
			}
		}
	}
	private void showGroupChatroom(GroupItem listItem) {
		ID id = listItem.getUUID();
		GroupChatroomUnit roomunit = (GroupChatroomUnit)NoxToolkit.getChatroomUnit(id);
		GroupChatroom room;
		
		if(roomunit == null){
			//未注册pipe, 更无chatroom.
			//新建聊天室, 会试图连接.
			//如果连接不上....
			//如果连接成功....
			room = new GroupChatroom(listItem, null, null);
		}else{
			//已注册pipe
			room = roomunit.getChatroom();
			if(room == null)
			{//不存在, 开新窗口
				room = parent.setupNewChatroomOver(listItem, roomunit.getOutPipe());
				//new NoxToolkit().registerChatroom(id, room);
			}else{
				room.pack();
				room.setVisible(true);
			}
		}
	}
	/**
	 * 切换列表时播放提示音
	 * ....某些音频文件会降低速度
	 */
	public void playAudio() {
		AudioClip playsound;
		try {
			// AudioClip audioClip = Applet.newAudioClip(completeURL)
			// codeBase = new URL("file:" + System.getProperty("user.dir") +
			// "/");
			URL url = new URL("file:/" + System.getProperty("user.dir")
					+ System.getProperty("file.separator")
					+ SystemPath.AUDIO_RESOURCE_PATH
					+ "folderwpcm.wav");
			playsound = Applet.newAudioClip(url);
			// System.out.println(url);
			playsound.play();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}
}

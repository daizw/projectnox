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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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

import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
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
	Cheyenne(ObjectList flist, ObjectList glist, ObjectList blist) {
		super("NoX: a IM system", "resrc\\images\\bkgrd.png", 
				"resrc\\logo\\NoXlogo_20.png", "resrc\\logo\\nox.png",
				"resrc\\buttons\\minimize.png", "resrc\\buttons\\minimize_rollover.png",
				"resrc\\buttons\\maximize.png", "resrc\\buttons\\maximize_rollover.png",
				"resrc\\buttons\\normalize.png", "resrc\\buttons\\normalize_rollover.png",
				"resrc\\buttons\\close.png", "resrc\\buttons\\close_rollover.png", true);

		friendlist = flist;
		grouplist = glist;
		blacklist = blist;
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
		profile = new MiniProfilePane(this, "resrc\\portrait\\portrait.png",
				"Shinysky", "Hello, everyone~");
		// profile.setBackground(new Color(0, 255, 0));
		profile.setSize(new Dimension(WIDTH_DEFLT, 50));
		profile.setPreferredSize(new Dimension(WIDTH_PREF, 50));
		profile.setMaximumSize(new Dimension(WIDTH_MAX, 50));
		profile.setMinimumSize(new Dimension(WIDTH_MIN, 50));

		tabs = new ListsPane(friendlist, grouplist, blacklist, "resrc\\icons\\chat.png",
				"resrc\\icons\\chatroom.png", "resrc\\icons\\blacklist.png");

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.add(profile);
		contentPane.add(tabs);
		
		setForegroundColor();
		initTrayIcon();
		
		sfrm.setLocation(0, 0);
		sfrm.setSize(new Dimension(1000, 350));
	}
	/**
	 * 将广告所代表的peer添加到好友列表中
	 * @param adv 要添加的peer的广告 
	 * @return 成功:返回false; 如果已经处于好友列表中: 返回true.
	 */
	public boolean add2Friendlist(PeerAdvertisement adv){
		//TODO 将广告所代表的peer添加到好友列表中
		PeerItem newFriend = new PeerItem(new ImageIcon(
		"resrc\\portrait\\user.png"), adv.getName(), adv.getDescription(), adv.getPeerID().toString());
		
		friendlist.addItem(newFriend);
		tabs.repaint();
		return false;
	}
	/**
	 * 将广告所代表的peer添加到好友列表中
	 * @param adv 要添加的peer的广告 
	 * @return 成功:返回false; 如果已经处于该组中: 返回true.
	 */
	public boolean joinThisGroup(PeerGroupAdvertisement adv){
		//TODO 加入到adv所代表的组中
		grouplist.toString();
		tabs.repaint();
		return false;
	}
	
	/**
	 * 设置托盘
	 */
	private void initTrayIcon(){
        try{
            SystemTray tray=SystemTray.getSystemTray();
            Image trayImg=ImageIO.read(new File("resrc\\logo\\NoXlogo_16.png"));
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
                    System.exit(0);
                }
            });
            TrayIcon trayIcon=new TrayIcon(trayImg,"NoX",traymenu);
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
	void ShowConfigCenter(){
		ccf.setVisible(true);
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
		myStatus = new JComboBox();
		// myStatus.setOpaque(false);
		mySign = new JTextField(sign);
		mySign.setOpaque(false);
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
	public void setForegroundColor(Color color){
		nick.setForeground(color);
		mySign.setForeground(color);
	}
}

class ListsPane extends JTabbedPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7834874261118553395L;

	private JPanel frdlistpane;
	private JPanel grplistpane;
	private JScrollPane frdListScrPane;
	private JScrollPane grpListScrPane;
	private JScrollPane blkListScrPane;
	
	JButton myFriends = new JButton("My Friends("+7+'/'+15+')');
	JButton blacklist = new JButton("Blacklist");

	Dimension btnsize = new Dimension(Cheyenne.WIDTH_PREF, 20);

	NoxJListItem fi = null;
	
	ListsPane(final ObjectList flist, final ObjectList glist, final ObjectList blist,
			String path_flist, String path_glist, String path_blist) {
		frdlistpane = new JPanel();
		grplistpane = new JPanel();
		
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
				//playAudio();
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
				//playAudio();
			}
		});
		
		/*flist.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent se) {
				// TODO Auto-generated method stub
			}
		});*/
		flist.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					//TODO 判断所点击的cell的在线状态进行对应处理, 暂时直接弹出弹出聊天窗口.
					/**
					 * TODO 应该对每一个对象只开一个窗口, 可以设定标记, 如果已经打开了一个则显示之, 否则开新窗口
					 */
					Chatroom room = new Chatroom((PeerItem)flist.getSelectedValue());
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu fiendOprMenu = new JPopupMenu();
					
					fi = (PeerItem)flist.getSelectedValue();
					if(fi == null)
						return;
					//System.out.println("You just Right Click the List Item!");
					fiendOprMenu.add(new AbstractAction("Talk to him/her") {
						/**
						 * 
						 */
						private static final long serialVersionUID = -729947600305959488L;

						public void actionPerformed(ActionEvent e) {
							Chatroom room = new Chatroom((PeerItem)flist.getSelectedValue());
						}
					});
					fiendOprMenu.add(new AbstractAction("His/Her information") {
						/**
						 * 
						 */
						private static final long serialVersionUID = -729947600305959488L;

						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog((Component) null, 
									"<html>"//<BODY bgColor=#ffffff>"
									+ "<img width=64 height=64 src=\"file:///E:/Java/NoX/resrc/dump/edit_user.png\"><br>"
									+"<Font color=black>昵称:</Font> <Font color=blue>"
									+ fi.getNick()
									+"<br></Font>"
									+"<Font color=black>签名档:</Font> <Font color=blue>"
									+ fi.getSign()
									+"<br></Font>"
									+"<Font color=black>联系方式:</Font> <Font color=blue>"
									+ "110, 119, 120, 114, 117"
									+"<br></Font>"
									+"<Font color=black>个人说明:</Font> <Font color=blue>"
									+ fi.getNick() + " owns me so much MONEY!! "
									+"<br></Font></BODY></html>",
									"User Information", JOptionPane.INFORMATION_MESSAGE);
						}
					});
					MenuElement els[] = fiendOprMenu.getSubElements();
					for(int i = 0; i < els.length; i++)
						els[i].getComponent().setBackground(Color.WHITE);
					fiendOprMenu.setLightWeightPopupEnabled(true);
					fiendOprMenu.pack();
					// 位置应该是相对于源的位置
					fiendOprMenu.show((Component) me.getSource(), me.getPoint().x, me.getPoint().y);
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
		String[] flistItems = { "Chris", "Joshua", "Daniel", "Michael", "Don",
				"Kimi", "Kelly", "Keagan", "夏", "张三", "张四", "张五", "张三丰" };

		final GroupItem[] gmembers = new GroupItem[flistItems.length];
		// ArrayList<FriendItem> friends = new ArrayList<FriendItem>();

		for (int i = 0; i < flistItems.length; i++) {
			gmembers[i] = new GroupItem(new ImageIcon(
					"resrc\\portrait\\user.png"), flistItems[i], "欢迎加入我们: "
					+ flistItems[i], "uuid:jxta:xxxxxxxxxxxxxxxxxxxxxxx", 0, 0);
		}
		
		glist.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent me) {
				if(me.getClickCount() == 2){
					//TODO 判断所点击的cell的在线状态进行对应处理, 暂时直接弹出弹出聊天窗口.
					String title = ((GroupItem)glist.getSelectedValue()).getNick();
					Chatroom room = new Chatroom(title, gmembers);
					room.pack();
					room.setVisible(true);
				}else if(me.getButton() == MouseEvent.BUTTON3){
					final JPopupMenu groupOprMenu = new JPopupMenu();
					/*
					 * 怎么实现右键可选取JListItem?
					 */
					fi = (GroupItem)glist.getSelectedValue();
					//System.out.println(flist.getComponentAt(me.getPoint()).toString());
					
					if(fi == null)
						return;
					//System.out.println("You just Right Click the List Item!");
					groupOprMenu.add(new AbstractAction("Enter this chatroom") {
						/**
						 * 
						 */
						private static final long serialVersionUID = -729947600305959488L;

						public void actionPerformed(ActionEvent e) {
							String title = ((GroupItem)glist.getSelectedValue()).getNick();
							Chatroom room = new Chatroom(title, gmembers);
							room.pack();
							room.setVisible(true);
						}
					});
					groupOprMenu.add(new AbstractAction("Group information") {
						/**
						 * 
						 */
						private static final long serialVersionUID = -729947600305959488L;

						public void actionPerformed(ActionEvent e) {
							JOptionPane.showMessageDialog((Component) null, 
									"<html>"//<BODY bgColor=#ffffff>"
									+ "<img width=64 height=64 src=\"file:///E:/Java/NoX/resrc/dump/edit_user.png\"><br>"
									+"<Font color=black>组名:</Font> <Font color=blue>"
									+ fi.getNick()
									+"<br></Font>"
									+"<Font color=black>公告:</Font> <Font color=blue>"
									+ fi.getSign()
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
		
		/*blklistpane.setLayout(new BorderLayout());
		blklistpane.add(blkListScrPane, BorderLayout.CENTER);
		blklistpane.add(blist.getFilterField(), BorderLayout.NORTH);*/

		this.setTabPlacement(JTabbedPane.LEFT);
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);// 滚动标签(一行)
		// tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);//多行标签
		this.setBackground(Color.BLACK);
		this.setForeground(Color.WHITE);
		this.addTab(null, new ImageIcon(path_flist), frdlistpane);
		this.addTab(null, new ImageIcon(path_glist), grplistpane);
		//this.addTab(null, new ImageIcon(path_blist), blklistpane);
		this.setOpaque(false);
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
			URL url = new URL("file:\\" + System.getProperty("user.dir")
					+ "\\resrc\\audio\\type.wav");
			playsound = Applet.newAudioClip(url);
			// System.out.println(url);
			playsound.play();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}
}

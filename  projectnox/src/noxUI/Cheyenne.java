package noxUI;

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
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import net.nox.AuthenticationUtil;
import net.nox.GroupConnectionHandler;
import net.nox.NoxToolkit;
import net.nox.PeerGroupUtil;
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
	public Cheyenne(ObjectList flist, ObjectList glist, ObjectList blist, Connection conn) {
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
		
		Thread addGroupListenerThread = new Thread(new Runnable() {
			public void run() {
				System.out.println("Begining initGroupListener()");
				initGroupListener();
			}
		}, "Connector");
		addGroupListenerThread.start();
	}
	/**
	 * 重新加入各组, 然后为组添加管道监听器
	 */
	private void initGroupListener(){
		System.out.println("Begining grouplist.getGroupIDPwds()");
		Map<ID, String> idpwds = grouplist.getGroupIDPwds();
		if(idpwds == null){
			System.out.println("idpwds == null !!");
			return;
		}
		Iterator<Entry<ID, String>> it=idpwds.entrySet().iterator();

		int count = 0;
		//使用entrySet方法将hashMap转化为Set视图,返回的Set中的每个元素都是一个Map.Entry
		while(it.hasNext()){
		    Map.Entry<ID, String> entry=(Map.Entry<ID, String>)it.next();
			PeerGroupID curID = (PeerGroupID)entry.getKey();
			PeerGroupAdvertisement pga = PeerGroupUtil.getLocalAdvByID(NoxToolkit.getNetworkManager().getNetPeerGroup(), curID.toString());
			System.out.println("Begining authenticateThisGroup()");
			System.out.println("Count: " + count + "\n : " + curID.toString() + "\n : " + (String)entry.getValue());
			count++;
			/*boolean auth = authenticateThisGroup(pga, (String)entry.getValue());
			System.out.println("Authenticating result: " + auth);*/
			//TODO 为每个组建立inputPipe并添加监听器
			boolean isListening = addGroupPipeListener(pga);
			System.out.println("addGroupPipeListener result: " + isListening);
		}
	}
	private boolean addGroupPipeListener(PeerGroupAdvertisement adv){
		PeerGroup ppg = NoxToolkit.getNetworkManager().getNetPeerGroup();
		PeerGroup pg = null;
		try {
            pg = ppg.newGroup(adv);
        } catch (PeerGroupException pge) {
        	System.out.println("Creating pg with pga failed, what's wrong?");
        	pge.printStackTrace();
        	return false;
        }
        if (pg != null) {
        	Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
			"Incoming Group Connection Handler");
			System.out.println("Starting groupPipeListener Thread...");
			groupPipeListener.start();
			return true;
        }
        return false;
	}
	/**
	 * 为所有组重新认证, 并添加监听器, 暂时用不到...
	 * @param adv
	 * @param password
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean authenticateThisGroup(PeerGroupAdvertisement adv, String password) {
		PeerGroup ppg = NoxToolkit.getNetworkManager().getNetPeerGroup();
		PeerGroup pg = null;

        try {
            pg = ppg.newGroup(adv);
        } catch (PeerGroupException pge) {
        	pge.printStackTrace();
        	return false;
        }
        // if the group was successfully created join it
        if (pg != null) {
        	if(AuthenticationUtil.isAuthenticated(pg)){
        		//无密码组
        		Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
				"Incoming Group Connection Handler");
				System.out.println("Starting groupPipeListener Thread...");
				groupPipeListener.start();
				return true;
        	}
        	else{
        		boolean joined = PeerGroupUtil.joinPeerGroup(pg, PeerGroupUtil.MEMBERSHIP_ID, password);
        		
        		if(joined){
            		System.out.println("您已成功加入该组. 可在组列表中查看.");
            		//InputPipe inpipe = pg.getPipeService().createInputPipe(adv,);
            		//为组建立管道, 并添加监听器
            		Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
            				"Incoming Group Connection Handler");
            		System.out.println("Starting groupPipeListener Thread...");
            		groupPipeListener.start();
            		return true;
            	}
            	else{
            		System.out.println("未能成功加入该组, 密码错误?");
            		return false;
            	}
        	}
        } else {
            System.out.println("使用组广告创建组失败");
			return false;
        }
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
		System.out.println("In joinThisGroup(): \n" + adv);
		
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
        		if(grouplist.isExist(pg.getPeerGroupID())){
        			//如果列表里已经有该组则:"你已加入该组, 不需要重新加入"
        			System.out.println("你已加入该组, 不需要重新加入. If you're surpried, it may because this group need no password.");
        			JOptionPane.showMessageDialog((Component) null,
        					"你已加入该组, 不需要重新加入. If you're surpried, it may because this group need no password.",
        					"Succeed!",
        					JOptionPane.INFORMATION_MESSAGE);
        		}else{
        			//如果列表中没有, 说明该组不需要密码, 正常提示加入成功.
        			//将该组加入列表中
        			add2GroupList(adv, "");
	        		System.out.println("您已成功加入该组. 可在组列表中查看.");
	        		JOptionPane.showMessageDialog((Component) null,
	    					"您已成功加入该组. 可在组列表中查看.", "Succeed!",
	    					JOptionPane.INFORMATION_MESSAGE);
	        		//添加监听器
	        		Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
					"Incoming Group Connection Handler");
					System.out.println("Starting groupPipeListener Thread...");
					groupPipeListener.start();
        		}
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
        		add2GroupList(adv, password);
        		JOptionPane.showMessageDialog((Component) null,
    					"您已成功加入该组. 可在组列表中查看.", "Succeed!",
    					JOptionPane.INFORMATION_MESSAGE);
        		//添加监听器
        		Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
				"Incoming Group Connection Handler");
				System.out.println("Starting groupPipeListener Thread...");
				groupPipeListener.start();
				
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
	public GroupItem add2GroupList(PeerGroupAdvertisement adv, String password){
		//将该组加入列表中
		GroupItem newGroupItem = new GroupItem(new ImageIcon(
				SystemPath.PORTRAIT_RESOURCE_PATH + "group.png"), adv, password);
		try {
			newGroupItem = (GroupItem) grouplist.addItem(newGroupItem);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		tabs.repaint();
		return newGroupItem;
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
	 * 在不存在对应ID-Pipe对, 更不存在对应聊天室的情况下调用
	 * @param connhandler 用于管理连接的ConnectionHandler
	 * @return 新建立的chatroom, 用于注册到NoxToolkit
	 */
	public GroupChatroom setupNewChatroomOver(GroupItem group){
		//打开聊天室
		GroupChatroom chatroom = new GroupChatroom(group);
		//注册之
		NoxToolkit.registerChatroom(group.getUUID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}
	/**
	 * 大多数情况下(系统初始化时成功建立管道并监听之), 调用此函数建立组聊天室.</p>
	 * 在存在对应ID-Pipe对, 而不存在对应聊天室的情况下:
	 * <li>如果是好友的消息, 则(暂时)建立聊天室显示之.
	 * (应当)提示有新消息</li>
	 * <li>如果不是好友的消息, 则(暂时)将之添加为好友建立聊天室并显示之.
	 * (应当)提示有来自陌生人的新消息</li>
	 * @param connhandler 用于管理连接的ConnectionHandler
	 * @return 新建立的chatroom, 用于注册到NoxToolkit
	 */
	public GroupChatroom setupNewChatroomOver(PeerGroupAdvertisement pga, InputPipe ipipe, OutputPipe opipe){
		//打开聊天室
		GroupChatroom chatroom = new GroupChatroom(pga, ipipe, opipe);
		//注册之
		NoxToolkit.registerChatroom(pga.getPeerGroupID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}
	
	/*public boolean setupGroupChatroom(PeerGroupAdvertisement adv){
		//打开群聊窗口
		//do something
		return true;
	}*/
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
						//FIXME 关闭其它, 如管道, 组, JXTA网络, 等等等等清理工作.
						/*
						 * pipe.close();
						 * 
						 * group.stopApp();
						 * group.unref();

				        // Un-reference the parent peer group.
				        parentgroup.unref();
				        
				        *jxtanetwork.stop();
				        *
				        */
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

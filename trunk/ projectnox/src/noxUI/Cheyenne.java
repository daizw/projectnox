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
	 * Ĭ�ϳߴ糣��
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
	 * ��JPanel
	 */
	private MiniProfilePane profile;
	private ListsPane tabs;
	
	Connection sqlconn;
	/**
	 * �����б�/���б�/������
	 */
	ObjectList friendlist, grouplist, blacklist;

	
	/**
	 * ����/ϵͳ���ô���
	 */
	ConfigCenterFrame ccf = new ConfigCenterFrame(this);
	/**
	 * ��������
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
		 * mini profile ��� ��: ͷ��, �ǳ�, ״̬, ǩ��
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
	 * ���¼������, Ȼ��Ϊ����ӹܵ�������
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
		//ʹ��entrySet������hashMapת��ΪSet��ͼ,���ص�Set�е�ÿ��Ԫ�ض���һ��Map.Entry
		while(it.hasNext()){
		    Map.Entry<ID, String> entry=(Map.Entry<ID, String>)it.next();
			PeerGroupID curID = (PeerGroupID)entry.getKey();
			PeerGroupAdvertisement pga = PeerGroupUtil.getLocalAdvByID(NoxToolkit.getNetworkManager().getNetPeerGroup(), curID.toString());
			System.out.println("Begining authenticateThisGroup()");
			System.out.println("Count: " + count + "\n : " + curID.toString() + "\n : " + (String)entry.getValue());
			count++;
			/*boolean auth = authenticateThisGroup(pga, (String)entry.getValue());
			System.out.println("Authenticating result: " + auth);*/
			//TODO Ϊÿ���齨��inputPipe����Ӽ�����
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
	 * Ϊ������������֤, ����Ӽ�����, ��ʱ�ò���...
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
        		//��������
        		Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
				"Incoming Group Connection Handler");
				System.out.println("Starting groupPipeListener Thread...");
				groupPipeListener.start();
				return true;
        	}
        	else{
        		boolean joined = PeerGroupUtil.joinPeerGroup(pg, PeerGroupUtil.MEMBERSHIP_ID, password);
        		
        		if(joined){
            		System.out.println("���ѳɹ��������. �������б��в鿴.");
            		//InputPipe inpipe = pg.getPipeService().createInputPipe(adv,);
            		//Ϊ�齨���ܵ�, ����Ӽ�����
            		Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
            				"Incoming Group Connection Handler");
            		System.out.println("Starting groupPipeListener Thread...");
            		groupPipeListener.start();
            		return true;
            	}
            	else{
            		System.out.println("δ�ܳɹ��������, �������?");
            		return false;
            	}
        	}
        } else {
            System.out.println("ʹ�����洴����ʧ��");
			return false;
        }
	}
	public Connection getSQLConnection(){
		return sqlconn;
	}
	/**
	 * ������������peer��ӵ������б���
	 * @param adv Ҫ��ӵ�peer�Ĺ�� 
	 * @return ���ѵ��б�Ԫ��
	 */
	public PeerItem add2PeerList(PeerAdvertisement adv, boolean good){
		//TODO ������������peer��ӵ������б���
		
		PeerItem newFriend = new PeerItem(new ImageIcon(
				SystemPath.PORTRAIT_RESOURCE_PATH + "user.png"), adv);
		
		//�����ĸ�ֵĿǰû�б�Ҫ,
		//�������Ѿ����ں���, ��ͷ���б仯��ʱ������;
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
	 * ������������peer��ӵ������б���
	 * TODO �ⲿ�ֿ��Բο�JXTA Prog Guide2.5��Membership Service һ���ж�InteractiveAuthenticator�Ľ���.
	 * 
	 * @param adv Ҫ��ӵ�peer�Ĺ�� 
	 * @return �ɹ�:����false; ����Ѿ����ڸ�����: ����true.
	 */
	public boolean joinThisGroup(PeerGroupAdvertisement adv){
		System.out.println("In joinThisGroup(): \n" + adv);
		
		//TODO ���뵽adv�����������
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
        			//����б����Ѿ��и�����:"���Ѽ������, ����Ҫ���¼���"
        			System.out.println("���Ѽ������, ����Ҫ���¼���. If you're surpried, it may because this group need no password.");
        			JOptionPane.showMessageDialog((Component) null,
        					"���Ѽ������, ����Ҫ���¼���. If you're surpried, it may because this group need no password.",
        					"Succeed!",
        					JOptionPane.INFORMATION_MESSAGE);
        		}else{
        			//����б���û��, ˵�����鲻��Ҫ����, ������ʾ����ɹ�.
        			//����������б���
        			add2GroupList(adv, "");
	        		System.out.println("���ѳɹ��������. �������б��в鿴.");
	        		JOptionPane.showMessageDialog((Component) null,
	    					"���ѳɹ��������. �������б��в鿴.", "Succeed!",
	    					JOptionPane.INFORMATION_MESSAGE);
	        		//��Ӽ�����
	        		Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
					"Incoming Group Connection Handler");
					System.out.println("Starting groupPipeListener Thread...");
					groupPipeListener.start();
        		}
        		return true;
    		}
        	
        	System.out.println("���Լ�����");
        	String password = GetPassword();
        	if(password == null || password.trim().equals("")){
        		//TODO ���Ի�ȡ������Ϣ���������봰��:
        		//1. �û������OK;--�������
        		//2. �û����Cancel;--����
        		//3. �û�ֱ�ӹرմ���.--����
        		System.out.println("The user just canceled the joining process?");
        		return false;
        	}
        	//pg.getMembershipService().
        	boolean joined = PeerGroupUtil.joinPeerGroup(pg, PeerGroupUtil.MEMBERSHIP_ID, password);
        	
        	if(joined){
        		add2GroupList(adv, password);
        		JOptionPane.showMessageDialog((Component) null,
    					"���ѳɹ��������. �������б��в鿴.", "Succeed!",
    					JOptionPane.INFORMATION_MESSAGE);
        		//��Ӽ�����
        		Thread groupPipeListener = new Thread(new GroupConnectionHandler(pg),
				"Incoming Group Connection Handler");
				System.out.println("Starting groupPipeListener Thread...");
				groupPipeListener.start();
				
        		return true;
        	}
        	else{
        		JOptionPane.showMessageDialog((Component) null,
    					"δ�ܳɹ��������, �������?", "Failure!",
    					JOptionPane.ERROR_MESSAGE);
        		return false;
        	}
        } else {
            System.out.println("Error: failed to create new group");
            System.out.println("ʹ�����洴����ʧ��");
			JOptionPane.showMessageDialog((Component) null,
					"ʹ�����洴����ʧ��", "Phew~",
					JOptionPane.ERROR_MESSAGE);
			return false;
        }
	}
	public GroupItem add2GroupList(PeerGroupAdvertisement adv, String password){
		//����������б���
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
	 * �ڴ��ڶ�ӦID-Pipe��, �������ڶ�Ӧ�����ҵ������:
	 * <li>����Ǻ��ѵ���Ϣ, ��(��ʱ)������������ʾ֮.
	 * (Ӧ��)��ʾ������Ϣ</li>
	 * <li>������Ǻ��ѵ���Ϣ, ��(��ʱ)��֮���Ϊ���ѽ��������Ҳ���ʾ֮.
	 * (Ӧ��)��ʾ������İ���˵�����Ϣ</li>
	 * @param connhandler ���ڹ������ӵ�ConnectionHandler
	 * @return �½�����chatroom, ����ע�ᵽNoxToolkit
	 */
	public PeerChatroom setupNewChatroomOver(JxtaBiDiPipe pipe){
		//��ӵ������б�
		//����Ѿ����, ���������ù�.
		PeerItem friend = add2PeerList(pipe.getRemotePeerAdvertisement(), true);
		//��������
		PeerChatroom chatroom = new PeerChatroom(friend, pipe);
		//ע��֮
		NoxToolkit.registerChatroom(friend.getUUID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}
	/**
	 * �ڲ����ڶ�ӦID-Pipe��, �������ڶ�Ӧ�����ҵ�����µ���
	 * @param connhandler ���ڹ������ӵ�ConnectionHandler
	 * @return �½�����chatroom, ����ע�ᵽNoxToolkit
	 */
	public GroupChatroom setupNewChatroomOver(GroupItem group){
		//��������
		GroupChatroom chatroom = new GroupChatroom(group);
		//ע��֮
		NoxToolkit.registerChatroom(group.getUUID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}
	/**
	 * ����������(ϵͳ��ʼ��ʱ�ɹ������ܵ�������֮), ���ô˺���������������.</p>
	 * �ڴ��ڶ�ӦID-Pipe��, �������ڶ�Ӧ�����ҵ������:
	 * <li>����Ǻ��ѵ���Ϣ, ��(��ʱ)������������ʾ֮.
	 * (Ӧ��)��ʾ������Ϣ</li>
	 * <li>������Ǻ��ѵ���Ϣ, ��(��ʱ)��֮���Ϊ���ѽ��������Ҳ���ʾ֮.
	 * (Ӧ��)��ʾ������İ���˵�����Ϣ</li>
	 * @param connhandler ���ڹ������ӵ�ConnectionHandler
	 * @return �½�����chatroom, ����ע�ᵽNoxToolkit
	 */
	public GroupChatroom setupNewChatroomOver(PeerGroupAdvertisement pga, InputPipe ipipe, OutputPipe opipe){
		//��������
		GroupChatroom chatroom = new GroupChatroom(pga, ipipe, opipe);
		//ע��֮
		NoxToolkit.registerChatroom(pga.getPeerGroupID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}
	
	/*public boolean setupGroupChatroom(PeerGroupAdvertisement adv){
		//��Ⱥ�Ĵ���
		//do something
		return true;
	}*/
	public void showSearchingFrame(){
		sfrm.setVisible(true);
	}
	/**
	 * ��������
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
						//FIXME �ر�����, ��ܵ�, ��, JXTA����, �ȵȵȵ�������.
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
            //TODO �����ÿ���Ĳ˵�?
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
	 * ���ô���ǰ����ɫ
	 */
	public void setForegroundColor()
	{
		super.setForegroundColor();
		Color color = super.getForegroundColor();
		profile.setForegroundColor(color);
	}
	/**
	 * ���ô��ڱ�����ɫ
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
	 * ��ʾ����/ϵͳ���ô���
	 */
	public void ShowConfigCenter(){
		ccf.setVisible(true);
	}
	/**
	 * ��ʾ�����鴰��
	 */
	public void ShowCreateNewGroupDialog(){
		CreateNewGroupDialog cngD = new CreateNewGroupDialog(this);
		cngD.setVisible(true);
	}
}

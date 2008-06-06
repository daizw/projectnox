package nox.ui.me;

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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import nox.db.DBTableName;
import nox.net.common.LANTimeLimit;
import nox.net.common.NoxToolkit;
import nox.net.group.AuthenticationUtil;
import nox.net.group.GroupConnectionHandler;
import nox.net.group.PeerGroupUtil;
import nox.net.peer.PeerConnectionHandler;
import nox.ui.common.CreateNewGroupDialog;
import nox.ui.common.GroupItem;
import nox.ui.common.ItemStatus;
import nox.ui.common.NoxFrame;
import nox.ui.common.ObjectList;
import nox.ui.common.PeerItem;
import nox.ui.common.SystemPath;
import nox.ui.common.ObjectList.FilterModel;
import nox.ui.search.SearchingFrame;
import nox.xml.NoxPeerStatusUnit;
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
	
	public static final int InterStatusCheckingsSleepTime = 10 * 1000;
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
	ConfigCenterFrame ccf;
	/**
	 * ��������
	 */
	SearchingFrame sfrm = new SearchingFrame(Cheyenne.this);
	/**
	 * ����ping���к��ѻ�ȡ��״̬��Ϣ
	 */
	Thread peersStatusChecker;
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

		initMyStatus(conn, DBTableName.ME_SQLTABLE_NAME);
		
		tabs = new ListsPane(this, friendlist, grouplist, blacklist);

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		contentPane.add(profile);
		contentPane.add(tabs);
		
		setForegroundColor();
		initTrayIcon();
		
		sfrm.setLocation(100, 60);
		sfrm.setSize(new Dimension(500, 350));
		
		peersStatusChecker = new Thread(new NoOneLivesForeverExceptMe(), "PeersStatusChecker");
		peersStatusChecker.start();
		
		Thread addGroupListenerThread = new Thread(new Runnable() {
			public void run() {
				System.out.println("Begining initGroupListener()");
				initGroupListenerWithoutReauthenticating();
			}
		}, "Connector");
		addGroupListenerThread.start();
	}
	/**
	 * ��ʼ����������
	 * @param conn
	 * @param meSqltableName
	 */
	private void initMyStatus(Connection conn, String meSqltableName) {
		ImageIcon portrait = new ImageIcon(SystemPath.PORTRAIT_RESOURCE_PATH + "portrait.png");
		String nick = NoxToolkit.getNetworkConfigurator().getName();
		String sign = NoxToolkit.getNetworkManager().getNetPeerGroup().getPeerAdvertisement().getDescription();
		try {
			Statement stmt = sqlconn.createStatement();
			ResultSet rs = stmt.executeQuery("select * from " +
					meSqltableName + " where Tag = '" + DBTableName.MYPORTRAIT_TAG_NAME + "'");
			
			while(rs.next()){
				ObjectInputStream objInput = new ObjectInputStream(rs.getBinaryStream("Object"));
				System.out.println("Found my portrait");
				portrait = (ImageIcon)objInput.readObject();
			}
			
			rs = stmt.executeQuery("select * from " +
					meSqltableName + " where Tag = '" + DBTableName.MYNICKNAME_TAG_NAME + "'");
			
			while(rs.next()){
				ObjectInputStream objInput = new ObjectInputStream(rs.getBinaryStream("Object"));
				System.out.println("Found my nickname");
				nick = (String)objInput.readObject();
			}
			
			rs = stmt.executeQuery("select * from " +
					meSqltableName + " where Tag = '" + DBTableName.MYSIGN_TAG_NAME + "'");
			
			while(rs.next()){
				ObjectInputStream objInput = new ObjectInputStream(rs.getBinaryStream("Object"));
				System.out.println("Found my sign string");
				sign = (String)objInput.readObject();
			}
			stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		/**
		 * mini profile ��� ��: ͷ��, �ǳ�, ״̬, ǩ��
		 */
		profile = new MiniProfilePane(this, portrait, nick, sign);
		// profile.setBackground(new Color(0, 255, 0));
		profile.setSize(new Dimension(WIDTH_DEFLT, 50));
		profile.setPreferredSize(new Dimension(WIDTH_PREF, 50));
		profile.setMaximumSize(new Dimension(WIDTH_MAX, 50));
		profile.setMinimumSize(new Dimension(WIDTH_MIN, 50));
	}
	
	public NoxPeerStatusUnit getStatusUnit(){
		return profile.getStatusUnit();
	}
	public NoxPeerStatusUnit getFullStatusUnit(){
		return profile.getFullStatusUnit();
	}
	public void setMyPortrait(Icon icon){
		profile.setPortrait(icon);
		try {
			Statement stmt = sqlconn.createStatement();
			stmt.execute("delete from "+
					DBTableName.ME_SQLTABLE_NAME + " where Tag = '" + DBTableName.MYPORTRAIT_TAG_NAME + "'");
			//��ӵ����ݿ�
			PreparedStatement pstmt = sqlconn.prepareStatement("insert into " +
					DBTableName.ME_SQLTABLE_NAME + " values (?, ?)");
			pstmt.setString(1, DBTableName.MYPORTRAIT_TAG_NAME);
			
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
			out.writeObject((Serializable)icon);
			ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
			pstmt.setBinaryStream(2, input, byteArrayStream.size());
			pstmt.executeUpdate();
			pstmt.close();
			stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setMyNickName(String nick){
		profile.setNickName(nick);
		try {
			Statement stmt = sqlconn.createStatement();
			stmt.execute("delete from "+
					DBTableName.ME_SQLTABLE_NAME + " where Tag = '" + DBTableName.MYNICKNAME_TAG_NAME + "'");
			//��ӵ����ݿ�
			PreparedStatement pstmt = sqlconn.prepareStatement("insert into " +
					DBTableName.ME_SQLTABLE_NAME + " values (?, ?)");
			pstmt.setString(1, DBTableName.MYNICKNAME_TAG_NAME);
			
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
			out.writeObject((Serializable)nick);
			ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
			pstmt.setBinaryStream(2, input, byteArrayStream.size());
			pstmt.executeUpdate();
			pstmt.close();
			stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void savaMySign2DB(String text) {
		try {
			Statement stmt = sqlconn.createStatement();
			stmt.execute("delete from "+
					DBTableName.ME_SQLTABLE_NAME + " where Tag = '" + DBTableName.MYSIGN_TAG_NAME + "'");
			//��ӵ����ݿ�
			PreparedStatement pstmt = sqlconn.prepareStatement("insert into " +
					DBTableName.ME_SQLTABLE_NAME + " values (?, ?)");
			pstmt.setString(1, DBTableName.MYSIGN_TAG_NAME);
			
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteArrayStream);
			out.writeObject((Serializable)text);
			ByteArrayInputStream input = new ByteArrayInputStream(byteArrayStream.toByteArray());
			pstmt.setBinaryStream(2, input, byteArrayStream.size());
			pstmt.executeUpdate();
			pstmt.close();
			stmt.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void setStatus(ID id, NoxPeerStatusUnit stat){
		try {
			friendlist.setStatus(id, stat);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class NoOneLivesForeverExceptMe implements Runnable{
		FilterModel fmod;
		NoOneLivesForeverExceptMe(){
			fmod = (FilterModel) friendlist.getModel();
		}
		@Override
		public void run() {
			while(true){
				PeerItem peer;
				PeerConnectionHandler handler;
				
				for(int index = 0; index < fmod.getRealSize(); index++){					
					peer = (PeerItem) fmod.getRealElementAt(index);
					long curTime = new Date().getTime();
					if(curTime - peer.getTimeStamp() > LANTimeLimit.OFFLINE_TIMELIMIT){
						//�Ѿ�һ����û���յ��Է���״̬��Ϣ��, ��Ϊ����
						//�����ڶԷ������ߺ������������
						peer.setOnlineStatus(ItemStatus.OFFLINE);
					}
					System.out.println("Pinging: " + peer.getName());
					handler = NoxToolkit.getPeerConnectionHandler((PeerID) peer.getUUID());
					if(handler != null){
						handler.sendPingMsg();
					}else{
						//�����ڶ�Ӧ��handler, ��Ҫ����Ȼ��ע��handler
						//!!!ͬ����ʽ!!!!!!!!!!!!!!!!!!
						try {
							handler = new PeerConnectionHandler(peer, false);
							if(handler.isConnected())
								handler.sendPingMsg();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				//һ����ѯ֮����Ϣһ��ʱ��(InterStatusCheckingsSleepTime)
				try {
					Thread.sleep(Cheyenne.InterStatusCheckingsSleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	/**
	 * Ϊ��������ӹܵ�������, ��������������֤.
	 * һ����Ӻ�
	 */
	private void initGroupListenerWithoutReauthenticating(){
		System.out.println("Begining initGroupListenerWithoutReauthenticating()");
		
		FilterModel fmod = (FilterModel) grouplist.getModel();
		
		GroupItem group;
		GroupConnectionHandler handler;
		//�Ƿ�ȫ����ӳɹ�
		boolean finished = false;
		
		while(!finished){
			//��־λ, ����и��鲻���ڼ�����, ����Ϊfalse, �Խ�����һ��ѭ��.
			//�������һ��ѭ��, ���������鶼�м�����, ���ֵ���ᱻ�޸�.
			//�Ӷ�����ѭ��
			finished = true;
			
			for(int index = 0; index < fmod.getRealSize(); index++){					
				group = (GroupItem) fmod.getRealElementAt(index);
				handler = NoxToolkit.getGroupConnectionHandler((PeerGroupID) group.getUUID());
				
				if(handler != null){
					//do nothing
					//handler.sendPingMsg();
				}else{
					//�����ڶ�Ӧ��handler, ��Ҫ����Ȼ��ע��handler
					/*PeerGroupAdvertisement pga	= PeerGroupUtil.getLocalAdvByID(
								NoxToolkit.getNetworkManager().getNetPeerGroup(), group.getUUID().toString());
					addGroupPipeListener(pga, group);*/
					finished = false;
					//!!!ͬ����ʽ!!
					try {
						handler = new GroupConnectionHandler(group, false);
						if(handler.isConnected())
							handler.sendGreetingMessages();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			//һ�����֮����Ϣһ��ʱ��(InterStatusCheckingsSleepTime)
			try {
				Thread.sleep(Cheyenne.InterStatusCheckingsSleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
		}
		
		System.out.println("Ending initGroupListenerWithoutReauthenticating()");
	}
	/**
	 * ���ݹ�潨����, Ȼ���������̳߳�ʼ��GroupConnectionHandler
	 * @param adv
	 * @param item
	 * @return
	 * @deprecated
	 */
	private boolean addGroupPipeListener(PeerGroupAdvertisement adv, GroupItem item){
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
        	Thread groupPipeListener;
			try {
				groupPipeListener = new Thread(new GroupConnectionHandler(pg, item),
					"Incoming Group Connection Handler");
				System.out.println("Starting groupPipeListener Thread...");
				groupPipeListener.start();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
        }
        return false;
	}
	/**
	 * Ϊ��������ӹܵ�������, ������������֤
	 * @deprecated
	 */
	private void initGroupListenerWithReauthenticating(){
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
			//Ϊÿ���齨��inputPipe����Ӽ�����,
			/**
			 * @Fixme �õ�GroupItem������ʼ��������(GroupConnectiongListener)
			 */
			boolean isListening = addGroupPipeListener(pga, null);
			System.out.println("addGroupPipeListener result: " + isListening);
		}
	}
	/**
	 * Ϊ������������֤, ����Ӽ�����, ��ʱ�ò���...
	 * @param adv
	 * @param password
	 * @return
	 * @deprecated
	 */
	private boolean authenticateThisGroup(PeerGroupAdvertisement adv, String password, GroupItem item) {
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
				try {
					Thread groupPipeListener;
					groupPipeListener = new Thread(new GroupConnectionHandler(pg, item),
					"Incoming Group Connection Handler");
					System.out.println("Starting groupPipeListener Thread...");
					groupPipeListener.start();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
        	}
        	else{
        		boolean joined = PeerGroupUtil.joinPeerGroup(pg, PeerGroupUtil.MEMBERSHIP_ID, password);
        		
        		if(joined){
            		System.out.println("���ѳɹ��������. �������б��в鿴.");
            		//InputPipe inpipe = pg.getPipeService().createInputPipe(adv,);
            		//Ϊ�齨���ܵ�, ����Ӽ�����
            		try {
    					Thread groupPipeListener;
    					groupPipeListener = new Thread(new GroupConnectionHandler(pg, item),
    					"Incoming Group Connection Handler");
    					System.out.println("Starting groupPipeListener Thread...");
    					groupPipeListener.start();
    					return true;
    				} catch (Exception e) {
    					e.printStackTrace();
    					return false;
    				}
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
	 * ������������peer��ӵ������б���������
	 * @param adv Ҫ��ӵ�peer�Ĺ��
	 * @param good  ���ѻ��Ǻ�����
	 * @return ���ѵ��б�Ԫ��
	 */
	public PeerItem add2PeerList(PeerAdvertisement adv, boolean good){
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
	 * ������������peer��ӵ������б���, ����Ӽ�����.
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
        	return false;
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
        			GroupItem item = add2GroupList(adv, "");
	        		System.out.println("���ѳɹ��������. �������б��в鿴.");
	        		JOptionPane.showMessageDialog((Component) null,
	    					"���ѳɹ��������. �������б��в鿴.", "Succeed!",
	    					JOptionPane.INFORMATION_MESSAGE);
	        		//��Ӽ�����
					try {
						Thread groupPipeListener;
						groupPipeListener = new Thread(new GroupConnectionHandler(pg, item),
							"Incoming Group Connection Handler");
						System.out.println("Starting groupPipeListener Thread...");
						groupPipeListener.start();
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
        		}
        		return true;
    		}
        	
        	System.out.println("���Լ�����");
        	String password = getJoiningGroupPassword();
        	if(password == null || password.trim().equals("")){
        		//TODO ���Ի�ȡ������Ϣ���������봰��:
        		//1. �û������OK;--�������
        		//2. �û����Cancel;--����
        		//3. �û�ֱ�ӹرմ���.--����
        		System.out.println("Password inputted is empty/null");
        		return false;
        	}
        	//pg.getMembershipService().
        	boolean joined = PeerGroupUtil.joinPeerGroup(pg, PeerGroupUtil.MEMBERSHIP_ID, password);
        	
        	if(joined){
        		GroupItem item = add2GroupList(adv, password);
        		JOptionPane.showMessageDialog((Component) null,
    					"���ѳɹ��������. �������б��в鿴.", "Succeed!",
    					JOptionPane.INFORMATION_MESSAGE);
        		//��Ӽ�����
        		try {
					Thread groupPipeListener;
					groupPipeListener = new Thread(new GroupConnectionHandler(pg, item),
						"Incoming Group Connection Handler");
					System.out.println("Starting groupPipeListener Thread...");
					groupPipeListener.start();
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
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
	/**
	 * �ɵ�����Ϊ������Ӽ�����, ��������ֱ�����.
	 * @param adv
	 * @param password
	 * @return
	 */
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
	private String getJoiningGroupPassword() {
		return (String) JOptionPane.showInputDialog(this, "Please enter the password:",
				"Password Needed", JOptionPane.QUESTION_MESSAGE, null, null, "");
	}
	/**
	 * �ڲ����ڶ�ӦID-Pipe��, �������ڶ�Ӧ�����ҵ�����µ���
	 * @param connhandler ���ڹ������ӵ�ConnectionHandler
	 * @return �½�����chatroom, ����ע�ᵽNoxToolkit
	 */
	/*public GroupChatroom setupNewChatroomOver(GroupItem group){
		//��������
		GroupChatroom chatroom = new GroupChatroom(group);
		//ע��֮
		NoxToolkit.registerChatroom(group.getUUID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}*/
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
	/*public GroupChatroom setupNewChatroomOver(PeerGroupAdvertisement pga, InputPipe ipipe, OutputPipe opipe){
		//��������
		GroupChatroom chatroom = new GroupChatroom(pga, ipipe, opipe);
		//ע��֮
		NoxToolkit.registerChatroom(pga.getPeerGroupID(), chatroom);
		//TODO comment this
		chatroom.setVisible(true);
		
		return chatroom;
	}*/
	
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
                	/**
                	 * ����/ϵͳ���ô���
                	 */
                	ccf = new ConfigCenterFrame(Cheyenne.this);
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
		ccf = new ConfigCenterFrame(Cheyenne.this);
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

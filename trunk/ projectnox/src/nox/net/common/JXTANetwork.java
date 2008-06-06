package nox.net.common;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import javax.security.cert.CertificateException;
import javax.swing.JOptionPane;

import net.jxta.discovery.DiscoveryListener;
import net.jxta.exception.PeerGroupException;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import net.jxta.util.JxtaServerPipe;
import nox.net.common.NoxToolkit.CheckStatusEventHandler;
import nox.net.common.NoxToolkit.HuntingEventHandler;
import nox.net.peer.PeerConnectionHandler;
import nox.ui.login.LoginDialog;
import nox.ui.login.RegisterDialog;
/**
 * This is the JXTANetwork class<p/>
 * This class does the following :
 * <ol>
 * <li>Start the JXTA network.</li>
 * <li>Loading configuration if exist.</li>
 * <li>Listen for connect requests via {@code accept()}.</li>
 * <li>For each connect request spawn a thread which:
 * <ol>
 * <li>Sends {@code greeting} messages to the connection.</li>
 * <li>Waits responses.</li>
 * </ol>
 * </ol>
 * 
 * <li>Peer 广告格式:</li>
 * <ol>
 * <li>name: 用户自定义名称;</li>
 * <li>description: 用户自定义签名( + " - " + 该Peer所使用的PipeID);</li> 
 * </ol>
 * <li>管道广告格式:</li>
 * <ol>
 * <li>name: 发布者(Peer)的PeerID;</li>
 * <li>description: 发布时间(Long);</li>
 * </ol>
 * 
 * 
 * @author shinysky
 *
 */
public class JXTANetwork {

	public static final String Local_Peer_Name = "Local NoX Peer";
	public static final String Local_Network_Manager_Name = "Local NoX Network Manager";

	String locpeername = "";
	char[] locpeerpassword;
	//TODO 这里设为public static, 则NoxToolkit中某些函数可以简化
	static NetworkManager TheNetworkManager;
	static NetworkConfigurator TheConfig;
	static PeerGroup TheNetPeerGroup;
	
	AdvHunter disocveryClient;
	NoxToolkit toolkit;
	HuntingEventHandler hehandler;
	CheckStatusEventHandler cshandler;

	boolean StopDiscovery = false;

	public JXTANetwork() {
		// Creating the Network Manager
		System.out.println("Creating the Network Manager");
		
		while(true){
			LoginDialog login = new LoginDialog();
			String retVal = (String) login.showDialog();
			try {
				System.out.println("return value: " + retVal);
				if(retVal.equals(LoginDialog.REGISTERCMD)){
					//显示注册窗口
					RegisterDialog register = new RegisterDialog();
					while(true){
						String reg = (String) register.showDialog();
						System.out.println("register dialog returned value: " + reg);
						if(reg.equals(RegisterDialog.LOGINCMD)){
							if(new File(new File(".cache"), register.getUsername()).exists()){
								//register.setVisible(true);
								//reg = (String)register.showUsernameAlreadyExistentBalloon();
								continue;
							}else{
								locpeername = register.getUsername();
								locpeerpassword = register.getPassword();
								//TODO 创建新用户.............
								System.out.println("创建新用户..."
										+ locpeername+ ":" + new String(locpeerpassword));
								break;
							}
						} else {
							System.exit(0);
						}
					}
				} else if(retVal.equals(LoginDialog.LOGINCMD)){
					System.out.println(login.getUsername() + ':' + new String(login.getPassword()));
					
					locpeername = login.getUsername();
					locpeerpassword = login.getPassword();
				} else {
					System.exit(0);
				}
				TheNetworkManager = new NetworkManager(
						NetworkManager.ConfigMode.EDGE, Local_Network_Manager_Name,
						new File(new File(".cache"), locpeername).toURI());
				System.out.println("Network Manager created");
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(-1);
			}
			// Persisting it to make sure the Peer ID is not re-created each
			// time the Network Manager is instantiated
			TheNetworkManager.setConfigPersistent(true);

			// Since we won't be setting our own relay or rendezvous seed peers we
			// will use the default (public network) relay and rendezvous seeding.
			TheNetworkManager.setUseDefaultSeeds(true);

			TheNetworkManager.registerShutdownHook();

			// Retrieving the Network Configurator
			System.out.println("Retrieving the Network Configurator");
			try {
				TheConfig = TheNetworkManager.getConfigurator();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Network Configurator retrieved");

			// Does a local peer configuration exist?
			if (TheConfig.exists()) {
				//验证密码!
				System.out.println("Local configuration found, checking the password.......");
				// We load it
				File LocalConfig = new File(TheConfig.getHome(), "PlatformConfig");
				try {
					System.out.println("Loading found configuration");
					TheConfig.load(LocalConfig.toURI());
					System.out.println("Configuration loaded, password:" + TheConfig.getPassword());
					if(TheConfig.getPassword() != null){
						if(!TheConfig.getPassword().equals(new String(locpeerpassword))){
							JOptionPane.showMessageDialog((Component) null,
			    					"Password Incorrect! Please Check your input.", "Failure!",
			    					JOptionPane.ERROR_MESSAGE);
							continue;
						}else{
							break;
						}
					}else{
						String pwd = new String(locpeerpassword);
						if(pwd == null || pwd.equals(""))
							break;
						else {
							JOptionPane.showMessageDialog((Component) null,
			    					"Password Incorrect! Please Check your input.", "Failure!",
			    					JOptionPane.ERROR_MESSAGE);
							continue;
						}
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(-1);
				} catch (CertificateException ex) {
					// An issue with the existing peer certificate has been
					// encountered
					ex.printStackTrace();
					System.exit(-1);
				}
			} else {
				System.out.println("No local configuration found");
										
				TheConfig.setPrincipal(locpeername);
				TheConfig.setPassword(new String(locpeerpassword));
				TheConfig.setName(locpeername);
				TheConfig.setDescription("A NoX Peer");
				
				System.setProperty("net.jxta.tls.principal", locpeername);
	            System.setProperty("net.jxta.tls.password", new String(locpeerpassword));
				
				TheConfig.setTcpStartPort(9701);
				TheConfig.setTcpEndPort(65530);

				System.out.println("Principal: " + TheConfig.getPrincipal());
				System.out.println("Password : " + TheConfig.getPassword());

				try {
					System.out.println("Saving new configuration");
					TheConfig.save();
					System.out.println("New configuration saved successfully");
					System.out.println("saved Principal: " + TheConfig.getPrincipal());
					System.out.println("saved Password : " + TheConfig.getPassword());
					break;
				} catch (IOException ex) {
					ex.printStackTrace();
					System.exit(-1);
				}
			}
		}
		
		System.out.println("system principle: " + System.getProperty("net.jxta.tls.principal"));
        System.out.println("system password: " + System.getProperty("net.jxta.tls.password"));
        
		/**
		 * 初始化NoxToolkit
		 */
		hehandler = new NoxToolkit().new HuntingEventHandler(null);
		cshandler = new NoxToolkit().new CheckStatusEventHandler(null);
		toolkit = new NoxToolkit(this, TheNetworkManager, TheConfig, hehandler,
				cshandler);
	}

	/*private String GetPrincipal() {
		return locpeername = (String) JOptionPane.showInputDialog(null,
				"Enter username", "Username", JOptionPane.QUESTION_MESSAGE,
				null, null, "");
	}

	private String GetPassword() {
		return (String) JOptionPane.showInputDialog(null, "Enter password",
				"Password", JOptionPane.QUESTION_MESSAGE, null, null, "");
	}*/

	public void SeekRendezVousConnection() {
		try {
			System.out.println("Starting JXTA");
			TheNetPeerGroup = TheNetworkManager.startNetwork();
			System.out.println("JXTA Started");

			System.out.println("Peer name	: " + TheNetPeerGroup.getPeerName());
			System.out.println("Description	: "
					+ TheNetPeerGroup.getPeerAdvertisement().getDescription());
			System.out.println("(Group) Peer ID		: "
					+ TheNetPeerGroup.getPeerID().toString());
			System.out.println("(Config) Peer ID		: "
					+ TheConfig.getPeerID().toString());
			System.out.println("(Manager) Peer ID		: "
					+ TheNetworkManager.getPeerID().toString());

			System.out.println("Peer Group name	: "
					+ TheNetPeerGroup.getPeerGroupName());
			System.out.println("Peer Group ID	: "
					+ TheNetPeerGroup.getPeerGroupID().toString());

		} catch (PeerGroupException ex) {
			// Cannot initialize peer group
			ex.printStackTrace();
			System.exit(-1);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}

		long waittime = 2 * 1000;
		System.out.println("Waiting for a rendezvous connection for "
				+ (waittime / 1000) + " seconds " + "(maximum)");
		boolean connected = TheNetworkManager
				.waitForRendezvousConnection(waittime);
		System.out.println(MessageFormat.format("Connected :{0}", connected));
	}

	/**
	 * 搜索广告
	 * @param peerid
	 * @param AdvType
	 * @param attribute
	 * @param value
	 * @param threshold
	 * @param listener
	 * @deprecated
	 */
	public void GoHunting(String peerid, int AdvType, String attribute,
			String value, int threshold, DiscoveryListener listener) {
		disocveryClient = new AdvHunter(TheNetworkManager);
		// new AdvHunter(TheNetworkManager, AdvType);

		long startTime = new Date().getTime();
		long waittime = 5 * 1000L;
		System.out.println("Start Time: " + new Date().getTime());

		StopDiscovery = false;

		while (!StopDiscovery) {
			// startTime = new Date().getTime();
			// look for any peer
			// String peerid, int type, String attribute, String value, int
			// threshold, DiscoveryListener listener, long starttime
			disocveryClient.LookAround(peerid, AdvType, attribute, value,
					threshold, listener, startTime);
			// wait a bit before sending a discovery message
			try {
				System.out.println("Sleeping for :" + waittime);
				Thread.sleep(waittime);
			} catch (Exception e) {
				// ignored
			}
		}
	}

	/**
	 * 停止搜索广告
	 * @deprecated
	 */
	public void StopHunting() {
		System.out.println("Stop Hunting...");
		StopDiscovery = true;
	}

	public void StopNetwork() {
		System.out.println("Stopping JXTA");
		TheNetworkManager.stopNetwork();
	}

	public void Start() {
		SeekRendezVousConnection();
		/**
		 * 添加外来bidipipe连接监听器
		 */
		Thread inconn = new Thread(new Runnable() {
			@Override
			public void run() {
				addIncomingConnectionListener();
			}
		}, "Incoming Connection Listener");
		inconn.start();
	}

	private void addIncomingConnectionListener() {
		//PipeAdvertisement serverPipeAdv = getPipeAdvertisement();
		PipeAdvertisement serverPipeAdv = PipeUtil.getPipeAdvWithoutRemoteDiscovery(
				TheNetworkManager.getNetPeerGroup(),
				TheConfig.getPeerID().toString(),
				PipeService.UnicastType,
				//(BIDI_PIPEID == null)?null:BIDI_PIPEID.toString(),
				true);
		
		//如果在上一句中创建了新广告, 则下面属于重新发布...
		try {
			//得到自己需要用到的pipeAdv后, 从本地缓存中清除所有DiscoveryService.ADV类型的Adv,
			//然后再本地发布和远程发布正在使用的pipeAdv.
			//这样可以清除自己/别人过期(不再使用)的pipeAdv.
			//但是有可能造成网络整体性能的降低, 因为增加了远程发现的负担.
			System.out.println("Flushing advs...");
			/*//似乎第一个参数如果为null, 则没有效果. 怎么flush?
			//只能手动?
			TheNetworkManager.getNetPeerGroup().getDiscoveryService()
				.flushAdvertisements(null, DiscoveryService.ADV);*/
			PipeUtil.flushOldPipeAdvs(TheNetworkManager.getNetPeerGroup(), TheConfig.getPeerID().toString());
			System.out.println("Publishing pipe adv...");
			TheNetworkManager.getNetPeerGroup().getDiscoveryService().publish(serverPipeAdv);
			TheNetworkManager.getNetPeerGroup().getDiscoveryService().remotePublish(serverPipeAdv);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JxtaServerPipe serverPipe;
		try {
			serverPipe = new JxtaServerPipe(
					TheNetworkManager.getNetPeerGroup(), serverPipeAdv);

			serverPipe.setPipeTimeout(0);
			System.out
					.println("Waiting for JxtaBidiPipe connections on JxtaServerPipe :\n\t"
							+ serverPipeAdv.getPipeID());
			while (true) {
				JxtaBiDiPipe outbidipipe = serverPipe.accept();
				if (outbidipipe != null) {
					System.out.println("JxtaBidiPipe accepted from: "
							+ outbidipipe.getRemotePeerAdvertisement().getName());
					PeerID pid = outbidipipe.getRemotePeerAdvertisement().getPeerID();
					PeerConnectionHandler handler = NoxToolkit.getPeerConnectionHandler(pid);
					if(handler == null){
						Thread thread = null;
						try {
							thread = new Thread(	new PeerConnectionHandler(outbidipipe),
									"Incoming Connection Handler");
							thread.start();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else{
						//更新bidipipe
						handler.setBiDiPipe(outbidipipe);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
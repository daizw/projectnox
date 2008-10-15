package nox.net.peer;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.WireFormatMessage;
import net.jxta.endpoint.WireFormatMessageFactory;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;
import net.jxta.util.JxtaBiDiPipe;
import nox.net.common.ConnectionHandler;
import nox.net.common.LANTimeLimit;
import nox.net.common.NoxToolkit;
import nox.net.common.PipeUtil;
import nox.ui.chat.common.SlideInNotification;
import nox.ui.chat.peer.PeerChatroom;
import nox.ui.common.PeerItem;
import nox.ui.common.SystemPath;
import nox.xml.NoxFileUnit;
import nox.xml.NoxMsgUtil;
import nox.xml.NoxPeerStatusUnit;
import nox.xml.XmlMsgFormat;

/**
 * This is the server (receiver) side of the Bi-directional Pipe<p/>
 * This class does the following :
 * <ol>
 * <li>Open the received outpipe.</li>
 * <li>Sends {@code greeting} messages to the connection.</li>
 * <li>Waits responses.</li>
 * <li>For each incoming message does the following:
 * <ol>
 * <li>Identify if the caller is a friend, if so, setup a chatroom, and show the message.</li>
 * <li>If no, just ignore it, or add him/her to the friend list, depends on the configuration of the user.</li>
 * </ol>
 * </li>
 * </ol>
 * 
 */
public class PeerConnectionHandler implements ConnectionHandler, Runnable, PipeMsgListener {
	/**
	 * 实际上是双向管道, 即既可以收也可以发.
	 * 这里的命名只是为了强调发.
	 */
	private JxtaBiDiPipe outbidipipe;
	/**
	 * 对应的Chatroom
	 */
	private PeerChatroom room = null;
	/*private String roomname = "";
	private PeerID roomID;*/
	private PeerItem friend;
	
	private SecretKey DESKey = null;
	private boolean exchangingPubKeys = false;
	private boolean waiting4PubKey2 = false;
	private PublicKey bobPubKey = null;
	
	/**
	 * 构造函数, 根据第二个参数决定是否实例化聊天窗口<br>
	 * <ol>
	 * <li>如果立刻实例化窗口, 则连接过程在单独一个线程中运行(异步)</li>
	 * <li>如果不立刻实例化窗口, 则连接过程阻塞运行(同步)</li>
	 * </ol>
	 * @param friend 列表中对方的PeerItem
	 * @param showChatroom 是否立刻实例化窗口
	 * @throws Exception 第一个参数为空异常
	 */
	public PeerConnectionHandler(final PeerItem friend, boolean showChatroom) throws Exception{
		if(friend == null){
			throw new Exception("PeerConnectionHandler 初始化PeerItem参数为空.");
		}
		this.friend = friend;
		
		if(showChatroom){
			//显示聊天室窗口, 连接.
			//成功: 注册并监听
			//超时(不成功):提示用户连接超时, 是否重试!
			room = new PeerChatroom(friend, PeerConnectionHandler.this);
			room.setVisible(true);
			Thread connector = new Thread(new Runnable(){
				@Override
				public void run() {
					boolean retry = true;
					while(retry){
						TryToConnect(LANTimeLimit.UNIT_TIME * LANTimeLimit.CONNECT_MAXRETRIES);
						if(outbidipipe == null){
							retry = room.showTimeOutMsg();
						} else {
							System.out.println("连接成功!!");
							//TryToConnect()已自动注册ID-connectionHandler并监听
							room.setVisible(false);
							room.removeMask();
							room.setVisible(true);
							break;
						}
					}
				}
			});
			connector.start();
		} else {
			//不显示聊天室窗口, 只连接.
			//成功: 注册并监听
			TryToConnect(LANTimeLimit.UNIT_TIME * LANTimeLimit.CONNECT_MAXRETRIES);
		}
	}
	/**
	 * 构造函数, 当用户双击主界面好友列表时调用此构造函数.<br>
	 * 直接显示聊天窗口, 并试图连接<br>
	 * @param friend 列表中对方的PeerItem
	 * @throws Exception 参数为空异常
	 */
	public PeerConnectionHandler(final PeerItem friend) throws Exception {
		this(friend, true);
	}
	
	/**
	 * <ol>
	 * <li>Constructor for the ConnectionHandler object.
	 * Do these things:</li>
	 * <ol>
	 * <li>register the outbidipipe;</li>
	 * <li>set the message listener.</li>
	 * </ol>
	 * <li>该构造函数由系统检测到外来连接时调用.注册并监听该pipe, 但并不实例化chatroom</li>
	 * <li>在此之后, 如果监听到有消息到达, 则寻找/建立对应的chatroom, 并将消息传递给chatroom</li>
	 * </ol>
	 * @param pipe
	 *            message pipe
	 */
	public PeerConnectionHandler(JxtaBiDiPipe pipe)  throws Exception{
		if(pipe == null){
			throw new Exception("PeerConnectionHandler 初始化JxtaBiDiPipe参数为空.");
		}
		this.outbidipipe = pipe;
		outbidipipe.setMessageListener(this);
		
		friend = new PeerItem(new ImageIcon(
				SystemPath.PORTRAIT_RESOURCE_PATH + "user.png"), pipe.getRemotePeerAdvertisement());
		
		if(NoxToolkit.forceRegisterPeerConnectionHandler((PeerID) friend.getUUID(), this))
			return;
		else {
			System.out.println("使用该bidipipe未获取到peerID");
			throw new Exception("PeerConnectionHandler 初始化使用参数JxtaBiDiPipe未获取到对方PeerID");
		}
	}
	public boolean isConnected(){
		return outbidipipe != null;
	}
	/**
	 * 与该peer建立连接:
	 * 成功: 注册并监听;
	 * 不成功: 不注册
	 * @Fixme 获取广告方式, 最好改为在一定时间段内获取本地和远程的广告, 然后取出最新的.
	 */
	private void TryToConnect(long waittime) {
		// 如果已经有了outbidipipe, 则不需要重新连接
		if (outbidipipe != null) {
			System.out
					.println("["
							+ Thread.currentThread().getName()
							+ "] We have gotten a outbidipipe, cancelling TryToConnect().");
			return;
		}
		System.out.println("+++++++++++Begin TryToConnect()+++++++++++");
		// get the pipe service, and discovery
		PeerGroup group = NoxToolkit.getNetworkManager()
				.getNetPeerGroup();

		//localDiscoveryListener pipeListener = new localDiscoveryListener();
		System.out
				.println("[" + Thread.currentThread().getName()
						+ "] Fetching remote pipe adv to peer/group:"
						+ friend.getUUID());
		// 如果发现对应的pipe会修改newOutBidipipeAdv
		
		//查找管道广告时间, 固定: 2s : 应根据网络状况调整
		//尽量使用已有广告, 当需要时才重建管道.
		//获取"最新"广告所用时间(固定)
		int fetchRemotePipeAdvTimeCount = LANTimeLimit.FETCH_PIPEADV_MAXRETRIES/2;
		PipeAdvertisement newOutPipeAdv = null;
		/**
		 * @Fixme && outbidipipe == null ?
		 */
		while (fetchRemotePipeAdvTimeCount-- > 0 && outbidipipe == null) {
			newOutPipeAdv = PipeUtil.findNewestPipeAdv(group, friend.getUUID().toString(), LANTimeLimit.UNIT_TIME*2);
		}		
		// 1: 得到了pipe adv;
		// 2: timecount <= 0;
		// 3: outbidipipe != null: 搜索过程中对方连接过来,
		// 于是ConnectionHandler修改了outbidipipe.
		if (outbidipipe == null) {
			// 不是第三种情况
			// 有两种可能:
			// 1: 得到了pipe adv;
			// 2: 超时
			if (newOutPipeAdv == null) {
				// 2: 超时, 返回
				System.out
						.println("Failed to fetch a pipe adv: newOutBidipipeAdv == null.");
				return;
			}
			
			// 1: 得到pipe adv
			long timecount = waittime / LANTimeLimit.UNIT_TIME;
			System.out.println("I have got the latest pipe adv.");
			System.out.println("[" + Thread.currentThread().getName()
					+ "] Attempting to establish a connection on pipe : "
					+ newOutPipeAdv.getPipeID());
			System.out.println("timeout: "
					+ (int) ((timecount + 1) * LANTimeLimit.UNIT_TIME));
			
			while (outbidipipe == null && timecount > 0) {
				// Try again and again
				try {
					// 用四个单位的时间作为超时时间//timecount -= 4;
					timecount -= 2;
					outbidipipe = new JxtaBiDiPipe(group, newOutPipeAdv,
							(int) LANTimeLimit.UNIT_TIME * 4, this, true);
					if (outbidipipe == null) {
						// 这个if似乎没必要
						throw new IOException(
								" tryToConnect() failed: outbidipipe == null.");
					}
				} catch (IOException exc) {
					System.out.println("timecount: " + timecount
							+ " -->failed: IOException occured.");
					exc.printStackTrace();
				}
			}
			if(outbidipipe == null){
				System.out.println("Failed finally, I'm so tired of having tried so many times.");
				return;
			}

			System.out.println("[" + Thread.currentThread().getName()
					+ "] JxtaBiDiPipe pipe created");
			System.out.println("[" + Thread.currentThread().getName()
					+ "] Pipe name	: " + newOutPipeAdv.getName());
		}
		// 3: outbidipipe != null: 搜索过程中对方连接过来,
		// 于是ConnectionHandler修改了outbidipipe.
		// 或者经过以上过程得到了新的pipe
		//注册
		outbidipipe.setMessageListener(this);
		NoxToolkit.forceRegisterPeerConnectionHandler((PeerID) friend.getUUID(), PeerConnectionHandler.this);
		//say hello
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Saying hello ...");
		String hellomsg = "Hello [F:100] from "
				+ NoxToolkit.getNetworkConfigurator().getName();
		//以防万一...防止outbidipipe为空时导致递归调用trytoconnect();
		if(outbidipipe != null){
			/**
			 * 交换状态信息
			 */
			sendFullPingMsg();
			/**
			 * @Fixme 第二个参数...........
			 */
			SendMsg(hellomsg, true);
		}
		
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Room ID	: " + friend.getUUID());
		System.out.println("+++++++++++End of TryToConnect()+++++++++++");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(String strmsg, boolean encrypt) {
		System.out.println("Sending message:\n" + strmsg);
		boolean sent = SendMsg(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, strmsg.getBytes(), encrypt);
		if(sent)
			System.out.println("message sent");
		else
			System.out.println("failed to send message");
		
		return sent;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(BufferedImage bufImg, boolean encrypt) {
		/**
		 * 将图片或者ImageIcon转为byte[]
		 *  TODO imageio.write()的第二个参数.............
		 */
		ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
		try {
			javax.imageio.ImageIO.write(bufImg, "PNG", byteArrayOS);
		} catch (IOException e) {
			System.out.println("ImageIO.write(bufImg, \"PNG\", byteArrayOS)出错!");
			e.printStackTrace();
			return false;
		}finally{
			try {
				byteArrayOS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] picture = byteArrayOS.toByteArray();
		
		System.out.println("Sending a picture...");
		boolean sent = SendMsg(XmlMsgFormat.PICTUREMSG_NAMESPACE_NAME, picture, encrypt);
		if(sent)
			System.out.println("Picture sent");
		else
			System.out.println("Failed to send picture");
		
		return sent;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(File file, boolean encrypt) {
		/**
		 * 将文件转为byte[]
		 */
		byte[] fileBytes = null;
		byte[] fileData = null;
		NoxFileUnit noxFile;
		try {
			fileData = NoxMsgUtil.getBytesFromFile(file);
			noxFile = new NoxFileUnit(file.getName(), fileData);
			fileBytes = NoxMsgUtil.getBytesFromObject(noxFile);
			System.out.println("Sending a file...");
			boolean sent = SendMsg(XmlMsgFormat.FILEMSG_NAMESPACE_NAME, fileBytes, encrypt);
			if(sent)
				System.out.println("File sent");
			else
				System.out.println("Failed to send file");
			
			return sent;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 发送ping消息
	 */
	public void sendPingMsg() {
		NoxPeerStatusUnit status = NoxToolkit.getCheyenne().getStatusUnit();
		byte[] statBytes = null;
		try {
			statBytes = NoxMsgUtil.getBytesFromObject(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SendMsg(XmlMsgFormat.PINGMSG_NAMESPACE_NAME, statBytes, false);
	}
	/**
	 * 发送pong消息
	 */
	private void sendPongMsg() {
		NoxPeerStatusUnit status = NoxToolkit.getCheyenne().getStatusUnit();
		byte[] statBytes = null;
		try {
			statBytes = NoxMsgUtil.getBytesFromObject(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SendMsg(XmlMsgFormat.PONGMSG_NAMESPACE_NAME, statBytes, false);
	}
	/**
	 * 发送完整的pong消息(含头像)
	 */
	private void sendFullPingMsg() {
		NoxPeerStatusUnit status = NoxToolkit.getCheyenne().getFullStatusUnit();
		byte[] statBytes = null;
		try {
			statBytes = NoxMsgUtil.getBytesFromObject(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SendMsg(XmlMsgFormat.PINGMSG_NAMESPACE_NAME, statBytes, false);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(String namespace, byte[] data, boolean encrypt) {
		int retrial = 0;
		while (outbidipipe == null && retrial < LANTimeLimit.CONNECT_MAXRETRIES) {
			System.out
					.println("outBiDiPipe is null now, trying to connect : " + retrial);
			TryToConnect(LANTimeLimit.UNIT_TIME);
			retrial ++;
		}
		if(outbidipipe == null){
			System.out
			.println("outBiDiPipe is still null even after trying so many times, canceling sending msg...");
			return false;
		}

		Message msg = null;
		
		if(encrypt){
			//TODO 生成DES密钥, 用其加密数据(CBC模式), 产生相应带参数的消息
			if(DESKey == null){
				Security.addProvider(new com.sun.crypto.provider.SunJCE());
				//标明当前状态
				exchangingPubKeys = true;
				try {
					/**
					 * alice生成DH对, 然后将公钥编码后发给bob
					 */
					System.out.println("ALICE: 产生 DH 对 ...");
					KeyPairGenerator aliceKpairGen;
					aliceKpairGen = KeyPairGenerator.getInstance("DH");
					
					aliceKpairGen.initialize(512);
					KeyPair aliceKpair = aliceKpairGen.generateKeyPair(); // 生成时间长
					// 李四(Alice)生成公共密钥 alicePubKeyEnc 并发送给张三(Bob) ,
					// 比如用文件方式,socket.....
					byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
					
					// Alice creates and initializes her DH KeyAgreement object
			        System.out.println("ALICE: Initialization ...");
			        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
			        aliceKeyAgree.init(aliceKpair.getPrivate());
			        
					msg = NoxMsgUtil.generateMsg(XmlMsgFormat.PUBLICKEYENC_NAMESPACE_NAME,
							NoxToolkit.getNetworkConfigurator().getName(),
							NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
							friend.getName(), friend.getUUID().toString(),
							alicePubKeyEnc);
					outbidipipe.sendMessage(msg);
					waiting4PubKey2 = true;
					int i = 0;
					//等待对方(bob)返回public key
					while(waiting4PubKey2 && i++ < LANTimeLimit.CONNECT_MAXRETRIES*2){
						try {
							Thread.sleep(LANTimeLimit.UNIT_TIME);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							waiting4PubKey2 = false;
							exchangingPubKeys = false;
						}
					}
					if(waiting4PubKey2){
						System.out.println("交换公钥超时, 发送加密消息失败, 请重试.");
						waiting4PubKey2 = false;
						exchangingPubKeys = false;
						return false;
					}
					if(bobPubKey != null){
						waiting4PubKey2 = false;
						exchangingPubKeys = false;
						//KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
				        //x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
				        //PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
				        System.out.println("ALICE: Execute PHASE1 ...");
				        aliceKeyAgree.doPhase(bobPubKey, true);
				        DESKey = aliceKeyAgree.generateSecret("DES");
				        /**
				         * @Fixme TEMP code to save DES key as a file.
				         */
				        /*java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
								new java.io.FileOutputStream("DES.key"));
						out.writeObject(DESKey);
						out.close();*/
						
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(DESKey != null){
				Cipher aliceCipher;
				try {			        
			        aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			        aliceCipher.init(Cipher.ENCRYPT_MODE, DESKey);
			        //bobCipher.init(Cipher.ENCRYPT_MODE, bobDesKey, sr);

			        byte[] cipherData = aliceCipher.doFinal(data);
			        // Retrieve the parameter that was used, and transfer it to Alice in
			        // encoded format
			        byte[] encodedParams = aliceCipher.getParameters().getEncoded();
			        
			        msg = NoxMsgUtil.generateMsg(namespace,
							NoxToolkit.getNetworkConfigurator().getName(),
							NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
							friend.getName(), friend.getUUID().toString(),
							cipherData,
							encodedParams);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				} catch (BadPaddingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("产生DES密钥失败!");
				waiting4PubKey2 = false;
				exchangingPubKeys = false;
				return false;
			}
		} else {
			//不加密
			msg = NoxMsgUtil.generateMsg(namespace,
					NoxToolkit.getNetworkConfigurator().getName(),
					NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
					friend.getName(), friend.getUUID().toString(),
					data);
		}
		
		try {
			outbidipipe.sendMessage(msg);
		} catch (IOException e) {
			System.out.println("failed to send message");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void refreshStatus(NoxPeerStatusUnit stat) {
		NoxToolkit.getCheyenne().setStatus(friend.getUUID(), stat);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void pipeMsgEvent(PipeMsgEvent event) {
		System.out.println("===Begin ConnectionHandler PipeMsgEvent()===");
		// 处理消息
		// grab the message from the event
		Message msg = event.getMessage();
		System.out.println("Incoming call: " + msg.toString());
		System.out.println("Trying to setup a chatroom...");
		
		//处理收到的消息
		processIncomingMsg(msg, false);
		
		System.out.println("===End ConnectionHandler PipeMsgEvent()===");
	}

	/**
	 * 消息处理函数, 提示收到消息
	 * <ol>
	 * <li>如果聊天室可见, 直接显示</li>
	 * <li>TODO 如果不可见, 则提示用户有新消息到达, 由用户决定是否显示</li>
	 * <ol>
	 * <li>好友: 提示好友消息</li>
	 * <li>陌生人, 提示陌生人消息.</li>
	 * </ol>
	 * </ol>
	 * @param sender 发送者昵称
	 * @param time 时间戳
	 * @param msgdata 收到的消息
	 */
	private void promptIncomingMsg(final String sender, final String time, final Object msgdata) {
		/**
		 * 将消息输出到聊天窗口
		 */
		System.out.println("Have put the message to the Chatroom window...");
		System.out.println("Did you see the message?");
		//如果窗口不存在或者不可见
		if(room == null || !room.isVisible()){
			//TODO 应该是提示有消息, 而不是强行显示窗口
			/*this.setVisible(true);*/
			System.out.println("The window is null or not visible , I make it be!");
			Icon infoIcon = UIManager.getIcon ("OptionPane.informationIcon");
	        //JLabel label = new JLabel ("New message from " + this.getRoomName(), infoIcon, SwingConstants.LEFT);
			JPanel notif = new JPanel();
			JLabel label = new JLabel ("<html>New message from:  <br><Font color=red><center>" 
					+ this.getRoomName() 
					+ "</center></Font>",
					infoIcon, SwingConstants.LEFT);
			label.setBackground(Color.WHITE);
			label.setForeground(Color.BLACK);
			JButton showMeIt = new JButton("Show Me");
			showMeIt.setMargin(new Insets(0,0,0,0));
			
			JButton ignoreIt = new JButton("Check it later");
			ignoreIt.setMargin(new Insets(0,0,0,0));
			
			notif.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
	        notif.setLayout(new BoxLayout(notif, BoxLayout.X_AXIS));
			notif.add(label);
			notif.add(showMeIt);
			notif.add(ignoreIt);
			
	        final SlideInNotification slider = new SlideInNotification (notif);
	        slider.showAt (450);
	        
	        Thread playThd = new Thread(new Runnable() {
				@Override
				public void run() {
					playAudio();
				}
				/**
				 * 接收消息时播放提示音
				 */
				private void playAudio() {
					
					final AudioClip msgBeep;
					try {
						URL url = new URL("file:/" + System.getProperty("user.dir")
								+ System.getProperty("file.separator")
								+ SystemPath.AUDIO_RESOURCE_PATH
								+ "upwpcm.wav");
						msgBeep = Applet.newAudioClip(url);
						msgBeep.play();
					} catch (MalformedURLException e) {
						e.printStackTrace();
						System.out.println(e.toString());
					}
				}
			}, "Beeper");
			playThd.start();
			
			showMeIt.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0){
					/*if(room == null){
						// 新建chatroom, 显示消息
						room = new PeerChatroom(friend, PeerConnectionHandler.this);
						room.removeMask();
					}
					
					room.setVisible(true);*/
					showChatroom();
					room.incomingMsgProcessor(sender, time, msgdata);
					
					slider.Dispose();
				}
			});
			ignoreIt.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0){
					slider.Dispose();
				}
			});
		} else {
			room.incomingMsgProcessor(sender, time, msgdata);
		}
	}
	/**
	 * Process the incoming message
	 * <ol>
	 * <li>黑名单, 忽略之</li>
	 * <li>否则, 进行处理</li>
	 * <ol>
	 * <li>如果是系统ping/pong消息, 则进行相应处理</li>
	 * <li>如果是用户消息, 则交给promptIncomingMsg根据聊天室窗口状态进行相应处理</li>
	 * </ol>
	 * </ol>
	 * @param msg
	 *            the message
	 * @param verbose
	 *            dumps message element content if true
	 */
	public void processIncomingMsg(Message msg, boolean verbose) {
		String incomingMsg = "";
		
		try {
			CountingOutputStream cnt;
			ElementIterator it = msg.getMessageElements();
			System.out
					.println("------------------Begin Message---------------------");
			WireFormatMessage serialed = WireFormatMessageFactory.toWire(msg,
					new MimeMediaType("application/x-jxta-msg"), null);
			System.out.println("Message Size :" + serialed.getByteLength());
			while (it.hasNext()) {
				MessageElement el = it.next();
				String eName = el.getElementName();
				cnt = new CountingOutputStream(new DevNullOutputStream());
				el.sendToStream(cnt);
				long size = cnt.getBytesWritten();
				System.out.println("Element " + eName + " : " + size);
				if (verbose) {
					System.out.println("[" + el + "]");
				}
			}
			System.out
					.println("---------[F:100]----------End Message----------[F:035]------------");

			Iterator<String> namespaces = msg.getMessageNamespaces();
			
			while(namespaces.hasNext()){
				String curNamespace = namespaces.next();
				System.out.println("namespace: " + curNamespace);
				MessageElement senderEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.SENDER_ELEMENT_NAME);
				MessageElement senderIDEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.SENDERID_ELEMENT_NAME);
				MessageElement receiverEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.RECEIVER_ELEMENT_NAME);
				MessageElement receiverIDEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.RECEIVERID_ELEMENT_NAME);
				MessageElement timeEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.TIME_ELEMENT_NAME);
				ByteArrayMessageElement dataEle = (ByteArrayMessageElement) msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.DATA_ELEMENT_NAME);
				ByteArrayMessageElement paramEle = (ByteArrayMessageElement) msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.PARAMENC_ELEMENT_NAME);

				System.out.println("Detecting if the msg elements is null");

				if (null == senderIDEle || receiverIDEle == null) {
					System.out.println("Some key(ID) msg element is empty, it's weird.");
					continue;
				}
				System.out.println("Incoming call: From: " + senderEle.toString());
				System.out.println("Incoming call: FromID: " + senderIDEle.toString());
				System.out.println("Incoming call: To: " + receiverEle.toString());
				System.out.println("Incoming call: ToID: " + receiverIDEle.toString());
				System.out.println("Incoming call: At: " + timeEle.toString());
				/**
				 * @Fixme 传送大量数据的时候如果打印出来会降低速度
				 */
				System.out.println("Incoming call: Msg(Length): " + dataEle.getByteLength());
				
				//验证消息中收发者ID是否"正常"
				String whoami = NoxToolkit.getNetworkConfigurator().getPeerID().toString();
				String receiverID = receiverIDEle.toString();
				if(!whoami.equals(receiverID)){
					System.out.println("Receiver is not me but I still get it, that's funny.");
					continue;
				}
				String senderID = senderIDEle.toString();
				if(!friend.getUUID().toString().equals(senderID)){
					System.out.println("Sender is not who I'm talking to but I still get it, that's funny.");
					continue;
				}
				
				/**
				 * TODO 判断是否在黑名单中, 是则忽略之
				 */
				
				if(dataEle == null){
					System.out.println("data element is empty, what's wrong?");
					continue;
				}
				
				incomingMsg += dataEle.toString();
				
				if(curNamespace.equals(XmlMsgFormat.MESSAGE_NAMESPACE_NAME)){
					//TODO 处理string消息
					String strmsg = null;
					
					if(paramEle != null){
						//TODO 解密
						if(DESKey == null){
							// 从文件导入密钥DES
							// TODO 应该显示提示窗口, 而不是直接打开对话框...
							// 不过这段代码执行的机会不多(极少)...
							importDESKey();
							if(DESKey == null){
								System.out.println("You have no DES key to decrypt this message, " +
								"please contact with the msg sender, cancelling...");
								return;
							}
						}
						System.out.println("decrypting....");
						/*
				         * Alice decrypts, using DES in CBC mode
				         */
				        // Instantiate AlgorithmParameters object from parameter encoding
				        // obtained from Bob
				        AlgorithmParameters params = AlgorithmParameters.getInstance("DES");
				        params.init(paramEle.getBytes());
				        Cipher aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
				        aliceCipher.init(Cipher.DECRYPT_MODE, DESKey, params);
				        byte[] recoveredData = aliceCipher.doFinal(dataEle.getBytes());
				        strmsg = new String(recoveredData);
					} else {
						//无需解密
						strmsg = new String(dataEle.getBytes());
					}
					promptIncomingMsg(friend.getName(), timeEle.toString(), strmsg);
				} else if(curNamespace.equals(XmlMsgFormat.PICTUREMSG_NAMESPACE_NAME)){
					//TODO 处理图片消息
					ImageIcon incomingPic = null;
					byte[] picBytes = null;
					if(paramEle != null){
						//TODO 解密
						if(DESKey == null){
							// 从文件导入密钥DES
							importDESKey();
							if(DESKey == null){
								System.out.println("You have no DES key to decrypt this message, " +
								"please contact with the msg sender, cancelling...");
								return;
							}
						}
						System.out.println("decrypting....");
						/*
				         * Alice decrypts, using DES in CBC mode
				         */
				        // Instantiate AlgorithmParameters object from parameter encoding
				        // obtained from Bob
				        AlgorithmParameters params = AlgorithmParameters.getInstance("DES");
				        params.init(paramEle.getBytes());
				        Cipher aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
				        aliceCipher.init(Cipher.DECRYPT_MODE, DESKey, params);
				        picBytes = aliceCipher.doFinal(dataEle.getBytes());
					} else {
						//无需解密
						picBytes = dataEle.getBytes();
					}
					// 将byte[]转化为图片形式
					try {
						ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(picBytes);
						BufferedImage bufImg = javax.imageio.ImageIO.read(byteArrayIS);
						incomingPic = new ImageIcon(bufImg);
					} catch (IOException e) {
						e.printStackTrace();
					}
					promptIncomingMsg(friend.getName(), timeEle.toString(), incomingPic);
				} else if(curNamespace.equals(XmlMsgFormat.FILEMSG_NAMESPACE_NAME)){
					//TODO 处理文件消息
					NoxFileUnit incomingFile = null;
					byte[] fileBytes = null;
					if(paramEle != null){
						//TODO 解密
						if(DESKey == null){
							// 从文件导入密钥DES
							importDESKey();
							if(DESKey == null){
								System.out.println("You have no DES key to decrypt this message, " +
								"please contact with the msg sender, cancelling...");
								return;
							}
						}
						System.out.println("decrypting....");
						/*
				         * Alice decrypts, using DES in CBC mode
				         */
				        // Instantiate AlgorithmParameters object from parameter encoding
				        // obtained from Bob
				        AlgorithmParameters params = AlgorithmParameters.getInstance("DES");
				        params.init(paramEle.getBytes());
				        Cipher aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
				        aliceCipher.init(Cipher.DECRYPT_MODE, DESKey, params);
				        fileBytes = aliceCipher.doFinal(dataEle.getBytes());
					} else {
						//无需解密
						fileBytes = dataEle.getBytes();
					}
					/*promptIncomingMsg(roomname, timeEle.toString(),
							"(" + roomname + " just send over you a file." + ")");*/
					
					incomingFile = (NoxFileUnit)NoxMsgUtil.getObjectFromBytes(fileBytes);
					promptIncomingMsg(friend.getName(), timeEle.toString(), incomingFile);
				} else if(curNamespace.equals(XmlMsgFormat.PINGMSG_NAMESPACE_NAME)){
					//TODO 处理ping消息
					byte[] statBytes = dataEle.getBytes();
					NoxPeerStatusUnit stat = (NoxPeerStatusUnit) NoxMsgUtil.getObjectFromBytes(statBytes);
					refreshStatus(stat);
					sendPongMsg();
				} else if(curNamespace.equals(XmlMsgFormat.PONGMSG_NAMESPACE_NAME)){
					//TODO 处理pong消息
					//how about doing nothing?
					byte[] statBytes = dataEle.getBytes();
					NoxPeerStatusUnit stat = (NoxPeerStatusUnit) NoxMsgUtil.getObjectFromBytes(statBytes);
					refreshStatus(stat);
				} else if(curNamespace.equals(XmlMsgFormat.PUBLICKEYENC_NAMESPACE_NAME)){
					//TODO 处理publickey
					if(exchangingPubKeys && waiting4PubKey2){
						//do nothing
						//因为当前已经主动发送pubkey, 在等pubkey2或正在响应之前的请求-生成pubkey2
						System.out.println("received a public key. but I'm exchangingPubKeys && waiting4PubKey2, cancelling...");
						continue;
					}
					//否则
					exchangingPubKeys = true;
					/*
			         * Let's turn over to Bob. Bob has received Alice's public key
			         * in encoded format.
			         * He instantiates a DH public key from the encoded key material.
			         */
					byte[] alicePubKeyEnc = dataEle.getBytes();
			        KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
			        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
			            (alicePubKeyEnc);
			        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

			        /*
			         * Bob gets the DH parameters associated with Alice's public key.
			         * He must use the same parameters when he generates his own key
			         * pair.
			         */
			        DHParameterSpec dhParamSpec = ((DHPublicKey)alicePubKey).getParams();

			        // Bob creates his own DH key pair
			        System.out.println("BOB: Generate DH keypair ...");
			        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
			        bobKpairGen.initialize(dhParamSpec);
			        KeyPair bobKpair = bobKpairGen.generateKeyPair();

			        // Bob creates and initializes his DH KeyAgreement object
			        System.out.println("BOB: Initialization ...");
			        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
			        bobKeyAgree.init(bobKpair.getPrivate());

			        // Bob encodes his public key, and sends it over to Alice.
			        byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();
			        
			        Message pubkey2msg = NoxMsgUtil.generateMsg(XmlMsgFormat.PUBLICKEYENC2_NAMESPACE_NAME,
							NoxToolkit.getNetworkConfigurator().getName(),
							NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
							friend.getName(), friend.getUUID().toString(),
							bobPubKeyEnc);
			        
			        outbidipipe.sendMessage(pubkey2msg);
			        exchangingPubKeys = false;
			        
			        // Bob
			        // NOTE: The call to bobKeyAgree.generateSecret above reset the key
			        // agreement object, so we call doPhase again prior to another
			        // generateSecret call
			        bobKeyAgree.doPhase(alicePubKey, true);
			        DESKey = bobKeyAgree.generateSecret("DES");
				} else if(curNamespace.equals(XmlMsgFormat.PUBLICKEYENC2_NAMESPACE_NAME)){
					//TODO 处理publickey2
					/*
			         * Alice uses Bob's public key for the first (and only) phase
			         * of her version of the DH
			         * protocol.
			         * Before she can do so, she has to instantiate a DH public key
			         * from Bob's encoded key material.
			         */
					byte[] bobPubKeyEnc = dataEle.getBytes();
			        KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
			        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
			        bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
			        waiting4PubKey2 = false;
				} else {
					System.out.println("该消息格式不被此版本支持.");
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void importDESKey() {
		JFileChooser chooser = new JFileChooser(".");
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| (f.isFile() && (f.getName().endsWith(".key")));
			}

			@Override
			public String getDescription() {
				return "*.key";
			}
		};
		chooser.setFileFilter(filter);
		chooser.setDialogTitle("收到一条加密消息, 请选择解密所用的DES Key文件, 没有则点击取消.");
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// getJtf_pic().setText(chooser.getSelectedFile().getPath());
			System.out.println("You chose a deskey file: "
					+ chooser.getSelectedFile().getPath());
			File file = chooser.getSelectedFile();
			try {
				/*byte[] keyBytes = NoxMsgUtil.getBytesFromFile(file);
				DESKey = (SecretKey)NoxMsgUtil.getObjectFromBytes(keyBytes);*/
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
				DESKey = (SecretKey) in.readObject();
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send pong message when receive incoming connection
	 *  to declare my status. 
	 * TODO comment this
	 */
	public void run() {
		sendFullPingMsg();
	}
	
	/**
	 * Closes the output pipe and stops the platform
	 */
	public void stop() {
		if(outbidipipe != null){
			try {
				outbidipipe.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(room != null)
			room.dispose();
	}
	public PeerChatroom getRoom() {
		return room;
	}
	public void setRoom(PeerChatroom room) {
		if(room != null)
			this.room = room;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean ExtractDataAndProcess(String namespace, Message msg) {
		return false;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ID getRoomID() {
		return friend.getUUID();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRoomName() {
		return friend.getName();
	}
	/**
	 * 显示聊天窗口, 如果尚未实例化, 则实例化之<br>
	 * 如果需要实例化则不显示连接模糊进度指示器<br>
	 * 调用此函数的前提是在哈希表中找到PeerID对应的handler,
	 * 然后通过handler来显示chatroom. 所以对应的pipe肯定存在,
	 * 不然不会注册到哈希表中.
	 */
	public void showChatroom() {
		if(room == null){
			room = new PeerChatroom(friend, PeerConnectionHandler.this);
			room.removeMask();
		}
		
		room.setVisible(true);
	}
	/**
	 * 更新bidipipe, 应该考虑到此时在等待对方返回公钥的情况。
	 * 此情况下不能强行更新。
	 * @param outbidipipe\
	 * @deprecated
	 */
	public void setBiDiPipe(JxtaBiDiPipe outbidipipe) {
		int countdown = 0;
		while(waiting4PubKey2 && countdown++ < LANTimeLimit.CONNECT_MAXRETRIES*2){
			try {
				Thread.sleep(LANTimeLimit.UNIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(waiting4PubKey2)
			System.out.println("更新bidipipe时等候生成密钥超时。。。");
		this.outbidipipe = outbidipipe;
	}
}
package noxUI;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.endpoint.WireFormatMessage;
import net.jxta.endpoint.WireFormatMessageFactory;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;
import net.nox.NoxToolkit;
import net.nox.PeerGroupUtil;
import net.nox.PipeUtil;
import xml.nox.XmlMsgFormat;

/**
 * 
 * @author shinysky
 * 
 */
@SuppressWarnings("serial")
public class GroupChatroom extends Chatroom implements PipeMsgListener {
	/**
	 * 连接对方时显示的模糊进度指示器
	 */
	protected InfiniteProgressPanel glassPane;
	
	private Thread connector;
	
	private PeerGroup peergroup = null;
	/**
	 * 用于收发消息的pipe
	 */
	private InputPipe inpipe = null;
	private OutputPipe outpipe = null;

	public static String FROMALLSTR = "fromAll";
	
	/**
	 * 最终应该从主窗口继承颜色, 透明度 考虑实现:主窗口和从属窗口同步调节颜色和透明度.
	 * 在实例化从属窗口的时候将引用保存在一个Vector中,
	 * 调节颜色及透明度时对 Vector中实例依次调用调节函数.
	 * <li>构造函数.</li>
	 * <li>用户双击好友图标时, 如果不存在对应的chatroom, 则</li>
	 * <ol>
	 * <li>建立新的chatroom;</li>
	 * <li>自动尝试连接.</li>
	 * </ol>
	 * @param group 代表好友的PeerItem
	 * @see PeerItem
	 */
	public GroupChatroom(final GroupItem group) {
		super(group.getName(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_48.png", false);
		roomID = group.getUUID();

		GroupChatroomSidePane gcsp = new GroupChatroomSidePane(this, group
				.getName(), new PeerItem[0]);
		rootpane.add(gcsp);
		rootpane.add(chatroompane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
		this.setVisible(true);
		connector = new Thread(new Runnable() {
			public void run() {
				/**
				 * 与该peer建立连接
				 */
				TryToConnect(5 * 1000);
			}
		}, "Connector");
		connector.start();
	}
	public GroupChatroom(PeerGroupAdvertisement pga, InputPipe ipipe, OutputPipe opipe) {
		super(pga.getName(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_48.png", false);
		this.inpipe = ipipe;
		this.outpipe = opipe;
		
		/**
		 * 初始化peergroup, 供搜索组员使用
		 */
		PeerGroup parentgroup = NoxToolkit.getNetworkManager().getNetPeerGroup();

		if(pga != null){
			try {
				peergroup = parentgroup.newGroup(pga);
			} catch (PeerGroupException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * TODO 移除原来的管道监听器
		 */
		roomID = pga.getPeerGroupID();
		
		GroupChatroomSidePane gcsp 
			= new GroupChatroomSidePane(this, pga.getDescription(), new PeerItem[0]);
		rootpane.add(gcsp);
		rootpane.add(chatroompane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
		this.setVisible(true);
	}
	/*public GroupChatroom(final GroupItem group, GroupItem[] gmembers) {
		super(group.getNick(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_48.png", false);
		
		GroupChatroomSidePane groupmembers = new GroupChatroomSidePane(
				"Hello, everyone, happy everyday!", gmembers);
		rootpane.add(groupmembers);
		rootpane.add(chatroompane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
		roomID = group.getUUID();
		this.setVisible(true);
	}*/
	public DiscoveryService getDiscoveryService(){
		if(peergroup != null)
			return peergroup.getDiscoveryService();
		else
			return null;
	}
	public OutputPipe getOutBidipipe() {
		/*if (connectionHandler != null)
			return connectionHandler.getPipe();
		else
			return null;*/
		return outpipe;
	}

	public void setOutpipe(OutputPipe pipe) {
		if (pipe != null) {
			outpipe = pipe;
			System.out
					.println("In setOutpipe(), the parameter 'pipe' is not null, good!");
		} else
			System.out.println("Unbelievable! the parameter 'pipe' is null!!");
	}

	public void discoverMembers(){
		;
	}
	public void TryToConnectAgain(long waittime) {
		connector.start();
		rootpane.setVisible(false);
		glassPane.setVisible(true);
		glassPane.start();
	}

	/**
	 * 与该peer建立连接:
	 */
	private void TryToConnect(long waittime) {
		// 如果已经有了outbidipipe, 则不需要重新连接
		if (outpipe != null && inpipe != null) {
			System.out
					.println("["
							+ Thread.currentThread().getName()
							+ "] We have gotten a I/O pipe, cancelling TryToConnect().");
			return;
		}
		System.out.println("+++++++++++Begin TryToConnect()+++++++++++");
		// get the pipe service, and discovery
		PeerGroup parentgroup = NoxToolkit.getNetworkManager()
				.getNetPeerGroup();

		PeerGroupAdvertisement pga = PeerGroupUtil.getLocalAdvByID(parentgroup, roomID.toString());
		if(pga != null){
			peergroup = null;
			try {
				peergroup = parentgroup.newGroup(pga);
			} catch (PeerGroupException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(peergroup != null){
				System.out.println("成功创建组, 正在查找该组所用管道广告...");
				PipeAdvertisement pia = null;
				pia = PipeUtil.findPipeAdv(peergroup, peergroup.getPeerGroupID().toString());
				if(pia == null){
					System.out.println("Failed to find or create a pipe adv, it's a fatal error");
					return;
				}
				System.out.println("Creating Propagated InputPipe for pipe: " + pia.getPipeID());
		        try {
		        	if(inpipe == null)
		        		inpipe = peergroup.getPipeService().createInputPipe(pia, this);
		            if(outpipe == null)
		            	outpipe = peergroup.getPipeService().createOutputPipe(pia, waittime);
		        } catch (IOException e) {
		        	System.out.println("Failed to create Propagated InputPipe for pipe: " + pia.getPipeID());
		            e.printStackTrace();
		            System.exit(-1);
		        }
			}
		}
		if (outpipe != null) {
			//群发消息
			Message msg;

			System.out.println("[" + Thread.currentThread().getName()
					+ "] Saying hello ...");
			// create the message
			msg = new Message();
			Date date = new Date(System.currentTimeMillis());
			String hellomsg = "Hello [F:100] from "
					+ NoxToolkit.getNetworkConfigurator().getName();
			// add a string message element with the current date
			StringMessageElement senderEle = new StringMessageElement(
					XmlMsgFormat.SENDER_ELEMENT_NAME, NoxToolkit
							.getNetworkConfigurator().getName(), null);
			StringMessageElement senderIDEle = new StringMessageElement(
					XmlMsgFormat.SENDERID_ELEMENT_NAME, NoxToolkit
							.getNetworkConfigurator().getPeerID().toString(),
					null);
			StringMessageElement receiverEle = new StringMessageElement(
					XmlMsgFormat.RECEIVER_ELEMENT_NAME, this.roomname, null);
			StringMessageElement receiverIDEle = new StringMessageElement(
					XmlMsgFormat.RECEIVERID_ELEMENT_NAME, roomID
							.toString(), null);
			StringMessageElement timeEle = new StringMessageElement(
					XmlMsgFormat.TIME_ELEMENT_NAME, date.toString(), null);
			StringMessageElement msgEle = new StringMessageElement(
					XmlMsgFormat.MESSAGE_ELEMENT_NAME, hellomsg, null);

			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					senderEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					senderIDEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					receiverEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					receiverIDEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, timeEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, msgEle);

			try {
				outpipe.send(msg);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Done!");
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Room ID	: " + roomID);
		System.out.println("+++++++++++End of TryToConnect()+++++++++++");
	}

	/**
	 * Process the incoming message
	 * <ol>
	 * <li>如果聊天室可见, 直接显示</li>
	 * <li>TODO 如果不可见, 则提示用户有新消息到达</li>
	 * 
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

			MessageElement msgEle = msg.getMessageElement(
					XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					XmlMsgFormat.MESSAGE_ELEMENT_NAME);
			incomingMsg += msgEle.toString();

			System.out
					.println("---------[F:100]----------End Message----------[F:035]------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * 将消息输出到聊天窗口
		 */
		System.out.println("Put the message to the Chatroom window...");
		/**
		 * !!!!!!群聊消息!!!!!!!!
		 */
		MessageElement senderEle = msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.SENDER_ELEMENT_NAME);
		MessageElement timeEle = msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.TIME_ELEMENT_NAME);
		
		String[] strArrayMsg = {FROMALLSTR, senderEle.toString(), "", timeEle.toString(),
				incomingMsg };

		ByteArrayMessageElement picEle = (ByteArrayMessageElement) msg
				.getMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
						XmlMsgFormat.PICTURE_ELEMENT_NAME);

		ImageIcon incomingPic = null;
		if (picEle != null) {
			// 将byte[]转化为图片形式
			try {
				ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
						picEle.getBytes());
				BufferedImage bufImg = javax.imageio.ImageIO.read(byteArrayIS);
				incomingPic = new ImageIcon(bufImg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		chatroompane.incomingMsgProcessor(strArrayMsg, incomingPic);

		System.out.println("Did you see the message?");
		if(!this.isVisible()){
			//TODO 应该是提示有消息, 而不是强行显示窗口
			/*this.setVisible(true);
			System.out.println("The window is not visible, I make it be!");*/
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
					GroupChatroom.this.setVisible(true);
					slider.Dispose();
				}
			});
			ignoreIt.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0){
					slider.Dispose();
				}
			});
		}
	}

	/**
	 * Closes the output pipe and stops the platform
	 */
	@SuppressWarnings("unused")
	private void stop() {
		inpipe.close();
		outpipe.close();
	}

	/**
	 * 向外发送消息
	 * 
	 * @param strmsg
	 *            string msg
	 * @return succeed or not
	 */
	public boolean SendMsg(String strmsg, BufferedImage bufImg) {
		int retrial = 0;
		while (outpipe == null && retrial < Chatroom.MAXRETRIES) {
			System.out
					.println("outPipe is null now, trying to connect...");
			TryToConnect(Chatroom.UnitWaitTime);
			retrial ++;
		}
		if(outpipe == null){
			System.out
			.println("outPipe is still null even after trying so many times, canceling sending msg...");
			return false;
		}
		
		Message msg;
		try {
			System.out.println("Sending message:\n" + strmsg);
			// create the message
			msg = new Message();
			Date date = new Date(System.currentTimeMillis());
			// add a string message element with the current date
			StringMessageElement senderEle = new StringMessageElement(
					XmlMsgFormat.SENDER_ELEMENT_NAME, NoxToolkit
							.getNetworkConfigurator().getName(), null);
			StringMessageElement senderIDEle = new StringMessageElement(
					XmlMsgFormat.SENDERID_ELEMENT_NAME, NoxToolkit
							.getNetworkConfigurator().getPeerID().toString(),
					null);
			StringMessageElement receiverEle = new StringMessageElement(
					XmlMsgFormat.RECEIVER_ELEMENT_NAME, this.roomname, null);
			StringMessageElement receiverIDEle = new StringMessageElement(
					XmlMsgFormat.RECEIVERID_ELEMENT_NAME, roomID
							.toString(), null);
			StringMessageElement timeEle = new StringMessageElement(
					XmlMsgFormat.TIME_ELEMENT_NAME, date.toString(), null);
			StringMessageElement msgEle = new StringMessageElement(
					XmlMsgFormat.MESSAGE_ELEMENT_NAME, strmsg, null);

			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					senderEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					senderIDEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					receiverEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					receiverIDEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, timeEle);
			msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, msgEle);

			if (bufImg != null) {
				/**
				 * TODO 将图片或者ImageIcon转为byte[]
				 */
				ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
				javax.imageio.ImageIO.write(bufImg, "PNG", byteArrayOS);
				byte[] picture = byteArrayOS.toByteArray();

				ByteArrayMessageElement picEle = new ByteArrayMessageElement(
						XmlMsgFormat.PICTURE_ELEMENT_NAME, MimeMediaType.AOS,
						picture, null);
				msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
						picEle);
			}
			outpipe.send(msg);
			System.out.println("message sent");
		} catch (IOException e) {
			System.out.println("failed to send message");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * PipeMsgListener interface for asynchronous message arrival notification
	 * 
	 * @param event
	 *            the message event
	 */
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {
		System.out.println("+++Begin Chatroom pipeMsgEvent()...+++");
		Message msg = event.getMessage();

		System.out.println("Incoming call: " + msg.toString());

		// get the message element named SenderMessage
		MessageElement senderEle = msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.SENDER_ELEMENT_NAME);
		MessageElement senderIDEle = msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.SENDERID_ELEMENT_NAME);
		MessageElement receiverEle = msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.RECEIVER_ELEMENT_NAME);
		MessageElement receiverIDEle = msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.RECEIVERID_ELEMENT_NAME);
		MessageElement timeEle = msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.TIME_ELEMENT_NAME);
		MessageElement msgEle = msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.MESSAGE_ELEMENT_NAME);

		System.out.println("Detecting if the msg elements is null");

		if (null == senderEle || receiverEle == null || timeEle == null
				|| msgEle == null) {
			System.out.println("Some msg element is empty, it's weird.");
			return;
		}
		System.out.println("Incoming call: From: " + senderEle.toString());
		System.out.println("Incoming call: FromID: " + senderIDEle.toString());
		System.out.println("Incoming call: To: " + receiverEle.toString());
		System.out.println("Incoming call: ToID: " + receiverIDEle.toString());
		System.out.println("Incoming call: At: " + timeEle.toString());
		System.out.println("Incoming call: Msg: " + msgEle.toString());

		// Get message
		// TODO 这是在干嘛?
		if (null == senderEle.toString() || receiverEle.toString() == null
				|| timeEle.toString() == null || msgEle.toString() == null) {
			System.out
					.println("Msg.toString() is empty, it's weird even more.");
			return;
		}
		if(senderIDEle.toString().equals(NoxToolkit.getNetworkConfigurator().getPeerID().toString())){
			//自己发的消息, 忽略之
			return;
		}
		this.processIncomingMsg(msg, false);
		System.out.println("+++End Chatroom pipeMsgEvent()...+++");
	}
}

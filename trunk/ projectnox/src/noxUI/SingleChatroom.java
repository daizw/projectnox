package noxUI;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.endpoint.WireFormatMessage;
import net.jxta.endpoint.WireFormatMessageFactory;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;
import net.jxta.util.JxtaBiDiPipe;
import net.nox.NoxToolkit;
import xml.nox.XmlMsgFormat;

/**
 * 
 * @author shinysky
 * 
 */
@SuppressWarnings("serial")
public class SingleChatroom extends Chatroom implements PipeMsgListener {
	/**
	 * ���ӶԷ�ʱ��ʾ��ģ������ָʾ��
	 */
	protected InfiniteProgressPanel glassPane;
	
	private Thread connector;
	/**
	 * ���ڱ����ҵ��Ĺܵ����
	 */
	protected PipeAdvertisement newOutPipeAdv = null;
	/**
	 * �����շ���Ϣ��bidipipe
	 */
	private JxtaBiDiPipe outbidipipe = null;
	
	/**
	 * ����Ӧ�ô������ڼ̳���ɫ, ͸���� ����ʵ��---------�����ںʹ�������ͬ��������ɫ��͸����
	 * ��ʵ�����������ڵ�ʱ�����ñ�����һ��Vector��, ������ɫ��͸����ʱ�� Vector��ʵ�����ε��õ��ں���
	 * 
	 * @param title
	 *            �����ұ���, һ���ǶԷ����ǳ�, ��������
	 * @param type
	 *            ����������:
	 *            Chatroom.PRIVATE_CHATROOM(˽��);Chatroom.GROUP_CHATROOM(Ⱥ��);
	 */
	SingleChatroom(String title) {
		super(title + " - NoX Chatroom", SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "chat_green_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "chat_green_48.png", title, false);
		// ���մ˴�ӦΪfalse

		roomname = title;
		this.setBounds(100, 80, WIDTH_DEFLT, HEIGHT_DEFLT);
		this.setSize(new Dimension(WIDTH_DEFLT, HEIGHT_DEFLT));
		this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));

		chatroompane = new ChatRoomPane(this);
		// crp.setLayout(new FlowLayout());
		rootpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rootpane.setOneTouchExpandable(true);
		// rootpane.setDividerLocation(0.2f);
		rootpane.setDividerLocation(0f);
		rootpane.setDividerSize(8);
		rootpane.setResizeWeight(0.2d);
	}

	/**
	 * <li>���캯��.</li>
	 * <li>�û�˫������ͼ��ʱ, ��������ڶ�Ӧ��chatroom, ��</li>
	 * <ol>
	 * <li>�����µ�chatroom;</li>
	 * <li>�Զ���������.</li>
	 * </ol>
	 * @param friend ������ѵ�PeerItem
	 * @see PeerItem
	 */
	public SingleChatroom(final PeerItem friend, JxtaBiDiPipe pipe) {
		this(friend.getNick());
		roomID = friend.getUUID();

		SingleChatRoomSidePane portraits = new SingleChatRoomSidePane(friend
				.getNick(), friend.getPortrait(), new ImageIcon(
				SystemPath.PORTRAIT_RESOURCE_PATH + "portrait.png"));
		rootpane.add(portraits);
		rootpane.add(chatroompane);
		
		glassPane = new InfiniteProgressPanel("������, ���Ժ�...", 12);
		glassPane.setBounds(0, -NoxFrame.TITLE_HEIGHT, WIDTH_PREF, HEIGHT_PREF
				- NoxFrame.TITLE_HEIGHT * 2);

		if(pipe == null){
			this.getContainer().add(glassPane);
			rootpane.setVisible(false);
			glassPane.start();

			connector = new Thread(new Runnable() {
				public void run() {
					/**
					 * ���peer��������
					 */
					TryToConnect(5 * 1000);
					/**
					 * �Ⱥ�N����
					 */
					glassPane.stop();
					if (outbidipipe == null) {
						System.out.println("Failed to connect to the peer.");
						int choice = JOptionPane
								.showConfirmDialog(
										SingleChatroom.this,
										"Sorry, you can get him/her right now. Open the Chatroom anyway?",
										"Failed to connect",
										JOptionPane.YES_NO_OPTION);
						if (choice == JOptionPane.YES_OPTION) {
							rootpane.setVisible(true);
							glassPane.setVisible(false);
							SingleChatroom.this.setVisible(true);
						} else
							SingleChatroom.this.dispose();
					} else {
						//ע��֮, ע��: Ӧע��ChatroomUnit������Chatroom!
						//��Ϊע��Chatroomֻ�������Ѵ���ID-pipe�Ե����
						NoxToolkit.registerChatroomUnit(roomID, outbidipipe, SingleChatroom.this);
						outbidipipe.setMessageListener(SingleChatroom.this);
						rootpane.setVisible(true);
						glassPane.setVisible(false);
						SingleChatroom.this.setVisible(true);
					}
				}
			}, "Connector");
			connector.start();
		}else{
			outbidipipe = pipe;
			outbidipipe.setMessageListener(SingleChatroom.this);
		}
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane);
		/**
		 * �Զ���ʾ
		 */
		this.setVisible(true);
	}
	
	public JxtaBiDiPipe getOutBidipipe() {
		/*if (connectionHandler != null)
			return connectionHandler.getPipe();
		else
			return null;*/
		return outbidipipe;
	}

	public void setOutBidipipe(JxtaBiDiPipe pipe) {
		if (pipe != null) {
			outbidipipe = pipe;
			System.out
					.println("In setOutBidipipe(), the parameter 'pipe' is not null, good!");
			rootpane.setVisible(true);
			glassPane.setVisible(false);
			glassPane.stop();
		} else
			System.out.println("Unbelievable! the parameter 'pipe' is null!!");
	}

	public void TryToConnectAgain(long waittime) {
		connector.start();
		rootpane.setVisible(false);
		glassPane.setVisible(true);
		glassPane.start();
	}

	/**
	 * TODO ���peer��������:
	 */
	private void TryToConnect(long waittime) {
		// ����Ѿ�����outbidipipe, ����Ҫ��������
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

		try {
			// PipeAdvertisement connect_pipe =
			// MsgReceiver.getPipeAdvertisement();

			localDiscoveryListener pipeListener = new localDiscoveryListener();
			newOutPipeAdv = null;
			System.out
					.println("[" + Thread.currentThread().getName()
							+ "] Fetching remote pipe adv to peer/group:"
							+ roomID);
			// ������ֶ�Ӧ��pipe���޸�newOutBidipipeAdv
			group.getDiscoveryService().getRemoteAdvertisements(
					roomID.toString(), DiscoveryService.ADV, PipeAdvertisement.NameTag,
					roomID.toString(), 65535, pipeListener);
			long unittime = 500;
			long timecount = waittime / unittime;
			//���ҹܵ����ʱ��, �̶�: 2s : Ӧ��������״������
			//TODO �Լ��Ĺܵ����������ݿ���, ����Ҫʱ���ؽ��ܵ�.
			int fetchRemotePipeAdvTimeCount = 4;
			// ���û�ҵ�gotPipeAdv���߳�ʱ�����ڴ�ʱ������Ȼû����������
			// (�����и�ͬ��������, �ڲ��ҹ�����, ����Է���������, ��û���޸�gotPipeAdv, ���������Ȼ���³�ʱ)
			while (fetchRemotePipeAdvTimeCount-- > 0 && getOutBidipipe() == null) {
				try {
					Thread.sleep(unittime);
					System.out.println("[" + Thread.currentThread().getName()
							+ "] timecount:	" + timecount);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// 1: �õ���pipe adv;
			// 2: timecount <= 0;
			// 3: outbidipipe != null: ���������жԷ����ӹ���,
			// ����ConnectionHandler�޸���outbidipipe.
			if (outbidipipe == null) {
				// ���ǵ��������
				// �����ֿ���:
				// 1: �õ���pipe adv;
				// 2: ��ʱ
				if (newOutPipeAdv == null) {
					// 2: ��ʱ, ����
					System.out
							.println("Failed to fetch a remote pipe adv: newOutBidipipeAdv == null.");
					return;
				}
				// 1: �õ�pipe adv
				System.out.println("I have got the latest remote pipe adv.");

				System.out.println("[" + Thread.currentThread().getName()
						+ "] Attempting to establish a connection on pipe : "
						+ newOutPipeAdv.getPipeID());
				System.out.println("timeout: "
						+ (int) ((timecount + 1) * unittime));
				while (outbidipipe == null && timecount > 0) {
					// Try again and again
					try {
						// ���ĸ���λ��ʱ����Ϊ��ʱʱ��//timecount -= 4;
						timecount -= 2;
						outbidipipe = new JxtaBiDiPipe(group, newOutPipeAdv,
								(int) unittime * 4, this, true);
						if (outbidipipe == null) {
							// ���if�ƺ�û��Ҫ
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
			// 3: outbidipipe != null: ���������жԷ����ӹ���,
			// ����ConnectionHandler�޸���outbidipipe.
			// ���߾������Ϲ��̵õ����µ�pipe

			// We registered ourself as the msg listener for the pipe. We now
			// just need to wait until the transmission is finished.
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

			outbidipipe.sendMessage(msg);

			System.out.println("[" + Thread.currentThread().getName()
					+ "] Done!");
		} catch (IOException failure) {
			failure.printStackTrace();
		}
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Room ID	: " + roomID);
		System.out.println("+++++++++++End of TryToConnect()+++++++++++");
	}

	/**
	 * Process the incoming message
	 * <ol>
	 * <li>��������ҿɼ�, ֱ����ʾ</li>
	 * <li>TODO ������ɼ�, ����ʾ�û�������Ϣ����</li>
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
		 * ����Ϣ��������촰��
		 */
		System.out.println("Put the message to the Chatroom window...");
		String whoami = "ME";
		whoami = NoxToolkit.getNetworkConfigurator().getName();
		String[] strArrayMsg = { "", roomname, whoami, msg.getMessageElement(
				XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				XmlMsgFormat.TIME_ELEMENT_NAME).toString(),
				incomingMsg };

		ByteArrayMessageElement picEle = (ByteArrayMessageElement) msg
				.getMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
						XmlMsgFormat.PICTURE_ELEMENT_NAME);

		ImageIcon incomingPic = null;
		if (picEle != null) {
			// ��byte[]ת��ΪͼƬ��ʽ
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
			//TODO Ӧ������ʾ����Ϣ, ������ǿ����ʾ����
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
				 * ������Ϣʱ������ʾ��
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
					SingleChatroom.this.setVisible(true);
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
		try {
			outbidipipe.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * ���ⷢ����Ϣ
	 * 
	 * @param strmsg
	 *            string msg
	 * @return succeed or not
	 */
	@Override
	public boolean SendMsg(String strmsg, BufferedImage bufImg) {
		if (outbidipipe == null) {
			System.out
					.println("outBiDiPipe is null now, canceling sending msg.");
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
				 * TODO ��ͼƬ����ImageIconתΪbyte[]
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
			outbidipipe.sendMessage(msg);
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
		// TODO �����ڸ���?
		if (null == senderEle.toString() || receiverEle.toString() == null
				|| timeEle.toString() == null || msgEle.toString() == null) {
			System.out
					.println("Msg.toString() is empty, it's weird even more.");
			return;
		}
		this.processIncomingMsg(msg, false);
		System.out.println("+++End Chatroom pipeMsgEvent()...+++");
	}

	class localDiscoveryListener implements DiscoveryListener {
		public void discoveryEvent(DiscoveryEvent e) {
			PipeAdvertisement pipeAdv = null;
			Enumeration<Advertisement> responses = e.getSearchResults();
			/**
			 * TODO ��ʵ�������ƺ�����Ҫwhileѭ��; �����п��ܴ��ڹ����ڵ�����
			 */
			while (responses.hasMoreElements()) {
				pipeAdv = (PipeAdvertisement) responses.nextElement();
				if(pipeAdv.getDescription() == null){
					continue;
				}
				try {
					if(newOutPipeAdv == null){
						newOutPipeAdv = pipeAdv;
						System.out.println("Got the first pipe adv: " + pipeAdv.getPipeID());
					}else{
						if(Long.parseLong(newOutPipeAdv.getDescription()) 
								< Long.parseLong(pipeAdv.getDescription())){
							newOutPipeAdv = pipeAdv;
							System.out.println("Got a newer pipe adv: " + pipeAdv.getPipeID());
						}else{
							System.out.println("Got a older pipe adv: " + pipeAdv.getPipeID());
						}
					}
					// do something with pipe advertisement
				} catch (Exception ee) {
					// not a pipe advertisement
				}
			}
		}
	}
}

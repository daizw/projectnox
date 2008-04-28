package noxUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.endpoint.WireFormatMessage;
import net.jxta.endpoint.WireFormatMessageFactory;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.OutputPipeListener;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;
import net.jxta.util.JxtaBiDiPipe;
import net.nox.ConnectionHandler;
import net.nox.NoxToolkit;
import xml.XmlMsgFormat;
/**
 * 
 * @author shinysky
 * 
 */
public class Chatroom extends NoxFrame implements 	PipeMsgListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7981736228268584688L;
	/**
	 * Ĭ�ϳߴ糣��
	 */
	public static final int WIDTH_DEFLT = 700;
	public static final int WIDTH_PREF = 700;
	public static final int WIDTH_MAX = 2000;
	public static final int WIDTH_MIN = 300;
	public static final int HEIGHT_DEFLT = 500;
	public static final int HEIGHT_PREF = 500;
	public static final int HEIGHT_MAX = 2000;
	public static final int HEIGHT_MIN = 200;

	public static final int PRIVATE_CHATROOM = 0;
	public static final int GROUP_CHATROOM = 1;

	public static final String RESOURCES_PATH = "resrc/";
	public static final String ICON_PREFIX = "/";
	public static final String ICON_SUFFIX_REGEX = "[^\\/]+\\.gif";
	public static final String ICON_RESOURCES_PATH = RESOURCES_PATH
			.concat("faces/");

	JSplitPane rootpane;
	ChatRoomPane chatroompane;
	protected InfiniteProgressPanel glassPane;
	boolean connected = false;
	boolean gotPipeAdv = false;

	PipeAdvertisement newOutBidipipeAdv;

	/**
	 * ˽��: ��ֵΪ�Է�ID; Ⱥ��:Ϊ��ID
	 */
	private ID roomID;
	private String roomname;
	/**
	 * The per connection bi-directional pipe instance.
	 */
	private JxtaBiDiPipe outbidipipe = null;

	PipeAdvertisement serverPipeAdv;

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
	Chatroom(String title) {
		super(title + " - NoX Chatroom", "resrc\\images\\bkgrd.png",
				"resrc\\icons\\chat2.png", title,
				"resrc\\buttons\\minimize.png",
				"resrc\\buttons\\minimize_rollover.png",
				"resrc\\buttons\\maximize.png",
				"resrc\\buttons\\maximize_rollover.png",
				"resrc\\buttons\\normalize.png",
				"resrc\\buttons\\normalize_rollover.png",
				"resrc\\buttons\\close.png",
				"resrc\\buttons\\close_rollover.png", false);
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

	public Chatroom(final PeerItem friend, JxtaBiDiPipe bidipipe) {
		this(friend.getNick());
		this.outbidipipe = bidipipe;
		if(outbidipipe != null)
			outbidipipe.setMessageListener(this);
		
		SingleChatRoomSidePane portraits = new SingleChatRoomSidePane(friend
				.getNick(), friend.getPortrait(), new ImageIcon(
				"resrc\\portrait\\portrait.png"));
		rootpane.add(portraits);
		rootpane.add(chatroompane);

		glassPane = new InfiniteProgressPanel("������, ���Ժ�...", 12);
		glassPane.setBounds(0, -NoxFrame.TITLE_HEIGHT, WIDTH_PREF, HEIGHT_PREF
				- NoxFrame.TITLE_HEIGHT * 2);
		glassPane.start();

		Thread connector = new Thread(new Runnable() {
			public void run() {
				/**
				 * ���peer��������
				 */
				TryToConnect(5 * 1000);
				/**
				 * �Ⱥ�N����
				 */
				glassPane.stop();
				if (!connected) {
					System.out.println("Failed to connect to the peer.");
					int choice = JOptionPane
							.showConfirmDialog(
									Chatroom.this,
									"Sorry, you can get him/her right now. Open the Chatroom anyway?",
									"Failed to connect",
									JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.YES_OPTION) {
						rootpane.setVisible(true);
						glassPane.setVisible(false);
						Chatroom.this.setVisible(true);
					} else
						Chatroom.this.dispose();
				} else {
					rootpane.setVisible(true);
					glassPane.setVisible(false);
					Chatroom.this.setVisible(true);
				}
			}
		}, "Connector");
		connector.start();

		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(glassPane/* , BorderLayout.CENTER */);
		this.getContainer().add(rootpane/* , BorderLayout.SOUTH */);
		rootpane.setVisible(false);

		roomID = friend.getUUID();
		this.setVisible(true);
	}

	public Chatroom(final GroupItem group, GroupItem[] gmembers) {
		this(group.getNick());
		MultiChatRoomSidePane groupmembers = new MultiChatRoomSidePane(
				"Hello, everyone, happy everyday!", gmembers);
		rootpane.add(groupmembers);
		rootpane.add(chatroompane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
		roomID = group.getUUID();
		this.setVisible(true);
	}

	public ID getRoomID() {
		return roomID;
	}
	public void setOutBidipipe(JxtaBiDiPipe pipe){
		if(pipe != null){
			outbidipipe = pipe;
			outbidipipe.setMessageListener(this);
			System.out.println("In setOutBidipipe(), the parameter 'pipe' is not null, good!");
		}else
			System.out.println("Unbelievable! the parameter 'pipe' is null!!");
	}

	/**
	 * TODO ���peer��������:
	 * 
	 * @return
	 */
	private void TryToConnect(long waittime) {
		//����Ѿ�����outbidipipe, ����Ҫ��������
		if(outbidipipe != null){
			System.out.println("[" + Thread.currentThread().getName()
					+ "] We have gotten a outbidipipe, cancelling TryToConnect().");
			connected = true;
			return;
		}
		System.out.println("+++++++++++Begin TryToConnect()+++++++++++");
		// get the pipe service, and discovery
		PeerGroup group = new NoxToolkit().getNetworkManager()
				.getNetPeerGroup();

		try {
			// PipeAdvertisement connect_pipe =
			// MsgReceiver.getPipeAdvertisement();

			localDiscoveryListener pipeListener = new localDiscoveryListener();
			newOutBidipipeAdv = null;
			System.out.println("[" + Thread.currentThread().getName()
					+ "] Fetching remote pipe adv to peer/group:" + getRoomID());
			//������ֶ�Ӧ��pipe���޸�newOutBidipipeAdv
			group.getDiscoveryService().getRemoteAdvertisements(
					getRoomID().toString(), DiscoveryService.ADV, "Name",
					getRoomID().toString(), 10, pipeListener);
			long unittime = 500;
			long timecount = waittime/unittime;
			while(!gotPipeAdv && timecount-- > 0){
				try {
					Thread.sleep(500);
					System.out.println("[" + Thread.currentThread().getName()
							+ "] timecount:	"+timecount);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(newOutBidipipeAdv == null){
				return;
			}
			System.out.println("May have got a remote pipe adv...");
			
			System.out.println("[" + Thread.currentThread().getName()
					+ "] Attempting to establish a connection on pipe : "
					+ newOutBidipipeAdv.getPipeID());
			System.out.println("timeout: "+(int) ((timecount+1)*unittime));
			outbidipipe = new JxtaBiDiPipe(group, newOutBidipipeAdv, (int) ((timecount+1)*unittime),
					this, true);
			System.out.println("[" + Thread.currentThread().getName()
					+ "] JxtaBiDiPipe pipe created");

			// We registered ourself as the msg listener for the pipe. We now
			// just need to wait until the transmission is finished.
			connected = true;
			Message msg;

			System.out.println("[" + Thread.currentThread().getName()
					+ "] Sending message...");
			// create the message
			msg = new Message();
			Date date = new Date(System.currentTimeMillis());
			String hellomsg = "Hello [F:100] from "
				+  new NoxToolkit().getNetworkConfigurator().getName();
			// add a string message element with the current date
			StringMessageElement senderEle = new StringMessageElement(
					XmlMsgFormat.SENDER_ELEMENT_NAME, new NoxToolkit().getNetworkConfigurator().getName(), null);
			StringMessageElement senderIDEle = new StringMessageElement(
					XmlMsgFormat.SENDERID_ELEMENT_NAME, new NoxToolkit().getNetworkConfigurator().getPeerID().toString(), null);
			StringMessageElement receiverEle = new StringMessageElement(
					XmlMsgFormat.RECEIVER_ELEMENT_NAME, this.roomname, null);
			StringMessageElement receiverIDEle = new StringMessageElement(
					XmlMsgFormat.RECEIVERID_ELEMENT_NAME, getRoomID().toString(), null);
			StringMessageElement timeEle = new StringMessageElement(
					XmlMsgFormat.TIME_ELEMENT_NAME, date.toString(), null);
			StringMessageElement msgEle = new StringMessageElement(
					XmlMsgFormat.MESSAGE_ELEMENT_NAME, hellomsg, null);

			msg.addMessageElement(null, senderEle);
			msg.addMessageElement(null, senderIDEle);
			msg.addMessageElement(null, receiverEle);
			msg.addMessageElement(null, receiverIDEle);
			msg.addMessageElement(null, timeEle);
			msg.addMessageElement(null, msgEle);

			outbidipipe.sendMessage(msg);

			System.out.println("[" + Thread.currentThread().getName()
					+ "] Done!");
		} catch (IOException failure) {
			failure.printStackTrace();
		}

		System.out.println("[" + Thread.currentThread().getName()
				+ "] Pipe name	: " + newOutBidipipeAdv.getName());
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Room ID	: " + getRoomID());
		System.out.println("+++++++++++End of TryToConnect()+++++++++++");
	}

	/**
	 * Dumps the message content to stdout
	 * 
	 * @param msg
	 *            the message
	 * @param verbose
	 *            dumps message element content if true
	 */
	public void processIncomingMsg(Message msg, boolean verbose) {
		String inputMsg = "++";
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
					//XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
					null,
					XmlMsgFormat.MESSAGE_ELEMENT_NAME);
			inputMsg += msgEle.toString();
			
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
		whoami = new NoxToolkit().getNetworkConfigurator().getName();
		String[] strArrayMsg = { "", roomname, whoami, new Date().toString(), inputMsg };
		chatroompane.receiveMsgAndAccess(strArrayMsg);
		System.out.println("Have you see the message?");
		if(!this.isVisible()){
			//TODO Ӧ������ʾ����Ϣ, ������ǿ����ʾ����
			this.setVisible(true);
			System.out.println("The window is not visible, I make it be!");
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
	 * @param strmsg string msg
	 * @return succeed or not
	 */
	public boolean SendMsg(String strmsg) {
		if (outbidipipe == null)
			return false;
		Message msg;
		try {
			System.out.println("Sending message:\n" + strmsg);
			// create the message
			msg = new Message();
			Date date = new Date(System.currentTimeMillis());
			// add a string message element with the current date
			StringMessageElement senderEle = new StringMessageElement(
					XmlMsgFormat.SENDER_ELEMENT_NAME, new NoxToolkit().getNetworkConfigurator().getName(), null);
			StringMessageElement senderIDEle = new StringMessageElement(
					XmlMsgFormat.SENDERID_ELEMENT_NAME, new NoxToolkit().getNetworkConfigurator().getPeerID().toString(), null);
			StringMessageElement receiverEle = new StringMessageElement(
					XmlMsgFormat.RECEIVER_ELEMENT_NAME, this.roomname, null);
			StringMessageElement receiverIDEle = new StringMessageElement(
					XmlMsgFormat.RECEIVERID_ELEMENT_NAME, getRoomID().toString(), null);
			StringMessageElement timeEle = new StringMessageElement(
					XmlMsgFormat.TIME_ELEMENT_NAME, date.toString(), null);
			StringMessageElement msgEle = new StringMessageElement(
					XmlMsgFormat.MESSAGE_ELEMENT_NAME, strmsg, null);

			msg.addMessageElement(null, senderEle);
			msg.addMessageElement(null, senderIDEle);
			msg.addMessageElement(null, receiverEle);
			msg.addMessageElement(null, receiverIDEle);
			msg.addMessageElement(null, timeEle);
			msg.addMessageElement(null, msgEle);

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
				//XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null,
				XmlMsgFormat.SENDER_ELEMENT_NAME);
		MessageElement senderIDEle = msg.getMessageElement(
				//XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null,
				XmlMsgFormat.SENDERID_ELEMENT_NAME);
		MessageElement receiverEle = msg.getMessageElement(
				//XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null,
				XmlMsgFormat.RECEIVER_ELEMENT_NAME);
		MessageElement receiverIDEle = msg.getMessageElement(
				//XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null,
				XmlMsgFormat.RECEIVERID_ELEMENT_NAME);
		MessageElement timeEle = msg.getMessageElement(
				//XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null,
				XmlMsgFormat.TIME_ELEMENT_NAME);
		MessageElement msgEle = msg.getMessageElement(
				//XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null,
				XmlMsgFormat.MESSAGE_ELEMENT_NAME);
		
		System.out.println("Detecting if the msg elements is null");

		if (null == senderEle || receiverEle == null || timeEle == null
				|| msgEle == null){
			System.out.println("Msg is empty, it's weird.");
			return;
		}
		System.out.println("Incoming call: From: " + senderEle.toString());
		System.out.println("Incoming call: FromID: " + senderIDEle.toString());
		System.out.println("Incoming call: To: " + receiverEle.toString());
		System.out.println("Incoming call: ToID: " + receiverIDEle.toString());
		System.out.println("Incoming call: At: " + timeEle.toString());
		System.out.println("Incoming call: Msg: " + msgEle.toString());

		// Get message
		//TODO �����ڸ���?
		if (null == senderEle.toString() || receiverEle.toString() == null
				|| timeEle.toString() == null || msgEle.toString() == null){
			System.out.println("Msg.toString() is empty, it's weird even more.");
			return;
		}
		this.processIncomingMsg(msg, true);
		System.out.println("+++End Chatroom pipeMsgEvent()...+++");
	}

	class localDiscoveryListener implements DiscoveryListener {
		public void discoveryEvent(DiscoveryEvent e) {
			PipeAdvertisement pipeAdv = null;
			Enumeration<Advertisement> responses = e.getSearchResults();
			while (responses.hasMoreElements()) {
				try {
					pipeAdv = (PipeAdvertisement) responses.nextElement();
					// do something with pipe advertisement
				} catch (Exception ee) {
					// not a pipe advertisement
				}
			}
			//����ҵ���pipe
			if(pipeAdv != null){
				gotPipeAdv = true;
				newOutBidipipeAdv = pipeAdv;
			}
		}
	}
}

package noxUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.endpoint.WireFormatMessage;
import net.jxta.endpoint.WireFormatMessageFactory;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.OutputPipeListener;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;
import net.nox.NoxToolkit;

/**
 * 
 * @author shinysky
 * 
 */
public class Chatroom extends NoxFrame implements OutputPipeListener,
		PipeMsgListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7981736228268584688L;
	/**
	 * 默认尺寸常量
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

	private PipeService pipeService;
	private PipeAdvertisement pipeAdv;
	private OutputPipe outputPipe;
	public final static String MESSAGE_NAME_SPACE = "NoXMessage";
	public final static String MESSAGE_TIME_NAME_SPACE = "SENDTIME";

	private InputPipe inputPipe = null;
	private ID roomID;

	/**
	 * 最终应该从主窗口继承颜色, 透明度 考虑实现---------主窗口和从属窗口同步调节颜色和透明度
	 * 在实例化从属窗口的时候将引用保存在一个Vector中, 调节颜色及透明度时对 Vector中实例依次调用调节函数
	 * 
	 * @param title
	 *            聊天室标题, 一般是对方的昵称, 或者组名
	 * @param type
	 *            聊天室类型:
	 *            Chatroom.PRIVATE_CHATROOM(私聊);Chatroom.GROUP_CHATROOM(群聊);
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
		// 最终此处应为false

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

	public Chatroom(final PeerItem friend) {
		this(friend.getNick());
		SingleChatRoomSidePane portraits = new SingleChatRoomSidePane(friend
				.getNick(), friend.getPortrait(), new ImageIcon(
				"resrc\\portrait\\portrait.png"));
		rootpane.add(portraits);
		rootpane.add(chatroompane);

		glassPane = new InfiniteProgressPanel("连接中, 请稍候...", 12);
		glassPane.setBounds(0, -NoxFrame.TITLE_HEIGHT, WIDTH_PREF, HEIGHT_PREF
				- NoxFrame.TITLE_HEIGHT * 2);
		glassPane.start();

		Thread connector = new Thread(new Runnable() {
			public void run() {
				/**
				 * 与该peer建立连接
				 */
				TryToConnect(5 * 1000);
				/**
				 * 等候N秒钟
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

	/**
	 * TODO 与该peer建立连接:
	 * 
	 * @return
	 */
	private void TryToConnect(long waittime) {
		// get the pipe service, and discovery
		pipeService = new NoxToolkit().getNetworkManager().getNetPeerGroup()
				.getPipeService();

		//BidirectionalPipeService bps;
		// create the pipe advertisement
		pipeAdv = getPipeAdvertisement();

		System.out.println("Pipe Adv	: " + pipeAdv);
		System.out.println("Room ID	: " + getRoomID());

		Set<PeerID> resolvablePeers = new HashSet<PeerID>();
		resolvablePeers.clear();
		resolvablePeers.add((PeerID) getRoomID());

		long unittime = 500;
		long timecount = waittime / unittime;

		try {TryToGetInputPipe();
			// issue a pipe resolution asynchronously.
			// outputPipeEvent() is called once the pipe has resolved
			// pipeService.createOutputPipe(pipeAdv, (OutputPipeListener) this);
			pipeService.createOutputPipe(pipeAdv, this);
			pipeService.createOutputPipe(pipeAdv, resolvablePeers, this);
			
			/**
			 * 等待N秒钟
			 */
			try {
				while (!connected && timecount > 0) {
					Thread.sleep(unittime);
					timecount--;
					System.out.println("timecount: " + timecount);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.out.println("OutputPipe creation failure");
			e.printStackTrace();
		}
		System.out.println("-------=========End of TryToConnect()");
	}

	/**
	 * Creates the input pipe with this as the message listener
	 */
	public void TryToGetInputPipe() {
		try {
			System.out.println("Creating input pipe");
			// Create the InputPipe and register this for message arrival
			// notification call-back
			inputPipe = pipeService.createInputPipe(pipeAdv, this);
		} catch (IOException io) {
			io.printStackTrace();
			return;
		}
		if (inputPipe == null) {
			System.out.println(" cannot open InputPipe");
			System.exit(-1);
		}
		System.out.println("Waiting for msgs on input pipe");
	}

	/**
	 * Creates the pipe advertisement pipe ID
	 * 
	 * @return the pre-defined Pipe Advertisement
	 */
	private static PipeAdvertisement getPipeAdvertisement() {
		PipeID pipeID = null;
		// String PIPEIDSTR =
		// "urn:jxta:uuid-59616261646162614E50472050325033C0C1DE89719B456691A596B983BA0E1004";
		// pipeID = (PipeID) IDFactory.fromURI(new URI(PIPEIDSTR));
		pipeID = (PipeID) IDFactory.newPipeID(new NoxToolkit()
				.getNetworkManager().getNetPeerGroup().getPeerGroupID());
		
		PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory
				.newAdvertisement(PipeAdvertisement.getAdvertisementType());

		advertisement.setPipeID(pipeID);
		advertisement.setType(PipeService.UnicastType);
		advertisement.setName("NoX Wormhole");

		return advertisement;
	}

	/**
	 * Dumps the message content to stdout
	 * 
	 * @param msg
	 *            the message
	 * @param verbose
	 *            dumps message element content if true
	 */
	public void printMessageStats(Message msg, boolean verbose) {
		String inputMsg = "";
		try {
			CountingOutputStream cnt;
			ElementIterator it = msg.getMessageElements();

			System.out
					.println("------------------Begin Message---------------------");
			inputMsg += "------------------Begin Message---------------------^n";

			WireFormatMessage serialed = WireFormatMessageFactory.toWire(msg,
					new MimeMediaType("application/x-jxta-msg"), null);

			System.out.println("Message Size :" + serialed.getByteLength());
			inputMsg += ("Message Size :" + serialed.getByteLength() + "^n");

			while (it.hasNext()) {
				MessageElement el = it.next();
				String eName = el.getElementName();

				cnt = new CountingOutputStream(new DevNullOutputStream());
				el.sendToStream(cnt);
				long size = cnt.getBytesWritten();

				System.out.println("Element " + eName + " : " + size);
				inputMsg += ("Element " + eName + " : " + size + "^n");

				if (verbose) {
					System.out.println("[" + el + "]");
					inputMsg += ("[" + el + "]" + "^n");
				}
			}
			System.out
					.println("---------[F:100]----------End Message----------[F:035]------------");
			inputMsg += "---------[F:100]----------End Message----------[F:035]------------";
		} catch (Exception e) {
			e.printStackTrace();
		}
		String whoami = "ME";
		whoami = new NoxToolkit().getNetworkConfigurator().getName();
		String[] fullMsg = { "", "Someone", whoami, new Date().toString(),
				inputMsg };
		chatroompane.receiveMsgAndAccess(fullMsg);
	}

	/**
	 * Closes the output pipe and stops the platform
	 */
	@SuppressWarnings("unused")
	private void stop() {
		// Close the output pipe
		outputPipe.close();
		// lock.notify();
		inputPipe.close();
	}

	public boolean SendMsg(String strbuf_msg) {
		if (outputPipe == null)
			return false;
		Message msg;
		try {
			System.out.println("Sending message");
			// create the message
			msg = new Message();
			Date date = new Date(System.currentTimeMillis());
			// add a string message element with the current date
			StringMessageElement stns = new StringMessageElement(
					MESSAGE_TIME_NAME_SPACE, date.toString(), null);
			/*
			 * StringMessageElement bla = new StringMessageElement( "FUNNY",
			 * "Isn't it?", null); msg.addMessageElement(null, sme);
			 * msg.addMessageElement(null, bla);
			 */
			StringMessageElement mns = new StringMessageElement(
					MESSAGE_NAME_SPACE, strbuf_msg, null);

			msg.addMessageElement(null, stns);
			msg.addMessageElement(null, mns);
			// send the message
			outputPipe.send(msg);
			System.out.println("message sent");
		} catch (IOException e) {
			System.out.println("failed to send message");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void outputPipeEvent(OutputPipeEvent event) {
		connected = true;
		System.out.println("Received the output pipe resolution event");
		// get the output pipe object
		outputPipe = event.getOutputPipe();
		SendMsg("It's funny, huh?");
	}

	/**
	 * PipeMsgListener interface for asynchronous message arrival notification
	 * 
	 * @param event
	 *            the message event
	 */
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {
		Message msg;
		try {
			// Obtain the message from the event
			msg = event.getMessage();
			if (msg == null) {
				System.out.println("Received an empty message");
				return;
			}
			// dump the message content to screen
			printMessageStats(msg, true);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// get all the message elements
		Message.ElementIterator en = msg.getMessageElements();

		if (!en.hasNext()) {
			return;
		}

		// get the message element in the name space
		// PipeClient.MESSAGE_NAME_SPACE
		MessageElement msgElement = msg.getMessageElement(null,
				MESSAGE_TIME_NAME_SPACE);

		// Get message
		if (msgElement.toString() == null) {
			System.out.println("null msg received");
		} else {
			Date date = new Date(System.currentTimeMillis());
			System.out.println("Message received at :" + date.toString());
			System.out.println("Message  created at :" + msgElement.toString());
		}
	}
}

package noxUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.OutputPipeEvent;
import net.jxta.pipe.OutputPipeListener;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;

import net.nox.*;

/**
 * 
 * @author shinysky
 * 
 */
public class Chatroom extends NoxFrame implements OutputPipeListener {
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
	ChatRoomPane crp;
	protected InfiniteProgressPanel glassPane;
	boolean connected = false;

	private PipeService pipeService;
	private PipeAdvertisement pipeAdv;
	private OutputPipe outputPipe;
	private final Object lock = new Object();
	public final static String MESSAGE_NAME_SPACE = "PipeTutorial";

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

		crp = new ChatRoomPane(this);
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
		rootpane.add(crp);

		glassPane = new InfiniteProgressPanel("连接中, 请稍候...", 12);
		glassPane.setBounds(0, 0, WIDTH_PREF, HEIGHT_PREF - 100);
		glassPane.start();

		Thread connector = new Thread(new Runnable() {
			public void run() {
				/**
				 * 与该peer建立连接
				 */
				TryToConnect(friend.getUUID());
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

		this.setVisible(true);
	}

	public Chatroom(String title, GroupItem[] gmembers) {
		this(title);
		MultiChatRoomSidePane groupmembers = new MultiChatRoomSidePane(
				"Hello, everyone, happy everyday!", gmembers);
		rootpane.add(groupmembers);
		rootpane.add(crp);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
	}

	/**
	 * TODO 与该peer建立连接:
	 * 
	 * @return
	 */
	private void TryToConnect(String peerid) {
		// get the pipe service, and discovery
		pipeService = new NoxToolkit().getNetworkManager().getNetPeerGroup()
				.getPipeService();
		// create the pipe advertisement
		pipeAdv = getPipeAdvertisement();
		PeerID peerID = null;
		/*
		 * try { peerID = (PeerID) IDFactory.fromURI(new URI(peerid)); } catch
		 * (URISyntaxException use) { use.printStackTrace(); }
		 */
		System.out.println("Pipe Adv	: " + pipeAdv);
		System.out.println("Peer ID	: " + peerID);

		try {
			// issue a pipe resolution asynchronously. outputPipeEvent() is
			// called
			// once the pipe has resolved
			// pipeService.createOutputPipe(pipeAdv, (OutputPipeListener) this);
			pipeService.createOutputPipe(pipeAdv, this);
			/**
			 * 等待10秒钟
			 */
			try {
				synchronized (lock) {
					lock.wait(2000);
				}
			} catch (InterruptedException e) {
				System.out.println("Thread interrupted");
			}
		} catch (IOException e) {
			System.out.println("OutputPipe creation failure");
			e.printStackTrace();
		}
		System.out.println("-------=========End of TryToConnect()");
	}

	/**
	 * Creates the pipe advertisement pipe ID
	 * 
	 * @return the pre-defined Pipe Advertisement
	 */
	private static PipeAdvertisement getPipeAdvertisement() {
		PipeID pipeID = null;
		String PIPEIDSTR = "urn:jxta:uuid-59616261646162614E50472050325033C0C1DE89719B456691A596B983BA0E1004";

		try {
			pipeID = (PipeID) IDFactory.fromURI(new URI(PIPEIDSTR));
		} catch (URISyntaxException use) {
			use.printStackTrace();
		}
		PipeAdvertisement advertisement = (PipeAdvertisement) AdvertisementFactory
				.newAdvertisement(PipeAdvertisement.getAdvertisementType());

		advertisement.setPipeID(pipeID);
		advertisement.setType(PipeService.UnicastType);
		advertisement.setName("Pipe tutorial");

		return advertisement;
	}

	/**
	 * Closes the output pipe and stops the platform
	 */
	private void stop() {
		// Close the output pipe
		outputPipe.close();
		// lock.notify();
	}

	@Override
	public void outputPipeEvent(OutputPipeEvent event) {

		connected = true;
		/*
		 * try { lock.notify(); } catch (IllegalMonitorStateException exc) {
		 * exc.printStackTrace(); }
		 */

		System.out.println("Received the output pipe resolution event");
		// get the output pipe object
		outputPipe = event.getOutputPipe();

		Message msg;

		try {
			System.out.println("Sending message");
			// create the message
			msg = new Message();
			Date date = new Date(System.currentTimeMillis());
			// add a string message element with the current date
			StringMessageElement sme = new StringMessageElement(
					MESSAGE_NAME_SPACE, date.toString(), null);

			msg.addMessageElement(null, sme);
			// send the message
			outputPipe.send(msg);
			System.out.println("message sent");
		} catch (IOException e) {
			System.out.println("failed to send message");
			e.printStackTrace();
			System.exit(-1);
		}
		stop();
	}

	protected Container buildInfinitePanel() {

		return null;
	}
}

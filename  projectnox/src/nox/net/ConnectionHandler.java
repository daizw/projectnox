package nox.net;

import java.io.IOException;
import java.util.Date;

import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.ID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.util.JxtaBiDiPipe;
import nox.net.PeerChatroomUnit;
import nox.ui.PeerChatroom;
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
public class ConnectionHandler implements Runnable, PipeMsgListener {
	/**
	 * 实际上是双向管道, 即既可以收也可以发.
	 * 这里的命名只是为了强调发.
	 */
	private JxtaBiDiPipe outbidipipe;
	/**
	 * 对应的ChatroomUnit
	 */
	private PeerChatroomUnit roomunit;
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
	public ConnectionHandler(JxtaBiDiPipe pipe) {
		this.outbidipipe = pipe;
		outbidipipe.setMessageListener(this);
		//TODO register the chatroom.
		//if it has exist, then refresh the outpipe(?).
		//if not, handle the connection considering the condition.
		registerPipe(outbidipipe);
	}
	/**
	 * 负责建立和维护ID-Pipe对, 同时更新此处成员变量room.
	 * <ol>
	 * <li>如果对应pipe的ChatroomUnit不存在, 则注册一个对应的ID-Pipe对.</li>
	 * <li>如果存在则更新pipe.</li>
	 * </ol>
	 * <li>以上这两种情况都不会试图去实例化Chatroom.</li>
	 * 
	 * @param pipe
	 * @see PeerChatroomUnit
	 */
	private void registerPipe(JxtaBiDiPipe pipe) {
		ID roomID = pipe.getRemotePeerAdvertisement().getPeerID();
		roomunit = (PeerChatroomUnit)NoxToolkit.getChatroomUnit(roomID);
		if (roomunit == null)// 该ID对应的ChatroomUnit不存在
		{
			//注册该pipe
			System.out.println("The chatroom doesn't exist yet, I will register the pipe.");
			roomunit = NoxToolkit.registerChatroomUnit(roomID, pipe);
		}else{
			//重设pipe
			roomunit.setOutPipe(pipe);
		}
	}	
	/**
	 * {@inheritDoc}
	 */
	public void pipeMsgEvent(PipeMsgEvent event) {
		System.out.println("===Begin ConnectionHandler PipeMsgEvent()===");
		// TODO 处理消息
		// 做得细致的话, 应该消息分多种, 对不同消息调用不同处理函数.
		// grab the message from the event
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
		// TODO 这是在干嘛?
		if (null == senderEle.toString() || receiverEle.toString() == null
				|| timeEle.toString() == null || msgEle.toString() == null) {
			System.out
					.println("Msg.toString() is empty, it's weird even more.");
			return;
		}

		System.out.println("Connection-Handler got Message :"
				+ msgEle.toString());
		
		// TODO 将经过处理的消息传给对应的Chatroom.
		System.out.println("Trying to setup a chatroom...");
		//registerChatroom(senderIDEle, msg, true);
		//提示收到的消息
		promptIncomingMsg(msg);
		
		System.out.println("===End ConnectionHandler PipeMsgEvent()===");
	}

	/**
	 * 消息处理函数, 提示收到消息
	 * <ol>
	 * <li>如果聊天室已经存在, 则将消息传递给对应聊天室;</li>
	 * <li>如果聊天室尚不存在:
	 * <ol>
	 * <li>如果是好友的消息, 则(暂时)建立聊天室显示之.</li>
	 * <li>如果不是好友的消息, 则(暂时)将之添加为好友建立聊天室并显示之.</li>
	 * </ol>
	 * </ol>
	 * @param msg 收到的消息
	 */
	private void promptIncomingMsg(Message msg) {
		if (roomunit.getChatroom() != null){
			//传递消息
			roomunit.getChatroom().processIncomingMsg(msg, false);
			return;
		}else{
			//如果聊天室尚不存在:
			//如果是好友的消息, 则(暂时)建立聊天室显示之.
			//(应当)提示有新消息
			//如果不是好友的消息, 则(暂时)将之添加为好友建立聊天室并显示之.
			//然后注册该chatroom.
			
			//建立新聊天室
			PeerChatroom room = NoxToolkit.getCheyenne().setupNewChatroomOver(outbidipipe);
			//注册聊天室
			roomunit.setChatroom(room);
			//new NoxToolkit().registerChatroom(room.getRoomID(), room);
			//处理消息
			room.processIncomingMsg(msg, false);
		}
	}

	/**
	 * Send a greeting message over the pipe
	 * 
	 * @param bidipipe
	 *            the pipe to send messages over
	 * @throws IOException
	 *             Thrown for errors sending messages.
	 */
	private void sendGreetingMessages(JxtaBiDiPipe bidipipe) throws IOException {
		System.out.println("Sending greeting message...");
		// create the message
		Message msg = new Message();
		Date date = new Date(System.currentTimeMillis());
		// add a string message element with the current date
		String hellomsg = "Greetings! What's up? [F:100]\nIn ConnectionHandler sendGreetingMessages() from "
				+ NoxToolkit.getNetworkConfigurator().getName();

		StringMessageElement senderEle = new StringMessageElement(
				XmlMsgFormat.SENDER_ELEMENT_NAME, NoxToolkit.getNetworkConfigurator().getName(), null);
		StringMessageElement senderIDEle = new StringMessageElement(
				XmlMsgFormat.SENDERID_ELEMENT_NAME, NoxToolkit.getNetworkConfigurator().getPeerID().toString(), null);
		StringMessageElement receiverEle = new StringMessageElement(
				XmlMsgFormat.RECEIVER_ELEMENT_NAME, bidipipe.getRemotePeerAdvertisement().getName(), null);
		StringMessageElement receiverIDEle = new StringMessageElement(
				XmlMsgFormat.RECEIVERID_ELEMENT_NAME, bidipipe.getRemotePeerAdvertisement().getPeerID().toString(), null);
		StringMessageElement timeEle = new StringMessageElement(
				XmlMsgFormat.TIME_ELEMENT_NAME, date.toString(), null);
		StringMessageElement msgEle = new StringMessageElement(
				XmlMsgFormat.MESSAGE_ELEMENT_NAME, hellomsg, null);

		msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, senderEle);
		msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, senderIDEle);
		msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, receiverEle);
		msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, receiverIDEle);
		msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, timeEle);
		msg.addMessageElement(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, msgEle);

		bidipipe.sendMessage(msg);
	}

	/**
	 * Send greeting message when receive incoming connection.
	 * TODO comment this
	 */
	public void run() {
		try {
			sendGreetingMessages(outbidipipe);
		} catch (Throwable all) {
			all.printStackTrace();
		}
	}
}

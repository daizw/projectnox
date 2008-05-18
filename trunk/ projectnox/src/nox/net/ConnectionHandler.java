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
	 * ʵ������˫��ܵ�, ���ȿ�����Ҳ���Է�.
	 * ���������ֻ��Ϊ��ǿ����.
	 */
	private JxtaBiDiPipe outbidipipe;
	/**
	 * ��Ӧ��ChatroomUnit
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
	 * <li>�ù��캯����ϵͳ��⵽��������ʱ����.ע�Ტ������pipe, ������ʵ����chatroom</li>
	 * <li>�ڴ�֮��, �������������Ϣ����, ��Ѱ��/������Ӧ��chatroom, ������Ϣ���ݸ�chatroom</li>
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
	 * ��������ά��ID-Pipe��, ͬʱ���´˴���Ա����room.
	 * <ol>
	 * <li>�����Ӧpipe��ChatroomUnit������, ��ע��һ����Ӧ��ID-Pipe��.</li>
	 * <li>������������pipe.</li>
	 * </ol>
	 * <li>���������������������ͼȥʵ����Chatroom.</li>
	 * 
	 * @param pipe
	 * @see PeerChatroomUnit
	 */
	private void registerPipe(JxtaBiDiPipe pipe) {
		ID roomID = pipe.getRemotePeerAdvertisement().getPeerID();
		roomunit = (PeerChatroomUnit)NoxToolkit.getChatroomUnit(roomID);
		if (roomunit == null)// ��ID��Ӧ��ChatroomUnit������
		{
			//ע���pipe
			System.out.println("The chatroom doesn't exist yet, I will register the pipe.");
			roomunit = NoxToolkit.registerChatroomUnit(roomID, pipe);
		}else{
			//����pipe
			roomunit.setOutPipe(pipe);
		}
	}	
	/**
	 * {@inheritDoc}
	 */
	public void pipeMsgEvent(PipeMsgEvent event) {
		System.out.println("===Begin ConnectionHandler PipeMsgEvent()===");
		// TODO ������Ϣ
		// ����ϸ�µĻ�, Ӧ����Ϣ�ֶ���, �Բ�ͬ��Ϣ���ò�ͬ������.
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
		// TODO �����ڸ���?
		if (null == senderEle.toString() || receiverEle.toString() == null
				|| timeEle.toString() == null || msgEle.toString() == null) {
			System.out
					.println("Msg.toString() is empty, it's weird even more.");
			return;
		}

		System.out.println("Connection-Handler got Message :"
				+ msgEle.toString());
		
		// TODO �������������Ϣ������Ӧ��Chatroom.
		System.out.println("Trying to setup a chatroom...");
		//registerChatroom(senderIDEle, msg, true);
		//��ʾ�յ�����Ϣ
		promptIncomingMsg(msg);
		
		System.out.println("===End ConnectionHandler PipeMsgEvent()===");
	}

	/**
	 * ��Ϣ������, ��ʾ�յ���Ϣ
	 * <ol>
	 * <li>����������Ѿ�����, ����Ϣ���ݸ���Ӧ������;</li>
	 * <li>����������в�����:
	 * <ol>
	 * <li>����Ǻ��ѵ���Ϣ, ��(��ʱ)������������ʾ֮.</li>
	 * <li>������Ǻ��ѵ���Ϣ, ��(��ʱ)��֮���Ϊ���ѽ��������Ҳ���ʾ֮.</li>
	 * </ol>
	 * </ol>
	 * @param msg �յ�����Ϣ
	 */
	private void promptIncomingMsg(Message msg) {
		if (roomunit.getChatroom() != null){
			//������Ϣ
			roomunit.getChatroom().processIncomingMsg(msg, false);
			return;
		}else{
			//����������в�����:
			//����Ǻ��ѵ���Ϣ, ��(��ʱ)������������ʾ֮.
			//(Ӧ��)��ʾ������Ϣ
			//������Ǻ��ѵ���Ϣ, ��(��ʱ)��֮���Ϊ���ѽ��������Ҳ���ʾ֮.
			//Ȼ��ע���chatroom.
			
			//������������
			PeerChatroom room = NoxToolkit.getCheyenne().setupNewChatroomOver(outbidipipe);
			//ע��������
			roomunit.setChatroom(room);
			//new NoxToolkit().registerChatroom(room.getRoomID(), room);
			//������Ϣ
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

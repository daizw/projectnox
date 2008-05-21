package nox.net;

import java.io.IOException;

import net.jxta.endpoint.Message;
import net.jxta.id.ID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import nox.ui.chat.peer.PeerChatroom;
import nox.xml.NoxMsgUtil;
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
		//TODO register the chatroom.
		//if it has exist, then refresh the outpipe(?).
		//if not, handle the connection considering the condition.
		registerPipe(outbidipipe);
		outbidipipe.setMessageListener(this);
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
		// add a string message element with the current date
		String hellomsg = "Greetings! What's up? [F:100]\nIn ConnectionHandler sendGreetingMessages() from "
				+ NoxToolkit.getNetworkConfigurator().getName();

		PeerAdvertisement adv = bidipipe.getRemotePeerAdvertisement();
		
		Message msg = NoxMsgUtil.generateMsg(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				NoxToolkit.getNetworkConfigurator().getName(),
				NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
				adv.getName(), adv.getPeerID().toString(),
				hellomsg.getBytes());

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

package net.nox;

import java.io.IOException;
import java.util.Date;

import xml.nox.XmlMsgFormat;

import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import noxUI.GroupChatroom;

public class GroupConnectionHandler implements Runnable, PipeMsgListener{
	PeerGroup peergroup;
	InputPipe inpipe = null;
	OutputPipe outpipe = null;
	int waittime = 5*1000;
	
	/**
	 * ��Ӧ��ChatroomUnit
	 */
	private GroupChatroomUnit roomunit;

	public GroupConnectionHandler(PeerGroup pg){
		this.peergroup = pg;
		PipeAdvertisement pia = null;
		pia = PipeUtil.findPipeAdv(pg, pg.getPeerGroupID().toString());
		if(pia == null){
			System.out.println("Failed to find or create a pipe adv, it's a fatal error");
			return;
		}
		System.out.println("Creating Propagated InputPipe for pipe: " + pia.getPipeID());
        try {
            inpipe = pg.getPipeService().createInputPipe(pia, this);
            outpipe = pg.getPipeService().createOutputPipe(pia, waittime);
        } catch (IOException e) {
        	System.out.println("Failed to create Propagated InputPipe for pipe: " + pia.getPipeID());
            e.printStackTrace();
            System.exit(-1);
        }
        if(inpipe != null || outpipe != null){
        	registerPipe(inpipe, outpipe);
        }
	}
	/**
	 * ��������ά��ID-Pipe��, ͬʱ���´˴���Ա����room.
	 * <ol>
	 * <li>�����Ӧpipe��ChatroomUnit������, ��ע��һ����Ӧ��ID-Pipe��.</li>
	 * <li>������������pipe.</li>
	 * </ol>
	 * <li>���������������������ͼȥʵ����Chatroom.</li>
	 * 
	 * @param ipipe
	 * @param opipe
	 */
	private void registerPipe(InputPipe ipipe, OutputPipe opipe) {
		ID roomID = peergroup.getPeerGroupID();
		roomunit = (GroupChatroomUnit)NoxToolkit.getChatroomUnit(roomID);
		if (roomunit == null)// ��ID��Ӧ��ChatroomUnit������
		{
			//ע���pipe
			System.out.println("The chatroom doesn't exist yet, I will register the pipe.");
			roomunit = NoxToolkit.registerChatroomUnit(roomID, ipipe, opipe);
		}else{
			//����pipe
			roomunit.setIOPipe(ipipe, opipe);
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
	private void sendGreetingMessages(OutputPipe outpipe) throws IOException {
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
				XmlMsgFormat.RECEIVER_ELEMENT_NAME, peergroup.getPeerGroupName(), null);
		StringMessageElement receiverIDEle = new StringMessageElement(
				XmlMsgFormat.RECEIVERID_ELEMENT_NAME, peergroup.getPeerGroupID().toString(), null);
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

		outpipe.send(msg);
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
			GroupChatroom room = NoxToolkit.getCheyenne().setupNewChatroomOver(peergroup, inpipe, outpipe);
			//ע��������
			roomunit.setChatroom(room);
			//new NoxToolkit().registerChatroom(room.getRoomID(), room);
			//������Ϣ
			room.processIncomingMsg(msg, false);
		}
	}

	@Override
	public void run() {
		try {
			if(outpipe != null)
				sendGreetingMessages(outpipe);
		} catch (Throwable all) {
			all.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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
		if(senderIDEle.toString().equals(NoxToolkit.getNetworkConfigurator().getPeerID().toString())){
			//�Լ�������Ϣ, ����֮
			return;
		}
		//��ʾ�յ�����Ϣ
		promptIncomingMsg(msg);
		
		System.out.println("===End ConnectionHandler PipeMsgEvent()===");
	}

}

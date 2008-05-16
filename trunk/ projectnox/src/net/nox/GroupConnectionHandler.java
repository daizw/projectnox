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
	 * 对应的ChatroomUnit
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
	 * 负责建立和维护ID-Pipe对, 同时更新此处成员变量room.
	 * <ol>
	 * <li>如果对应pipe的ChatroomUnit不存在, 则注册一个对应的ID-Pipe对.</li>
	 * <li>如果存在则更新pipe.</li>
	 * </ol>
	 * <li>以上这两种情况都不会试图去实例化Chatroom.</li>
	 * 
	 * @param ipipe
	 * @param opipe
	 */
	private void registerPipe(InputPipe ipipe, OutputPipe opipe) {
		ID roomID = peergroup.getPeerGroupID();
		roomunit = (GroupChatroomUnit)NoxToolkit.getChatroomUnit(roomID);
		if (roomunit == null)// 该ID对应的ChatroomUnit不存在
		{
			//注册该pipe
			System.out.println("The chatroom doesn't exist yet, I will register the pipe.");
			roomunit = NoxToolkit.registerChatroomUnit(roomID, ipipe, opipe);
		}else{
			//重设pipe
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
			GroupChatroom room = NoxToolkit.getCheyenne().setupNewChatroomOver(peergroup, inpipe, outpipe);
			//注册聊天室
			roomunit.setChatroom(room);
			//new NoxToolkit().registerChatroom(room.getRoomID(), room);
			//处理消息
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
		if(senderIDEle.toString().equals(NoxToolkit.getNetworkConfigurator().getPeerID().toString())){
			//自己发的消息, 忽略之
			return;
		}
		//提示收到的消息
		promptIncomingMsg(msg);
		
		System.out.println("===End ConnectionHandler PipeMsgEvent()===");
	}

}

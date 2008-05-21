package nox.net;

import java.io.IOException;
import java.util.Date;


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
import nox.ui.chat.group.GroupChatroom;
import nox.xml.NoxMsgUtil;
import nox.xml.XmlMsgFormat;

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
		String hellomsg = "Greetings! What's up? [F:100]\nIn ConnectionHandler sendGreetingMessages() from "
				+ NoxToolkit.getNetworkConfigurator().getName();

		Message msg = NoxMsgUtil.generateMsg(XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				NoxToolkit.getNetworkConfigurator().getName(),
				NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
				peergroup.getPeerGroupName(), peergroup.getPeerGroupID().toString(),
				hellomsg.getBytes());
		
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
			GroupChatroom room = NoxToolkit.getCheyenne().setupNewChatroomOver(peergroup.getPeerGroupAdvertisement(), inpipe, outpipe);
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
		System.out.println("===Begin GroupConnectionHandler PipeMsgEvent()===");
		// TODO 处理消息
		// 做得细致的话, 应该消息分多种, 对不同消息调用不同处理函数.
		// grab the message from the event
		Message msg = event.getMessage();

		System.out.println("Incoming call: " + msg.toString());

		// TODO 将经过处理的消息传给对应的Chatroom.
		System.out.println("Trying to pass the msg to the chatroom...");
		//提示收到的消息
		promptIncomingMsg(msg);
		
		System.out.println("===End GroupConnectionHandler PipeMsgEvent()===");
	}

}

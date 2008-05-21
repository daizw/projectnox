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
			GroupChatroom room = NoxToolkit.getCheyenne().setupNewChatroomOver(peergroup.getPeerGroupAdvertisement(), inpipe, outpipe);
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
		System.out.println("===Begin GroupConnectionHandler PipeMsgEvent()===");
		// TODO ������Ϣ
		// ����ϸ�µĻ�, Ӧ����Ϣ�ֶ���, �Բ�ͬ��Ϣ���ò�ͬ������.
		// grab the message from the event
		Message msg = event.getMessage();

		System.out.println("Incoming call: " + msg.toString());

		// TODO �������������Ϣ������Ӧ��Chatroom.
		System.out.println("Trying to pass the msg to the chatroom...");
		//��ʾ�յ�����Ϣ
		promptIncomingMsg(msg);
		
		System.out.println("===End GroupConnectionHandler PipeMsgEvent()===");
	}

}

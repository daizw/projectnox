package net.nox;

import net.jxta.id.ID;
import net.jxta.util.JxtaBiDiPipe;
import noxUI.SingleChatroom;

/**
 * Chatroom��Ԫ�ṹ,
 * ���ڱ���roomID, outpipe, ��chatroom�Ķ�Ӧ��ϵ.
 * 
 * @author shinysky
 *
 */
public class ChatroomUnit{
	private ID roomID = null;
	private JxtaBiDiPipe outbidipipe = null;
	private SingleChatroom room = null;
	
	public ChatroomUnit(ID id, JxtaBiDiPipe pipe){
		this(id, pipe, null);
	}
	public ChatroomUnit(ID id, JxtaBiDiPipe pipe, SingleChatroom rm){
		roomID = id;
		outbidipipe = pipe;
		room = rm;
	}
	public ID getRoomID(){
		return roomID;
	}
	public JxtaBiDiPipe getOutPipe(){
		return outbidipipe;
	}
	/**
	 * ����ChatroomUnit��outbidipipe, ��ͬ������Chatroom(�������)��outbidipipe
	 * @param pipe
	 */
	public void setOutPipe(JxtaBiDiPipe pipe){
		outbidipipe = pipe;
		//ͬ��room��outpipe
		if(room != null)
			room.setOutBidipipe(pipe);
	}
	public SingleChatroom getChatroom(){
		return room;
	}
	public void setChatroom(SingleChatroom rm){
		room = rm;
	}
}
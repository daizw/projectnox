package net.nox;

import net.jxta.id.ID;
import net.jxta.util.JxtaBiDiPipe;
import noxUI.SingleChatroom;

/**
 * Chatroom单元结构,
 * 用于保存roomID, outpipe, 和chatroom的对应关系.
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
	 * 设置ChatroomUnit的outbidipipe, 并同步更新Chatroom(如果存在)的outbidipipe
	 * @param pipe
	 */
	public void setOutPipe(JxtaBiDiPipe pipe){
		outbidipipe = pipe;
		//同步room的outpipe
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
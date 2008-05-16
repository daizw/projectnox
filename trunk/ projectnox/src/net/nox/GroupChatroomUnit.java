package net.nox;

import net.jxta.id.ID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import noxUI.Chatroom;
import noxUI.GroupChatroom;

public class GroupChatroomUnit implements ChatroomUnit{
	private ID roomID = null;
	private InputPipe inpipe = null;
	private OutputPipe outpipe = null;
	private GroupChatroom room = null;
	
	public GroupChatroomUnit(ID id, InputPipe inpipe, OutputPipe outpipe){
		this(id, inpipe, outpipe, null);
	}
	public GroupChatroomUnit(ID id, InputPipe inpipe, OutputPipe outpipe, GroupChatroom rm){
		this.roomID = id;
		this.inpipe = inpipe;
		this.room = rm;
	}
	public ID getRoomID(){
		return roomID;
	}
	public InputPipe getInPipe(){
		return inpipe;
	}
	public OutputPipe getOutPipe(){
		return outpipe;
	}
	/**
	 * 设置ChatroomUnit的inpipe, TODO 同步更新Chatroom(如果存在)的inpipe
	 * @param pipe
	 */
	public void setInPipe(InputPipe pipe){
		inpipe = pipe;
		//同步room的outpipe
		/*if(room != null)
			room.setOutPipe(pipe);*/
	}
	/**
	 * 设置ChatroomUnit的outpipe, TODO 同步更新Chatroom(如果存在)的outpipe
	 * @param pipe
	 */
	public void setOutPipe(OutputPipe pipe){
		outpipe = pipe;
		//同步room的outpipe
		/*if(room != null)
			room.setOutPipe(pipe);*/
	}
	/**
	 * 设置I/O pipe
	 * @param ipipe inputpipe
	 * @param opipe outputpipe
	 */
	public void setIOPipe(InputPipe ipipe, OutputPipe opipe) {
		setInPipe(ipipe);
		setOutPipe(opipe);
	}
	@Override
	public GroupChatroom getChatroom(){
		return room;
	}
	@Override
	public void setChatroom(Chatroom rm) {
		room = (GroupChatroom)rm;
	}
}

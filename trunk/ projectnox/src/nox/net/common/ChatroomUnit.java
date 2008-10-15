package nox.net.common;

import net.jxta.id.ID;
import nox.ui.chat.common.Chatroom;
/**
 * 
 * @author shinysky
 * @deprecated
 */
public interface ChatroomUnit {
	public abstract ID getRoomID();
	public abstract Chatroom getChatroom();
	public abstract void setChatroom(Chatroom rm);
}
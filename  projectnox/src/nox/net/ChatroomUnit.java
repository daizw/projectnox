package nox.net;

import net.jxta.id.ID;
import nox.ui.chat.common.Chatroom;

public interface ChatroomUnit {
	public abstract ID getRoomID();
	public abstract Chatroom getChatroom();
	public abstract void setChatroom(Chatroom rm);
}

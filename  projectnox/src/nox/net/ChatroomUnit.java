package nox.net;

import net.jxta.id.ID;
import nox.ui.Chatroom;

public interface ChatroomUnit {
	public abstract ID getRoomID();
	public abstract Chatroom getChatroom();
	public abstract void setChatroom(Chatroom rm);
}

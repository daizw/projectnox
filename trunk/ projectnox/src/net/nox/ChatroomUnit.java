package net.nox;

import net.jxta.id.ID;
import noxUI.Chatroom;

public interface ChatroomUnit {
	public abstract ID getRoomID();
	public abstract Chatroom getChatroom();
	public abstract void setChatroom(Chatroom rm);
}

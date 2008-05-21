package nox.ui.common;

import java.io.Serializable;

public enum ItemStatus implements Serializable{
	ONLINE(),//在线
	OFFLINE(),//离线
	INVISIBLE(),//隐身
	UNAVAILABLE(),//离开
	BUSY(),//忙碌
	UNKNOWN;//未知
	
	public static final String OnlineStr = "Online";
	public static final String OfflineStr = "Offline";
	public static final String InvisibleStr = "Invisible";
	public static final String UnavailableStr = "Unavailable";
	public static final String BusyStr = "Busy";
	public static final String UnknownStr = "Unknown";

	ItemStatus(){
    }
}

package nox.ui.common;

public enum ItemStatus{
	ONLINE(),//����
	OFFLINE(),//����
	INVISIBLE(),//����
	UNAVAILABLE(),//�뿪
	BUSY(),//æµ
	UNKNOWN;//δ֪
	
	public static final String OnlineStr = "Online";
	public static final String OfflineStr = "Offline";
	public static final String InvisibleStr = "Invisible";
	public static final String UnavailableStr = "Unavailable";
	public static final String BusyStr = "Busy";
	public static final String UnknownStr = "Unknown";

	ItemStatus(){
    }
}

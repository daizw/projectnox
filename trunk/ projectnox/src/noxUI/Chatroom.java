package noxUI;

import java.awt.image.BufferedImage;

import javax.swing.JSplitPane;

import net.jxta.id.ID;

@SuppressWarnings("serial")
public abstract class Chatroom extends NoxFrame{
	/**
	 * 默认尺寸常量
	 */
	public static final int WIDTH_DEFLT = 700;
	public static final int WIDTH_PREF = 700;
	public static final int WIDTH_MAX = 2000;
	public static final int WIDTH_MIN = 300;
	public static final int HEIGHT_DEFLT = 500;
	public static final int HEIGHT_PREF = 500;
	public static final int HEIGHT_MAX = 2000;
	public static final int HEIGHT_MIN = 200;

	public static final int PRIVATE_CHATROOM = 0;
	public static final int GROUP_CHATROOM = 1;
	
	protected JSplitPane rootpane;
	protected ChatRoomPane chatroompane;
	
	/**
	 * 私聊: 该值为对方ID;
	 * 群聊:为组ID
	 */
	protected ID roomID;
	protected String roomname;
	
	Chatroom(String title, String path_background, String path_logo,
			String path_logo_big, String path_title, boolean IAmBase) {
		super(title, path_background, path_logo, path_logo_big, path_title, IAmBase);
		// TODO Auto-generated constructor stub
	}
	/**
	 * TODO comment this method
	 * @return room ID
	 */
	public ID getRoomID() {
		return roomID;
	}

	public String getRoomName() {
		return roomname;
	}
	public abstract boolean SendMsg(String strmsg, BufferedImage bufImg);
}

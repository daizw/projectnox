package nox.ui.chat.common;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JSplitPane;

import net.jxta.id.ID;
import nox.ui.common.NoxFrame;

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
	protected ChatroomPane chatroompane;
	
	/**
	 * 私聊: 该值为对方ID;
	 * 群聊:为组ID
	 */
	protected ID roomID;
	protected String roomname;
	
	protected Chatroom(String title, String path_background, String path_logo,
			String path_logo_big, boolean IAmBase) {
		super(title + " - NoX Chatroom", path_background, path_logo, path_logo_big, title, IAmBase);

		roomname = title;
		this.setBounds(100, 80, WIDTH_DEFLT, HEIGHT_DEFLT);
		this.setSize(new Dimension(WIDTH_DEFLT, HEIGHT_DEFLT));
		this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));

		chatroompane = new ChatroomPane(this);
		// crp.setLayout(new FlowLayout());
		rootpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rootpane.setOneTouchExpandable(true);
		// rootpane.setDividerLocation(0.2f);
		rootpane.setDividerLocation(0f);
		rootpane.setDividerSize(8);
		rootpane.setResizeWeight(0.2d);
	}
	/**
	 * 获取聊天室名称(对方昵称或组名)
	 * @return 对方昵称或组名
	 */
	public String getRoomName(){
		return roomname;
	}
	public ID getRoomID(){
		return roomID;
	}
	/**
	 * 向外发送文本消息
	 * 
	 * @param strmsg
	 *            string msg
	 * @return succeed or not
	 */
	public abstract boolean SendMsg(String strmsg, boolean encrypt);
	/**
	 * 向外发送图片
	 * 
	 * @param bufImg
	 *            buffered image
	 * @return succeed or not
	 */
	public abstract boolean SendMsg(BufferedImage bufImg, boolean encrypt);
	/**
	 * 向外发送文件
	 * 
	 * @param file
	 *            file to be sent out
	 * @return succeed or not
	 */
	public abstract boolean SendMsg(File file, boolean encrypt);
}
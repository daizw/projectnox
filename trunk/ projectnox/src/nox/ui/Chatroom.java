package nox.ui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JSplitPane;

import net.jxta.endpoint.Message;
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
	protected ChatroomPane chatroompane;
	
	public static final int UnitWaitTime = 500;		// 远程发现的等待时间，需根据网络情况调整
	public static final int MAXRETRIES = 5;		// 远程发现时的重试次数，需根据网络情况调整
	
	/**
	 * 私聊: 该值为对方ID;
	 * 群聊:为组ID
	 */
	protected ID roomID;
	protected String roomname;
	
	Chatroom(String title, String path_background, String path_logo,
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
	 * TODO comment this method
	 * @return room ID
	 */
	public ID getRoomID() {
		return roomID;
	}

	public String getRoomName() {
		return roomname;
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
	/**
	 * 向外发送字节数组, 实际是供SendMsg(string/image/file)调用.<br>
	 * 当然, 也可独立使用.<br>
	 * 
	 * @param namespace 消息namespace, 表明消息数据类型(text/image/file)
	 * @param data
	 * @return
	 */
	public abstract boolean SendMsg(String namespace, byte[] data, boolean encrypt);

	/**
	 * 从指定的namespace中解析出数据, 然后传递给chatroompane处理.
	 * @param namespace
	 * @param msg
	 * @return 是否成功, 是否正常
	 * @deprecated 暂时用不到
	 */
	public abstract boolean ExtractDataAndProcess(String namespace, Message msg);
}

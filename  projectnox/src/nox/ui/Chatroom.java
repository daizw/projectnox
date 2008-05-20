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
	 * Ĭ�ϳߴ糣��
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
	
	public static final int UnitWaitTime = 500;		// Զ�̷��ֵĵȴ�ʱ�䣬����������������
	public static final int MAXRETRIES = 5;		// Զ�̷���ʱ�����Դ���������������������
	
	/**
	 * ˽��: ��ֵΪ�Է�ID;
	 * Ⱥ��:Ϊ��ID
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
	 * ���ⷢ���ı���Ϣ
	 * 
	 * @param strmsg
	 *            string msg
	 * @return succeed or not
	 */
	public abstract boolean SendMsg(String strmsg, boolean encrypt);
	/**
	 * ���ⷢ��ͼƬ
	 * 
	 * @param bufImg
	 *            buffered image
	 * @return succeed or not
	 */
	public abstract boolean SendMsg(BufferedImage bufImg, boolean encrypt);
	/**
	 * ���ⷢ���ļ�
	 * 
	 * @param file
	 *            file to be sent out
	 * @return succeed or not
	 */
	public abstract boolean SendMsg(File file, boolean encrypt);
	/**
	 * ���ⷢ���ֽ�����, ʵ���ǹ�SendMsg(string/image/file)����.<br>
	 * ��Ȼ, Ҳ�ɶ���ʹ��.<br>
	 * 
	 * @param namespace ��Ϣnamespace, ������Ϣ��������(text/image/file)
	 * @param data
	 * @return
	 */
	public abstract boolean SendMsg(String namespace, byte[] data, boolean encrypt);

	/**
	 * ��ָ����namespace�н���������, Ȼ�󴫵ݸ�chatroompane����.
	 * @param namespace
	 * @param msg
	 * @return �Ƿ�ɹ�, �Ƿ�����
	 * @deprecated ��ʱ�ò���
	 */
	public abstract boolean ExtractDataAndProcess(String namespace, Message msg);
}
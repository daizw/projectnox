package nox.net.common;

import java.awt.image.BufferedImage;
import java.io.File;

import net.jxta.endpoint.Message;
import net.jxta.id.ID;

public interface ConnectionHandler {
	/**
	 * TODO comment this method
	 * 获取对方ID(PeerID/GroupID)
	 * @return room ID
	 */
	public abstract ID getRoomID();

	/**
	 * 获取聊天室名称(对方昵称或组名)
	 * @return 对方昵称或组名
	 */
	public abstract String getRoomName();
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

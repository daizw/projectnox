package nox.net.common;

import java.awt.image.BufferedImage;
import java.io.File;

import net.jxta.endpoint.Message;
import net.jxta.id.ID;

public interface ConnectionHandler {
	/**
	 * TODO comment this method
	 * ��ȡ�Է�ID(PeerID/GroupID)
	 * @return room ID
	 */
	public abstract ID getRoomID();

	/**
	 * ��ȡ����������(�Է��ǳƻ�����)
	 * @return �Է��ǳƻ�����
	 */
	public abstract String getRoomName();
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

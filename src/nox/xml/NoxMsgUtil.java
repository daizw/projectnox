package nox.xml;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.StringMessageElement;

public class NoxMsgUtil {
	/**
	 * 根据所给参数生成消息
	 * @param namespace 消息namespace, 表明消息类型
	 * @param sender
	 * @param senderID
	 * @param receiver
	 * @param receiverID
	 * @param dataEleName 消息主体元素名
	 * @param data 消息主体数据
	 * @param param 加密所用参数
	 * @return 生成的消息
	 */
	public static Message generateMsg(String namespace,
			String sender, String senderID,
			String receiver, String receiverID,
			//String time, String dataEleName,
			byte[] data,
			byte[] param){
		Message msg = new Message();
		
		System.out.println("Generating message\n");
		// create the message
		msg = new Message();
		long date = System.currentTimeMillis();
		// add a string message element with the current date
		StringMessageElement senderEle = new StringMessageElement(
				XmlMsgFormat.SENDER_ELEMENT_NAME, sender, null);
		StringMessageElement senderIDEle = new StringMessageElement(
				XmlMsgFormat.SENDERID_ELEMENT_NAME, senderID, null);
		StringMessageElement receiverEle = new StringMessageElement(
				XmlMsgFormat.RECEIVER_ELEMENT_NAME, receiver, null);
		StringMessageElement receiverIDEle = new StringMessageElement(
				XmlMsgFormat.RECEIVERID_ELEMENT_NAME, receiverID, null);
		StringMessageElement timeEle = new StringMessageElement(
				XmlMsgFormat.TIME_ELEMENT_NAME, ""+date, null);
		/*StringMessageElement msgEle = new StringMessageElement(
				XmlMsgFormat.MESSAGE_ELEMENT_NAME, strmsg, null);*/
		ByteArrayMessageElement dataEle = new ByteArrayMessageElement(
				XmlMsgFormat.DATA_ELEMENT_NAME, MimeMediaType.AOS, data, null);
		
		msg.addMessageElement(namespace, senderEle);
		msg.addMessageElement(namespace, senderIDEle);
		msg.addMessageElement(namespace, receiverEle);
		msg.addMessageElement(namespace, receiverIDEle);
		msg.addMessageElement(namespace, timeEle);
		//msg.addMessageElement(namespace, msgEle);
		msg.addMessageElement(namespace, dataEle);
		
		if(param != null){
			ByteArrayMessageElement paramEle = new ByteArrayMessageElement(
					XmlMsgFormat.PARAMENC_ELEMENT_NAME, MimeMediaType.AOS, param, null);
			msg.addMessageElement(namespace, paramEle);
		}
		
		return msg;
	}
	/**
	 * 根据所给参数生成消息
	 * @param namespace 消息namespace, 表明消息类型
	 * @param sender
	 * @param senderID
	 * @param receiver
	 * @param receiverID
	 * @param time
	 * @param dataEleName 消息主体元素名
	 * @param data 消息主体数据
	 * @return 生成的消息
	 */
	public static Message generateMsg(String namespace,
			String sender, String senderID,
			String receiver, String receiverID,
			//String time,
			//String dataEleName,
			byte[] data){
		return generateMsg(namespace, sender, senderID, receiver, receiverID,
				//time, dataEleName,
				data, null);
	}
	
    /**
     * Returns the contents of the file in a byte array.
     * {@link http://exampledepot.com/egs/java.io/File2ByteArray.html}
     */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        	System.out.println("文件尺寸过大, 取消...");
        	return null;
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * 把字节数组保存为一个文件
     * @Author Sean.guo
     * @EditTime 2007-8-13 上午11:45:56
     * @ReEditBy shinysky
     */
    public static File getFileFromBytes(byte[] bytes, String outputFile) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }
    /**
     * 从字节数组获取对象
     * @Author Sean.guo
     * @EditTime 2007-8-13 上午11:46:34
     * @ReEditBy shinysky
     */
    public static Object getObjectFromBytes(byte[] objBytes) throws Exception {
        if (objBytes == null || objBytes.length == 0) {
            return null;
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(objBytes);
        ObjectInputStream oi = new ObjectInputStream(bi);
        return oi.readObject();
    }

    /**
     * 从对象获取一个字节数组
     * @Author Sean.guo
     * @EditTime 2007-8-13 上午11:46:56
     * @ReEditBy shinysky
     */
    public static byte[] getBytesFromObject(Serializable obj) throws Exception {
        if (obj == null) {
            return null;
        }
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);
        return bo.toByteArray();
    }
}
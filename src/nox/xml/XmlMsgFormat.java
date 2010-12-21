package nox.xml;

/**
 * 消息标签常量</p>
 * 消息的一般格式为:<br>
 * <code>
 * &ltnamespace><br>
 * &ltsender/><br>
 * &ltsenderID/><br>
 * &ltreceiver/><br>
 * &ltreceiverID/><br>
 * &lttime/><br>
 * <b>&ltdata/></b><br>
 * &ltparam/><br>
 * &lt/namespace><br>
 * </code></p>
 * 其中:<br>
 * <ol>
 * <li>消息主体部分是<b>&ltdata/></b>, 数据类型取决于&ltnamespace>的值.</li>
 * <li>&ltparam/>是可选的, 当传递的是加密(DES/CBC)消息时才是必要的.
 * 如果消息中不含此元素, 则视之为未加密消息.</li>
 * </ol><br>
 * @author shinysky
 *
 */
public class XmlMsgFormat {
	/**
	 * PING/PONG msg namespace name
	 */
	public final static String PINGMSG_NAMESPACE_NAME = "NoXPingMsg";
	public final static String PONGMSG_NAMESPACE_NAME = "NoXPongMsg";
	
	/**
	 * Text/Picture/File msg namespace name 
	 */
	public final static String MESSAGE_NAMESPACE_NAME = "NoXMessage";
	public final static String PICTUREMSG_NAMESPACE_NAME = "NoXPictureMsg";
	public final static String FILEMSG_NAMESPACE_NAME = "NoXFileMsg";
	
	/**
	 * encoded public key
	 */
	public final static String PUBLICKEYENC_NAMESPACE_NAME = "NoXPublicKey";
	/**
	 * returned encoded public key
	 */
	public final static String PUBLICKEYENC2_NAMESPACE_NAME = "NoXPublicKey2";
	
	/**
	 * msg elements name
	 */
	public final static String SENDER_ELEMENT_NAME = "SENDER";
	public final static String SENDERID_ELEMENT_NAME = "SENDERID";
	public final static String RECEIVER_ELEMENT_NAME = "RECEIVER";
	public final static String RECEIVERID_ELEMENT_NAME = "RECEIVERID";
	public final static String TIME_ELEMENT_NAME = "TIME";
	
	public final static String DATA_ELEMENT_NAME = "DATA";
	
	/*public final static String MESSAGE_ELEMENT_NAME = "MSG";
	public final static String PICTURE_ELEMENT_NAME = "PICTURE";
	public final static String FILE_ELEMENT_NAME = "FILE";
	public final static String RESPONSE_ELEMENT_NAME = "RSPON";
	public final static String PUBLICKEYENC_ELEMENT_NAME = "PUBKEY";*/
	
	public final static String PARAMENC_ELEMENT_NAME = "PARAM";
}
package nox.net.peer;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.WireFormatMessage;
import net.jxta.endpoint.WireFormatMessageFactory;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.id.ID;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;
import net.jxta.util.JxtaBiDiPipe;
import nox.net.common.ConnectionHandler;
import nox.net.common.LANTimeLimit;
import nox.net.common.NoxToolkit;
import nox.net.common.PipeUtil;
import nox.ui.chat.common.SlideInNotification;
import nox.ui.chat.peer.PeerChatroom;
import nox.ui.common.PeerItem;
import nox.ui.common.SystemPath;
import nox.xml.NoxFileUnit;
import nox.xml.NoxMsgUtil;
import nox.xml.NoxPeerStatusUnit;
import nox.xml.XmlMsgFormat;

/**
 * This is the server (receiver) side of the Bi-directional Pipe<p/>
 * This class does the following :
 * <ol>
 * <li>Open the received outpipe.</li>
 * <li>Sends {@code greeting} messages to the connection.</li>
 * <li>Waits responses.</li>
 * <li>For each incoming message does the following:
 * <ol>
 * <li>Identify if the caller is a friend, if so, setup a chatroom, and show the message.</li>
 * <li>If no, just ignore it, or add him/her to the friend list, depends on the configuration of the user.</li>
 * </ol>
 * </li>
 * </ol>
 * 
 */
public class PeerConnectionHandler implements ConnectionHandler, Runnable, PipeMsgListener {
	/**
	 * ʵ������˫��ܵ�, ���ȿ�����Ҳ���Է�.
	 * ���������ֻ��Ϊ��ǿ����.
	 */
	private JxtaBiDiPipe outbidipipe;
	/**
	 * ��Ӧ��Chatroom
	 */
	private PeerChatroom room = null;
	/*private String roomname = "";
	private PeerID roomID;*/
	private PeerItem friend;
	
	private SecretKey DESKey = null;
	private boolean exchangingPubKeys = false;
	private boolean waiting4PubKey2 = false;
	private PublicKey bobPubKey = null;
	
	/**
	 * ���캯��, ���ݵڶ������������Ƿ�ʵ�������촰��<br>
	 * <ol>
	 * <li>�������ʵ��������, �����ӹ����ڵ���һ���߳�������(�첽)</li>
	 * <li>���������ʵ��������, �����ӹ�����������(ͬ��)</li>
	 * </ol>
	 * @param friend �б��жԷ���PeerItem
	 * @param showChatroom �Ƿ�����ʵ��������
	 * @throws Exception ��һ������Ϊ���쳣
	 */
	public PeerConnectionHandler(final PeerItem friend, boolean showChatroom) throws Exception{
		if(friend == null){
			throw new Exception("PeerConnectionHandler ��ʼ��PeerItem����Ϊ��.");
		}
		this.friend = friend;
		
		if(showChatroom){
			//��ʾ�����Ҵ���, ����.
			//�ɹ�: ע�Ტ����
			//��ʱ(���ɹ�):��ʾ�û����ӳ�ʱ, �Ƿ�����!
			room = new PeerChatroom(friend, PeerConnectionHandler.this);
			room.setVisible(true);
			Thread connector = new Thread(new Runnable(){
				@Override
				public void run() {
					boolean retry = true;
					while(retry){
						TryToConnect(LANTimeLimit.UNIT_TIME * LANTimeLimit.CONNECT_MAXRETRIES);
						if(outbidipipe == null){
							retry = room.showTimeOutMsg();
						} else {
							System.out.println("���ӳɹ�!!");
							//TryToConnect()���Զ�ע��ID-connectionHandler������
							room.setVisible(false);
							room.removeMask();
							room.setVisible(true);
							break;
						}
					}
				}
			});
			connector.start();
		} else {
			//����ʾ�����Ҵ���, ֻ����.
			//�ɹ�: ע�Ტ����
			TryToConnect(LANTimeLimit.UNIT_TIME * LANTimeLimit.CONNECT_MAXRETRIES);
		}
	}
	/**
	 * ���캯��, ���û�˫������������б�ʱ���ô˹��캯��.<br>
	 * ֱ����ʾ���촰��, ����ͼ����<br>
	 * @param friend �б��жԷ���PeerItem
	 * @throws Exception ����Ϊ���쳣
	 */
	public PeerConnectionHandler(final PeerItem friend) throws Exception {
		this(friend, true);
	}
	
	/**
	 * <ol>
	 * <li>Constructor for the ConnectionHandler object.
	 * Do these things:</li>
	 * <ol>
	 * <li>register the outbidipipe;</li>
	 * <li>set the message listener.</li>
	 * </ol>
	 * <li>�ù��캯����ϵͳ��⵽��������ʱ����.ע�Ტ������pipe, ������ʵ����chatroom</li>
	 * <li>�ڴ�֮��, �������������Ϣ����, ��Ѱ��/������Ӧ��chatroom, ������Ϣ���ݸ�chatroom</li>
	 * </ol>
	 * @param pipe
	 *            message pipe
	 */
	public PeerConnectionHandler(JxtaBiDiPipe pipe)  throws Exception{
		if(pipe == null){
			throw new Exception("PeerConnectionHandler ��ʼ��JxtaBiDiPipe����Ϊ��.");
		}
		this.outbidipipe = pipe;
		outbidipipe.setMessageListener(this);
		
		friend = new PeerItem(new ImageIcon(
				SystemPath.PORTRAIT_RESOURCE_PATH + "user.png"), pipe.getRemotePeerAdvertisement());
		
		if(NoxToolkit.forceRegisterPeerConnectionHandler((PeerID) friend.getUUID(), this))
			return;
		else {
			System.out.println("ʹ�ø�bidipipeδ��ȡ��peerID");
			throw new Exception("PeerConnectionHandler ��ʼ��ʹ�ò���JxtaBiDiPipeδ��ȡ���Է�PeerID");
		}
	}
	public boolean isConnected(){
		return outbidipipe != null;
	}
	/**
	 * ���peer��������:
	 * �ɹ�: ע�Ტ����;
	 * ���ɹ�: ��ע��
	 * @Fixme ��ȡ��淽ʽ, ��ø�Ϊ��һ��ʱ����ڻ�ȡ���غ�Զ�̵Ĺ��, Ȼ��ȡ�����µ�.
	 */
	private void TryToConnect(long waittime) {
		// ����Ѿ�����outbidipipe, ����Ҫ��������
		if (outbidipipe != null) {
			System.out
					.println("["
							+ Thread.currentThread().getName()
							+ "] We have gotten a outbidipipe, cancelling TryToConnect().");
			return;
		}
		System.out.println("+++++++++++Begin TryToConnect()+++++++++++");
		// get the pipe service, and discovery
		PeerGroup group = NoxToolkit.getNetworkManager()
				.getNetPeerGroup();

		//localDiscoveryListener pipeListener = new localDiscoveryListener();
		System.out
				.println("[" + Thread.currentThread().getName()
						+ "] Fetching remote pipe adv to peer/group:"
						+ friend.getUUID());
		// ������ֶ�Ӧ��pipe���޸�newOutBidipipeAdv
		
		//���ҹܵ����ʱ��, �̶�: 2s : Ӧ��������״������
		//����ʹ�����й��, ����Ҫʱ���ؽ��ܵ�.
		//��ȡ"����"�������ʱ��(�̶�)
		int fetchRemotePipeAdvTimeCount = LANTimeLimit.FETCH_PIPEADV_MAXRETRIES/2;
		PipeAdvertisement newOutPipeAdv = null;
		/**
		 * @Fixme && outbidipipe == null ?
		 */
		while (fetchRemotePipeAdvTimeCount-- > 0 && outbidipipe == null) {
			newOutPipeAdv = PipeUtil.findNewestPipeAdv(group, friend.getUUID().toString(), LANTimeLimit.UNIT_TIME*2);
		}		
		// 1: �õ���pipe adv;
		// 2: timecount <= 0;
		// 3: outbidipipe != null: ���������жԷ����ӹ���,
		// ����ConnectionHandler�޸���outbidipipe.
		if (outbidipipe == null) {
			// ���ǵ��������
			// �����ֿ���:
			// 1: �õ���pipe adv;
			// 2: ��ʱ
			if (newOutPipeAdv == null) {
				// 2: ��ʱ, ����
				System.out
						.println("Failed to fetch a pipe adv: newOutBidipipeAdv == null.");
				return;
			}
			
			// 1: �õ�pipe adv
			long timecount = waittime / LANTimeLimit.UNIT_TIME;
			System.out.println("I have got the latest pipe adv.");
			System.out.println("[" + Thread.currentThread().getName()
					+ "] Attempting to establish a connection on pipe : "
					+ newOutPipeAdv.getPipeID());
			System.out.println("timeout: "
					+ (int) ((timecount + 1) * LANTimeLimit.UNIT_TIME));
			
			while (outbidipipe == null && timecount > 0) {
				// Try again and again
				try {
					// ���ĸ���λ��ʱ����Ϊ��ʱʱ��//timecount -= 4;
					timecount -= 2;
					outbidipipe = new JxtaBiDiPipe(group, newOutPipeAdv,
							(int) LANTimeLimit.UNIT_TIME * 4, this, true);
					if (outbidipipe == null) {
						// ���if�ƺ�û��Ҫ
						throw new IOException(
								" tryToConnect() failed: outbidipipe == null.");
					}
				} catch (IOException exc) {
					System.out.println("timecount: " + timecount
							+ " -->failed: IOException occured.");
					exc.printStackTrace();
				}
			}
			if(outbidipipe == null){
				System.out.println("Failed finally, I'm so tired of having tried so many times.");
				return;
			}

			System.out.println("[" + Thread.currentThread().getName()
					+ "] JxtaBiDiPipe pipe created");
			System.out.println("[" + Thread.currentThread().getName()
					+ "] Pipe name	: " + newOutPipeAdv.getName());
		}
		// 3: outbidipipe != null: ���������жԷ����ӹ���,
		// ����ConnectionHandler�޸���outbidipipe.
		// ���߾������Ϲ��̵õ����µ�pipe
		//ע��
		outbidipipe.setMessageListener(this);
		NoxToolkit.forceRegisterPeerConnectionHandler((PeerID) friend.getUUID(), PeerConnectionHandler.this);
		//say hello
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Saying hello ...");
		String hellomsg = "Hello [F:100] from "
				+ NoxToolkit.getNetworkConfigurator().getName();
		//�Է���һ...��ֹoutbidipipeΪ��ʱ���µݹ����trytoconnect();
		if(outbidipipe != null){
			/**
			 * ����״̬��Ϣ
			 */
			sendFullPingMsg();
			/**
			 * @Fixme �ڶ�������...........
			 */
			SendMsg(hellomsg, true);
		}
		
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Room ID	: " + friend.getUUID());
		System.out.println("+++++++++++End of TryToConnect()+++++++++++");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(String strmsg, boolean encrypt) {
		System.out.println("Sending message:\n" + strmsg);
		boolean sent = SendMsg(XmlMsgFormat.MESSAGE_NAMESPACE_NAME, strmsg.getBytes(), encrypt);
		if(sent)
			System.out.println("message sent");
		else
			System.out.println("failed to send message");
		
		return sent;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(BufferedImage bufImg, boolean encrypt) {
		/**
		 * ��ͼƬ����ImageIconתΪbyte[]
		 *  TODO imageio.write()�ĵڶ�������.............
		 */
		ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
		try {
			javax.imageio.ImageIO.write(bufImg, "PNG", byteArrayOS);
		} catch (IOException e) {
			System.out.println("ImageIO.write(bufImg, \"PNG\", byteArrayOS)����!");
			e.printStackTrace();
			return false;
		}finally{
			try {
				byteArrayOS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] picture = byteArrayOS.toByteArray();
		
		System.out.println("Sending a picture...");
		boolean sent = SendMsg(XmlMsgFormat.PICTUREMSG_NAMESPACE_NAME, picture, encrypt);
		if(sent)
			System.out.println("Picture sent");
		else
			System.out.println("Failed to send picture");
		
		return sent;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(File file, boolean encrypt) {
		/**
		 * ���ļ�תΪbyte[]
		 */
		byte[] fileBytes = null;
		byte[] fileData = null;
		NoxFileUnit noxFile;
		try {
			fileData = NoxMsgUtil.getBytesFromFile(file);
			noxFile = new NoxFileUnit(file.getName(), fileData);
			fileBytes = NoxMsgUtil.getBytesFromObject(noxFile);
			System.out.println("Sending a file...");
			boolean sent = SendMsg(XmlMsgFormat.FILEMSG_NAMESPACE_NAME, fileBytes, encrypt);
			if(sent)
				System.out.println("File sent");
			else
				System.out.println("Failed to send file");
			
			return sent;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * ����ping��Ϣ
	 */
	public void sendPingMsg() {
		NoxPeerStatusUnit status = NoxToolkit.getCheyenne().getStatusUnit();
		byte[] statBytes = null;
		try {
			statBytes = NoxMsgUtil.getBytesFromObject(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SendMsg(XmlMsgFormat.PINGMSG_NAMESPACE_NAME, statBytes, false);
	}
	/**
	 * ����pong��Ϣ
	 */
	private void sendPongMsg() {
		NoxPeerStatusUnit status = NoxToolkit.getCheyenne().getStatusUnit();
		byte[] statBytes = null;
		try {
			statBytes = NoxMsgUtil.getBytesFromObject(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SendMsg(XmlMsgFormat.PONGMSG_NAMESPACE_NAME, statBytes, false);
	}
	/**
	 * ����������pong��Ϣ(��ͷ��)
	 */
	private void sendFullPingMsg() {
		NoxPeerStatusUnit status = NoxToolkit.getCheyenne().getFullStatusUnit();
		byte[] statBytes = null;
		try {
			statBytes = NoxMsgUtil.getBytesFromObject(status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SendMsg(XmlMsgFormat.PINGMSG_NAMESPACE_NAME, statBytes, false);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(String namespace, byte[] data, boolean encrypt) {
		int retrial = 0;
		while (outbidipipe == null && retrial < LANTimeLimit.CONNECT_MAXRETRIES) {
			System.out
					.println("outBiDiPipe is null now, trying to connect : " + retrial);
			TryToConnect(LANTimeLimit.UNIT_TIME);
			retrial ++;
		}
		if(outbidipipe == null){
			System.out
			.println("outBiDiPipe is still null even after trying so many times, canceling sending msg...");
			return false;
		}

		Message msg = null;
		
		if(encrypt){
			//TODO ����DES��Կ, �����������(CBCģʽ), ������Ӧ����������Ϣ
			if(DESKey == null){
				Security.addProvider(new com.sun.crypto.provider.SunJCE());
				//������ǰ״̬
				exchangingPubKeys = true;
				try {
					/**
					 * alice����DH��, Ȼ�󽫹�Կ����󷢸�bob
					 */
					System.out.println("ALICE: ���� DH �� ...");
					KeyPairGenerator aliceKpairGen;
					aliceKpairGen = KeyPairGenerator.getInstance("DH");
					
					aliceKpairGen.initialize(512);
					KeyPair aliceKpair = aliceKpairGen.generateKeyPair(); // ����ʱ�䳤
					// ����(Alice)���ɹ�����Կ alicePubKeyEnc �����͸�����(Bob) ,
					// �������ļ���ʽ,socket.....
					byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();
					
					// Alice creates and initializes her DH KeyAgreement object
			        System.out.println("ALICE: Initialization ...");
			        KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
			        aliceKeyAgree.init(aliceKpair.getPrivate());
			        
					msg = NoxMsgUtil.generateMsg(XmlMsgFormat.PUBLICKEYENC_NAMESPACE_NAME,
							NoxToolkit.getNetworkConfigurator().getName(),
							NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
							friend.getName(), friend.getUUID().toString(),
							alicePubKeyEnc);
					outbidipipe.sendMessage(msg);
					waiting4PubKey2 = true;
					int i = 0;
					//�ȴ��Է�(bob)����public key
					while(waiting4PubKey2 && i++ < LANTimeLimit.CONNECT_MAXRETRIES*2){
						try {
							Thread.sleep(LANTimeLimit.UNIT_TIME);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							waiting4PubKey2 = false;
							exchangingPubKeys = false;
						}
					}
					if(waiting4PubKey2){
						System.out.println("������Կ��ʱ, ���ͼ�����Ϣʧ��, ������.");
						waiting4PubKey2 = false;
						exchangingPubKeys = false;
						return false;
					}
					if(bobPubKey != null){
						waiting4PubKey2 = false;
						exchangingPubKeys = false;
						//KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
				        //x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
				        //PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
				        System.out.println("ALICE: Execute PHASE1 ...");
				        aliceKeyAgree.doPhase(bobPubKey, true);
				        DESKey = aliceKeyAgree.generateSecret("DES");
				        /**
				         * @Fixme TEMP code to save DES key as a file.
				         */
				        /*java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
								new java.io.FileOutputStream("DES.key"));
						out.writeObject(DESKey);
						out.close();*/
						
					}
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(DESKey != null){
				Cipher aliceCipher;
				try {			        
			        aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
			        aliceCipher.init(Cipher.ENCRYPT_MODE, DESKey);
			        //bobCipher.init(Cipher.ENCRYPT_MODE, bobDesKey, sr);

			        byte[] cipherData = aliceCipher.doFinal(data);
			        // Retrieve the parameter that was used, and transfer it to Alice in
			        // encoded format
			        byte[] encodedParams = aliceCipher.getParameters().getEncoded();
			        
			        msg = NoxMsgUtil.generateMsg(namespace,
							NoxToolkit.getNetworkConfigurator().getName(),
							NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
							friend.getName(), friend.getUUID().toString(),
							cipherData,
							encodedParams);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				} catch (BadPaddingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("����DES��Կʧ��!");
				waiting4PubKey2 = false;
				exchangingPubKeys = false;
				return false;
			}
		} else {
			//������
			msg = NoxMsgUtil.generateMsg(namespace,
					NoxToolkit.getNetworkConfigurator().getName(),
					NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
					friend.getName(), friend.getUUID().toString(),
					data);
		}
		
		try {
			outbidipipe.sendMessage(msg);
		} catch (IOException e) {
			System.out.println("failed to send message");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void refreshStatus(NoxPeerStatusUnit stat) {
		NoxToolkit.getCheyenne().setStatus(friend.getUUID(), stat);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void pipeMsgEvent(PipeMsgEvent event) {
		System.out.println("===Begin ConnectionHandler PipeMsgEvent()===");
		// ������Ϣ
		// grab the message from the event
		Message msg = event.getMessage();
		System.out.println("Incoming call: " + msg.toString());
		System.out.println("Trying to setup a chatroom...");
		
		//�����յ�����Ϣ
		processIncomingMsg(msg, false);
		
		System.out.println("===End ConnectionHandler PipeMsgEvent()===");
	}

	/**
	 * ��Ϣ������, ��ʾ�յ���Ϣ
	 * <ol>
	 * <li>��������ҿɼ�, ֱ����ʾ</li>
	 * <li>TODO ������ɼ�, ����ʾ�û�������Ϣ����, ���û������Ƿ���ʾ</li>
	 * <ol>
	 * <li>����: ��ʾ������Ϣ</li>
	 * <li>İ����, ��ʾİ������Ϣ.</li>
	 * </ol>
	 * </ol>
	 * @param sender �������ǳ�
	 * @param time ʱ���
	 * @param msgdata �յ�����Ϣ
	 */
	private void promptIncomingMsg(final String sender, final String time, final Object msgdata) {
		/**
		 * ����Ϣ��������촰��
		 */
		System.out.println("Have put the message to the Chatroom window...");
		System.out.println("Did you see the message?");
		//������ڲ����ڻ��߲��ɼ�
		if(room == null || !room.isVisible()){
			//TODO Ӧ������ʾ����Ϣ, ������ǿ����ʾ����
			/*this.setVisible(true);*/
			System.out.println("The window is null or not visible , I make it be!");
			Icon infoIcon = UIManager.getIcon ("OptionPane.informationIcon");
	        //JLabel label = new JLabel ("New message from " + this.getRoomName(), infoIcon, SwingConstants.LEFT);
			JPanel notif = new JPanel();
			JLabel label = new JLabel ("<html>New message from:  <br><Font color=red><center>" 
					+ this.getRoomName() 
					+ "</center></Font>",
					infoIcon, SwingConstants.LEFT);
			label.setBackground(Color.WHITE);
			label.setForeground(Color.BLACK);
			JButton showMeIt = new JButton("Show Me");
			showMeIt.setMargin(new Insets(0,0,0,0));
			
			JButton ignoreIt = new JButton("Check it later");
			ignoreIt.setMargin(new Insets(0,0,0,0));
			
			notif.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.RED));
	        notif.setLayout(new BoxLayout(notif, BoxLayout.X_AXIS));
			notif.add(label);
			notif.add(showMeIt);
			notif.add(ignoreIt);
			
	        final SlideInNotification slider = new SlideInNotification (notif);
	        slider.showAt (450);
	        
	        Thread playThd = new Thread(new Runnable() {
				@Override
				public void run() {
					playAudio();
				}
				/**
				 * ������Ϣʱ������ʾ��
				 */
				private void playAudio() {
					
					final AudioClip msgBeep;
					try {
						URL url = new URL("file:/" + System.getProperty("user.dir")
								+ System.getProperty("file.separator")
								+ SystemPath.AUDIO_RESOURCE_PATH
								+ "upwpcm.wav");
						msgBeep = Applet.newAudioClip(url);
						msgBeep.play();
					} catch (MalformedURLException e) {
						e.printStackTrace();
						System.out.println(e.toString());
					}
				}
			}, "Beeper");
			playThd.start();
			
			showMeIt.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0){
					/*if(room == null){
						// �½�chatroom, ��ʾ��Ϣ
						room = new PeerChatroom(friend, PeerConnectionHandler.this);
						room.removeMask();
					}
					
					room.setVisible(true);*/
					showChatroom();
					room.incomingMsgProcessor(sender, time, msgdata);
					
					slider.Dispose();
				}
			});
			ignoreIt.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent arg0){
					slider.Dispose();
				}
			});
		} else {
			room.incomingMsgProcessor(sender, time, msgdata);
		}
	}
	/**
	 * Process the incoming message
	 * <ol>
	 * <li>������, ����֮</li>
	 * <li>����, ���д���</li>
	 * <ol>
	 * <li>�����ϵͳping/pong��Ϣ, �������Ӧ����</li>
	 * <li>������û���Ϣ, �򽻸�promptIncomingMsg���������Ҵ���״̬������Ӧ����</li>
	 * </ol>
	 * </ol>
	 * @param msg
	 *            the message
	 * @param verbose
	 *            dumps message element content if true
	 */
	public void processIncomingMsg(Message msg, boolean verbose) {
		String incomingMsg = "";
		
		try {
			CountingOutputStream cnt;
			ElementIterator it = msg.getMessageElements();
			System.out
					.println("------------------Begin Message---------------------");
			WireFormatMessage serialed = WireFormatMessageFactory.toWire(msg,
					new MimeMediaType("application/x-jxta-msg"), null);
			System.out.println("Message Size :" + serialed.getByteLength());
			while (it.hasNext()) {
				MessageElement el = it.next();
				String eName = el.getElementName();
				cnt = new CountingOutputStream(new DevNullOutputStream());
				el.sendToStream(cnt);
				long size = cnt.getBytesWritten();
				System.out.println("Element " + eName + " : " + size);
				if (verbose) {
					System.out.println("[" + el + "]");
				}
			}
			System.out
					.println("---------[F:100]----------End Message----------[F:035]------------");

			Iterator<String> namespaces = msg.getMessageNamespaces();
			
			while(namespaces.hasNext()){
				String curNamespace = namespaces.next();
				System.out.println("namespace: " + curNamespace);
				MessageElement senderEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.SENDER_ELEMENT_NAME);
				MessageElement senderIDEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.SENDERID_ELEMENT_NAME);
				MessageElement receiverEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.RECEIVER_ELEMENT_NAME);
				MessageElement receiverIDEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.RECEIVERID_ELEMENT_NAME);
				MessageElement timeEle = msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.TIME_ELEMENT_NAME);
				ByteArrayMessageElement dataEle = (ByteArrayMessageElement) msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.DATA_ELEMENT_NAME);
				ByteArrayMessageElement paramEle = (ByteArrayMessageElement) msg.getMessageElement(
						curNamespace,
						XmlMsgFormat.PARAMENC_ELEMENT_NAME);

				System.out.println("Detecting if the msg elements is null");

				if (null == senderIDEle || receiverIDEle == null) {
					System.out.println("Some key(ID) msg element is empty, it's weird.");
					continue;
				}
				System.out.println("Incoming call: From: " + senderEle.toString());
				System.out.println("Incoming call: FromID: " + senderIDEle.toString());
				System.out.println("Incoming call: To: " + receiverEle.toString());
				System.out.println("Incoming call: ToID: " + receiverIDEle.toString());
				System.out.println("Incoming call: At: " + timeEle.toString());
				/**
				 * @Fixme ���ʹ������ݵ�ʱ�������ӡ�����ή���ٶ�
				 */
				System.out.println("Incoming call: Msg(Length): " + dataEle.getByteLength());
				
				//��֤��Ϣ���շ���ID�Ƿ�"����"
				String whoami = NoxToolkit.getNetworkConfigurator().getPeerID().toString();
				String receiverID = receiverIDEle.toString();
				if(!whoami.equals(receiverID)){
					System.out.println("Receiver is not me but I still get it, that's funny.");
					continue;
				}
				String senderID = senderIDEle.toString();
				if(!friend.getUUID().toString().equals(senderID)){
					System.out.println("Sender is not who I'm talking to but I still get it, that's funny.");
					continue;
				}
				
				/**
				 * TODO �ж��Ƿ��ں�������, �������֮
				 */
				
				if(dataEle == null){
					System.out.println("data element is empty, what's wrong?");
					continue;
				}
				
				incomingMsg += dataEle.toString();
				
				if(curNamespace.equals(XmlMsgFormat.MESSAGE_NAMESPACE_NAME)){
					//TODO ����string��Ϣ
					String strmsg = null;
					
					if(paramEle != null){
						//TODO ����
						if(DESKey == null){
							// ���ļ�������ԿDES
							// TODO Ӧ����ʾ��ʾ����, ������ֱ�Ӵ򿪶Ի���...
							// ������δ���ִ�еĻ��᲻��(����)...
							importDESKey();
							if(DESKey == null){
								System.out.println("You have no DES key to decrypt this message, " +
								"please contact with the msg sender, cancelling...");
								return;
							}
						}
						System.out.println("decrypting....");
						/*
				         * Alice decrypts, using DES in CBC mode
				         */
				        // Instantiate AlgorithmParameters object from parameter encoding
				        // obtained from Bob
				        AlgorithmParameters params = AlgorithmParameters.getInstance("DES");
				        params.init(paramEle.getBytes());
				        Cipher aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
				        aliceCipher.init(Cipher.DECRYPT_MODE, DESKey, params);
				        byte[] recoveredData = aliceCipher.doFinal(dataEle.getBytes());
				        strmsg = new String(recoveredData);
					} else {
						//�������
						strmsg = new String(dataEle.getBytes());
					}
					promptIncomingMsg(friend.getName(), timeEle.toString(), strmsg);
				} else if(curNamespace.equals(XmlMsgFormat.PICTUREMSG_NAMESPACE_NAME)){
					//TODO ����ͼƬ��Ϣ
					ImageIcon incomingPic = null;
					byte[] picBytes = null;
					if(paramEle != null){
						//TODO ����
						if(DESKey == null){
							// ���ļ�������ԿDES
							importDESKey();
							if(DESKey == null){
								System.out.println("You have no DES key to decrypt this message, " +
								"please contact with the msg sender, cancelling...");
								return;
							}
						}
						System.out.println("decrypting....");
						/*
				         * Alice decrypts, using DES in CBC mode
				         */
				        // Instantiate AlgorithmParameters object from parameter encoding
				        // obtained from Bob
				        AlgorithmParameters params = AlgorithmParameters.getInstance("DES");
				        params.init(paramEle.getBytes());
				        Cipher aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
				        aliceCipher.init(Cipher.DECRYPT_MODE, DESKey, params);
				        picBytes = aliceCipher.doFinal(dataEle.getBytes());
					} else {
						//�������
						picBytes = dataEle.getBytes();
					}
					// ��byte[]ת��ΪͼƬ��ʽ
					try {
						ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(picBytes);
						BufferedImage bufImg = javax.imageio.ImageIO.read(byteArrayIS);
						incomingPic = new ImageIcon(bufImg);
					} catch (IOException e) {
						e.printStackTrace();
					}
					promptIncomingMsg(friend.getName(), timeEle.toString(), incomingPic);
				} else if(curNamespace.equals(XmlMsgFormat.FILEMSG_NAMESPACE_NAME)){
					//TODO �����ļ���Ϣ
					NoxFileUnit incomingFile = null;
					byte[] fileBytes = null;
					if(paramEle != null){
						//TODO ����
						if(DESKey == null){
							// ���ļ�������ԿDES
							importDESKey();
							if(DESKey == null){
								System.out.println("You have no DES key to decrypt this message, " +
								"please contact with the msg sender, cancelling...");
								return;
							}
						}
						System.out.println("decrypting....");
						/*
				         * Alice decrypts, using DES in CBC mode
				         */
				        // Instantiate AlgorithmParameters object from parameter encoding
				        // obtained from Bob
				        AlgorithmParameters params = AlgorithmParameters.getInstance("DES");
				        params.init(paramEle.getBytes());
				        Cipher aliceCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
				        aliceCipher.init(Cipher.DECRYPT_MODE, DESKey, params);
				        fileBytes = aliceCipher.doFinal(dataEle.getBytes());
					} else {
						//�������
						fileBytes = dataEle.getBytes();
					}
					/*promptIncomingMsg(roomname, timeEle.toString(),
							"(" + roomname + " just send over you a file." + ")");*/
					
					incomingFile = (NoxFileUnit)NoxMsgUtil.getObjectFromBytes(fileBytes);
					promptIncomingMsg(friend.getName(), timeEle.toString(), incomingFile);
				} else if(curNamespace.equals(XmlMsgFormat.PINGMSG_NAMESPACE_NAME)){
					//TODO ����ping��Ϣ
					byte[] statBytes = dataEle.getBytes();
					NoxPeerStatusUnit stat = (NoxPeerStatusUnit) NoxMsgUtil.getObjectFromBytes(statBytes);
					refreshStatus(stat);
					sendPongMsg();
				} else if(curNamespace.equals(XmlMsgFormat.PONGMSG_NAMESPACE_NAME)){
					//TODO ����pong��Ϣ
					//how about doing nothing?
					byte[] statBytes = dataEle.getBytes();
					NoxPeerStatusUnit stat = (NoxPeerStatusUnit) NoxMsgUtil.getObjectFromBytes(statBytes);
					refreshStatus(stat);
				} else if(curNamespace.equals(XmlMsgFormat.PUBLICKEYENC_NAMESPACE_NAME)){
					//TODO ����publickey
					if(exchangingPubKeys && waiting4PubKey2){
						//do nothing
						//��Ϊ��ǰ�Ѿ���������pubkey, �ڵ�pubkey2��������Ӧ֮ǰ������-����pubkey2
						System.out.println("received a public key. but I'm exchangingPubKeys && waiting4PubKey2, cancelling...");
						continue;
					}
					//����
					exchangingPubKeys = true;
					/*
			         * Let's turn over to Bob. Bob has received Alice's public key
			         * in encoded format.
			         * He instantiates a DH public key from the encoded key material.
			         */
					byte[] alicePubKeyEnc = dataEle.getBytes();
			        KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
			        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec
			            (alicePubKeyEnc);
			        PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

			        /*
			         * Bob gets the DH parameters associated with Alice's public key.
			         * He must use the same parameters when he generates his own key
			         * pair.
			         */
			        DHParameterSpec dhParamSpec = ((DHPublicKey)alicePubKey).getParams();

			        // Bob creates his own DH key pair
			        System.out.println("BOB: Generate DH keypair ...");
			        KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
			        bobKpairGen.initialize(dhParamSpec);
			        KeyPair bobKpair = bobKpairGen.generateKeyPair();

			        // Bob creates and initializes his DH KeyAgreement object
			        System.out.println("BOB: Initialization ...");
			        KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
			        bobKeyAgree.init(bobKpair.getPrivate());

			        // Bob encodes his public key, and sends it over to Alice.
			        byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();
			        
			        Message pubkey2msg = NoxMsgUtil.generateMsg(XmlMsgFormat.PUBLICKEYENC2_NAMESPACE_NAME,
							NoxToolkit.getNetworkConfigurator().getName(),
							NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
							friend.getName(), friend.getUUID().toString(),
							bobPubKeyEnc);
			        
			        outbidipipe.sendMessage(pubkey2msg);
			        exchangingPubKeys = false;
			        
			        // Bob
			        // NOTE: The call to bobKeyAgree.generateSecret above reset the key
			        // agreement object, so we call doPhase again prior to another
			        // generateSecret call
			        bobKeyAgree.doPhase(alicePubKey, true);
			        DESKey = bobKeyAgree.generateSecret("DES");
				} else if(curNamespace.equals(XmlMsgFormat.PUBLICKEYENC2_NAMESPACE_NAME)){
					//TODO ����publickey2
					/*
			         * Alice uses Bob's public key for the first (and only) phase
			         * of her version of the DH
			         * protocol.
			         * Before she can do so, she has to instantiate a DH public key
			         * from Bob's encoded key material.
			         */
					byte[] bobPubKeyEnc = dataEle.getBytes();
			        KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
			        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPubKeyEnc);
			        bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
			        waiting4PubKey2 = false;
				} else {
					System.out.println("����Ϣ��ʽ�����˰汾֧��.");
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void importDESKey() {
		JFileChooser chooser = new JFileChooser(".");
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| (f.isFile() && (f.getName().endsWith(".key")));
			}

			@Override
			public String getDescription() {
				return "*.key";
			}
		};
		chooser.setFileFilter(filter);
		chooser.setDialogTitle("�յ�һ��������Ϣ, ��ѡ��������õ�DES Key�ļ�, û������ȡ��.");
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// getJtf_pic().setText(chooser.getSelectedFile().getPath());
			System.out.println("You chose a deskey file: "
					+ chooser.getSelectedFile().getPath());
			File file = chooser.getSelectedFile();
			try {
				/*byte[] keyBytes = NoxMsgUtil.getBytesFromFile(file);
				DESKey = (SecretKey)NoxMsgUtil.getObjectFromBytes(keyBytes);*/
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
				DESKey = (SecretKey) in.readObject();
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Send pong message when receive incoming connection
	 *  to declare my status. 
	 * TODO comment this
	 */
	public void run() {
		sendFullPingMsg();
	}
	
	/**
	 * Closes the output pipe and stops the platform
	 */
	@SuppressWarnings("unused")
	private void stop() {
		try {
			outbidipipe.close();
			room.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean ExtractDataAndProcess(String namespace, Message msg) {
		return false;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ID getRoomID() {
		return friend.getUUID();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRoomName() {
		return friend.getName();
	}
	/**
	 * ��ʾ���촰��, �����δʵ����, ��ʵ����֮<br>
	 * �����Ҫʵ��������ʾ����ģ������ָʾ��<br>
	 * ���ô˺�����ǰ�����ڹ�ϣ�����ҵ�PeerID��Ӧ��handler,
	 * Ȼ��ͨ��handler����ʾchatroom. ���Զ�Ӧ��pipe�϶�����,
	 * ��Ȼ����ע�ᵽ��ϣ����.
	 */
	public void showChatroom() {
		if(room == null){
			room = new PeerChatroom(friend, PeerConnectionHandler.this);
			room.removeMask();
		}
		
		room.setVisible(true);
	}
}

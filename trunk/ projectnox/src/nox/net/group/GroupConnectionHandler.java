package nox.net.group;

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
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
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

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.WireFormatMessage;
import net.jxta.endpoint.WireFormatMessageFactory;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;
import nox.net.common.ConnectionHandler;
import nox.net.common.LANTimeLimit;
import nox.net.common.NoxToolkit;
import nox.net.common.PipeUtil;
import nox.ui.chat.common.SlideInNotification;
import nox.ui.chat.group.GroupChatroom;
import nox.ui.common.GroupItem;
import nox.ui.common.SystemPath;
import nox.xml.NoxFileUnit;
import nox.xml.NoxMsgUtil;
import nox.xml.XmlMsgFormat;

public class GroupConnectionHandler implements ConnectionHandler, Runnable, PipeMsgListener{
	private PeerGroup peergroup;
	private InputPipe inpipe = null;
	private OutputPipe outpipe = null;
	
	private GroupChatroom room = null;
	private GroupItem groupItem;
	
	private SecretKey DESKey = null;

	/**
	 * ���캯��, ���ݵڶ������������Ƿ�ʵ�������촰��<br>
	 * <ol>
	 * <li>�������ʵ��������, �����ӹ����ڵ���һ���߳�������(�첽)</li>
	 * <li>���������ʵ��������, �����ӹ�����������(ͬ��)</li>
	 * </ol>
	 * @param groupItem �б������GroupItem
	 * @param showChatroom �Ƿ�����ʵ��������
	 * @throws Exception ��һ������Ϊ���쳣
	 */
	public GroupConnectionHandler(final GroupItem groupItem, boolean showChatroom) throws Exception{
		if(groupItem == null)
			throw new Exception("GroupConnectionHandler ��ʼ������Ϊ��.");
		
		PeerGroup ppg = NoxToolkit.getNetworkManager().getNetPeerGroup();
		PeerGroupAdvertisement pga	= PeerGroupUtil.getLocalAdvByID(
				ppg, groupItem.getUUID().toString());
		
		this.peergroup = ppg.newGroup(pga);
		this.groupItem = groupItem;
		
		if(showChatroom){
			//��ʾ�����Ҵ���, ����.
			//�ɹ�: ע�Ტ����
			//��ʱ(���ɹ�):��ʾ�û����ӳ�ʱ, �Ƿ�����!
			room = new GroupChatroom(groupItem, GroupConnectionHandler.this);
			room.setVisible(true);
			Thread connector = new Thread(new Runnable(){
				@Override
				public void run() {
					boolean retry = true;
					while(retry){
						TryToConnect(LANTimeLimit.UNIT_TIME * LANTimeLimit.CONNECT_MAXRETRIES);
						if(outpipe == null){
							retry = room.showTimeOutMsg();
						} else {
							System.out.println("���ӳɹ�!!");
							//TryToConnect()���Զ�ע��ID-connectionHandler������
							room.removeMask();
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
	 * ���캯��, ���û�˫�����������б�ʱ���ô˹��캯��.<br>
	 * ֱ����ʾ���촰��, ����ͼ����<br>
	 * @param friend �б������GroupItem
	 * @throws Exception ����Ϊ���쳣
	 */
	public GroupConnectionHandler(GroupItem groupItem) throws Exception{
		this(groupItem, true);
	}
	/**
	 * ���˱�ĳЩ@@deprecated�ķ���������,<br>
	 * ��Ҫ����;�Ǽ��������ʱ����ô˹��캯��.<br>
	 * ��Ϊ�������������PeerGroup, ����������캯�����Ա����ظ�����.<br>
	 * �˴���ͬ����, �����ڱ�����ʱ����Ϊһ���̵߳�runnable, ���첽��.<br>
	 * @param pg
	 * @param groupItem
	 * @throws Exception
	 */
	public GroupConnectionHandler(PeerGroup pg, GroupItem groupItem) throws Exception{
		if(pg == null || groupItem == null)
			throw new Exception("GroupConnectionHandler ��ʼ������Ϊ��.");
		
		this.peergroup = pg;
		this.groupItem = groupItem;
		
		TryToConnect(LANTimeLimit.UNIT_TIME * LANTimeLimit.CONNECT_MAXRETRIES);
		/*PipeAdvertisement pia = null;
		pia = PipeUtil.findPipeAdv(pg, pg.getPeerGroupID().toString());
		if(pia == null){
			System.out.println("Failed to find or create a pipe adv, it's a fatal error");
			return;
		}
		System.out.println("Creating Propagated InputPipe for pipe: " + pia.getPipeID());
		
		inpipe = pg.getPipeService().createInputPipe(pia, this);
		outpipe = pg.getPipeService().createOutputPipe(pia, LANTimeLimit.CREATE_OUTPUT_PIPE_WAITTIME);*/
	}
	public boolean isConnected(){
		return outpipe != null;
	}
	/**
	 * ��������:
	 */
	private void TryToConnect(long waittime) {
		// ����Ѿ�����outbidipipe, ����Ҫ��������
		if (outpipe != null && inpipe != null) {
			System.out
					.println("["
							+ Thread.currentThread().getName()
							+ "] We have gotten a I/O pipe, cancelling TryToConnect().");
			return;
		}
		System.out.println("+++++++++++Begin TryToConnect()+++++++++++");
		// get the pipe service, and discovery
		PeerGroup parentgroup = NoxToolkit.getNetworkManager()
				.getNetPeerGroup();

		PeerGroupAdvertisement pga = PeerGroupUtil.getLocalAdvByID(parentgroup, groupItem.getUUID().toString());
		if(pga != null){
			peergroup = null;
			try {
				peergroup = parentgroup.newGroup(pga);
			} catch (PeerGroupException e) {
				e.printStackTrace();
			}
			if(peergroup != null){
				System.out.println("�ɹ�������, ���ڲ��Ҹ������ùܵ����...");
				PipeAdvertisement pia = null;
				pia = PipeUtil.findNewestPipeAdv(peergroup, peergroup.getPeerGroupID().toString(), LANTimeLimit.UNIT_TIME*2);
				if(pia == null){
					System.out.println("Failed to find or create a pipe adv, it's a fatal error");
					return;
				}
				System.out.println("Creating Propagated InputPipe for pipe: " + pia.getPipeID());
		        try {
		        	if(inpipe == null)
		        		inpipe = peergroup.getPipeService().createInputPipe(pia, this);
		            if(outpipe == null)
		            	outpipe = peergroup.getPipeService().createOutputPipe(pia, waittime);
		        } catch (IOException e) {
		        	System.out.println("Failed to create Propagated InputPipe for pipe: " + pia.getPipeID());
		            e.printStackTrace();
		            System.exit(-1);
		        }
			}
		}
		//ע�ᴦ����
		if(inpipe != null)
			NoxToolkit.forceRegisterGroupConnectionHandler((PeerGroupID) groupItem.getUUID(), this);
		
		if (outpipe != null) {
			//Ⱥ����Ϣ
			System.out.println("[" + Thread.currentThread().getName()
					+ "] Saying hello ...");
			String hellomsg = "Hello [F:100] from "
				+ NoxToolkit.getNetworkConfigurator().getName();
			SendMsg(hellomsg, false);
		}
		
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Done!");
		System.out.println("[" + Thread.currentThread().getName()
				+ "] Room ID	: " + groupItem.getUUID());
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
		long size = file.length();
		/**
		 * @Fixme msg�ߴ�����
		 */
		if(size >= 60000){
			System.out.println("�ļ��ߴ���������60K����");
			return false;
		}
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
	 * Send a greeting message over the pipe
	 * 
	 * @param bidipipe
	 *            the pipe to send messages over
	 * @throws IOException
	 *             Thrown for errors sending messages.
	 */
	public void sendGreetingMessages() throws IOException {
		System.out.println("Sending greeting message...");
		// create the message
		String hellomsg = "Greetings! What's up? [F:100]\nIn ConnectionHandler sendGreetingMessages() from "
				+ NoxToolkit.getNetworkConfigurator().getName();
		SendMsg(hellomsg, false);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(String namespace, byte[] data, boolean encrypt) {
		int retrial = 0;
		while (outpipe == null && retrial < LANTimeLimit.CONNECT_MAXRETRIES) {
			System.out
					.println("outBiDiPipe is null now, trying to connect...");
			TryToConnect(LANTimeLimit.UNIT_TIME);
			retrial ++;
		}
		if(outpipe == null){
			System.out
			.println("outpipe is still null even after trying so many times, canceling sending msg...");
			return false;
		}

		Message msg = null;
		
		if(encrypt){
			//TODO ����DES��Կ, �����������(CBCģʽ), ������Ӧ����������Ϣ
			if(DESKey == null){
				// ���ļ�������ԿDES
				importDESKey();
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
							groupItem.getName(), groupItem.getUUID().toString(),
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
				return false;
			}
		} else {
			//������
			msg = NoxMsgUtil.generateMsg(namespace,
					NoxToolkit.getNetworkConfigurator().getName(),
					NoxToolkit.getNetworkConfigurator().getPeerID().toString(),
					groupItem.getName(), groupItem.getUUID().toString(),
					data);
		}
		
		try {
			outpipe.send(msg);
		} catch (IOException e) {
			System.out.println("failed to send message");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public DiscoveryService getDiscoveryService(){
		if(peergroup != null)
			return peergroup.getDiscoveryService();
		else
			return null;
	}
	/**
	 * ��Ϣ������, ��ʾ�յ���Ϣ
	 * <ol>
	 * <li>��������ҿɼ�, ֱ����ʾ</li>
	 * <li>������ɼ�, ����ʾ�û�������Ϣ����, ���û������Ƿ���ʾ</li>
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
					if(room == null){
						// �½�chatroom, ��ʾ��Ϣ
						room = new GroupChatroom(groupItem, GroupConnectionHandler.this);
						room.removeMask();
					}
					
					room.setVisible(true);
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
	 * <li>��������ҿɼ�, ֱ����ʾ</li>
	 * <li>TODO ������ɼ�, ����ʾ�û�������Ϣ����</li>
	 * 
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
				System.out.println("Incoming call: Msg: " + dataEle.toString());
				
				if(senderIDEle.toString().equals(NoxToolkit.getNetworkConfigurator().getPeerID().toString())){
					//�Լ�������Ϣ, ����֮
					System.out.println("It's a msg from myself, just omit it...");
					return;
				}
				
				//��֤��Ϣ���շ���ID�Ƿ�"����"
				String receiverID = receiverIDEle.toString();
				if(!groupItem.getUUID().toString().equals(receiverID)){
					System.out.println("Receiver is not me but I still get it, that's funny.");
					continue;
				}
				
				if(dataEle == null){
					System.out.println("data element is empty, what's wrong?");
					continue;
				}
				
				incomingMsg += dataEle.toString();
				
				//String[] strArrayMsg = { "", roomname, timeEle.toString()};
				
				if(curNamespace.equals(XmlMsgFormat.MESSAGE_NAMESPACE_NAME)){
					//TODO ����string��Ϣ
					String strmsg = null;
					
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
				        byte[] recoveredData = aliceCipher.doFinal(dataEle.getBytes());
				        strmsg = new String(recoveredData);
					} else {
						//�������
						strmsg = new String(dataEle.getBytes());
					}
					promptIncomingMsg(senderEle.toString(), timeEle.toString(), strmsg);
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
					promptIncomingMsg(senderEle.toString(), timeEle.toString(), incomingPic);
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
					incomingFile = (NoxFileUnit)NoxMsgUtil.getObjectFromBytes(fileBytes);
					promptIncomingMsg(senderEle.toString(), timeEle.toString(), incomingFile);
				} else if(curNamespace.equals(XmlMsgFormat.PINGMSG_NAMESPACE_NAME)){
					//TODO ����ping��Ϣ
				} else if(curNamespace.equals(XmlMsgFormat.PONGMSG_NAMESPACE_NAME)){
					//TODO ����pong��Ϣ
					//how about doing nothing?
				} else if(curNamespace.equals(XmlMsgFormat.PUBLICKEYENC_NAMESPACE_NAME)){
					//TODO ����publickey
				} else if(curNamespace.equals(XmlMsgFormat.PUBLICKEYENC2_NAMESPACE_NAME)){
					//TODO ����publickey2
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
		System.out.println("importing DES key file...");
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
		chooser.setDialogTitle("�յ�һ��������Ϣ, ��ѡ��ӽ������õ�DES Key�ļ�, û������ȡ��.");
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
		return groupItem.getUUID();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRoomName() {
		return groupItem.getName();
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
			room = new GroupChatroom(groupItem, GroupConnectionHandler.this);
			room.removeMask();
		}
		
		room.setVisible(true);
	}

	@Override
	public void run() {
		try {
			if(outpipe != null)
				sendGreetingMessages();
		} catch (Throwable all) {
			all.printStackTrace();
		}
	}
	/**
	 * Closes the output pipe and stops the platform
	 */
	private void stop() {
		if(inpipe != null)
			inpipe.close();
		if(outpipe != null)
			outpipe.close();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {
		System.out.println("+++Begin Chatroom pipeMsgEvent()...+++");
		Message msg = event.getMessage();

		System.out.println("Incoming call: " + msg.toString());

		this.processIncomingMsg(msg, false);
		System.out.println("+++End Chatroom pipeMsgEvent()...+++");
	}
	/**
	 * �˳�����
	 */
	public void resign(){
		stop();
		if(peergroup != null)
			try {
				peergroup.getMembershipService().resign();
			} catch (PeerGroupException e) {
				e.printStackTrace();
			}
		if(room != null)
			room.dispose();
	}
}

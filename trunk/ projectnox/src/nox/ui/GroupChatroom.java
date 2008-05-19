package nox.ui;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.CountingOutputStream;
import net.jxta.util.DevNullOutputStream;
import nox.net.NoxToolkit;
import nox.net.PeerGroupUtil;
import nox.net.PipeUtil;
import nox.xml.NoxFileUnit;
import nox.xml.NoxMsgUtil;
import nox.xml.XmlMsgFormat;

/**
 * 
 * @author shinysky
 * 
 */
@SuppressWarnings("serial")
public class GroupChatroom extends Chatroom implements PipeMsgListener {
	/**
	 * ���ӶԷ�ʱ��ʾ��ģ������ָʾ��
	 */
	protected InfiniteProgressPanel glassPane;
	
	private Thread connector;
	
	private PeerGroup peergroup = null;
	/**
	 * �����շ���Ϣ��pipe
	 */
	private InputPipe inpipe = null;
	private OutputPipe outpipe = null;
	
	private SecretKey DESKey = null;
	
	/**
	 * ����Ӧ�ô������ڼ̳���ɫ, ͸���� ����ʵ��:�����ںʹ�������ͬ��������ɫ��͸����.
	 * ��ʵ�����������ڵ�ʱ�����ñ�����һ��Vector��,
	 * ������ɫ��͸����ʱ�� Vector��ʵ�����ε��õ��ں���.
	 * <li>���캯��.</li>
	 * <li>�û�˫������ͼ��ʱ, ��������ڶ�Ӧ��chatroom, ��</li>
	 * <ol>
	 * <li>�����µ�chatroom;</li>
	 * <li>�Զ���������.</li>
	 * </ol>
	 * @param group ������ѵ�PeerItem
	 * @see PeerItem
	 */
	public GroupChatroom(final GroupItem group) {
		super(group.getName(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_48.png", false);
		roomID = group.getUUID();

		GroupChatroomSidePane gcsp = new GroupChatroomSidePane(this, group
				.getName(), new PeerItem[0]);
		rootpane.add(gcsp);
		rootpane.add(chatroompane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
		this.setVisible(true);
		connector = new Thread(new Runnable() {
			public void run() {
				/**
				 * ���peer��������
				 */
				TryToConnect(5 * 1000);
			}
		}, "Connector");
		connector.start();
	}
	public GroupChatroom(PeerGroupAdvertisement pga, InputPipe ipipe, OutputPipe opipe) {
		super(pga.getName(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_48.png", false);
		this.inpipe = ipipe;
		this.outpipe = opipe;
		
		/**
		 * ��ʼ��peergroup, ��������Աʹ��
		 */
		PeerGroup parentgroup = NoxToolkit.getNetworkManager().getNetPeerGroup();

		if(pga != null){
			try {
				peergroup = parentgroup.newGroup(pga);
			} catch (PeerGroupException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * TODO �Ƴ�ԭ���Ĺܵ�������
		 */
		roomID = pga.getPeerGroupID();
		
		GroupChatroomSidePane gcsp 
			= new GroupChatroomSidePane(this, pga.getDescription(), new PeerItem[0]);
		rootpane.add(gcsp);
		rootpane.add(chatroompane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
		this.setVisible(true);
	}
	/*public GroupChatroom(final GroupItem group, GroupItem[] gmembers) {
		super(group.getNick(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_48.png", false);
		
		GroupChatroomSidePane groupmembers = new GroupChatroomSidePane(
				"Hello, everyone, happy everyday!", gmembers);
		rootpane.add(groupmembers);
		rootpane.add(chatroompane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
		roomID = group.getUUID();
		this.setVisible(true);
	}*/
	public DiscoveryService getDiscoveryService(){
		if(peergroup != null)
			return peergroup.getDiscoveryService();
		else
			return null;
	}
	public OutputPipe getOutBidipipe() {
		/*if (connectionHandler != null)
			return connectionHandler.getPipe();
		else
			return null;*/
		return outpipe;
	}

	public void setOutpipe(OutputPipe pipe) {
		if (pipe != null) {
			outpipe = pipe;
			System.out
					.println("In setOutpipe(), the parameter 'pipe' is not null, good!");
		} else
			System.out.println("Unbelievable! the parameter 'pipe' is null!!");
	}

	public void discoverMembers(){
		;
	}
	public void TryToConnectAgain(long waittime) {
		connector.start();
		rootpane.setVisible(false);
		glassPane.setVisible(true);
		glassPane.start();
	}

	/**
	 * ���peer��������:
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

		PeerGroupAdvertisement pga = PeerGroupUtil.getLocalAdvByID(parentgroup, roomID.toString());
		if(pga != null){
			peergroup = null;
			try {
				peergroup = parentgroup.newGroup(pga);
			} catch (PeerGroupException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(peergroup != null){
				System.out.println("�ɹ�������, ���ڲ��Ҹ������ùܵ����...");
				PipeAdvertisement pia = null;
				pia = PipeUtil.findPipeAdv(peergroup, peergroup.getPeerGroupID().toString());
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
				+ "] Room ID	: " + roomID);
		System.out.println("+++++++++++End of TryToConnect()+++++++++++");
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
				if(!roomID.toString().equals(receiverID)){
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
					chatroompane.incomingMsgProcessor(senderEle.toString(), timeEle.toString(), strmsg);
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
					chatroompane.incomingMsgProcessor(senderEle.toString(), timeEle.toString(), incomingPic);
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
					chatroompane.incomingMsgProcessor(senderEle.toString(), timeEle.toString(),
							"(" + senderEle.toString() + " just send over a file." + ")");
					incomingFile = (NoxFileUnit)NoxMsgUtil.getObjectFromBytes(fileBytes);
					String filename = incomingFile.getName();
					
					byte[] fileDataBytes = incomingFile.getData();
					
					JFileChooser chooser=new JFileChooser(".");
					chooser.setDialogTitle("����-�������ļ���");
					chooser.setSelectedFile( new File(filename) );
					int returnVal = chooser.showSaveDialog(GroupChatroom.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						System.out.println("Saving file as: "
								+ chooser.getSelectedFile().getPath());
						FileOutputStream fstream = new FileOutputStream(chooser.getSelectedFile());
			            BufferedOutputStream stream = new BufferedOutputStream(fstream);
						try {
				            stream.write(fileDataBytes);
				        } catch (Exception e) {
				            e.printStackTrace();
				        } finally {
				            if (stream != null) {
				                try {
				                    stream.close();
				                    fstream.close();
				                } catch (IOException e1) {
				                    e1.printStackTrace();
				                }
				            }
				        }
					}
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
				/**
				 * ����Ϣ��������촰��
				 */
				System.out.println("Have put the message to the Chatroom window...");
				System.out.println("Did you see the message?");
				
				if(!this.isVisible()){
					//TODO Ӧ������ʾ����Ϣ, ������ǿ����ʾ����
					/*this.setVisible(true);
					System.out.println("The window is not visible, I make it be!");*/
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
							GroupChatroom.this.setVisible(true);
							slider.Dispose();
						}
					});
					ignoreIt.addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent arg0){
							slider.Dispose();
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes the output pipe and stops the platform
	 */
	@SuppressWarnings("unused")
	private void stop() {
		inpipe.close();
		outpipe.close();
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
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(String namespace, byte[] data, boolean encrypt) {
		int retrial = 0;
		while (outpipe == null && retrial < Chatroom.MAXRETRIES) {
			System.out
					.println("outBiDiPipe is null now, trying to connect...");
			TryToConnect(Chatroom.UnitWaitTime);
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
							this.roomname, this.roomID.toString(),
							cipherData,
							encodedParams);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BadPaddingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
					this.roomname, this.roomID.toString(),
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
	private void importDESKey() {
		System.out.println("importing DES key file...");
		JFileChooser chooser = new JFileChooser();
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
		chooser.setDialogTitle("��ѡ��ӽ������õ�DES Key�ļ�");
		int returnVal = chooser.showOpenDialog(GroupChatroom.this);
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
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * PipeMsgListener interface for asynchronous message arrival notification
	 * 
	 * @param event
	 *            the message event
	 */
	@Override
	public void pipeMsgEvent(PipeMsgEvent event) {
		System.out.println("+++Begin Chatroom pipeMsgEvent()...+++");
		Message msg = event.getMessage();

		System.out.println("Incoming call: " + msg.toString());

		this.processIncomingMsg(msg, false);
		System.out.println("+++End Chatroom pipeMsgEvent()...+++");
	}
}

package nox.ui.chat.common;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import nox.ui.chat.peer.PeerChatroom;
import nox.ui.common.DialogEarthquakeCenter;
import nox.ui.common.JNABalloon;
import nox.ui.common.SystemPath;

/*
 * Created on 2006-9-9
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * ����"����"�����촰��: ����ѡ�����弰��ɫ; ���Բ���ͼƬ
 * 
 * @author shinysky
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * 
 * (Ҫʱ��׼���Ŵӷ�����������Ϣ) ��Ϣ��ʽ: ��һ����:who sayTo who at time �ڶ�����:the message
 * ����Ϣ��ӵ���Ϣ��¼, Ȼ��ͨ����鷢���˺�noDisturb�����������Ƿ���Ӹ���Ϣ����ʷ��Ϣ����
 */
public class ChatroomPane extends JSplitPane implements ActionListener// ,MouseListener
{
	/**
	 * we don't know more about it
	 */
	private static final long serialVersionUID = -1915394855935441419L;

	/**
	 * ��ʷ��ϢJScrollPane
	 */
	private JScrollPane sp_historymsg;
	/**
	 * ��ʷ��ϢJTextPane
	 */
	private JTextPane tp_historymsg;
	/**
	 * ��Ϣ���봰�ڼ����
	 */
	private JPanel p_inputpaneAndButtons;
	/**
	 * ��ϢJScrollPane
	 */
	private JScrollPane sp_input;
	/**
	 * ��Ϣ����� JTextPane
	 */
	private JTextPane tp_input;
	/**
	 * ��ťJPanel, ��������鰴ť/������ť/.../���Ͱ�ť
	 */
	private JPanel p_buttons;
	/**
	 * �������JButton
	 */
	private JButton b_emotion;
	/**
	 * ����ѡ��Ի���
	 */
	private FaceDialog selFace;
	/**
	 * ������
	 */
	private JButton b_shake;
	private static final String shakeMsg = "[F:999]"; 
	/**
	 * ����ͼƬ��ť
	 */
	private JButton b_sendPic;
	/**
	 * �����ļ���ť
	 */
	private JButton b_sendFile;
	/**
	 * ������ť
	 */
	private JButton b_snapshot;
	private JButton b_snapconfig;
	JPopupMenu menuSnap;
	JMenuItem doSnap;
	JCheckBoxMenuItem hideFrame;
	/**
	 * ͼƬ������ʽ������
	 */
	public static final DecimalFormat fmNum = new DecimalFormat("000");
	/**
	 * ��Ϣ����JToggleButton
	 */
	private JToggleButton tb_encrypt;
	
	/**
	 * ��Ϣ����JButton
	 */
	private JButton b_send;
	/**
	 * ��ʷ��Ϣ,���ڱ������
	 */
	String historymsg_save;
	/**
	 * ��ǰ����������е���Ϣ, ���ڱ������
	 */
	String currentmsg_save;
	/**
	 * �ı����ģ��
	 */
	StyledDocument styledDoc;
	/**
	 * ������
	 */
	/**
	 * ��ͨ
	 */
	Style normal;
	/**
	 * ��ɫ
	 */
	Style blue;
	/**
	 * ��ɫ
	 */
	Style green;
	/**
	 * ��ɫ
	 */
	Style gray;
	/**
	 * ��ɫ
	 */
	Style red;
	/**
	 * ����
	 */
	Style bold;
	/**
	 * б��
	 */
	Style italic;
	/**
	 * ���
	 */
	Style bigSize;
	/**
	 * ��������
	 */
	/**
	 * ���ڱ�ǩ��ʽ
	 */
	private Format fmDate = new SimpleDateFormat("yyyy/MM/dd E HH:mm:ss");
	/**
	 * �ַ��������м����
	 */
	// private int position;
	/**
	 * �ַ�������
	 */
	// private int strLength;
	/**
	 * ��ӭ��Ϣ
	 */
	private String sayHello;

	/***************************************************************************
	 * //
	 */
	/**
	 * ������������(ͨ������paintComponent()) (�������δ���) ...But CPUռ���ʴﵽ100% !Faint! ����ʧ��;
	 * 
	 * ��һЩ���ܵ�����Ļˢ�µİ�ť��Ӧ�������Ӧ����� setRenderingHints(hints); ������������ �����Ǹ����ķ���,
	 * �����û���ѡ���ı�����Ҳ��������ʹ��������"��ʧ" ����,����ı��а�����̬ͼƬ,Ҳ��ʹ���Զ�ʧ
	 */
	/*
	 * public void paintComponent(Graphics g) { super.paintComponent(g);
	 * Graphics2D g2 = (Graphics2D)g; g2.setRenderingHints(hints); //
	 * RenderingHints hints_in = new RenderingHints(null); //
	 * hints_in.put(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON); //
	 * ChatRoomPane.this.setRenderingHints(hints_in); }
	 * 
	 *//**
		 * ���������������ػ����
		 * 
		 * @param h
		 *            RenderingHints
		 */
	/*
	 * public void setRenderingHints(RenderingHints h) { hints = h; repaint(); }
	 * private RenderingHints hints = new RenderingHints(null);
	 */
	// ********************************************************
	Chatroom parent;
	
	/**
	 * JSplitPane �������, �������/��Ϣ����/���鰴ť/������ť/.../���Ͱ�ť ��
	 * 
	 * @param par
	 *            �����, ����ʹ����par��
	 */
	public ChatroomPane(Chatroom par) {
		super(JSplitPane.VERTICAL_SPLIT);
		parent = par;

		sayHello = new String(
				"\t------====  Welcome to the Chat Room  ====------\n"
						+ "\t  ------====     What do U wanna say ?   ====------\n");
		// strLength = sayHello.length();
		// position = 0;
		/**
		 * ��ʷ��Ϣ����
		 */
		tp_historymsg = new JTextPane();
		historymsg_save = new String();
		historymsg_save += sayHello;
		styledDoc = tp_historymsg.getStyledDocument();
		/**
		 * �½����
		 */
		normal = styledDoc.addStyle("normal", null);
		StyleConstants.setFontFamily(normal, "SansSerif");

		blue = styledDoc.addStyle("blue", normal);
		StyleConstants.setForeground(blue, Color.blue);

		green = styledDoc.addStyle("green", normal);
		StyleConstants.setForeground(green, Color.GREEN.darker());

		gray = styledDoc.addStyle("gray", normal);
		StyleConstants.setForeground(gray, Color.GRAY);

		red = styledDoc.addStyle("red", normal);
		StyleConstants.setForeground(red, Color.red);

		bold = styledDoc.addStyle("bold", normal);
		StyleConstants.setBold(bold, true);

		italic = styledDoc.addStyle("italic", normal);
		StyleConstants.setItalic(italic, true);

		bigSize = styledDoc.addStyle("bigSize", normal);
		StyleConstants.setFontSize(bigSize, 24);
		/**
		 * ��ӷ���ı�(��ӭ��Ϣ)
		 */
		styledDoc.setLogicalStyle(0, red);

		tp_historymsg.replaceSelection(sayHello);

		// tp_historymsg.setToolTipText("History Messages");
		tp_historymsg.setBackground(new Color(180, 250, 250));
		tp_historymsg.setSelectionColor(Color.YELLOW);
		/**
		 * �������Ϊ�ɱ༭:�û���������ɱ༭ �������Ϊ���ɱ༭:�����޷�����������ı�.��ô��??
		 * 
		 * ...�ѽ��(��֪�Ƿ��㹻��ȫ): ͨ���������Ϊ���ɱ༭ ������������ı�ʱ,��ʱ��Ϊ�ɱ༭;
		 * ���������ò����λ��(��֤�����λ�����ı�β) (�����У�������λ��,�û����ܲ��ܱ༭,�����ǿ��Ըı�����λ��)
		 * ����ı���,��������Ϊ���ɱ༭. �㶨!^-^ ���appendToHMsg();
		 */
		tp_historymsg.setEditable(false);
		sp_historymsg = new JScrollPane(tp_historymsg);
		sp_historymsg.setAutoscrolls(true);

		p_inputpaneAndButtons = new JPanel();

		/**
		 * ��Ϣ���봰��
		 */
		tp_input = new JTextPane();
		// tp_msg.setText(sayHello);
		tp_input
		.setToolTipText(getHtmlText("Input your message and press \"Send\" <br>or press Ctrl+Enter"));

		/**
		 * �����¼�������/�¼�����
		 */
		tp_input.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent event) {
				int keyCode = event.getKeyCode();
				/**
				 * �������ΪCtrl+Enter������Ϣ
				 */
				if (keyCode == KeyEvent.VK_ENTER && event.isControlDown()) {
					System.out.println("You press the combo-key : Ctrl+Enter");
					sendMessage();
				}
			}

			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
			}
		});
		sp_input = new JScrollPane(tp_input);

		/**
		 * ������鰴ť �� ���Ͱ�ť
		 */
		p_buttons = new JPanel();

		Dimension buttonSize = new Dimension(26, 26);

		b_emotion = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "emotion.png"));
		b_emotion.setToolTipText(getHtmlText("Insert a emotion image"));
		b_emotion.setActionCommand("Emotion");
		b_emotion.addActionListener(this);
		// b_InsertImg.setContentAreaFilled(false);
		b_emotion.setSize(buttonSize);
		b_emotion.setPreferredSize(buttonSize);
		b_emotion.setMaximumSize(buttonSize);
		b_emotion.setMinimumSize(buttonSize);

		/**
		 * ����������������ڵĻ�, ������޷���ȡ��ѡ��ı�������.
		 */
		selFace = new FaceDialog("Insert a face", true, SystemPath.FACES_RESOURCE_PATH);
		// ��FaceDialog.setDefaultLookAndFeelDecorated(true);����ͬʱʹ��
		selFace.setBounds(450, 350, FaceDialog.FACECELLWIDTH
				* FaceDialog.FACECOLUMNS, FaceDialog.FACECELLHEIGHT
				* FaceDialog.FACEROWS + 30);// 30Ϊb_cr_cancel�ĸ߶�
		selFace.pack();

		b_shake = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "shake.png"));
		b_shake.setToolTipText(getHtmlText("Rock and Roll !"));
		b_shake.setActionCommand("Shake");
		b_shake.addActionListener(this);
		b_shake.setSize(buttonSize);
		b_shake.setPreferredSize(buttonSize);
		b_shake.setMaximumSize(buttonSize);
		b_shake.setMinimumSize(buttonSize);

		b_sendPic = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "sendpic.png"));
		b_sendPic.setToolTipText(getHtmlText("Send a picture"));
		b_sendPic.setActionCommand("SendPic");
		b_sendPic.addActionListener(this);
		b_sendPic.setSize(buttonSize);
		b_sendPic.setPreferredSize(buttonSize);
		b_sendPic.setMaximumSize(buttonSize);
		b_sendPic.setMinimumSize(buttonSize);
		
		b_sendFile = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "sendfile.png"));
		b_sendFile.setToolTipText(getHtmlText("Send a file"));
		b_sendFile.setActionCommand("SendFile");
		b_sendFile.addActionListener(this);
		b_sendFile.setSize(buttonSize);
		b_sendFile.setPreferredSize(buttonSize);
		b_sendFile.setMaximumSize(buttonSize);
		b_sendFile.setMinimumSize(buttonSize);

		b_snapshot = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "snapshot.png"));
		b_snapshot.setToolTipText(getHtmlText("Snap it !"));
		b_snapshot.setActionCommand("Snapshot");
		b_snapshot.addActionListener(this);
		b_snapshot.setSize(buttonSize);
		b_snapshot.setPreferredSize(buttonSize);
		b_snapshot.setMaximumSize(buttonSize);
		b_snapshot.setMinimumSize(buttonSize);
		
		b_snapconfig = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "snapconfig.png"));
		b_snapconfig.setMargin(new Insets(0,0,0,0));
		b_snapconfig.setToolTipText(getHtmlText("Snap Config"));
		b_snapconfig.setActionCommand("SnapshotConfig");
		b_snapconfig.addActionListener(this);
		b_snapconfig.setSize(new Dimension(buttonSize.width/2, buttonSize.height));
		b_snapconfig.setPreferredSize(new Dimension(buttonSize.width/2, buttonSize.height));
		b_snapconfig.setMaximumSize(new Dimension(buttonSize.width/2, buttonSize.height));
		b_snapconfig.setMinimumSize(new Dimension(buttonSize.width/2, buttonSize.height));

		menuSnap = new JPopupMenu();
		doSnap = new JMenuItem("Let's GO!");
		hideFrame = new JCheckBoxMenuItem("Hide this window while snapping",
				true);
		doSnap.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (hideFrame.getState())// ����û�ѡ�����ش���, ������
				{
					// parent.setVisible(false);
					parent.setState(JFrame.ICONIFIED);
					// System.out.println("in if: What the hell is wrong with
					// you!" + hideFrame.isSelected());
				}
				try {
					// ��Ϊ��Ҫִ�����¼��������˵�������ʧ, ���������������.
					// menuSnap.setVisible(false);
					Thread.sleep(300);// ˯500������Ϊ����������ȫ����
					Robot ro = new Robot();
					Toolkit tk = Toolkit.getDefaultToolkit();
					Dimension screenSize = tk.getScreenSize();
					Rectangle rec = new Rectangle(0, 0, screenSize.width,
							screenSize.height);
					BufferedImage buffImg = ro.createScreenCapture(rec);
					final JDialog fakeWin = new JDialog(parent, true);
					fakeWin.addKeyListener(new KeyListener() {
						public void keyPressed(KeyEvent event) {
							int keyCode = event.getKeyCode();
							/**
							 * �������ΪESC���˳�����
							 */
							if (keyCode == KeyEvent.VK_ESCAPE) {
								fakeWin.dispose();
							}
						}

						public void keyTyped(KeyEvent e) {
							// TODO Auto-generated method stub
						}

						public void keyReleased(KeyEvent e) {
							// TODO Auto-generated method stub
						}
					});
					ScreenCapturer temp = new ScreenCapturer(fakeWin, buffImg,
							screenSize.width, screenSize.height);
					fakeWin.getContentPane().add(temp, BorderLayout.CENTER);
					fakeWin.setUndecorated(true);
					fakeWin.setSize(screenSize);
					fakeWin.setVisible(true);
					fakeWin.setAlwaysOnTop(true);

					parent.setState(JFrame.NORMAL);
					buffImg = temp.getWhatWeGot();
					if (buffImg != null) {
						ChatroomPane.this.sendAPicture(buffImg);
					} else {
						System.out.println("phew~we got nothing.");
					}
				} catch (Exception exe) {
					exe.printStackTrace();
				}
			}
		});

		menuSnap.add(doSnap);
		menuSnap.addSeparator();
		menuSnap.add(hideFrame);
		menuSnap.pack();

		tb_encrypt= new JToggleButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "unlock.png"));
		tb_encrypt.setToolTipText(getHtmlText("Encrypt or not"));
		tb_encrypt.setSelectedIcon(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "lock.png"));
		tb_encrypt.setSelected(true);
		tb_encrypt.setSize(buttonSize);
		tb_encrypt.setPreferredSize(buttonSize);
		tb_encrypt.setMaximumSize(buttonSize);
		tb_encrypt.setMinimumSize(buttonSize);
		
		// b_send = new JButton("Send");
		b_send = new JButton(new ImageIcon(SystemPath.ICONS_RESOURCE_PATH + "send.png"));
		b_send.setMnemonic('S');
		// b_send.setPreferredSize(new Dimension(100,40));
		b_send.setActionCommand("Send");
		b_send.setToolTipText(getHtmlText("Send"));
		b_send.addActionListener(this);
		// b_send.setContentAreaFilled(false);
		b_send.setSize(buttonSize);
		b_send.setPreferredSize(buttonSize);
		b_send.setMaximumSize(buttonSize);
		b_send.setMinimumSize(buttonSize);

		p_buttons.setOpaque(false);
		p_buttons.setLayout(new BoxLayout(p_buttons, BoxLayout.X_AXIS));
		p_buttons.add(b_emotion);
		p_buttons.add(b_shake);
		p_buttons.add(b_sendPic);
		p_buttons.add(b_sendFile);
		p_buttons.add(b_snapshot);
		p_buttons.add(b_snapconfig);
		p_buttons.add(Box.createHorizontalGlue());
		p_buttons.add(tb_encrypt);
		p_buttons.add(b_send);
		// p_buttons.add(p_side, BorderLayout.CENTER);
		// p_buttons.add(b_send, BorderLayout.WEST);

		p_inputpaneAndButtons.setOpaque(false);
		// p_msgAndButtons.setBackground(Color.blue);
		p_inputpaneAndButtons.setLayout(new BorderLayout());
		p_inputpaneAndButtons.add(p_buttons, BorderLayout.NORTH);
		p_inputpaneAndButtons.add(sp_input, BorderLayout.CENTER);

		/*
		 * msgPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp_hmsg,
		 * p_msgAndButtons); msgPane.setOneTouchExpandable(true);
		 * msgPane.setOpaque(false);
		 */
		// msgPane.setContinuousLayout(true);
		this.setSize(new Dimension(PeerChatroom.WIDTH_DEFLT,
				PeerChatroom.HEIGHT_DEFLT - 35));
		this.setDividerLocation(0.65);// ������ָ���ߴ����Ч
		this.setResizeWeight(0.62d);
		this.setDividerSize(3);
		// this.setOneTouchExpandable(true);
		this.add(sp_historymsg);
		this.add(p_inputpaneAndButtons);
		// this.setLayout(new BorderLayout());
		// this.add(msgPane, BorderLayout.CENTER);
		// this.setOpaque(false);//�ڵ�ǰʹ�õı�����, ��Ϊ͸���ƺ���̫�ÿ�...
	}

	private String getHtmlText(String text) {
		return ("<html><BODY bgColor=#ffffff><Font color=black>" + text + "</Font></BODY></html>");
	}

	/**
	 * ��ʾϵͳ��Ϣ
	 * 
	 * @param msg
	 *            ϵͳ��Ϣ
	 */
	public void showMsgDialog(String msg) {
		JOptionPane.showMessageDialog((Component) null, msg,
				"Message form the Server", JOptionPane.INFORMATION_MESSAGE);
	}

	public void showFailedSendingMsg(String msg){		
		tp_historymsg.setEditable(true);
		tp_historymsg.setCaretPosition(styledDoc.getLength());// !!!
		styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), italic);
		tp_historymsg
				.replaceSelection("Sorry, failed to send out the msg:\n" + msg + '\n');
		tp_historymsg.setEditable(false);
	}
	/**
	 * �����ⲿ�������,����ʾ��Ϣ
	 * 
	 * @param strs
	 *            1:sender;2:receiver;3:time;4:msg
	 * @param incomingPic ����ͼƬ
	 */	
	public void incomingMsgProcessor(String sender, String time,
			Object msgdata){
		if(msgdata instanceof String)
			incomingMsgProcessor(sender, time, (String)msgdata, null);
		else if(msgdata instanceof ImageIcon)
			incomingMsgProcessor(sender, time, null, (ImageIcon)msgdata);
	}
	
	public void incomingMsgProcessor(String sender, String time,
			final String strmsg, ImageIcon incomingPic) {
		System.out.println("playAudio()...");
		Thread playThd = new Thread(new Runnable() {
			@Override
			public void run() {
				if (strmsg != null && strmsg.equals(shakeMsg))
					playShakeAudio();
				else
					playAudio();
			}
		}, "Beeper");
		playThd.start();
		
		/**
		 * �����ⲿ��������Ϣ�ַ���
		 */
		String label = "[" + sender + "@" + time + "]";
		
		StringBuffer strbuf_msg = null;
		if(strmsg != null){
			strbuf_msg = new StringBuffer(strmsg);
			int caretPos = -1;

			for (; (caretPos = strbuf_msg.indexOf("^n", caretPos + 1)) >= 0;) {
				// ��"^n"�滻Ϊ"\n"
				strbuf_msg.replace(caretPos, caretPos + 2, "\n");
			}
		}
		
		appendToHMsg(label, (strbuf_msg != null)?strbuf_msg.toString():null, incomingPic, true, false);
	}

	/**
	 * ������Ϣʱ������ʾ��
	 * 
	 */
	public void playAudio() {
		final AudioClip msgBeep;
		try {
			// AudioClip audioClip = Applet.newAudioClip(completeURL)
			// codeBase = new URL("file:" + System.getProperty("user.dir") +
			// "/");
			URL url = new URL("file:/" + System.getProperty("user.dir")
					+ System.getProperty("file.separator")
					+ SystemPath.AUDIO_RESOURCE_PATH
					+ "typewpcm.wav");
			msgBeep = Applet.newAudioClip(url);
			msgBeep.play();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}
 
	/**
	 * ����/��������ʱ������ʾ��
	 */
	public void playShakeAudio() {
		final AudioClip msgBeep;
		try {
			// AudioClip audioClip = Applet.newAudioClip(completeURL)
			// codeBase = new URL("file:" + System.getProperty("user.dir") +
			// "/");
			URL url = new URL("file:/" + System.getProperty("user.dir")
					+ System.getProperty("file.separator")
					+ SystemPath.AUDIO_RESOURCE_PATH
					+ "nudgewpcm.wav");
			msgBeep = Applet.newAudioClip(url);
			msgBeep.play();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}

	/**
	 * ����ʷ��Ϣ��������ı�
	 * 
	 * ע��:���ckb_nodisturb Ϊ true,��ʾ������ģʽ����, ��ʱ�ӷ�����������Ⱥ����Ϣֻ�ᱻ��ӵ���ʷ��Ϣ�ַ���
	 * �����ᱻ��ӵ������� ֻ��˽�Ķ������Ϣ�Żᱻ��ӵ�������
	 * 
	 * @param label
	 *            ������/������/����ʱ�� ��ǩ
	 * @param msg
	 *            Ҫ��ӵ���Ϣ��¼���ַ���
	 * @param incomingPic ����ͼƬ
	 * @param visible
	 *            �Ƿ�Ҫ��ӵ���ʷ��Ϣ������(�ɼ�)
	 * @param isFromMe
	 *            �Ƿ����Լ����ⷢ�͵���Ϣ
	 * 
	 * ԭ����������Ĺ����ǽ���Ϣ�������ַ������뵽��ʷ��Ϣ����, ��Ȼ���ǲ�����.
	 * ��Ӳ�����,�ӷ��������ܵ�����ʷ��Ϣ����ͨ������������������뵽��ʷ��Ϣ����.
	 * ����֮,��ǿ�����������ͨ����.����Ϣ��ӵ���Ϣ��¼�Ĺ��ܴ����Ƴ�.
	 */
	public void appendToHMsg(String label, String msg, ImageIcon incomingPic, boolean visible,
			boolean isFromMe) {
		StringBuffer label_buf = new StringBuffer(label);
		
		// System.out.println("label_buf :" + label_buf);
		// System.out.println("msg_buf :" + msg_buf);

		// playAudio();
		// ����ǽ��Լ�����Ϣ��ӵ�������Ϊ�̱�ǩ, ����Ϊ��ɫ.
		Style labelStyle = isFromMe ? green : blue;

		/**
		 * ����Ϣ��ӵ���Ϣ��¼
		 */
		historymsg_save += (label_buf + "\n");
		
		/**
		 * �Ƿ�����������Ϣ
		 */
		if (msg != null && msg.equals(shakeMsg)) {
			// ʹ�û�ɫ��ǩ
			labelStyle = gray;

			DialogEarthquakeCenter dec = new DialogEarthquakeCenter(parent);
			dec.startShake();
			tp_historymsg.setEditable(true);
			tp_historymsg.setCaretPosition(styledDoc.getLength());
			styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(),
					labelStyle);
			tp_historymsg.replaceSelection(label + '\n');
			tp_historymsg.setCaretPosition(styledDoc.getLength());// !!!
			styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), italic);
			tp_historymsg
					.replaceSelection("YOU JUST RECEIVED A SHAKE EMOTION\n");
			tp_historymsg.setEditable(false);
			return;
		}

		/**
		 * if �ж��Ƿ�Ӧ����Ӵ���Ϣ����ʷ��Ϣ����
		 */
		if (visible)// Ӧ����Ӵ���Ϣ����ʷ��Ϣ����
		{
			tp_historymsg.setEditable(true);

			// ��Ϣ������/����/����ʱ�� ��Ϣ
			// ������Ϣ��ǩ
			tp_historymsg.setCaretPosition(styledDoc.getLength());
			styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(),
					labelStyle);
			tp_historymsg.replaceSelection(label + '\n');
			System.out.println("label :" + label);
			
			if(msg != null && !msg.equals("")){
				StringBuffer msg_buf = new StringBuffer(msg);
				historymsg_save += (msg_buf + "\n");
				//�û����Ϳ���Ϣ�ѱ���ֹ, ����������������ǿ���Ϣ,
				//��˵�����͹�������ͼƬ.
				//���Ե�msg��Ϊ�յ�ʱ����Ҫ��ʾ��Ϣ
				System.out.println("msg :" + msg);
	
				tp_historymsg.setCaretPosition(styledDoc.getLength());// !!!
				styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), bold);
				// *****************************************************
				/**
				 * ���ַ�������ͼƬ��,Ȼ�����HMsg�� ͬʱά��һ���ַ���HMsg.
				 */
				int position = 0, caretPos = 0;
	
				// ��position��ʼѰ���ַ���"[F:",�ҵ�����'['��λ��,�Ҳ���"[F:"����-1
				for (; (caretPos = msg_buf.indexOf("[F:", position)) >= 0;) {
					// System.out.println("caretPos : " + caretPos);
					// StringBuffer msgpiece = new
					// StringBuffer(msg_buf.substring(caretPos, caretPos + 6));
					// System.out.println("msgpiece : " + msgpiece);
	
					// 7: ���������ַ����ĳ���
					if (msg_buf.substring(caretPos, caretPos + 7).matches(
							"\\[F\\:[0-9][0-9][0-9]\\]")) {// �������������ʽ
						// �����position��caretPosǰһ���ַ������ַ���
						tp_historymsg.setCaretPosition(styledDoc.getLength());
						tp_historymsg.replaceSelection(msg_buf.substring(position,
								caretPos));
						// ����������ĳ���Ϊ7�����ַ�������ʾ�ı���ͼƬ
						tp_historymsg.setCaretPosition(styledDoc.getLength());
						int faceindex = Integer.parseInt(msg_buf.substring(
								caretPos + 3, caretPos + 6));
						tp_historymsg.insertIcon(getImageIconFace(faceindex));
						// ����position
						position = caretPos + 7;
					} else {// ���������������ʽ
						// �����position��caretPos+3ǰһ���ַ������ַ���
						tp_historymsg.setCaretPosition(styledDoc.getLength());
						tp_historymsg.replaceSelection(msg_buf.substring(position,
								caretPos + 3));
						// ����position
						position = caretPos + 3;
					}
				}
				// ����ʣ�����ַ���
				tp_historymsg.setCaretPosition(styledDoc.getLength());
				tp_historymsg.replaceSelection(msg_buf.substring(position) + '\n');
			}
			if(incomingPic != null){
				tp_historymsg.setCaretPosition(styledDoc.getLength());
				tp_historymsg.insertIcon(incomingPic);
				tp_historymsg.setCaretPosition(styledDoc.getLength());
				tp_historymsg.replaceSelection("\n");
			}
			// System.out.println("msg_buf.substring(position) :" +
			// msg_buf.substring(position));
			// *****************************************************
			tp_historymsg.setEditable(false);// ������Ϊ���ɱ༭
		}
		this.repaint();
	}

	/**
	 * ��ӱ���ͼƬ��msg���봰����
	 * 
	 * @param selectedFace
	 *            ��ѡ���ͼƬ������
	 */
	private void appendFaceToInputPane(int selectedFace) {
		tp_input.setEditable(true);
		/**
		 * �Բ����ַ�������ֱ�Ӳ���ͼƬ
		 */
		// System.out.println("fmNum.format(selectedFace) :" +
		tp_input.replaceSelection("[F:" + fmNum.format(selectedFace) + ']');
	}

	/**
	 * ��ӱ���ͼƬ��msg���봰����
	 * 
	 * @param selectedFace
	 *            ��ѡ���ͼƬ
	 */
	@SuppressWarnings("unused")
	private void appendFaceToInputPane(ImageIcon selectedFace) {
		tp_input.setEditable(true);
		/**
		 * ֱ�Ӳ���ͼƬ
		 */
		tp_input.insertIcon(selectedFace);
	}

	/**
	 * ��ȡ��ʷ��Ϣ
	 * 
	 * @return history messages
	 */
	public String getHistoryMsgs() {
		// return tp_hmsg.getText();
		return historymsg_save;// ������ά���İ汾(�����ַ���������)
	}

	/**
	 * ���û��������Ϣ���͵���ʷ��Ϣ���� (����,Ҫ���͵�������)
	 */
	private void sendMessage() {
		//System.out.println("sendMessage(): >" + tp_input.getText() + "<");
		if(tp_input.getText().equals("")){
			System.out.println("You're trying to send a empty message, it's not suggested.");
			//TODO ���Գ��Ե���������ʾ
			final String BALLOON_TEXT = "<html><center>"
	            + "You're trying to send an empty message<br>"
	            + "which is not suggested/supported.<br>"
	            + "(Click to dismiss this balloon)</center></html>";
			JNABalloon balloon = new JNABalloon(BALLOON_TEXT, tp_input, 100, 20);
			balloon.showBalloon();
			
			return;
		}
		/**
		 * ��ʽ������
		 */
		Date date = new Date();
		// fmDate = new SimpleDateFormat("yyyy/MM/dd E HH:mm:ss");
		String label = "I say@" + fmDate.format(date) + ":";

		appendToHMsg(label, tp_input.getText(), null, true, true);
		/**
		 * ��Է�������Ϣ
		 */
		StringBuffer strbuf_msg = new StringBuffer(tp_input.getText());
		int caretPos = -1;

		// ��position��ʼѰ���ַ���"[F:",�ҵ�����'['��λ��,�Ҳ���"[F:"����-1
		for (; (caretPos = strbuf_msg.indexOf("\r\n", caretPos + 1)) >= 0;) {
			// ��"\n"�滻Ϊ"^n"
			strbuf_msg.replace(caretPos, caretPos + 2, "^n");
		}
		System.out.println("strbuf_msg :" + strbuf_msg);

		boolean succeed = parent.SendMsg(new String(strbuf_msg), tb_encrypt.isSelected());
		if(!succeed){
			//TODO tell user what happend.
			showFailedSendingMsg(new String(strbuf_msg));
		}

		tp_input.setText("");// ��������
	}

	private void sendAShakeEmotion() {
		/**
		 * ��ʽ������
		 */
		Date date = new Date();
		// fmDate = new SimpleDateFormat("yyyy/MM/dd E HH:mm:ss");
		String label = "Sending a Shake Emotion to " + parent.getRoomName()
				+ "@" + fmDate.format(date) + ":";

		//appendToHMsg(label, tp_input.getText(), null, true, true);
		appendToHMsg(label, null, null, true, true);
		/**
		 * ��Է�������Ϣ 999:��ʾ��������
		 */
		boolean succeed = parent.SendMsg(shakeMsg, tb_encrypt.isSelected());
		if(!succeed){
			//TODO tell user what happend.
			showFailedSendingMsg("(It's a shake emotion actually.)");
		}
	}

	/**
	 * ����������ȡ����ͼƬ
	 * 
	 * @param index
	 *            ͼƬ����
	 * @return ����ͼƬ
	 */
	private ImageIcon getImageIconFace(int index) {
		if (index < 105)
			return new ImageIcon(SystemPath.FACES_RESOURCE_PATH + index + ".gif");
		else
			return new ImageIcon(SystemPath.FACES_RESOURCE_PATH + "newFace\\" + (int) (index - 105)
					+ ".png");
	}

	/**
	 * ����ѡ��ͼƬ����, ���������չΪ�����ļ�
	 * @param imgPath ͼƬ·��
	 */
	private void sendAPicture(String imgPath) {
		File thePicFile = new File(imgPath);
		if(thePicFile.exists()){
			BufferedImage bufImg = null;
			try {
				bufImg = javax.imageio.ImageIO.read(thePicFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sendAPicture(bufImg);
		}
	}

	/**
	 * ���ڷ���ͼƬ�ļ�/����ͼƬ
	 * @param img ͼƬbuffer
	 */
	private void sendAPicture(BufferedImage bufImg) {
		if(bufImg == null){
			System.out.println("bufImg is null in sendAPicture()");
			return;
		}
		Date date = new Date();
		String label = "I send a picture to "
			+ parent.getRoomName()
			+ ", at " + fmDate.format(date)
			+ " :";
		StringBuffer label_buf = new StringBuffer(label);
		StringBuffer msg_buf = new StringBuffer("A Screen Snapshot");

		/**
		 * ����Ϣ��ӵ���Ϣ��¼
		 */
		historymsg_save += (label_buf + "\n");
		historymsg_save += (msg_buf + "\n");

		tp_historymsg.setEditable(true);

		// ��Ϣ������/����/����ʱ�� ��Ϣ
		// ������Ϣ��ǩ
		tp_historymsg.setCaretPosition(styledDoc.getLength());
		styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), blue);
		tp_historymsg.replaceSelection(label + '\n');
		System.out.println("label :" + label);

		ImageIcon img = new ImageIcon(bufImg);
		tp_historymsg.setCaretPosition(styledDoc.getLength());// !!!
		tp_historymsg.insertIcon(img);
		tp_historymsg.setCaretPosition(styledDoc.getLength());
		tp_historymsg.replaceSelection("\n");
		tp_historymsg.setEditable(false);// ������Ϊ���ɱ༭
		boolean succeed = parent.SendMsg(bufImg, tb_encrypt.isSelected());
		if(!succeed){
			//TODO tell user what happend.
			showFailedSendingMsg("(It's a picture actually.)");
		}
	}
	
	/**
	 * ���ڷ���ѡ����ļ�
	 * @param filePath �ļ�·��
	 */
	private void sendAFile(String filePath) {
		File theFile = new File(filePath);
		if(theFile.exists()){
			boolean succeed = parent.SendMsg(theFile, tb_encrypt.isSelected());
			if(!succeed){
				//TODO tell user what happend.
				showFailedSendingMsg("(It's a file actually.)");
			}
		}
	}

	/**
	 * (��ť)�¼���Ӧ
	 */
	public void actionPerformed(ActionEvent e) {
		JButton srcButton = (JButton) e.getSource();
		if (srcButton.getActionCommand().equals("Send")) {
			System.out.println("You clicked the button : Send");
			// insert(getText(), null);
			sendMessage();
		} else if (srcButton.getActionCommand().equals("Emotion")) {
			/**
			 * �������: Ŀǰֻ�ܲ��뵽�������, ���뵽��ʷ��Ϣ���л�û��ʵ��. ���û��ֱ�ӿ����ķ���, ��ôֻ��������һ������:
			 * ���ǰѱ������ַ���ʾ,�ڲ��뵽��ʷ��Ϣ����ʱ�����ַ����� ���������Ƚ��鷳
			 * 
			 * ...������̫�鷳,�ѽ��,���ᳵ��˧�İ취
			 */
			System.out.println("You clicked the button : InsertImage");
			// ��ʾ����ѡ�񴰿�
			selFace.setLocationRelativeTo(b_emotion);
			selFace.setVisible(true);

			int selectedfaceIndex = selFace.getSelectedFaceIndex();
			if (selectedfaceIndex != -1) {
				System.out.println("You selected the face : "
						+ selectedfaceIndex + ".gif");
				appendFaceToInputPane(selectedfaceIndex);
			}
			/*
			 * ImageIcon selectedface = getImageIconFace(selectedfaceIndex); if
			 * (selectedface != null) { System.out.println("You selected the
			 * face : " + selectedfaceIndex + ".gif");
			 * appendFaceToInputPane(selectedface); }
			 */
		} else if (srcButton.getActionCommand().equals("Shake")) {
			DialogEarthquakeCenter dec = new DialogEarthquakeCenter(parent);
			dec.startShake();// �Ի������setModal (false)�ſ��Զ���, ������
			//TODO ����Ӧ�ò�������, ����ʱ��ֹ, ��ֹ�ص�, ��ͻ.
			// playShakeAudio();
			/**
			 * ����һ��������
			 */
			sendAShakeEmotion();
		} else if (srcButton.getActionCommand().equals("SendPic")) {
			JFileChooser chooser = new JFileChooser();
			FileFilter filter = new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory()
							|| (f.isFile() && (f.getName().endsWith(".PNG")
									|| f.getName().endsWith(".png")
									|| f.getName().endsWith(".JPG")
									|| f.getName().endsWith(".jpg")
									|| f.getName().endsWith(".BMP")
									|| f.getName().endsWith(".bmp")
									|| f.getName().endsWith(".GIF") || f
									.getName().endsWith(".gif")));
				}

				@Override
				public String getDescription() {
					return "BMP, JPG, PNG, or GIF";
				}
			};
			chooser.setFileFilter(filter);
			chooser.setDialogTitle("��ѡ����Ҫ���͵�ͼƬ");
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// getJtf_pic().setText(chooser.getSelectedFile().getPath());
				System.out.println("You chose a pic: "
						+ chooser.getSelectedFile().getPath());
				sendAPicture(chooser.getSelectedFile().getPath());
			}
		}  else if (srcButton.getActionCommand().equals("SendFile")) {
			JFileChooser chooser = new JFileChooser();
			FileFilter filter = new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory() || f.isFile();
				}
				@Override
				public String getDescription() {
					return "*.*";
				}
			};
			chooser.setFileFilter(filter);
			chooser.setDialogTitle("��ѡ����Ҫ���͵��ļ�");
			int returnVal = chooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// getJtf_pic().setText(chooser.getSelectedFile().getPath());
				System.out.println("You chose a pic: "
						+ chooser.getSelectedFile().getPath());
				sendAFile(chooser.getSelectedFile().getPath());
			}
		} else if (srcButton.getActionCommand().equals("Snapshot")) {
			// λ��Ӧ���������JButton��λ��
			//menuSnap.show((Component) e.getSource(), 0, 26);
			doSnap.doClick();
		} else if (srcButton.getActionCommand().equals("SnapshotConfig")) {
			// λ��Ӧ���������JButton��λ��
			menuSnap.show((Component) e.getSource(), 0, 26);
		}
	}
}

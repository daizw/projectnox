package noxUI;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
 * ����Ϣ���ӵ���Ϣ��¼, Ȼ��ͨ����鷢���˺�noDisturb�����������Ƿ����Ӹ���Ϣ����ʷ��Ϣ����
 */
public class ChatRoomPane extends JSplitPane implements ActionListener// ,MouseListener
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
	private JButton b_InsertImg;
	/**
	 * �����
	 */
	private FaceDialog selFace;
	/**
	 * ������
	 */
	private JButton b_shake;
	/**
	 * ͼƬ������ʽ������
	 */
	public static final DecimalFormat fmNum = new DecimalFormat("000");
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
	 * �ַ��������м����
	 */
	private int position;
	/**
	 * �ַ�������
	 */
	private int strLength;
	/**
	 * ��ӭ��Ϣ
	 */
	private String sayHello;
	/**
	 * ������
	 */
	private boolean noDisturb = false;
	/**
	 * ͼ��·��
	 */
	private String path_icon = new String("resrc\\icons\\");
	/**
	 * ����ͼƬ·��
	 */
	private String path_faces = new String("resrc\\faces\\");

	/***************************************************************************
	 * //
	 */
	/**
	 * ������������(ͨ������paintComponent()) (�������δ���) ...But CPUռ���ʴﵽ100% !Faint! ����ʧ��;
	 * 
	 * ��һЩ���ܵ�����Ļˢ�µİ�ť��Ӧ�������Ӧ������ setRenderingHints(hints); ������������ �����Ǹ����ķ���,
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
	JFrame parent;

	/**
	 * JSplitPane �������, �������/��Ϣ����/���鰴ť/������ť/.../���Ͱ�ť ��
	 * 
	 * @param par
	 *            �����, ����ʹ����par��
	 */
	public ChatRoomPane(JFrame par) {
		super(JSplitPane.VERTICAL_SPLIT);
		parent = par;

		sayHello = new String(
				"\t------====  Welcome to the Chat Room  ====------\n"
						+ "\t  ------====     What do U wanna say ?   ====------\n");
		strLength = sayHello.length();
		position = 0;
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

		red = styledDoc.addStyle("red", normal);
		StyleConstants.setForeground(red, Color.red);

		bold = styledDoc.addStyle("bold", normal);
		StyleConstants.setBold(bold, true);

		italic = styledDoc.addStyle("italic", normal);
		StyleConstants.setItalic(italic, true);

		bigSize = styledDoc.addStyle("bigSize", normal);
		StyleConstants.setFontSize(bigSize, 24);
		/**
		 * ���ӷ���ı�(��ӭ��Ϣ)
		 */
		styledDoc.setLogicalStyle(0, red);

		tp_historymsg.replaceSelection(sayHello);

		// tp_historymsg.setToolTipText("History Messages");
		tp_historymsg.setBackground(new Color(180, 250, 250));
		tp_historymsg.setSelectionColor(Color.YELLOW);
		/**
		 * �������Ϊ�ɱ༭:�û���������ɱ༭ �������Ϊ���ɱ༭:�����޷������������ı�.��ô��??
		 * 
		 * ...�ѽ��(��֪�Ƿ��㹻��ȫ): ͨ���������Ϊ���ɱ༭ �������������ı�ʱ,��ʱ��Ϊ�ɱ༭;
		 * ���������ò����λ��(��֤�����λ�����ı�β) (�����У�������λ��,�û����ܲ��ܱ༭,�����ǿ��Ըı�����λ��)
		 * �����ı���,��������Ϊ���ɱ༭. �㶨!^-^ ���appendToHMsg();
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
				.setToolTipText("Input your message and press \"Send\" or type Ctrl+Enter");

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
					appendMyMsg2HMsg();
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

		b_InsertImg = new JButton(new ImageIcon(path_icon + "insertimg.png"));
		b_InsertImg.setToolTipText("Insert a image to the message");
		b_InsertImg.setActionCommand("InsertImage");
		b_InsertImg.addActionListener(this);
		// b_InsertImg.setContentAreaFilled(false);
		b_InsertImg.setSize(buttonSize);
		b_InsertImg.setPreferredSize(buttonSize);
		b_InsertImg.setMaximumSize(buttonSize);
		b_InsertImg.setMinimumSize(buttonSize);

		/**
		 * ����������������ڵĻ�, ������޷���ȡ��ѡ��ı�������.
		 */
		selFace = new FaceDialog("Insert a face", true, path_faces);
		// ��FaceDialog.setDefaultLookAndFeelDecorated(true);����ͬʱʹ��
		selFace.setBounds(450, 350, FaceDialog.FACECELLWIDTH
				* FaceDialog.FACECOLUMNS, FaceDialog.FACECELLHEIGHT
				* FaceDialog.FACEROWS + 30);// 30Ϊb_cr_cancel�ĸ߶�
		selFace.pack();

		b_shake = new JButton(new ImageIcon(path_icon + "shake.png"));
		b_shake.setToolTipText("Rock and Roll !");
		b_shake.setActionCommand("Shake");
		b_shake.addActionListener(this);
		b_shake.setSize(buttonSize);
		b_shake.setPreferredSize(buttonSize);
		b_shake.setMaximumSize(buttonSize);
		b_shake.setMinimumSize(buttonSize);

		// b_send = new JButton("Send");
		b_send = new JButton(new ImageIcon(path_icon + "send.png"));
		b_send.setMnemonic('S');
		// b_send.setPreferredSize(new Dimension(100,40));
		b_send.setActionCommand("Send");
		b_send.addActionListener(this);
		// b_send.setContentAreaFilled(false);
		b_send.setSize(buttonSize);
		b_send.setPreferredSize(buttonSize);
		b_send.setMaximumSize(buttonSize);
		b_send.setMinimumSize(buttonSize);

		p_buttons.setOpaque(false);
		p_buttons.setLayout(new BoxLayout(p_buttons, BoxLayout.X_AXIS));
		p_buttons.add(b_InsertImg);
		p_buttons.add(b_shake);
		p_buttons.add(Box.createHorizontalGlue());
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
		this.setSize(new Dimension(Chatroom.WIDTH_DEFLT,
				Chatroom.HEIGHT_DEFLT - 35));
		this.setDividerLocation(0.65);// ������ָ���ߴ����Ч
		this.setDividerSize(3);
		// this.setOneTouchExpandable(true);
		this.add(sp_historymsg);
		this.add(p_inputpaneAndButtons);
		// this.setLayout(new BorderLayout());
		// this.add(msgPane, BorderLayout.CENTER);
		// this.setOpaque(false);//�ڵ�ǰʹ�õı�����, ��Ϊ͸���ƺ���̫�ÿ�...
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

	/**
	 * �����ⲿ�������,����ʾ��Ϣ
	 * 
	 * @param strs
	 *            1:sender;2:receiver;3:time;4:msg
	 */
	public void receiveMsgAndAccess(String[] strs) {// 1:sender;2:receiver;3:time;4:msg
		System.out.println("playAudio()...");
		// System.out.println("public void receiveMsgAndAccess(String[] strs)");
		// System.out.println("currentUsername :" + currentUsername);

		/**
		 * �����ⲿ��������Ϣ�ַ���
		 */
		StringBuffer strbuf_msg = new StringBuffer(strs[4]);
		int caretPos = -1;

		for (; (caretPos = strbuf_msg.indexOf("^n", caretPos + 1)) >= 0;) {
			// ��"^n"�滻Ϊ"\n"
			strbuf_msg.replace(caretPos, caretPos + 2, "\n");
		}

		if (strs[2].equals("ME"))// �˴�Ҫ��ȡ��ǰ�û����û���
		{
			String label = strs[1] + " say to me at " + strs[3];
			appendToHMsg(label, strbuf_msg.toString(), true);
			playAudio();
		} else if (strs[0].equals("fromAll"))// Ⱥ����Ϣ
		{
			System.out.println("noDisturb " + noDisturb);
			String label = strs[1] + " say to ALL at " + strs[2] + ":";
			// appendToHMsg(label, strbuf_msg.toString(), !noDisturb);
			if (noDisturb)// �� �����Ŵ�
			{
				appendToHMsg(label, strbuf_msg.toString(), false);
				return;
			} else// ������δ��
			{
				appendToHMsg(label, strbuf_msg.toString(), true);
				playAudio();
			}
		} else
			;
	}

	/**
	 * ������Ϣʱ������ʾ��
	 * 
	 */
	public void playAudio() {
		AudioClip playsound;
		try {
			// AudioClip audioClip = Applet.newAudioClip(completeURL)
			// codeBase = new URL("file:" + System.getProperty("user.dir") +
			// "/");
			URL url = new URL("file:\\" + System.getProperty("user.dir")
					+ "\\resrc\\audio\\type.wav");
			playsound = Applet.newAudioClip(url);
			// System.out.println(url);
			playsound.play();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.toString());
		}
	}
	/**
	 * ����ʷ��Ϣ���������ı�
	 * 
	 * ע��:���ckb_nodisturb Ϊ true,��ʾ������ģʽ����, ��ʱ�ӷ�����������Ⱥ����Ϣֻ�ᱻ���ӵ���ʷ��Ϣ�ַ���
	 * �����ᱻ���ӵ������� ֻ��˽�Ķ������Ϣ�Żᱻ���ӵ�������
	 * 
	 * @param msg
	 *            Ҫ���ӵ���Ϣ��¼���ַ���
	 * @param visible
	 *            �Ƿ�Ҫ���ӵ���ʷ��Ϣ������(�ɼ�)
	 * 
	 * ԭ����������Ĺ����ǽ���Ϣ�������ַ������뵽��ʷ��Ϣ����, ��Ȼ���ǲ�����.
	 * ���Ӳ�����,�ӷ��������ܵ�����ʷ��Ϣ����ͨ������������������뵽��ʷ��Ϣ���� ����֮,��ǿ�����������ͨ���� ����Ϣ���ӵ���Ϣ��¼�Ĺ��ܴ����Ƴ�,
	 */
	public void appendToHMsg(String label, String msg, boolean visible) {
		StringBuffer label_buf = new StringBuffer(label);
		StringBuffer msg_buf = new StringBuffer(msg);

		// System.out.println("label_buf :" + label_buf);
		// System.out.println("msg_buf :" + msg_buf);

		//playAudio();
		
		/**
		 * ����Ϣ���ӵ���Ϣ��¼
		 */
		historymsg_save += (label_buf + "\n");
		historymsg_save += (msg_buf + "\n");

		/**
		 * if �ж��Ƿ�Ӧ�����Ӵ���Ϣ����ʷ��Ϣ����
		 */
		if (visible)// Ӧ�����Ӵ���Ϣ����ʷ��Ϣ����
		{
			tp_historymsg.setEditable(true);

			// System.out.println("label :" + label);
			// ��Ϣ������/����/����ʱ�� ��Ϣ
			// ������Ϣ��ǩ
			tp_historymsg.setCaretPosition(styledDoc.getLength());
			styledDoc.setLogicalStyle(tp_historymsg.getCaretPosition(), blue);
			tp_historymsg.replaceSelection(label + '\n');
			System.out.println("label :" + label);

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

				// 7: ����������ַ����ĳ���
				if (msg_buf.substring(caretPos, caretPos + 7).matches(
						"\\[F\\:[0-9][0-9][0-9]\\]")) {// ��������������ʽ
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
				} else {// ����������������ʽ
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
			// System.out.println("msg_buf.substring(position) :" +
			// msg_buf.substring(position));
			// *****************************************************
			tp_historymsg.setEditable(false);// ������Ϊ���ɱ༭
		}
	}

	/**
	 * ���ӱ���ͼƬ��msg���봰����
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
	 *  ���ӱ���ͼƬ��msg���봰����
	 * @param selectedFace
	 * 			��ѡ���ͼƬ
	 */
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
	private void appendMyMsg2HMsg() {
		/**
		 * ��ʽ������
		 */
		Date date = new Date();
		Format fmDate = new SimpleDateFormat("yyyy/MM/dd E HH:mm:ss");
		String label = "I say to someone, at " + fmDate.format(date) + " :";

		appendToHMsg(label, tp_input.getText(), true);
		/**
		 * �������������Ϣ
		 */
		StringBuffer strbuf_msg = new StringBuffer(tp_input.getText());
		int caretPos = -1;

		// ��position��ʼѰ���ַ���"[F:",�ҵ�����'['��λ��,�Ҳ���"[F:"����-1
		for (; (caretPos = strbuf_msg.indexOf("\r\n", caretPos + 1)) >= 0;) {
			// ��"\n"�滻Ϊ"^n"
			strbuf_msg.replace(caretPos, caretPos + 2, "^n");
		}
		System.out.println("strbuf_msg :" + strbuf_msg);

		tp_input.setText("");// ��������
	}

	/**
	 * ����������ȡ����ͼƬ
	 * @param index ͼƬ����
	 * @return ����ͼƬ
	 */
	private ImageIcon getImageIconFace(int index) {
		if (index < 105)
			return new ImageIcon(path_faces + index + ".gif");
		else
			return new ImageIcon(path_faces + "newFace\\" + (int) (index - 105)
					+ ".png");
	}

	/**
	 * (��ť)�¼���Ӧ
	 */
	public void actionPerformed(ActionEvent e) {
		JButton srcButton = (JButton) e.getSource();
		if (srcButton.getActionCommand().equals("Send")) {
			System.out.println("You clicked the button : Send");
			//insert(getText(), null);
			appendMyMsg2HMsg();
		} else if (srcButton.getActionCommand().equals("InsertImage")) {
			/**
			 * �������: Ŀǰֻ�ܲ��뵽�������, ���뵽��ʷ��Ϣ���л�û��ʵ��. ���û��ֱ�ӿ����ķ���, ��ôֻ��������һ������:
			 * ���ǰѱ������ַ���ʾ,�ڲ��뵽��ʷ��Ϣ����ʱ�����ַ����� ���������Ƚ��鷳
			 * 
			 * ...������̫�鷳,�ѽ��,���ᳵ��˧�İ취
			 */
			System.out.println("You clicked the button : InsertImage");
			// ��ʾ����ѡ�񴰿�
			selFace.setVisible(true);
			int selectedfaceIndex = selFace.getSelectedFaceIndex();			
			if (selectedfaceIndex != -1) {
				System.out.println("You selected the face : "
						+ selectedfaceIndex + ".gif");
				appendFaceToInputPane(selectedfaceIndex);
			}
			/*ImageIcon selectedface = getImageIconFace(selectedfaceIndex);
			if (selectedface != null) {
				System.out.println("You selected the face : "
						+ selectedfaceIndex + ".gif");
				appendFaceToInputPane(selectedface);
			}*/
		} else if (srcButton.getActionCommand().equals("Shake")) {
			// AboutDialog about = new AboutDialog();
			DialogEarthquakeCenter dec = new DialogEarthquakeCenter(parent);
			dec.startShake();// �Ի������setModal (false)�ſ��Զ���, ������
		}
	}
	
	/*// + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + +
	public String[] getText() {
		List<String> array = new ArrayList<String>();
		Map<Integer, String> mp = new HashMap<Integer, String>();
		String t = tp_input.getText();
		System.out.println("t--->" + t);
		
		List<Element> els = getAllElements();
		for (Element el : els) {
			Icon icon = StyleConstants.getIcon(el.getAttributes());
			if (icon != null) {
				String tmp = Chatroom.ICON_PREFIX.concat(new File(
						((ImageIcon) icon).getDescription()).getName());
				mp.put(el.getStartOffset(), tmp);
			}
		}

		for (int c = 0; c < t.length(); c++) {
			String s = t.substring(c, c + 1);
			String v = mp.get(new Integer(c));
			if (v == null)
				array.add(s);
			else
				array.add(v);
		}
		String[] tmp = new String[array.size()];
		array.toArray(tmp);
		return tmp;
	}
	// + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + +
	List<Element> getAllElements() {
		Element[] roots = tp_input.getStyledDocument().getRootElements();
		return getAllElements(roots);
	}

	private List<Element> getAllElements(Element[] roots) {
		List<Element> icons = new LinkedList<Element>();
		for (int a = 0; a < roots.length; a++) {
			if (roots[a] == null)
				continue;
			icons.add(roots[a]);
			for (int c = 0; c < roots[a].getElementCount(); c++) {
				Element element = roots[a].getElement(c);
				icons.addAll(getAllElements(new Element[] { element }));
			}
		}
		return icons;
	}
	// + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + +
	public void insert(String[] str, AttributeSet attr) {
		List<String> strs = new ArrayList<String>();
		if (str.length == 0) {
			System.out.println("str.length == 0");
			return;
		}
		boolean b = str[str.length - 1].endsWith("\n");
		for (String s : str) {
			strs.add(s.replaceAll("\r", "").replaceAll("\n", ""));
		}
		if (b)
			strs.add("\n");

		Document doc = tp_historymsg.getStyledDocument();
		String tmp = "";
		System.out.println("--->--->" + str.length);
		for (String s : strs) {
			if (s.length() > 0) {
				if (s.matches(path_faces.concat(Chatroom.ICON_SUFFIX_REGEX))) {
					try {
						if (tmp.length() > 0)
							doc.insertString(doc.getLength(), tmp, attr);
						tp_historymsg.setSelectionStart(doc.getLength());
						tp_historymsg.insertIcon(new ImageIcon(
								Chatroom.ICON_RESOURCES_PATH.concat(s)));
						tmp = "";
					} catch (Exception ex) {
					}
				} else {
					tmp = tmp.concat(s);
				}
			}

			//System.out.println("--->" + s);
		}
		try {
			if (tmp.length() > 0)
				doc.insertString(doc.getLength(), tmp, attr);
		} catch (Exception ex) {
		}
	}
	// + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + + +
*/
}
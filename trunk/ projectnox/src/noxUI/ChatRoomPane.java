package noxUI;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
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
import javax.swing.JLabel;
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
 * ����Ϣ��ӵ���Ϣ��¼, Ȼ��ͨ����鷢���˺�noDisturb�����������Ƿ���Ӹ���Ϣ����ʷ��Ϣ����
 */
public class ChatRoomPane extends JSplitPane implements ActionListener// ,MouseListener
{
	/**
	 * we don't know more about it
	 */
	private static final long serialVersionUID = -1915394855935441419L;

	// private PrintWriter pw;
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
	/**
	 * ....Ŀǰ���������и�ȱ��: Ⱥ�ĺ�˽�ĵ���Ϣ��ʾ��ͬһ��������,�Ե��е��� Ŀ��:˽�Ĵ����ܹ���������
	 * (����˽��ʱ�û�����ѡ������Ⱥ����Ϣ--��ѡ)
	 */
	JFrame parent;
	/**
	 * JSplitPane
	 * @param par �����, ����ʹ����par��
	 */
	public ChatRoomPane(JFrame par/* String username, String[] onlineUser */) {
		super(JSplitPane.VERTICAL_SPLIT);
		// pw = cpw;
		parent = par;
		System.out
				.println("public ChatRoomPane(String username, String[] onlineUser)");// /////////////
		// for (int i = 0; i < onlineUser.length; ++i)
		// {
		// System.out.print(onlineUser[i]);///////////tjj///////////////
		// }//////////////////////////////////////////

		sayHello = new String(
				"------====  Welcome to the Chat Room  ====------\n"
						+ "  ------====     What do U wanna say ?   ====------\n");
		strLength = sayHello.length();
		position = 0;
		// currentUsername = username;
		/**
		 * ��ʷ��Ϣ����
		 */
		tp_hmsg = new JTextPane();// ��Ȼ�����˳�ʼ��!
		hmsg_save = new String();
		hmsg_save += sayHello;
		styledDoc = tp_hmsg.getStyledDocument();
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
		 * ��ӷ���ı�(��ӭ��Ϣ)
		 */
		styledDoc.setLogicalStyle(0, red);

		tp_hmsg.replaceSelection(sayHello);

		tp_hmsg.setToolTipText("History Messages");
		tp_hmsg.setBackground(new Color(180, 250, 250));
		tp_hmsg.setSelectionColor(Color.YELLOW);
		/**
		 * �������Ϊ�ɱ༭:�û���������ɱ༭ �������Ϊ���ɱ༭:�����޷�����������ı�.��ô��??
		 * 
		 * ...�ѽ��(��֪�Ƿ��㹻��ȫ): ͨ���������Ϊ���ɱ༭ ������������ı�ʱ,��ʱ��Ϊ�ɱ༭;
		 * ���������ò����λ��(��֤�����λ�����ı�β)
		 * (�����У�������λ��,�û����ܲ��ܱ༭,�����ǿ��Ըı�����λ��)
		 * ����ı���,��������Ϊ���ɱ༭. �㶨!^-^ ���appendToHMsg();
		 */
		tp_hmsg.setEditable(false);
		sp_hmsg = new JScrollPane(tp_hmsg);
		sp_hmsg.setAutoscrolls(true);
		sp_hmsg.setPreferredSize(new Dimension(300, 200));

		p_msgAndButtons = new JPanel();

		/**
		 * ��Ϣ���봰��
		 */
		tp_msg = new JTextPane();
		// tp_msg.setText(sayHello);
		tp_msg
				.setToolTipText("Input your message and press \"Send\" or type Ctrl+Enter");

		/**
		 * �����¼�������/�¼�����
		 */
		tp_msg.addKeyListener(new KeyListener() {
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
		sp_msg = new JScrollPane(tp_msg);
		sp_msg.setPreferredSize(new Dimension(200, 100));

		/**
		 * ������鰴ť �� ���Ͱ�ť
		 */
		p_buttons = new JPanel();
		
		Dimension buttonSize = new Dimension(26, 26);

		b_InsertImg = new JButton(new ImageIcon(path_icon + "insertimg.png"));
		b_InsertImg.setToolTipText("Insert a image to the message");
		b_InsertImg.setActionCommand("InsertImage");
		b_InsertImg.addActionListener(this);
		//b_InsertImg.setContentAreaFilled(false);
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

		JButton b_shake = new JButton(new ImageIcon(path_icon + "shake.png"));
		b_shake.setToolTipText("Rock and Roll !");
		b_shake.setActionCommand("Shake");
		b_shake.addActionListener(this);
		b_shake.setSize(buttonSize);
		b_shake.setPreferredSize(buttonSize);
		b_shake.setMaximumSize(buttonSize);
		b_shake.setMinimumSize(buttonSize);
		
		//b_send = new JButton("Send");
		b_send = new JButton(new ImageIcon(path_icon + "send.png"));
		b_send.setMnemonic('S');
		// b_send.setPreferredSize(new Dimension(100,40));
		b_send.setActionCommand("Send");
		b_send.addActionListener(this);
		//b_send.setContentAreaFilled(false);
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

		p_msgAndButtons.setOpaque(false);
		//p_msgAndButtons.setBackground(Color.blue);
		p_msgAndButtons.setLayout(new BorderLayout());
		p_msgAndButtons.add(p_buttons, BorderLayout.NORTH);
		p_msgAndButtons.add(sp_msg, BorderLayout.CENTER);
		
		/*msgPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp_hmsg, p_msgAndButtons);
		msgPane.setOneTouchExpandable(true);
		msgPane.setOpaque(false);*/
		//msgPane.setContinuousLayout(true);

		this.setOneTouchExpandable(true);
		this.add(sp_hmsg);
		this.add(p_msgAndButtons);
		//this.setLayout(new BorderLayout());
		//this.add(msgPane, BorderLayout.CENTER);
		//this.setOpaque(false);//�ڵ�ǰʹ�õı�����, ��Ϊ͸���ƺ���̫�ÿ�...
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
			String label = strs[1] + " say to I at " + strs[3];
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
	 * ����ʷ��Ϣ��������ı�
	 * 
	 * ע��:���ckb_nodisturb Ϊ true,��ʾ������ģʽ����, ��ʱ�ӷ�����������Ⱥ����Ϣֻ�ᱻ��ӵ���ʷ��Ϣ�ַ���
	 * �����ᱻ��ӵ������� ֻ��˽�Ķ������Ϣ�Żᱻ��ӵ�������
	 * 
	 * @param msg
	 *            Ҫ��ӵ���Ϣ��¼���ַ���
	 * @param visible
	 *            �Ƿ�Ҫ��ӵ���ʷ��Ϣ������(�ɼ�)
	 * 
	 * ԭ����������Ĺ����ǽ���Ϣ�������ַ������뵽��ʷ��Ϣ����, ��Ȼ���ǲ�����.
	 * ��Ӳ�����,�ӷ��������ܵ�����ʷ��Ϣ����ͨ������������������뵽��ʷ��Ϣ���� ����֮,��ǿ�����������ͨ���� ����Ϣ��ӵ���Ϣ��¼�Ĺ��ܴ����Ƴ�,
	 */
	public void appendToHMsg(String label, String msg, boolean visible) {
		StringBuffer label_buf = new StringBuffer(label);
		StringBuffer msg_buf = new StringBuffer(msg);

		// System.out.println("label_buf :" + label_buf);
		// System.out.println("msg_buf :" + msg_buf);

		/**
		 * ����Ϣ��ӵ���Ϣ��¼
		 */
		hmsg_save += (label_buf + "\n");
		hmsg_save += (msg_buf + "\n");

		/**
		 * if �ж��Ƿ�Ӧ����Ӵ���Ϣ����ʷ��Ϣ����
		 */
		if (visible)// Ӧ����Ӵ���Ϣ����ʷ��Ϣ����
		{
			tp_hmsg.setEditable(true);

			// System.out.println("label :" + label);
			// ��Ϣ������/����/����ʱ�� ��Ϣ
			// ������Ϣ��ǩ
			tp_hmsg.setCaretPosition(styledDoc.getLength());
			styledDoc.setLogicalStyle(tp_hmsg.getCaretPosition(), blue);
			tp_hmsg.replaceSelection(label + '\n');
			System.out.println("label :" + label);

			tp_hmsg.setCaretPosition(styledDoc.getLength());// !!!
			styledDoc.setLogicalStyle(tp_hmsg.getCaretPosition(), bold);
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
					tp_hmsg.setCaretPosition(styledDoc.getLength());
					tp_hmsg.replaceSelection(msg_buf.substring(position,
							caretPos));
					// ����������ĳ���Ϊ7�����ַ�������ʾ�ı���ͼƬ
					tp_hmsg.setCaretPosition(styledDoc.getLength());
					int faceindex = Integer.parseInt(msg_buf.substring(caretPos + 3,
							caretPos + 6));
					if(faceindex<105)
						tp_hmsg.insertIcon(new ImageIcon(path_faces
							+ faceindex + ".gif"));
					else//�±���
					{
						faceindex -= 105;
						tp_hmsg.insertIcon(new ImageIcon(path_faces + "newFace\\"
								+ faceindex + ".png"));
					}
						
					// ����position
					position = caretPos + 7;
				} else {// ���������������ʽ
					// �����position��caretPos+3ǰһ���ַ������ַ���
					tp_hmsg.setCaretPosition(styledDoc.getLength());
					tp_hmsg.replaceSelection(msg_buf.substring(position,
							caretPos + 3));
					// ����position
					position = caretPos + 3;
				}
			}
			// ����ʣ�����ַ���
			tp_hmsg.setCaretPosition(styledDoc.getLength());
			tp_hmsg.replaceSelection(msg_buf.substring(position) + '\n');
			// System.out.println("msg_buf.substring(position) :" +
			// msg_buf.substring(position));
			// *****************************************************
			tp_hmsg.setEditable(false);// ������Ϊ���ɱ༭
		}
	}

	/**
	 * ��ӱ���ͼƬ��msg���봰����
	 * 
	 * @param selectedFace
	 *            ��ѡ���ͼƬ������
	 */
	private void appendFaceToMsg(int selectedFace) {
		tp_msg.setEditable(true);
		/**
		 * �Բ����ַ�������ֱ�Ӳ���ͼƬ
		 */
		// System.out.println("fmNum.format(selectedFace) :" +
		tp_msg.replaceSelection("[F:" + fmNum.format(selectedFace) + ']');
	}
	private void appendFaceToMsg(ImageIcon selectedFace) {
		tp_msg.setEditable(true);
		/**
		 * ֱ�Ӳ���ͼƬ
		 */
		tp_msg.insertIcon(selectedFace);
	}

	/**
	 * ��ȡ��ʷ��Ϣ
	 * 
	 * @return history messages
	 */
	public String getHistoryMsgs() {
		// return tp_hmsg.getText();
		return hmsg_save;// ������ά���İ汾(�����ַ���������)
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

		appendToHMsg(label, tp_msg.getText(), true);
		/**
		 * �������������Ϣ
		 */
		StringBuffer strbuf_msg = new StringBuffer(tp_msg.getText());
		int caretPos = -1;

		// ��position��ʼѰ���ַ���"[F:",�ҵ�����'['��λ��,�Ҳ���"[F:"����-1
		for (; (caretPos = strbuf_msg.indexOf("\r\n", caretPos + 1)) >= 0;) {
			// ��"\n"�滻Ϊ"^n"
			strbuf_msg.replace(caretPos, caretPos + 2, "^n");
		}
		System.out.println("strbuf_msg :" + strbuf_msg);

		tp_msg.setText("");// ��������
	}

	/**
	 * ��ʷ��ϢJScrollPane
	 */
	private JScrollPane sp_hmsg;
	/**
	 * ��ʷ��ϢJTextPane
	 */
	private JTextPane tp_hmsg;
	/**
	 * ��Ϣ���봰�ڼ����
	 */
	private JPanel p_msgAndButtons;
	/**
	 * ��ϢJScrollPane
	 */
	private JScrollPane sp_msg;
	/**
	 * ��ʷ��ϢJTextPane
	 */
	private JTextPane tp_msg;
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
	String hmsg_save;
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

	// int selectedface;
	/**
	 * (��ť)�¼���Ӧ
	 */
	public void actionPerformed(ActionEvent e) {
		JButton srcButton = (JButton) e.getSource();
		if (srcButton.getActionCommand().equals("Send")) {
			System.out.println("You clicked the button : Send");
			appendMyMsg2HMsg();
		} else if (srcButton.getActionCommand().equals("InsertImage")) {
			/**
			 * �������:
			 * Ŀǰֻ�ܲ��뵽�������, ���뵽��ʷ��Ϣ���л�û��ʵ��.
			 * ���û��ֱ�ӿ����ķ���, ��ôֻ��������һ������:
			 * ���ǰѱ������ַ���ʾ,�ڲ��뵽��ʷ��Ϣ����ʱ�����ַ����� ���������Ƚ��鷳
			 * 
			 * ...������̫�鷳,�ѽ��,���ᳵ��˧�İ취
			 */
			System.out.println("You clicked the button : InsertImage");
			// ��ʾ����ѡ�񴰿�
			selFace.setVisible(true);
			int selectedfaceIndex = selFace.getSelectedFaceIndex();
			ImageIcon selectedface = selFace.getSelectedFace();
			if (selectedfaceIndex != -1) {
				System.out.println("You selected the face : " + selectedfaceIndex
						+ ".gif");
				appendFaceToMsg(selectedfaceIndex);
			}
			if (selectedface != null) {
				System.out.println("You selected the face : " + selectedfaceIndex
						+ ".gif");
				appendFaceToMsg(selectedface);
			}
		}
		else if (srcButton.getActionCommand().equals("Shake")){
			//AboutDialog about = new AboutDialog();
			DialogEarthquakeCenter dec = new DialogEarthquakeCenter(parent);
			dec.startShake();// �Ի������setModal (false)�ſ��Զ���, ������
		}
	}
}

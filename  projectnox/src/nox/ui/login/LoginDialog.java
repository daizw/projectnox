package nox.ui.login;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.sun.jna.examples.WindowUtils;

import nox.ui.common.JImgPanel;
import nox.ui.common.JNABalloon;
import nox.ui.common.SystemPath;

@SuppressWarnings("serial")
public class LoginDialog extends JDialog implements ActionListener{
	
	public static void main(String[] args){
		LoginDialog login = new LoginDialog();
		System.out.println(login.showDialog());
		System.out.println(login.getUsername() + ':' + new String(login.getPassword()));
	}
	public static final int LOGOSIZE = 260;
	public static final int LOGINDIALOGWIDTH = 310;
	public static final int LOGINDIALOGHEIGHT = 120 + LOGOSIZE;
	
	public static final String LOGINCMD = "LOGIN";
	public static final String REGISTERCMD = "REGISTER";
	public static final String CANCELCMD = "CANCEL";
	
	/**
	 * ����ϵͳ�ײ��������߶�
	 */
	public static final int DOCKHEIGHT = 30;
	
	//JLabel l_logo = new JLabel();
	/**
	 * �������
	 */
	JPanel p_input = new JPanel();
	/**
	 * �û���JPanel
	 */
	JPanel p_name = new JPanel();
	/**
	 * ����JPanel
	 */
	JPanel p_pwd = new JPanel();
	/**
	 * �û���JLabel
	 */
	JLabel l_name = new JLabel("Username:");
	/**
	 * ����JLabel
	 */
	JLabel l_pwd = new JLabel("Password:");
	/**
	 * �û���JTextField
	 */
	final JTextField t_name = new JTextField(18);
	/**
	 * ����JPasswordField
	 */
	final JPasswordField t_pwd = new JPasswordField("", 18);
	
	JPanel p_buttons = new JPanel();
	final JButton b_login = new JButton("Login");
	final JButton b_register = new JButton("Register");
	final JButton b_cancel = new JButton("Cancel");
	
	/*String name = "";
	char[] password;*/
	
	/**
	 * ������ȡͼƬ
	 */
	private Toolkit tk;
	private Image background;
	private Image img_logo;
	private JImgPanel rootpane;
	
	private Object returnVal = null;
	
	public LoginDialog() {
		// This statement is important, only modal dialogs can return values
        this.setModal(true);
        
		tk = Toolkit.getDefaultToolkit();
		background = tk.getImage(SystemPath.IMAGES_RESOURCE_PATH + "loginbkgrd.png");
		img_logo = tk.getImage(SystemPath.LOGO_RESOURCE_PATH + "NoXlogo_48.png");
		
		this.prepareImage(background, rootpane);
		
		//���ڶԻ����ƺ�û��...
		this.setIconImage(img_logo);
		
		rootpane = new JImgPanel(background);
		
		this.setUndecorated(true);
		l_name.setForeground(Color.WHITE);
		l_pwd.setForeground(Color.WHITE);
		
		t_name.setToolTipText("Input your name");
		t_pwd.setToolTipText("Input your password");
		t_pwd.setEchoChar('��');
		
		p_name.add(l_name);
		p_name.add(t_name);
		p_pwd.add(l_pwd);
		p_pwd.add(t_pwd);
		
		p_name.setOpaque(false);
		p_pwd.setOpaque(false);

		p_input.setLayout(new BoxLayout(p_input, BoxLayout.Y_AXIS));
		//			p_input.setBounds(10,10,20,400);
		p_input.add(p_name);
		//p_input.add(p_pwd);
		p_input.setOpaque(false);
		/**
		 * ���ӿ�ݼ�
		 */
		b_login.setMnemonic('L');
		b_register.setMnemonic('R');
		b_cancel.setMnemonic('C');

		p_buttons.add(b_login);
		p_buttons.add(b_register);
		p_buttons.add(b_cancel);
		p_buttons.setOpaque(false);

		//��¼
		b_login.addActionListener(this);

		//ע��:1.����������,�رձ�����->��ʾע�ᴰ��
		//       2.��ʾע�ᴰ��(ע��󷵻ص�¼����)
		b_register.addActionListener(this);
		b_cancel.addActionListener(this);

		//rootpane.add(l_logo, BorderLayout.NORTH);
		//rootpane.add(Box.createVerticalStrut(LOGOSIZE), BorderLayout.NORTH);
		rootpane.setLayout(new BoxLayout(rootpane, BoxLayout.Y_AXIS));
		rootpane.add(Box.createVerticalStrut(LOGOSIZE));
		rootpane.add(p_input);
		rootpane.add(p_buttons);
		rootpane.add(Box.createVerticalStrut(15));
		
		getRootPane().setDefaultButton(b_login);
		this.setContentPane(rootpane);
		
		 Dimension screenDim = tk.getScreenSize();
		 int ScrWidth = screenDim.width; 
		 int ScrHeight = screenDim.height - DOCKHEIGHT;
		 
		 this.setBounds((ScrWidth-LOGINDIALOGWIDTH)/2, (ScrHeight-LOGINDIALOGHEIGHT)/2,
				 LOGINDIALOGWIDTH, LOGINDIALOGHEIGHT);
		 this.setSize(new Dimension(LOGINDIALOGWIDTH, LOGINDIALOGHEIGHT));
		 this.setPreferredSize(new Dimension(LOGINDIALOGWIDTH, LOGINDIALOGHEIGHT));
		 this.setMaximumSize(new Dimension(LOGINDIALOGWIDTH, LOGINDIALOGHEIGHT));
		 this.setMinimumSize(new Dimension(LOGINDIALOGWIDTH, LOGINDIALOGHEIGHT));
		 
		 this.setAlwaysOnTop(true);
		 this.setOpacity(0.8f);
		 this.setRoundRecangle();
		 this.pack();
	}

    /** *//**
     * change statements here to get other return values
     * @return
     */
    public Object showDialog(){
        this.pack();
        this.setVisible(true);
        return this.returnVal;
    }
    
    /**
	 * ���ò�͸����
	 * 
	 * @return �Ƿ����óɹ�
	 */
	public boolean setOpacity(float alpha) {
		if (WindowUtils.isWindowAlphaSupported()) {
			WindowUtils.setWindowAlpha(this, alpha);
			return true;
		} else {
			System.out.println("Sorry, WindowAlpha is not Supported");
			return false;
		}
	}
	public boolean setRoundRecangle(){
		RoundRectangle2D.Float mask = 
			new RoundRectangle2D.Float(0, 0, 
					LOGINDIALOGWIDTH, LOGINDIALOGHEIGHT , 60, 60);
	    WindowUtils.setWindowMask(this, mask);
	    return true;
	}
    
    public String getUsername(){
    	return t_name.getText();
    }
    
    public char[] getPassword(){
    	return t_pwd.getPassword();
    }
    
    public void showUsernameInexistentBalloon(){
    	final String BALLOON_TEXT = "<html><center>"
            + "The username doesn't exist.<br>"
            + "Please register a count or try again.<br>"
            + "(Click to dismiss this balloon)</center></html>";
		JNABalloon balloon = new JNABalloon(BALLOON_TEXT, t_name, 10, 5);
		balloon.showBalloon();
    }
    
    public void showPasswordIncorrectBalloon(){
    	final String BALLOON_TEXT = "<html><center>"
            + "Password incorrect!<br>"
            + "(Click to dismiss this balloon)</center></html>";
		JNABalloon balloon = new JNABalloon(BALLOON_TEXT, t_pwd, 10, 5);
		balloon.showBalloon();
    }

    @Override
	public void actionPerformed(ActionEvent ae) {
    	//doReturn(e.getSource() == btnCancel);
    	if(ae.getSource() == b_login){
    		returnVal = LoginDialog.LOGINCMD;
    		if(!new File(new File(".cache"), getUsername()).exists()){
    			showUsernameInexistentBalloon();
    			return;
    		}
    	}
    	else if(ae.getSource() == b_register)
    		returnVal = LoginDialog.REGISTERCMD;
    	else
    		returnVal = LoginDialog.CANCELCMD;
    	this.dispose();
	}
}

	
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
public class RegisterDialog extends JDialog implements ActionListener{
	
	public static void main(String[] args){
		RegisterDialog login = new RegisterDialog();
		System.out.println(login.showDialog());
		System.out.println(login.getUsername() + ':' + new String(login.getPassword()));
	}
	public static final int LOGOSIZE = 250;
	public static final int REGISTERDIALOGWIDTH = 310;
	public static final int REGISTERDIALOGHEIGHT = 150 + LOGOSIZE;
	
	public static final String LOGINCMD = "LOGIN";
	public static final String CANCELCMD = "CANCEL";
	
	/**
	 * 操作系统底部任务栏高度
	 */
	public static final int DOCKHEIGHT = 30;
	
	//JLabel l_logo = new JLabel();
	/**
	 * 输入面板
	 */
	JPanel p_input = new JPanel();
	/**
	 * 用户名JPanel
	 */
	JPanel p_name = new JPanel();
	/**
	 * 用户名JLabel
	 */
	JLabel l_name = new JLabel("Username:");
	/**
	 * 用户名JTextField
	 */
	JTextField t_name = new JTextField(18);
	
	/**
	 * 密码JPanel
	 */
	JPanel p_pwd = new JPanel();
	/**
	 * 密码JLabel
	 */
	JLabel l_pwd = new JLabel("Password:");
	/**
	 * 密码JPasswordField
	 */
	JPasswordField t_pwd = new JPasswordField("", 18);
	
	/**
	 * 密码JPanel
	 */
	JPanel p_vfpwd = new JPanel();
	/**
	 * 密码JLabel
	 */
	JLabel l_vfpwd = new JLabel("       Verify:");
	/**
	 * 密码JPasswordField
	 */
	JPasswordField t_vfpwd = new JPasswordField("", 18);
	
	JPanel p_buttons = new JPanel();
	JButton b_login = new JButton("OK");
	JButton b_cancel = new JButton("Cancel");
	
	/*String name = "";
	char[] password;*/
	
	/**
	 * 用来获取图片
	 */
	private Toolkit tk;
	private Image background;
	private Image img_logo;
	private JImgPanel rootpane;
	
	private Object returnVal = null;
	
	public RegisterDialog() {
		// This statement is important, only modal dialogs can return values
        this.setModal(true);
        
		tk = Toolkit.getDefaultToolkit();
		background = tk.getImage(SystemPath.IMAGES_RESOURCE_PATH + "registerbkgrd.png");
		img_logo = tk.getImage(SystemPath.LOGO_RESOURCE_PATH + "NoXlogo_48.png");
		
		this.prepareImage(background, rootpane);
		
		//对于对话框似乎没用...
		this.setIconImage(img_logo);
		
		rootpane = new JImgPanel(background);
		
		this.setUndecorated(true);
		l_name.setForeground(Color.WHITE);
		l_pwd.setForeground(Color.WHITE);
		l_vfpwd.setForeground(Color.WHITE);
		
		t_name.setToolTipText("Input your name");
		t_pwd.setToolTipText("Input your password");
		t_pwd.setEchoChar('●');
		t_vfpwd.setToolTipText("Verify your password");
		t_vfpwd.setEchoChar('●');
		
		p_name.add(l_name);
		p_name.add(t_name);
		p_pwd.add(l_pwd);
		p_pwd.add(t_pwd);
		p_vfpwd.add(l_vfpwd);
		p_vfpwd.add(t_vfpwd);

		//p_input.setAlignmentX(0.5f);
		
		p_name.setOpaque(false);
		p_pwd.setOpaque(false);
		p_vfpwd.setOpaque(false);

		p_input.setLayout(new BoxLayout(p_input, BoxLayout.Y_AXIS));
		//			p_input.setBounds(10,10,20,400);
		p_input.add(p_name);
		p_input.add(p_pwd);
		p_input.add(p_vfpwd);
		p_input.setOpaque(false);
		/**
		 * 添加快捷键
		 */
		b_login.setMnemonic('L');
		b_cancel.setMnemonic('C');

		p_buttons.add(b_login);
		p_buttons.add(b_cancel);
		p_buttons.setOpaque(false);

		//登录
		b_login.addActionListener(this);

		//注册:1.返回主函数,关闭本窗口->显示注册窗口
		//       2.显示注册窗口(注册后返回登录窗口)
		b_cancel.addActionListener(this);

		//rootpane.add(l_logo, BorderLayout.NORTH);
		//rootpane.add(Box.createVerticalStrut(LOGOSIZE), BorderLayout.NORTH);
		rootpane.setLayout(new BoxLayout(rootpane, BoxLayout.Y_AXIS));
		rootpane.add(Box.createVerticalStrut(LOGOSIZE));
		rootpane.add(p_input);
		rootpane.add(p_buttons);
		rootpane.add(Box.createVerticalStrut(45));
		
		getRootPane().setDefaultButton(b_login);
		this.setContentPane(rootpane);
		
		 Dimension screenDim = tk.getScreenSize();
		 int ScrWidth = screenDim.width; 
		 int ScrHeight = screenDim.height - DOCKHEIGHT;
		 
		 this.setBounds((ScrWidth-REGISTERDIALOGWIDTH)/2, (ScrHeight-REGISTERDIALOGHEIGHT)/2,
				 REGISTERDIALOGWIDTH, REGISTERDIALOGHEIGHT);
		 this.setSize(new Dimension(REGISTERDIALOGWIDTH, REGISTERDIALOGHEIGHT));
		 this.setPreferredSize(new Dimension(REGISTERDIALOGWIDTH, REGISTERDIALOGHEIGHT));
		 this.setMaximumSize(new Dimension(REGISTERDIALOGWIDTH, REGISTERDIALOGHEIGHT));
		 this.setMinimumSize(new Dimension(REGISTERDIALOGWIDTH, REGISTERDIALOGHEIGHT));
		 
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
    
    public String getUsername(){
    	return t_name.getText();
    }
    
    public char[] getPassword(){
    	return t_pwd.getPassword();
    }
    
    public void showUsernameAlreadyExistentBalloon(){
    	final String BALLOON_TEXT = "<html><center>"
            + "The username already exist.<br>"
            + "Please try another username.<br>"
            + "(Click to dismiss this balloon)</center></html>";
		JNABalloon balloon = new JNABalloon(BALLOON_TEXT, t_name, 10, 5);
		balloon.showBalloon();
    }
    
    public void showPasswordIncorrespondBalloon(){
    	final String BALLOON_TEXT = "<html><center>"
            + "Passwords don't correspond to each other.<br>"
            + "(Click to dismiss this balloon)</center></html>";
		JNABalloon balloon = new JNABalloon(BALLOON_TEXT, t_pwd, 10, 5);
		balloon.showBalloon();
    }

    /**
	 * 设置不透明度
	 * 
	 * @return 是否设置成功
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
					REGISTERDIALOGWIDTH , REGISTERDIALOGHEIGHT , 20, 20);
	    WindowUtils.setWindowMask(this, mask);
	    return true;
	}
    @Override
	public void actionPerformed(ActionEvent ae) {
    	//doReturn(e.getSource() == btnCancel);
    	if(ae.getSource() == b_login){
    		returnVal = RegisterDialog.LOGINCMD;
    		if(new File(new File(".cache"), t_name.getText()).exists()){
				showUsernameAlreadyExistentBalloon();
				return;
    		}
    		if(new String(t_pwd.getPassword()).equals(new String(t_vfpwd.getPassword()))){
    			//do nothing
    		}else{
    			showPasswordIncorrespondBalloon();
    			return;
    		}
    	}
    	else
    		returnVal = RegisterDialog.CANCELCMD;
    	this.dispose();
	}
}
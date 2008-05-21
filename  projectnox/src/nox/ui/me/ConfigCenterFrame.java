/**
 *  ConfigCenterFrame.java
 *  设置中心窗口.
 *  
 *  by DaiZW
 *  2008.04.24
 */
package nox.ui.me;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import nox.ui.common.NoxFrame;
import nox.ui.common.SystemPath;
import nox.xml.NoxPeerStatusUnit;

/**
 * 设置中心窗口. 在这里可以设置个人及系统的各项参数: 例如: 个人设置: 昵称; 签名档; 个人资料; 头像; 身份验证; 其它. 系统设置:
 * 代理服务器; 数据保存文件夹; 修改密码; 自动登录; 隐身登录; 其它. (有些细节可以以后实现)
 * 启动后隐藏界面,保持在最顶端.
 * 
 * @author shinysky
 */
public class ConfigCenterFrame extends NoxFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7203586597981079637L;

	public static final int WIDTH = 600;
	public static final int HEIGHT = 400;
	public static int MenuItemWidth = 100;
	public static int MenuItemHeight = 20;

	JPanel menuPane;
	JButton personalConfigSideBtn = new JButton("个人设置");
	JPanel perConfigSidePane = new JPanel();
	JScrollPane perConfigSideScrPane;
	JButton systemConfigSideBtn = new JButton("系统设置");
	JPanel sysConfigSidePane = new JPanel();
	JScrollPane sysConfigSideScrPane;

	public static Dimension rightPaneSize;
	
	PersonalBasicConfigPane  perBCRightPane;
	SysBasicConfigPane sysBCRightPane;
	
	JPanel rootPane;
	Cheyenne parent;
	ConfigCenterFrame(Cheyenne prt) {
		super("Configuration Center", SystemPath.IMAGES_RESOURCE_PATH + "bkgrd.png",
				SystemPath.ICONS_RESOURCE_PATH + "config_20.png",
				SystemPath.ICONS_RESOURCE_PATH + "config_48.png", "NoX Configuration Center", false);
		/**
		 * TODO: 尽管super设为false, 关闭按钮还是可以exit, 让我很诧异, 先不管这个
		 */
		parent = prt;

		Dimension btnsize = new Dimension(MenuItemWidth, 16);
		personalConfigSideBtn.setSize(btnsize);
		personalConfigSideBtn.setPreferredSize(btnsize);
		personalConfigSideBtn.setMaximumSize(btnsize);
		personalConfigSideBtn.setMinimumSize(btnsize);
		systemConfigSideBtn.setSize(btnsize);
		systemConfigSideBtn.setPreferredSize(btnsize);
		systemConfigSideBtn.setMaximumSize(btnsize);
		systemConfigSideBtn.setMinimumSize(btnsize);

		/**
		 * 个人设置列表
		 */
		String[] pclistItems = { "  个人资料", "  联系方式", "  身份验证", "  What!",
				"  dfa", "  fdas" };

		JList pclist = new JList(pclistItems);

		/**
		 * 系统设置列表
		 */
		String[] sclistItems = { "  基本设置", "  登录设置", "  代理设置", "  安全设置",
				"  外观设置", "  fda" };

		JList sclist = new JList(sclistItems);

		pclist.setFixedCellWidth(MenuItemWidth);
		pclist.setFixedCellHeight(MenuItemHeight);

		sclist.setFixedCellWidth(MenuItemWidth);
		sclist.setFixedCellHeight(MenuItemHeight);

		/**
		 * 单选模式
		 */
		pclist.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		sclist.getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		
		pclist.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				int index = lse.getFirstIndex();
				switch(index){
				case 0: perBCRightPane.setVisible(true);
								sysBCRightPane.setVisible(false);
								break;
				default: break;
				}
			}
		});
		sclist.addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				int index = lse.getFirstIndex();
				switch(index){
				case 0: perBCRightPane.setVisible(false);
								sysBCRightPane.setVisible(true);
								break;
				default: break;
				}
			}
		});

		perConfigSidePane.setLayout(new BorderLayout());
		perConfigSidePane.add(pclist, BorderLayout.CENTER);

		sysConfigSidePane.setLayout(new BorderLayout());
		sysConfigSidePane.add(sclist, BorderLayout.CENTER);

		/*
		 * Dimension pcpsize = new Dimension(MenuItemWidth, MenuItemHeight
		 * *pclist.getModel().getSize()); System.out.println("personal size: " +
		 * pclist.getModel().getSize());
		 * 
		 * perConfigPane.setSize(pcpsize);
		 * perConfigPane.setPreferredSize(pcpsize);
		 * perConfigPane.setMaximumSize(pcpsize);
		 * perConfigPane.setMinimumSize(pcpsize);
		 * 
		 * Dimension scpsize = new Dimension(MenuItemWidth, MenuItemHeight
		 * *sclist.getModel().getSize()); System.out.println("system size: " +
		 * sclist.getModel().getSize());
		 * 
		 * sysConfigPane.setSize(scpsize);
		 * sysConfigPane.setPreferredSize(scpsize);
		 * sysConfigPane.setMaximumSize(scpsize);
		 * sysConfigPane.setMinimumSize(scpsize);
		 */

		menuPane = new JPanel();
		// menuPane.setLayout(new BoxLayout(menuPane, BoxLayout.Y_AXIS));
		menuPane.add(personalConfigSideBtn);
		menuPane.add(perConfigSidePane);
		menuPane.add(systemConfigSideBtn);
		menuPane.add(sysConfigSidePane);
		menuPane.add(Box.createVerticalGlue());
		menuPane.setBackground(Color.WHITE);
		sysConfigSidePane.setVisible(false);// 默认初始只有perConfigPane可见

		Dimension size = new Dimension(WIDTH, HEIGHT);

		// menuPane.setBounds(0, 0, menusize.width, menusize.height);
		menuPane.setSize(new Dimension(MenuItemWidth, HEIGHT));
		menuPane.setPreferredSize(new Dimension(MenuItemWidth, HEIGHT));
		menuPane.setMaximumSize(new Dimension(MenuItemWidth, 2000));
		menuPane.setMinimumSize(new Dimension(MenuItemWidth, MenuItemHeight
				* (pclist.getModel().getSize() + sclist.getModel().getSize())
				+ btnsize.height * 2));

		personalConfigSideBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// menuPane.add(perConfigPane);
				// menuPane.remove(sysConfigPane);
				perConfigSidePane.setVisible(true);
				sysConfigSidePane.setVisible(false);
			}
		});
		systemConfigSideBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// menuPane.add(sysConfigPane);
				// menuPane.remove(perConfigPane);
				perConfigSidePane.setVisible(false);
				sysConfigSidePane.setVisible(true);
			}
		});

		rootPane = this.getContainer();
		rootPane.setLayout(new BoxLayout(rootPane, BoxLayout.X_AXIS));
		rootPane.add(menuPane);
		
		rightPaneSize = new Dimension(WIDTH-MenuItemWidth, HEIGHT-NoxFrame.TITLE_HEIGHT-NoxFrame.FOOT_HEIGHT);
		perBCRightPane = new PersonalBasicConfigPane(parent);
		perBCRightPane.setSize(rightPaneSize);
		perBCRightPane.setPreferredSize(rightPaneSize);
		perBCRightPane.setMaximumSize(rightPaneSize);
		perBCRightPane.setMinimumSize(rightPaneSize);
		
		sysBCRightPane = new SysBasicConfigPane(parent);
		sysBCRightPane.setSize(rightPaneSize);
		sysBCRightPane.setPreferredSize(rightPaneSize);
		sysBCRightPane.setMaximumSize(rightPaneSize);
		sysBCRightPane.setMinimumSize(rightPaneSize);
		
		sysBCRightPane.setVisible(false);
		
		rootPane.add(perBCRightPane);
		rootPane.add(sysBCRightPane);
		// rootPane.add(Box.createHorizontalGlue());

		this.removeResizeListener();
		this.setBounds(100, 100, size.width, size.height);
		this.setSize(size);
		this.setPreferredSize(size);
		this.setMaximumSize(size);
		this.setMinimumSize(size);
		this.pack();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//System.setProperty("sun.java2d.noddraw", "true");// 为半透明做准备
		ConfigCenterFrame ccf = new ConfigCenterFrame(null);
		ccf.setVisible(true);
	}
}

/**
 * 个人基本设置JPanel
 * @author shinysky
 * @Fixme
 */
class PersonalBasicConfigPane extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Cheyenne parent;
	JPanel portraitPane = new JPanel();
	JLabel portraitLab = new JLabel("Portrait:");
	JButton myPortrait;
	//JButton portraitBtn = new JButton("Modify");
	
	JPanel nicknamePane = new JPanel();
	JLabel nicknameLab = new JLabel("Nickname:");
	JTextField nickNameTf = new JTextField(20);
	
	JButton submitBtn = new JButton("Submit");
	
	PersonalBasicConfigPane(Cheyenne prt) {
		parent = prt;
		NoxPeerStatusUnit stat = parent.getFullStatusUnit();
		stat.getPortrait();
		
		myPortrait = new JButton(stat.getPortrait());
		myPortrait.setToolTipText(MiniProfilePane.getHtmlText("This is Me"));
		myPortrait.setSize(MiniProfilePane.portriatSize);
		myPortrait.setPreferredSize(MiniProfilePane.portriatSize);
		myPortrait.setMaximumSize(MiniProfilePane.portriatSize);
		myPortrait.setMinimumSize(MiniProfilePane.portriatSize);

		myPortrait.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
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
				chooser.setDialogTitle("请选择头像图片(建议尺寸大小为40x40)");
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// getJtf_pic().setText(chooser.getSelectedFile().getPath());
					System.out.println("You chose a pic: "
							+ chooser.getSelectedFile().getPath());
					File thePicFile = new File(chooser.getSelectedFile().getPath());
					if(thePicFile.exists()){
						BufferedImage bufImg = null;
						try {
							bufImg = javax.imageio.ImageIO.read(thePicFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						myPortrait.setIcon(new ImageIcon(bufImg));
					}
				}
			}
		});
		
		nickNameTf.setText(stat.getNickName());
		nickNameTf.setSize(new Dimension(
				ConfigCenterFrame.rightPaneSize.width-nicknameLab.getSize().width, 20));
		nickNameTf.setPreferredSize(new Dimension(
				ConfigCenterFrame.rightPaneSize.width-nicknameLab.getSize().width, 20));
		nickNameTf.setMaximumSize(new Dimension(
				ConfigCenterFrame.rightPaneSize.width-nicknameLab.getSize().width, 20));
		nickNameTf.setMinimumSize(new Dimension(
				ConfigCenterFrame.rightPaneSize.width-nicknameLab.getSize().width, 20));
		
		//portraitPane.setLayout(new BoxLayout(portraitPane, BoxLayout.X_AXIS));
		portraitPane.add(portraitLab);
		portraitPane.add(myPortrait);
		
		//nicknamePane.setLayout(new BoxLayout(nicknamePane, BoxLayout.X_AXIS));
		nicknamePane.add(nicknameLab);
		nicknamePane.add(nickNameTf);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalGlue());
		this.add(portraitPane);
		this.add(nicknamePane);
		this.add(submitBtn);
		this.add(Box.createVerticalGlue());
		
		submitBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				parent.setMyPortrait(myPortrait.getIcon());
				parent.setMyNickName(nickNameTf.getText());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
	}
}

/**
 * 系统基本设置JPanel
 * @author shinysky
 *
 */
class SysBasicConfigPane extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JRadioButton java, system;// JAVA界面,系统界面
	Cheyenne parent;
	SysBasicConfigPane(Cheyenne prt) {
		parent = prt;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(java = new JRadioButton("java主题", true));
		this.add(system = new JRadioButton("系统主题"));
		ButtonGroup bg=new ButtonGroup();
        bg.add(java);
        bg.add(system);
        
		java.addActionListener(this);
		system.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		Object source = ae.getSource();
		if (source == java) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
				SwingUtilities.updateComponentTreeUI(parent);
			} catch (Exception exe) {
				exe.printStackTrace();
			}
		} else if (source == system) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
				SwingUtilities.updateComponentTreeUI(parent);
			} catch (Exception exe) {
				exe.printStackTrace();
			}
		}
	}
}

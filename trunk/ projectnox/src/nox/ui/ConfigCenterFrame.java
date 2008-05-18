/**
 *  ConfigCenterFrame.java
 *  设置中心窗口.
 *  
 *  by DaiZW
 *  2008.04.24
 */
package nox.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
	JButton personalConfigBtn = new JButton("个人设置");
	JPanel perConfigPane = new JPanel();
	JScrollPane perConfigScrPane;
	JButton systemConfigBtn = new JButton("系统设置");
	JPanel sysConfigPane = new JPanel();
	JScrollPane sysConfigScrPane;

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
		personalConfigBtn.setSize(btnsize);
		personalConfigBtn.setPreferredSize(btnsize);
		personalConfigBtn.setMaximumSize(btnsize);
		personalConfigBtn.setMinimumSize(btnsize);
		systemConfigBtn.setSize(btnsize);
		systemConfigBtn.setPreferredSize(btnsize);
		systemConfigBtn.setMaximumSize(btnsize);
		systemConfigBtn.setMinimumSize(btnsize);

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

		perConfigPane.setLayout(new BorderLayout());
		perConfigPane.add(pclist, BorderLayout.CENTER);

		sysConfigPane.setLayout(new BorderLayout());
		sysConfigPane.add(sclist, BorderLayout.CENTER);

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
		menuPane.add(personalConfigBtn);
		menuPane.add(perConfigPane);
		menuPane.add(systemConfigBtn);
		menuPane.add(sysConfigPane);
		menuPane.add(Box.createVerticalGlue());
		menuPane.setBackground(Color.WHITE);
		sysConfigPane.setVisible(false);// 默认初始只有perConfigPane可见

		Dimension size = new Dimension(WIDTH, HEIGHT);

		// menuPane.setBounds(0, 0, menusize.width, menusize.height);
		menuPane.setSize(new Dimension(MenuItemWidth, HEIGHT));
		menuPane.setPreferredSize(new Dimension(MenuItemWidth, HEIGHT));
		menuPane.setMaximumSize(new Dimension(MenuItemWidth, 2000));
		menuPane.setMinimumSize(new Dimension(MenuItemWidth, MenuItemHeight
				* (pclist.getModel().getSize() + sclist.getModel().getSize())
				+ btnsize.height * 2));

		personalConfigBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// menuPane.add(perConfigPane);
				// menuPane.remove(sysConfigPane);
				perConfigPane.setVisible(true);
				sysConfigPane.setVisible(false);
			}
		});
		systemConfigBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// menuPane.add(sysConfigPane);
				// menuPane.remove(perConfigPane);
				perConfigPane.setVisible(false);
				sysConfigPane.setVisible(true);
			}
		});

		rootPane = this.getContainer();
		rootPane.setLayout(new BoxLayout(rootPane, BoxLayout.X_AXIS));
		rootPane.add(menuPane);
		SysBasicConfigPane rightPane = new SysBasicConfigPane(parent);
		rightPane.setSize(new Dimension(WIDTH-MenuItemWidth, HEIGHT-NoxFrame.TITLE_HEIGHT-NoxFrame.FOOT_HEIGHT));
		rightPane.setPreferredSize(new Dimension(WIDTH-MenuItemWidth, HEIGHT-NoxFrame.TITLE_HEIGHT-NoxFrame.FOOT_HEIGHT));
		rightPane.setMaximumSize(new Dimension(WIDTH-MenuItemWidth, HEIGHT-NoxFrame.TITLE_HEIGHT-NoxFrame.FOOT_HEIGHT));
		rightPane.setMinimumSize(new Dimension(WIDTH-MenuItemWidth, HEIGHT-NoxFrame.TITLE_HEIGHT-NoxFrame.FOOT_HEIGHT));
		
		rootPane.add(rightPane);
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

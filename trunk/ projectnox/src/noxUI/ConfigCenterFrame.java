/**
 *  ConfigCenterFrame.java
 *  �������Ĵ���.
 *  
 *  by DaiZW
 *  2008.04.24
 */
package noxUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;

/**
 * �������Ĵ���.
 * ������������ø��˼�ϵͳ�ĸ������:
 * ����: 
 * ��������: �ǳ�; ǩ����; ��������; ͷ��; �����֤; ����.
 * ϵͳ����: ���������; ���ݱ����ļ���; �޸�����; �Զ���¼; �����¼; ����.
 * (��Щϸ�ڿ����Ժ�ʵ��) 
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
	JButton personalConfigBtn = new JButton("��������");
	JPanel perConfigPane = new JPanel();
	JScrollPane perConfigScrPane;
	JButton systemConfigBtn = new JButton("ϵͳ����");
	JPanel sysConfigPane = new JPanel();
	JScrollPane sysConfigScrPane;
	
	JPanel rootPane;

	ConfigCenterFrame(){
		super("Configuration Center", "resrc\\images\\bkgrd.png", 
				"resrc\\logo\\NoXlogo_20.png", "NoX Configuration Center",
				"resrc\\buttons\\minimize.png", "resrc\\buttons\\minimize_rollover.png",
				"resrc\\buttons\\maximize.png", "resrc\\buttons\\maximize_rollover.png",
				"resrc\\buttons\\normalize.png", "resrc\\buttons\\normalize_rollover.png",
				"resrc\\buttons\\close.png", "resrc\\buttons\\close_rollover.png", false);
		/**
		 * TODO: ����super��Ϊfalse, �رհ�ť���ǿ���exit, ���Һܲ���, �Ȳ������
		 */
		
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
		 * ���������б�
		 */
		String[] pclistItems = { "  ��������", "  ��ϵ��ʽ", "  �����֤", "  What!", "  dfa", "  fdas"};

		JList pclist = new JList(pclistItems);
		
		/**
		 * ϵͳ�����б�
		 */
		String[] sclistItems = { "  ��������", "  ��¼����", "  ��������", "  ��ȫ����", "  �������", "  fda"};

		JList sclist = new JList(sclistItems);

		pclist.setFixedCellWidth(MenuItemWidth);
		pclist.setFixedCellHeight(MenuItemHeight);
		
		sclist.setFixedCellWidth(MenuItemWidth);
		sclist.setFixedCellHeight(MenuItemHeight);
		
		/**
		 * ��ѡģʽ
		 */
		pclist.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sclist.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		perConfigPane.setLayout(new BorderLayout());
		perConfigPane.add(pclist, BorderLayout.CENTER);
		
		sysConfigPane.setLayout(new BorderLayout());
		sysConfigPane.add(sclist, BorderLayout.CENTER);
		
		/*Dimension pcpsize = new Dimension(MenuItemWidth, 
				MenuItemHeight *pclist.getModel().getSize());
		System.out.println("personal size: " + pclist.getModel().getSize());
		
		perConfigPane.setSize(pcpsize);
		perConfigPane.setPreferredSize(pcpsize);
		perConfigPane.setMaximumSize(pcpsize);
		perConfigPane.setMinimumSize(pcpsize);
		
		Dimension scpsize = new Dimension(MenuItemWidth, 
				MenuItemHeight *sclist.getModel().getSize());
		System.out.println("system size: " + sclist.getModel().getSize());
		
		sysConfigPane.setSize(scpsize);
		sysConfigPane.setPreferredSize(scpsize);
		sysConfigPane.setMaximumSize(scpsize);
		sysConfigPane.setMinimumSize(scpsize);*/
		
		menuPane = new JPanel();
		//menuPane.setLayout(new BoxLayout(menuPane, BoxLayout.Y_AXIS));
		menuPane.add(personalConfigBtn);
		menuPane.add(perConfigPane);
		menuPane.add(systemConfigBtn);
		menuPane.add(sysConfigPane);
		menuPane.add(Box.createVerticalGlue());
		menuPane.setBackground(Color.WHITE);
		sysConfigPane.setVisible(false);//Ĭ�ϳ�ʼֻ��perConfigPane�ɼ�
		
		Dimension size = new Dimension(WIDTH, HEIGHT);
		
		//menuPane.setBounds(0, 0, menusize.width, menusize.height);
		menuPane.setSize(new Dimension(MenuItemWidth, HEIGHT));
		menuPane.setPreferredSize(new Dimension(MenuItemWidth, HEIGHT));
		menuPane.setMaximumSize(new Dimension(MenuItemWidth, 2000));
		menuPane.setMinimumSize(new Dimension(MenuItemWidth, 
				MenuItemHeight *(pclist.getModel().getSize() + sclist.getModel().getSize()) + btnsize.height*2));
		
		personalConfigBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//menuPane.add(perConfigPane);
				//menuPane.remove(sysConfigPane);
				perConfigPane.setVisible(true);
				sysConfigPane.setVisible(false);
			}
		});
		systemConfigBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//menuPane.add(sysConfigPane);
				//menuPane.remove(perConfigPane);
				perConfigPane.setVisible(false);
				sysConfigPane.setVisible(true);
			}
		});
		
		rootPane =  this.getContainer();
		rootPane.setLayout(new BoxLayout(rootPane, BoxLayout.X_AXIS));
		rootPane.add(menuPane);
		JPanel rightPane = new JPanel();
		rootPane.add(rightPane);
		//rootPane.add(Box.createHorizontalGlue());
		
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
		System.setProperty("sun.java2d.noddraw", "true");// Ϊ��͸����׼��
		ConfigCenterFrame ccf = new ConfigCenterFrame();
		ccf.setVisible(true);
	}
}

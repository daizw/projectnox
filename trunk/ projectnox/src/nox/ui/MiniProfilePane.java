package nox.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nox.xml.NoxPeerStatusUnit;

public class MiniProfilePane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6265273413794252382L;

	// JMapPanel myPortraitPane;
	JButton myPortrait;
	JPanel miniProfilePane;
	JPanel nickAndStat;
	JLabel myNick;
	JComboBox myStatus;
	JTextField mySign;
	
	public static Dimension portriatSize = new Dimension(50, 50);
	ImageIcon lastPortrait = null;

	/**
	 * mini profile 组件
	 * 
	 * @param path_portrait
	 *            头像图片路径
	 * @param nickname
	 *            昵称
	 * @param sign
	 *            签名档
	 */
	MiniProfilePane(final Cheyenne parent, ImageIcon portrait, String nickname, String sign) {
		lastPortrait = portrait;
		myPortrait = new JButton(lastPortrait);
		myPortrait.setToolTipText(getHtmlText("This is Me"));
		myPortrait.setSize(portriatSize);
		myPortrait.setPreferredSize(portriatSize);
		myPortrait.setMaximumSize(portriatSize);
		myPortrait.setMinimumSize(portriatSize);
		// myPortrait.setBorderPainted(true);

		/*
		 * myPortrait.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,
		 * Color.RED, Color.BLUE));
		 */
		myPortrait.setContentAreaFilled(false);
		myPortrait.setOpaque(false);

		myPortrait.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.ShowConfigCenter();
			}
		});
		
		/*
		 * JButton statSign = new JButton(new
		 * ImageIcon("resrc\\portrait\\busy.png")); statSign.setSize(new
		 * Dimension(50, 50)); statSign.setPreferredSize(new Dimension(50, 50));
		 * statSign.setMaximumSize(new Dimension(50, 50));
		 * statSign.setMinimumSize(new Dimension(50, 50));
		 * //statSign.setBorderPainted(false);
		 * //statSign.setContentAreaFilled(false); //statSign.setOpaque(false);
		 * 
		 * myPortrait.setLayout(new BoxLayout(myPortrait, BoxLayout.X_AXIS));
		 * myPortrait.add(statSign);
		 */

		/*
		 * myPortraitPane = new JMapPanel(myportrImg, new Point (5, 10), new
		 * Dimension(40, 40)); myPortraitPane.setSize(new Dimension(50, 50));
		 * myPortraitPane.setPreferredSize(new Dimension(50, 50));
		 * myPortraitPane.setMaximumSize(new Dimension(50, 50));
		 * myPortraitPane.setMinimumSize(new Dimension(50, 50));
		 * myPortraitPane.add(myPortrait);
		 */

		miniProfilePane = new JPanel();
		nickAndStat = new JPanel();
		myNick = new JLabel(nickname);
		myNick.setToolTipText(getHtmlText("My nickname"));
		myStatus = new JComboBox();
		// myStatus.setOpaque(false);
		mySign = new JTextField(sign);
		
		myStatus.addItem(ItemStatus.OnlineStr);
		myStatus.addItem(ItemStatus.BusyStr);
		myStatus.addItem(ItemStatus.UnavailableStr);
		//myStatus.addItem(Offline);
		myStatus.setToolTipText(getHtmlText("My Status"));
		myStatus.setSize(new Dimension(100, 20));
		myStatus.setPreferredSize(new Dimension(100, 20));
		myStatus.setMaximumSize(new Dimension(100, 20));
		myStatus.setMinimumSize(new Dimension(100, 20));
		// myStatus.setOpaque(false);

		nickAndStat.setOpaque(false);
		// nickAndStat.setBackground(new Color(0, 255, 0));
		nickAndStat.setSize(new Dimension(150, 20));
		nickAndStat.setPreferredSize(new Dimension(150, 20));
		nickAndStat.setMaximumSize(new Dimension(1000, 20));
		nickAndStat.setMinimumSize(new Dimension(150, 20));
		// nickAndStat.setAlignmentX(JComponent.LEFT_ALIGNMENT);
		nickAndStat.setLayout(new BoxLayout(nickAndStat, BoxLayout.X_AXIS));
		nickAndStat.add(myNick);
		nickAndStat.add(Box.createHorizontalStrut(10));
		nickAndStat.add(myStatus);

		mySign.setOpaque(false);
		mySign.setToolTipText(getHtmlText("My Description"));
		mySign.setSize(new Dimension(Cheyenne.WIDTH_DEFLT, 20));
		mySign.setPreferredSize(new Dimension(Cheyenne.WIDTH_PREF, 20));
		mySign.setMaximumSize(new Dimension(Cheyenne.WIDTH_MAX, 20));
		mySign.setMinimumSize(new Dimension(Cheyenne.WIDTH_MIN, 20));
		//mySign.setEnabled(false);
		mySign.setEditable(false);
		mySign.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				mySign.setEditable(true);
				mySign.setOpaque(true);
				if(mySign.getForeground().equals(Color.WHITE))
						mySign.setForeground(Color.BLACK);
			}
			@Override
			public void focusLost(FocusEvent arg0) {
				mySign.setEditable(false);
				mySign.setOpaque(false);
				mySign.setForeground(myNick.getForeground());
			}
		});
		
		// miniProfilePane.setAlignmentX(JComponent.TOP_ALIGNMENT);
		miniProfilePane.setLayout(new BoxLayout(miniProfilePane,
				BoxLayout.Y_AXIS));
		miniProfilePane.add(nickAndStat);
		miniProfilePane.add(mySign);
		miniProfilePane.setOpaque(false);

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(myPortrait);
		this.add(Box.createHorizontalStrut(5));
		this.add(miniProfilePane);
		this.setOpaque(false);
	}
	public NoxPeerStatusUnit getStatusUnit(){
		ItemStatus stat = null;
		String statStr = (String) myStatus.getSelectedItem();
		if(statStr.equals(ItemStatus.OnlineStr))
			stat = ItemStatus.ONLINE;
		else if(statStr.equals(ItemStatus.UnavailableStr))
			stat = ItemStatus.UNAVAILABLE;
		else if(statStr.equals(ItemStatus.BusyStr))
			stat = ItemStatus.BUSY;
		else
			stat = ItemStatus.UNKNOWN;
		
		ImageIcon curPortrait = null;
		if(lastPortrait.equals((ImageIcon) myPortrait.getIcon())){
			//头像无变化
			curPortrait = null;
		}else{
			curPortrait = (ImageIcon) myPortrait.getIcon();
		}
		
		return new NoxPeerStatusUnit(myNick.getText(),
				mySign.getText(),
				stat,
				curPortrait); 
	}
	/**
	 * 功能与getStatusUnit类似, 唯一不同在于无论有没有修改头像, 都会返回头像.
	 * @return
	 */
	public NoxPeerStatusUnit getFullStatusUnit(){
		ItemStatus stat = null;
		String statStr = (String) myStatus.getSelectedItem();
		if(statStr.equals(ItemStatus.OnlineStr))
			stat = ItemStatus.ONLINE;
		else if(statStr.equals(ItemStatus.UnavailableStr))
			stat = ItemStatus.UNAVAILABLE;
		else if(statStr.equals(ItemStatus.BusyStr))
			stat = ItemStatus.BUSY;
		else
			stat = ItemStatus.UNKNOWN;
		
		ImageIcon curPortrait = (ImageIcon) myPortrait.getIcon();
		
		return new NoxPeerStatusUnit(myNick.getText(),
				mySign.getText(),
				stat,
				curPortrait); 
	}
	public void setPortrait(Icon icon){
		myPortrait.setIcon(icon);
	}
	/**
	 * 设置昵称
	 * @param name
	 */
	public void setNickName(String name){
		myNick.setText(name);
	}
	/**
	 * 设置签名档
	 * @param sign
	 * @deprecated 还是直接在主界面设置吧!
	 */
	public void setSign(String sign){
		mySign.setText(sign);
	}
	public void setForegroundColor(Color color){
		myNick.setForeground(color);
		mySign.setForeground(color);
	}
	/**
	 * 返回TooltipTxt的html形式
	 * @param text
	 * @return
	 */
	public static String getHtmlText(String text) {
		return ("<html><BODY bgColor=#ffffff><Font color=black>" + text + "</Font></BODY></html>");
	}
}
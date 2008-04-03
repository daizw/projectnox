package noxUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
/**
 * 
 * @author shinysky
 *
 */
public class Chatroom extends NoxFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7981736228268584688L;
	/**
	 * 默认尺寸常量
	 */
	public static final int WIDTH_DEFLT = 600;
	public static final int WIDTH_PREF = 600;
	public static final int WIDTH_MAX = 2000;
	public static final int WIDTH_MIN = 300;
	public static final int HEIGHT_DEFLT = 450;
	public static final int HEIGHT_PREF = 450;
	public static final int HEIGHT_MAX = 2000;
	public static final int HEIGHT_MIN = 200;
	
	ChatRoomPane crp;
	
	Chatroom(){
		super("Nox Chatroom", "resrc\\images\\bkgrd.png",
				"resrc\\logo\\NoXlogo_20.png", "resrc\\logo\\nox.png",
				"resrc\\buttons\\minimize.png", "resrc\\buttons\\minimize_rollover.png",
				"resrc\\buttons\\maximize.png", "resrc\\buttons\\maximize_rollover.png",
				"resrc\\buttons\\normalize.png", "resrc\\buttons\\normalize_rollover.png",
				"resrc\\buttons\\close.png", "resrc\\buttons\\close_rollover.png", true);
		//最终此处应为false

		this.setBounds(100, 80, WIDTH_DEFLT, HEIGHT_DEFLT);
		this.setSize(new Dimension(WIDTH_DEFLT, HEIGHT_DEFLT));
		this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
		
		crp = new ChatRoomPane(this);
		//crp.setLayout(new FlowLayout());
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add (crp, BorderLayout.CENTER);
	}
}

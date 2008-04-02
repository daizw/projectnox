package noxUI;

import java.awt.Dimension;

public class Chatroom extends NoxFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7981736228268584688L;
	/**
	 * Ä¬ÈÏ³ß´ç³£Á¿
	 */
	public static final int WIDTH_DEFLT = 500;
	public static final int WIDTH_PREF = 500;
	public static final int WIDTH_MAX = 2000;
	public static final int WIDTH_MIN = 300;
	public static final int HEIGHT_DEFLT = 350;
	public static final int HEIGHT_PREF = 350;
	public static final int HEIGHT_MAX = 2000;
	public static final int HEIGHT_MIN = 200;
	
	Chatroom(){
		super("Nox Chatroom", "resrc\\bkgrd.png",
				"resrc\\NoXlogo_20.png", "resrc\\nox.png",
				"resrc\\minimize.png", "resrc\\minimize_rollover.png",
				"resrc\\maximize.png", "resrc\\maximize_rollover.png",
				"resrc\\normalize.png", "resrc\\normalize_rollover.png",
				"resrc\\close.png", "resrc\\close_rollover.png", false);

		this.setBounds(100, 80, WIDTH_DEFLT, HEIGHT_DEFLT);
		this.setSize(new Dimension(WIDTH_DEFLT, HEIGHT_DEFLT));
		this.setPreferredSize(new Dimension(WIDTH_PREF, HEIGHT_PREF));
		this.setMaximumSize(new Dimension(WIDTH_MAX, HEIGHT_MAX));
		this.setMinimumSize(new Dimension(WIDTH_MIN, HEIGHT_MIN));
	}
}

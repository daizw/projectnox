package nox.ui.chat.peer;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

public class PeerChatroomSidePane extends JSplitPane{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6138097557690702276L;
	
	public static final int PORTRAITSIZE = 60;
	
	JPanel friendPane;
	JLabel l_friend;
	JButton friend;
	JPanel myPane;
	JLabel l_me;
	JLabel me;
	PeerChatroomSidePane(String friendname, ImageIcon friendPortr, ImageIcon myPortr){
		super(JSplitPane.VERTICAL_SPLIT);
		
		l_friend = new JLabel("<html><Font color=red><h3>" 
				+ friendname
				+ ":  </h3></Font></html>");
		Dimension lsize = new Dimension(100, 20);
		l_friend.setSize(lsize);
		l_friend.setPreferredSize(lsize);
		l_friend.setMaximumSize(new Dimension(3000, 20));
		l_friend.setMinimumSize(lsize);
		
		friend = new JButton(friendPortr);
		Dimension psize = new Dimension(PORTRAITSIZE, PORTRAITSIZE);
		friend.setSize(psize);
		friend.setPreferredSize(psize);
		friend.setMaximumSize(psize);
		friend.setMinimumSize(psize);
		friendPane = new JPanel();
		friendPane.setLayout(new BoxLayout(friendPane, BoxLayout.Y_AXIS));
		friendPane.add(l_friend);
		friendPane.add(Box.createVerticalStrut(15));
		friendPane.add(friend);
		friendPane.add(Box.createVerticalBox());
		
		l_me = new JLabel("<html><Font color=red><h3>Me:  </h3></Font></html>");
		l_me.setSize(lsize);
		l_me.setPreferredSize(lsize);
		l_me.setMaximumSize(new Dimension(3000, 20));
		l_me.setMinimumSize(lsize);
		
		me = new JLabel(myPortr);
		me.setSize(psize);
		me.setPreferredSize(psize);
		me.setMaximumSize(psize);
		me.setMinimumSize(psize);
		myPane = new JPanel();
		myPane.setLayout(new BoxLayout(myPane, BoxLayout.Y_AXIS));
		myPane.add(l_me);
		myPane.add(Box.createVerticalStrut(15));
		myPane.add(me);
		//myPane.add(Box.createHorizontalGlue());
		this.setResizeWeight(0.5d);
		this.setDividerLocation(0.9f);
		this.setDividerSize(5);
		this.add(friendPane);
		this.add(myPane);		
	}
}


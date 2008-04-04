package noxUI;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

class SingleChatRoomSidePane extends JSplitPane{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6138097557690702276L;
	JPanel friendPane;
	JLabel l_friend;
	JButton friend;
	JPanel myPane;
	JLabel l_me;
	JButton me;
	SingleChatRoomSidePane(ImageIcon friendPortr, ImageIcon myPortr){
		super(JSplitPane.VERTICAL_SPLIT);
		l_friend = new JLabel("I'm talking to:");
		friend = new JButton(friendPortr);
		friendPane = new JPanel();
		friendPane.setLayout(new BoxLayout(friendPane, BoxLayout.Y_AXIS));
		friendPane.add(l_friend);
		friendPane.add(friend);
		
		l_me = new JLabel("My portrait");
		me = new JButton(myPortr);
		myPane = new JPanel();
		myPane.setLayout(new BoxLayout(myPane, BoxLayout.Y_AXIS));
		myPane.add(l_me);
		myPane.add(me);
		
		this.add(friendPane);
		this.add(myPane);
		this.setDividerLocation(0.5f);
		this.setDividerSize(5);
	}
}

class MultiChatRoomSidePane extends JSplitPane{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4038211438834118259L;
	JPanel groupInfoPane;
	JLabel l_groupInfo;
	JTextPane groupInfo;
	JScrollPane groupMemverListScrPane;
	JPanel groupMemberListPane;
	JLabel l_groupMembers;
	ObjectList groupmemerlist;
	MultiChatRoomSidePane(String grpInfo, Object[] members){
		super(JSplitPane.VERTICAL_SPLIT);
		
		l_groupInfo = new JLabel("Group Info:");
		groupInfo = new JTextPane();
		groupInfo.setText(grpInfo);
		groupInfo.setEditable(false);
		//groupInfo.setEnabled(false);
		groupInfo.setSize(new Dimension(100, 100));
		groupInfo.setPreferredSize(new Dimension(100, 100));
		groupInfo.setMaximumSize(new Dimension(10000, 100));
		groupInfo.setMinimumSize(new Dimension(20, 100));
		
		groupInfoPane = new JPanel();
		groupInfoPane.setLayout(new BoxLayout(groupInfoPane, BoxLayout.Y_AXIS));
		groupInfoPane.add(l_groupInfo);
		groupInfoPane.add(groupInfo);
		
		l_groupMembers = new JLabel("Group Members:");
		groupmemerlist = new ObjectList(members);
		groupMemverListScrPane = new JScrollPane(groupmemerlist,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		groupMemberListPane = new JPanel();
		groupMemberListPane.setLayout(new BoxLayout(groupMemberListPane, BoxLayout.Y_AXIS));
		groupMemberListPane.add(l_groupMembers);
		groupMemberListPane.add(groupmemerlist.getFilterField());
		groupMemberListPane.add(groupMemverListScrPane);
		
		
		this.add(groupInfoPane);
		this.add(groupMemberListPane);
		this.setDividerLocation(0.36f);
		this.setDividerSize(5);
	}
}

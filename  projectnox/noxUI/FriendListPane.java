package noxUI;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;

public class FriendListPane extends JScrollPane{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// test filter list
	FriendListPane() 
	{
		String[] listItems = {
            "Chris", "Joshua", "Daniel", "Michael",
            "Don", "Kimi", "Kelly", "Keagan"
        };
/*        JFrame frame = new JFrame ("FilteredJList");
        frame.getContentPane().setLayout (new BorderLayout());*/
        // populate list
        FilteredJList list = new FilteredJList();
        for (int i=0; i<listItems.length; i++)
            list.addItem (listItems[i]);
        /*// add to gui
        JScrollPane pane =
            new JScrollPane (list,
                             ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);*/
        add (list.getFilterField());
    }

}

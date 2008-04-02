package noxUI;

import java.awt.Container;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class AboutDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 显示关于信息JEditorPane
	 */
	private JEditorPane aboutDoc;
	/**
	 * 用户文件夹路径
	 */
	private String userDirProfix = new String("file:///"
			+ System.getProperty("user.dir")
			+ System.getProperty("file.separator"));

	AboutDialog() {
		this.setTitle("Welcome to the NoX world!");
		Container contentPane = getContentPane();

		aboutDoc = new JEditorPane();
		// p_rg_roleInfo.setText(" Role Infomation : ");
		aboutDoc.setEditable(false);
		// aboutDoc.setBounds(0, 0, 300, 200);
		try {
			String url = userDirProfix + "resrc\\docs\\About.htm";
			aboutDoc.setPage(url);
		} catch (IOException e1) {
		}
		aboutDoc.setEditable(false);// 设置为不可编辑以使超链接激活
		/**
		 * 添加超链接监听器
		 */
		aboutDoc.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e_m_hylink) {
				try {
					// System.out.println(e_m_hylink.getEventType().toString());
					/**
					 * if EventType == ENTERED ,it means that the user moves
					 * mouse over if EventType == EXITED ,it means that the user
					 * moves mouse over and moves out if EventType == ACTIVATED
					 * ,it means that the user clicks on the link
					 */
					if (e_m_hylink.getEventType().toString()
							.equals("ACTIVATED"))
						aboutDoc.setPage(e_m_hylink.getURL());
				} catch (IOException ex_m_hylink) {
				}
			}
		});

		// aboutDoc.setBackground(new Color(255, 0, 0));
		contentPane.add(aboutDoc);
	}
}

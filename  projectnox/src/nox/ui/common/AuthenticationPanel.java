package nox.ui.common;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;

/**
 * ���ڼ�����ʱ��������?
 * @author shinysky
 *
 */
@SuppressWarnings("serial")
public class AuthenticationPanel extends JPanel {

	public AuthenticationPanel(JFrame parent, PeerGroup pg) {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	public boolean isCanceled() {
		return false;
	}

	public boolean isReadyForJoin() {
		return false;
	}

	public void join() throws PeerGroupException{
	}

}

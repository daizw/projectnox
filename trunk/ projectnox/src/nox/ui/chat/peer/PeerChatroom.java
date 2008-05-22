package nox.ui.chat.peer;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import nox.net.peer.PeerConnectionHandler;
import nox.ui.chat.common.Chatroom;
import nox.ui.common.InfiniteProgressPanel;
import nox.ui.common.NoxFrame;
import nox.ui.common.PeerItem;
import nox.ui.common.SystemPath;
import nox.xml.NoxFileUnit;

/**
 * 
 * @author shinysky
 * 
 */
@SuppressWarnings("serial")
public class PeerChatroom extends Chatroom {
	/**
	 * ���ӶԷ�ʱ��ʾ��ģ������ָʾ��
	 */
	private InfiniteProgressPanel glassPane;
	private PeerConnectionHandler handler;
	/**
	 * ����Ӧ�ô������ڼ̳���ɫ, ͸���� ����ʵ��:�����ںʹ�������ͬ��������ɫ��͸����.
	 * ��ʵ�����������ڵ�ʱ�����ñ�����һ��Vector��,
	 * ������ɫ��͸����ʱ�� Vector��ʵ�����ε��õ��ں���.
	 * 
	 * <li>���캯��.</li>
	 * <li>�û�˫������ͼ��ʱ, ��������ڶ�Ӧ��chatroom, ��</li>
	 * <ol>
	 * <li>�����µ�chatroom;</li>
	 * <li>�Զ���������.</li>
	 * </ol>
	 * @param friend ������ѵ�PeerItem
	 * @see PeerItem
	 */
	public PeerChatroom(final PeerItem friend, PeerConnectionHandler handler) {
		super(friend.getName(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "chat_green_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "chat_green_48.png", false);
		
		this.handler = handler;
		//roomID �ƺ�δ�õ�
		roomID = friend.getUUID();
		roomname = friend.getName();

		PeerChatroomSidePane portraits = new PeerChatroomSidePane(friend
				.getName(), friend.getPortrait(), new ImageIcon(
				SystemPath.PORTRAIT_RESOURCE_PATH + "portrait.png"));
		rootpane.add(portraits);
		rootpane.add(chatroompane);
		
		glassPane = new InfiniteProgressPanel("������, ���Ժ�...", 12);
		glassPane.setBounds(0, -NoxFrame.TITLE_HEIGHT, WIDTH_PREF, HEIGHT_PREF
				- NoxFrame.TITLE_HEIGHT * 2);

		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(glassPane);
		this.getContainer().add(rootpane);
		
		rootpane.setVisible(false);
		glassPane.start();
	}

	public boolean showTimeOutMsg() {
		removeMask();
		
		System.out.println("Timeout: Failed to connect to the peer.");
		int choice = JOptionPane
				.showConfirmDialog(
						PeerChatroom.this,
						"Sorry, you can get him/her right now. Try again?",
						"Failed to connect",
						JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			rootpane.setVisible(false);
			glassPane.setVisible(true);
			glassPane.start();
			//PeerChatroom.this.setVisible(true);
			return true;
		} else{
			PeerChatroom.this.dispose();
			return false;
		}
	}

	public void removeMask() {
		glassPane.stop();
		glassPane.setVisible(false);
		rootpane.setVisible(true);
		this.repaint();
	}
	public void incomingMsgProcessor(String sender, String time, Object msgdata) {
		if(msgdata instanceof NoxFileUnit){
			NoxFileUnit incomingFile = (NoxFileUnit)msgdata;
			
			chatroompane.incomingMsgProcessor(sender,
					time, "(" + roomname + " just send over you a file." + ")");
			
			String filename = incomingFile.getName();
			
			byte[] fileDataBytes = incomingFile.getData();
			
			JFileChooser chooser=new JFileChooser(".");
			chooser.setDialogTitle("����-�������ļ���");
			chooser.setSelectedFile( new File(filename) );
			int returnVal = chooser.showSaveDialog(PeerChatroom.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				System.out.println("Saving file as: "
						+ chooser.getSelectedFile().getPath());
				FileOutputStream fstream = null;
				BufferedOutputStream stream = null;
				try {
					fstream = new FileOutputStream(chooser.getSelectedFile());
					stream = new BufferedOutputStream(fstream);
		            stream.write(fileDataBytes);
		        } catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
		            if (stream != null) {
		                try {
		                    stream.close();
		                    fstream.close();
		                } catch (IOException e1) {
		                    e1.printStackTrace();
		                }
		            }
		        }
			}
		} else
			chatroompane.incomingMsgProcessor(sender, time, msgdata);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(String strmsg, boolean encrypt) {
		return handler.SendMsg(strmsg, encrypt);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(BufferedImage bufImg, boolean encrypt) {
		return handler.SendMsg(bufImg, encrypt);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean SendMsg(File file, boolean encrypt) {
		return handler.SendMsg(file, encrypt);
	}
}

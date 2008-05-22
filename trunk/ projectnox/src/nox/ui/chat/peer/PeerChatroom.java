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
	 * 连接对方时显示的模糊进度指示器
	 */
	private InfiniteProgressPanel glassPane;
	private PeerConnectionHandler handler;
	/**
	 * 最终应该从主窗口继承颜色, 透明度 考虑实现:主窗口和从属窗口同步调节颜色和透明度.
	 * 在实例化从属窗口的时候将引用保存在一个Vector中,
	 * 调节颜色及透明度时对 Vector中实例依次调用调节函数.
	 * 
	 * <li>构造函数.</li>
	 * <li>用户双击好友图标时, 如果不存在对应的chatroom, 则</li>
	 * <ol>
	 * <li>建立新的chatroom;</li>
	 * <li>自动尝试连接.</li>
	 * </ol>
	 * @param friend 代表好友的PeerItem
	 * @see PeerItem
	 */
	public PeerChatroom(final PeerItem friend, PeerConnectionHandler handler) {
		super(friend.getName(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "chat_green_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "chat_green_48.png", false);
		
		this.handler = handler;
		//roomID 似乎未用到
		roomID = friend.getUUID();
		roomname = friend.getName();

		PeerChatroomSidePane portraits = new PeerChatroomSidePane(friend
				.getName(), friend.getPortrait(), new ImageIcon(
				SystemPath.PORTRAIT_RESOURCE_PATH + "portrait.png"));
		rootpane.add(portraits);
		rootpane.add(chatroompane);
		
		glassPane = new InfiniteProgressPanel("连接中, 请稍候...", 12);
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
			chooser.setDialogTitle("保存-请输入文件名");
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

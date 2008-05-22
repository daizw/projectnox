package nox.ui.chat.group;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.jxta.discovery.DiscoveryService;
import nox.net.group.GroupConnectionHandler;
import nox.ui.chat.common.Chatroom;
import nox.ui.common.GroupItem;
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
public class GroupChatroom extends Chatroom {
	/**
	 * ���ӶԷ�ʱ��ʾ��ģ������ָʾ��
	 */
	private InfiniteProgressPanel glassPane;
	private GroupConnectionHandler handler;
	/**
	 * ����Ӧ�ô������ڼ̳���ɫ, ͸���� ����ʵ��:�����ںʹ�������ͬ��������ɫ��͸����.
	 * ��ʵ�����������ڵ�ʱ�����ñ�����һ��Vector��,
	 * ������ɫ��͸����ʱ�� Vector��ʵ�����ε��õ��ں���.
	 * <li>���캯��.</li>
	 * <li>�û�˫������ͼ��ʱ, ��������ڶ�Ӧ��chatroom, ��</li>
	 * <ol>
	 * <li>�����µ�chatroom;</li>
	 * <li>�Զ���������.</li>
	 * </ol>
	 * @param group ������ѵ�PeerItem
	 * @see PeerItem
	 */
	public GroupChatroom(final GroupItem group, GroupConnectionHandler handler) {
		super(group.getName(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_48.png", false);
		
		this.handler = handler;
		roomID = group.getUUID();
		roomname = group.getName();

		GroupChatroomSidePane gcsp = new GroupChatroomSidePane(this, group
				.getName(), new PeerItem[0]);
		rootpane.add(gcsp);
		rootpane.add(chatroompane);
		
		glassPane = new InfiniteProgressPanel("������, ���Ժ�...", 12);
		glassPane.setBounds(0, -NoxFrame.TITLE_HEIGHT, WIDTH_PREF, HEIGHT_PREF
				- NoxFrame.TITLE_HEIGHT * 2);

		this.getContainer().add(glassPane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane);
		
		rootpane.setVisible(false);
		glassPane.start();
	}
	public DiscoveryService getDiscoveryService() {
		return handler.getDiscoveryService();
	}
	public boolean showTimeOutMsg() {
		removeMask();
		
		System.out.println("Timeout: Failed to connect to the peer.");
		int choice = JOptionPane
				.showConfirmDialog(
						GroupChatroom.this,
						"Sorry, can't connect to this group right now. Try again?",
						"Failed to connect",
						JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION) {
			rootpane.setVisible(false);
			glassPane.setVisible(true);
			glassPane.start();
			//GroupChatroom.this.setVisible(true);
			return true;
		} else{
			GroupChatroom.this.dispose();
			return false;
		}
	}
	public void removeMask() {
		glassPane.stop();
		glassPane.setVisible(false);
		rootpane.setVisible(true);
		rootpane.repaint();
		this.repaint();
		super.repaint();
	}
	/*public GroupChatroom(PeerGroupAdvertisement pga) {
		super(pga.getName(), SystemPath.IMAGES_RESOURCE_PATH
				+ "bkgrd.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_20.png", SystemPath.ICONS_RESOURCE_PATH
				+ "groupChat_48.png", false);
		
		*//**
		 * ��ʼ��peergroup, ��������Աʹ��
		 *//*
		PeerGroup parentgroup = NoxToolkit.getNetworkManager().getNetPeerGroup();

		if(pga != null){
			try {
				peergroup = parentgroup.newGroup(pga);
			} catch (PeerGroupException e) {
				e.printStackTrace();
			}
		}
		
		*//**
		 * TODO �Ƴ�ԭ���Ĺܵ�������
		 *//*
		roomID = pga.getPeerGroupID();
		
		GroupChatroomSidePane gcsp 
			= new GroupChatroomSidePane(this, pga.getDescription(), new PeerItem[0]);
		rootpane.add(gcsp);
		rootpane.add(chatroompane);
		this.getContainer().setLayout(new BorderLayout());
		this.getContainer().add(rootpane, BorderLayout.CENTER);
		//this.setVisible(true);
	}*/

	/*public void discoverMembers(){
		;
	}*/
	public void incomingMsgProcessor(String sender, String time, Object msgdata) {
		if(msgdata instanceof NoxFileUnit){
			NoxFileUnit incomingFile = (NoxFileUnit)msgdata;
			
			chatroompane.incomingMsgProcessor(sender,
					time, "(" + roomname + " just send over you a file." + ")");
			
			String filename = incomingFile.getName();
			
			byte[] fileDataBytes = incomingFile.getData();
			
			JFileChooser chooser=new JFileChooser(".");
			chooser.setDialogTitle("����:�������ļ���");
			chooser.setSelectedFile( new File(filename) );
			int returnVal = chooser.showSaveDialog(GroupChatroom.this);
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
	/*private void importDESKey() {
		System.out.println("importing DES key file...");
		JFileChooser chooser = new JFileChooser(".");
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| (f.isFile() && (f.getName().endsWith(".key")));
			}

			@Override
			public String getDescription() {
				return "*.key";
			}
		};
		chooser.setFileFilter(filter);
		chooser.setDialogTitle("��ѡ��ӽ������õ�DES Key�ļ�");
		int returnVal = chooser.showOpenDialog(GroupChatroom.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// getJtf_pic().setText(chooser.getSelectedFile().getPath());
			System.out.println("You chose a deskey file: "
					+ chooser.getSelectedFile().getPath());
			File file = chooser.getSelectedFile();
			try {
				byte[] keyBytes = NoxMsgUtil.getBytesFromFile(file);
				DESKey = (SecretKey)NoxMsgUtil.getObjectFromBytes(keyBytes);
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
				DESKey = (SecretKey) in.readObject();
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}*/
}

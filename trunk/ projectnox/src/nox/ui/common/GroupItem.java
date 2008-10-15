package nox.ui.common;

import javax.swing.ImageIcon;

import net.jxta.protocol.PeerGroupAdvertisement;

@SuppressWarnings("serial")
public class GroupItem extends NoxJListItem {
	private String password = null;
	private int onlineCount;
	private int memberCount;
	//private Object[] members;

	public GroupItem(ImageIcon portr, PeerGroupAdvertisement adv, String password) {
		super(portr, adv.getName(), adv.getDescription(), adv.getPeerGroupID());
		//this.members = new PeerItem[0];
		this.password = password;
	}

	public GroupItem(ImageIcon portr, PeerGroupAdvertisement adv) {
		this(portr, adv, null);
	}

	public String getPassword() {
		return password;
	}
	
	/*public Object[] getMembers(){
		return members;
	}*/

	public int getOnlineCount() {
		return onlineCount;
	}

	public int getCount() {
		return memberCount;
	}
}
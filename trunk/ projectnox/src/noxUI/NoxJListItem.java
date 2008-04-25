package noxUI;

import javax.swing.ImageIcon;

abstract class NoxJListItem {
	private ImageIcon portrait;
	private String nickname;
	private String sign;
	private String UUID;

	NoxJListItem(ImageIcon portr, String nick, String signstr, String uuid) {
		this.portrait = portr;
		this.nickname = nick;
		this.sign = signstr;
		this.UUID = uuid;
	}

	protected ImageIcon getPortrait() {
		return portrait;
	}

	protected String getNick() {
		return nickname;
	}

	protected String getSign() {
		return sign;
	}
	
	protected String getUUID(){
		return UUID;
	}
}

class PeerItem extends NoxJListItem{
	PeerItem(ImageIcon portr, String nick, String signstr, String uuid){
		super(portr, nick, signstr, uuid);
	}
}

class GroupItem extends NoxJListItem{
	private int onlineCount;
	private int memberCount;
	GroupItem(ImageIcon portr, String name, String signstr, String uuid, int oc, int mc){
		super(portr, name, signstr, uuid);
		onlineCount = oc;
		memberCount = mc;
	}
	public int getOnlineCount(){
		return onlineCount;
	}
	public int getCount(){
		return memberCount;
	}
}

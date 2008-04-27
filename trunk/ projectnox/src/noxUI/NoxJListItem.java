package noxUI;

import javax.swing.ImageIcon;

import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroupID;

public abstract class NoxJListItem {
	private ImageIcon portrait;
	private String nickname;
	private String sign;
	private ID UUID;
	protected ItemStatus stat;

	NoxJListItem(ImageIcon portr, String nick, String signstr, ID uuid) {
		this.portrait = portr;
		this.nickname = nick;
		this.sign = signstr;
		this.UUID = uuid;
	}
	
	public abstract void setStatus(Advertisement adv);
	public abstract ItemStatus getStatus();

	protected ImageIcon getPortrait() {
		return portrait;
	}

	protected String getNick() {
		return nickname;
	}

	protected String getSign() {
		return sign;
	}
	
	protected ID getUUID(){
		return UUID;
	}
}

class PeerItem extends NoxJListItem{
	PeerItem(ImageIcon portr, String nick, String signstr, ID uuid){
		super(portr, nick, signstr, uuid);
	}

	/**
	 * 根据adv来确定...混乱了...
	 */
	@Override
	public	void setStatus(Advertisement adv) {
		// TODO 根据adv设置状态
		stat = ItemStatus.ONLINE;
	}

	@Override
	public ItemStatus getStatus() {
		// TODO Auto-generated method stub
		return stat;
	}
}

class GroupItem extends NoxJListItem{
	private int onlineCount;
	private int memberCount;
	GroupItem(ImageIcon portr, String name, String signstr, PeerGroupID uuid, int oc, int mc){
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
	@Override
	public	void setStatus(Advertisement adv) {
		// TODO 根据adv设置状态
		stat = ItemStatus.ONLINE;
	}
	@Override
	public ItemStatus getStatus() {
		// TODO Auto-generated method stub
		return stat;
	}
}

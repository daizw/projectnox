package nox.ui;

import java.io.Serializable;
import java.util.Date;

import javax.swing.ImageIcon;

import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

public abstract class NoxJListItem implements Serializable{
	protected ImageIcon portrait;
	//本来打算保存广告, 但是在存储到数据库时发现XML文档不能序列化!
	//只好再改回来, 只保存一些必要信息.
	//Holy shit!
	//protected Advertisement adv;
	protected String name;
	protected String discription;
	protected ID id;
	protected ItemStatus stat;
	protected Long timeStamp;

	NoxJListItem(ImageIcon portr, String name, String disc, ID uuid) {
		this.portrait = portr;
		this.name = name;
		this.discription = disc;
		this.id = uuid;
		updateTimeStamp();
	}

	/**
	 * 设置状态
	 * 
	 * @param adv
	 *            根据该广告中某元素设置?
	 */
	public abstract void setStatus(Advertisement adv);

	/**
	 * 获取状态
	 * 
	 * @return 当前(在线)状态
	 */
	public abstract ItemStatus getOnlineStatus();

	/**
	 * 获取头像
	 * 
	 * @return 头像ImageIcon
	 */
	public ImageIcon getPortrait() {
		return portrait;
	}

	/**
	 * 获取名字(昵称)
	 * 
	 * @return 昵称Text
	 */
	public String getName(){
		return name;
	}
	/**
	 * 获取签名档(描述)
	 * 
	 * @return 签名档(描述)Text
	 */
	public String getDesc(){
		return discription;
	}
	/**
	 * 获取该Item的ID
	 * 
	 * @return 组的PeerGroupID,或者Peer的PeerID
	 */
	public ID getUUID() {
		return id;
	}

	/**
	 * 获取时间戳
	 * 
	 * @return 时间戳
	 */
	public Long getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * 将时间戳更新为当前时间
	 */
	public void updateTimeStamp() {
		this.timeStamp = new Date().getTime();
	}
	
	/**
	 * 设置头像
	 */
	public void setPortrait(ImageIcon portr) {
		portrait = portr;
	}

	/**
	 * 设置名字(昵称)
	 */
	public void setName(String n){
		name = n;
	}
	/**
	 * 设置签名档(描述)
	 */
	public void setDesc(String desc){
		discription = desc;
	}
}

@SuppressWarnings("serial")
class PeerItem extends NoxJListItem {
	PeerItem(ImageIcon portr, PeerAdvertisement adv) {
		super(portr, adv.getName(), adv.getDescription(), adv.getPeerID());
	}

	/**
	 * 根据adv来确定...混乱了...
	 */
	@Override
	public void setStatus(Advertisement adv) {
		// TODO 根据adv设置状态
		stat = ItemStatus.ONLINE;
		updateTimeStamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ItemStatus getOnlineStatus() {
		return stat;
	}
}

@SuppressWarnings("serial")
class GroupItem extends NoxJListItem {
	private String password = null;
	private int onlineCount;
	private int memberCount;
	//private Object[] members;

	GroupItem(ImageIcon portr, PeerGroupAdvertisement adv, String password) {
		super(portr, adv.getName(), adv.getDescription(), adv.getPeerGroupID());
		//this.members = new PeerItem[0];
		this.password = password;
	}

	GroupItem(ImageIcon portr, PeerGroupAdvertisement adv) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStatus(Advertisement adv) {
		// TODO 根据adv设置状态
		stat = ItemStatus.ONLINE;
		updateTimeStamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ItemStatus getOnlineStatus() {
		return stat;
	}
}

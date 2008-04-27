/**
 * 
 */
package net.nox;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import noxUI.NoxJListItem;
import noxUI.SearchingFrame.AdvTable;
import noxUI.Chatroom;
/**
 * NoX 工具包
 * @author shinysky
 */
public class NoxToolkit {
	/**
	 * 搜索事件处理器
	 * @author shinysky
	 *
	 */
	public class HuntingEventHandler extends DiscoveryEventHandler{
		AdvTable table;
		public HuntingEventHandler(AdvTable tab){
			table = tab;
		}
		public void setAdvTable(AdvTable tab){
			table = tab;
		}
		public void eventOccured(AdvTable searchResultTable, Advertisement adv, Object src, long delay) {
			try{
				table.addRow(adv, src, delay);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		@Override
		public void eventOccured(Advertisement adv, Object src, long delay) {
			eventOccured(table, adv, src, delay);
		}
	}
	/**
	 * 用于检查在线状态的事件处理器
	 * @author shinysky
	 *
	 */
	public class CheckStatusEventHandler extends DiscoveryEventHandler{
		NoxJListItem listItem;
		
		public CheckStatusEventHandler(NoxJListItem item){
			listItem = item;
		}
		public void setItem(NoxJListItem it){
			listItem = it;
		}

		@Override
		public void eventOccured(Advertisement adv, Object src, long delay) {
			eventOccured(listItem, adv, src, delay);
		}

		public void eventOccured(NoxJListItem item, Advertisement adv,
				Object src, long delay) {
			try{
				item.setStatus(adv);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	private static JXTANetwork network;
	private static NetworkManager manager;
	private static NetworkConfigurator configer;
	//private static AdvHunter advhunter;
	private static HuntingEventHandler hehandler;
	private static CheckStatusEventHandler cshandler;
	private static Set<Chatroom> chatrooms;
	
	public NoxToolkit(){
	}
	
	public NoxToolkit(JXTANetwork nw, NetworkManager mng, NetworkConfigurator conf, /*AdvHunter ah,*/ HuntingEventHandler heh, CheckStatusEventHandler csh){
		network = nw;
		manager = mng;
		configer = conf;
		//advhunter = ah;
		hehandler = heh;
		cshandler = csh;
		chatrooms = new HashSet<Chatroom>();
		chatrooms.clear();
	}
	public JXTANetwork getNetwork(){
		return network;
	}
	public NetworkManager getNetworkManager(){
		return manager;
	}
	public NetworkConfigurator getNetworkConfigurator(){
		return configer;
	}
	/*public AdvHunter getAdvHunter(){
		return advhunter;
	}*/
	public HuntingEventHandler getHuntingEventHandler(){
		return hehandler;
	}
	public CheckStatusEventHandler getCheckStatusEventHandler(){
		return cshandler;
	}
	public void addChatroom(Chatroom room){
		chatrooms.add(room);
	}
	public Chatroom getChatroom(ID id){
		Iterator<Chatroom> it = chatrooms.iterator();
		Chatroom room;
		while (it.hasNext())
		{// 遍历集合
			room = (Chatroom)(it.next());
			System.out.println("Iterator here : " + room.getRoomID());
			if(room.getRoomID() == null || id == null){
				System.err.println("Error	: This room has no ID or you want to get a Chatroom without any id, it's very strange!!");
				System.exit(-1);
			}
			if(id.equals(room.getRoomID())){
				return room;
			}
		}
		return null;
	}
}

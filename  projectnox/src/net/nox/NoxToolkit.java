/**
 * 
 */
package net.nox;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.jxta.document.Advertisement;
import net.jxta.id.ID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.util.JxtaBiDiPipe;
import noxUI.AdvTable;
import noxUI.Chatroom;
import noxUI.Cheyenne;
import noxUI.GroupChatroom;
import noxUI.NoxJListItem;
import noxUI.PeerChatroom;
/**
 * NoX 工具包
 * @author shinysky
 */
public class NoxToolkit {
	/**
	 * 搜索事件处理器,
	 * 目前无用
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
				searchResultTable.addRow(adv, src, delay);
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
	private static Cheyenne cheyenne;
	//TODO 考虑使用Map, 使用方法可参考tutorial中的PropagatedPipServer类
	private static Set<ChatroomUnit> chatrooms;
	private static float Opacity = 100;
	
	public NoxToolkit(){ }
	
	public NoxToolkit(JXTANetwork nw, NetworkManager mng, NetworkConfigurator conf, /*AdvHunter ah,*/ HuntingEventHandler heh, CheckStatusEventHandler csh){
		network = nw;
		manager = mng;
		configer = conf;
		//advhunter = ah;
		hehandler = heh;
		cshandler = csh;
		chatrooms = new HashSet<ChatroomUnit>();
		chatrooms.clear();
	}
	
	public static JXTANetwork getNetwork(){
		return network;
	}
	public static NetworkManager getNetworkManager(){
		return manager;
	}
	public static NetworkConfigurator getNetworkConfigurator(){
		return configer;
	}
	/*public AdvHunter getAdvHunter(){
		return advhunter;
	}*/
	public static HuntingEventHandler getHuntingEventHandler(){
		return hehandler;
	}
	public static CheckStatusEventHandler getCheckStatusEventHandler(){
		return cshandler;
	}
	
	public static void setCheyenne(Cheyenne chy){
		cheyenne = chy;
	}
	public static Cheyenne getCheyenne(){
		return cheyenne;
	}
	public static float getOpacity(){
		return Opacity;
	}
	public static void setOpacity(float opa){
		Opacity = opa;
	}
	/**
	 * 注册ID和Pipe的对应关系
	 * @param id
	 * @param pipe
	 */
	public static PeerChatroomUnit registerChatroomUnit(ID id, JxtaBiDiPipe pipe){
		PeerChatroomUnit newRoomUnit = new PeerChatroomUnit(id, pipe);
		chatrooms.add(newRoomUnit);
		return newRoomUnit;
	}
	public static PeerChatroomUnit registerChatroomUnit(ID id, JxtaBiDiPipe pipe, Chatroom room){
		PeerChatroomUnit newRoomUnit = new PeerChatroomUnit(id, pipe, (PeerChatroom) room);
		chatrooms.add(newRoomUnit);
		return newRoomUnit;
	}
	/**
	 * 注册ID和IOPipe的对应关系
	 * @param id chatroom 的ID(组ID)
	 * @param ipipe inputpipe
	 * @param opipe outputpipe
	 */
	public static GroupChatroomUnit registerChatroomUnit(ID id, InputPipe ipipe, OutputPipe opipe){
		GroupChatroomUnit newRoomUnit = new GroupChatroomUnit(id, ipipe, opipe);
		chatrooms.add(newRoomUnit);
		return newRoomUnit;
	}
	public static GroupChatroomUnit registerChatroomUnit(ID id, InputPipe ipipe, OutputPipe opipe, Chatroom room){
		GroupChatroomUnit newRoomUnit = new GroupChatroomUnit(id, ipipe, opipe, (GroupChatroom) room);
		chatrooms.add(newRoomUnit);
		return newRoomUnit;
	}
	/**
	 * 为ID添加对应的room
	 * @param id
	 * @param room
	 */
	public static void registerChatroom(ID id, Chatroom room){
		Iterator<ChatroomUnit> it = chatrooms.iterator();
		ChatroomUnit roomunit;

		while (it.hasNext())
		{// 遍历集合
			roomunit = it.next();
			System.out.println("Chatroom Iterator here : " + roomunit.getRoomID());
			if(roomunit.getRoomID() == null || id == null){
				System.out.println("Error	: This room has no ID or you want to get a Chatroom without any id, it's very strange!!");
				network.StopNetwork();
				System.exit(-1);
			}
			if(roomunit.getChatroom() != null){
				System.out.println("Error: That's bad, the room already exists! It's unusual!");
			}
			if(id.equals(roomunit.getRoomID())){
				System.out.println("I find the ID, now I will set the chatroom");
				roomunit.setChatroom(room);
				return;
			}
		}
		System.out.println("If you see this message, it's bad. Please check the NoxToolkit.registerChatroom()");
	}
	public static ChatroomUnit getChatroomUnit(ID id){
		Iterator<ChatroomUnit> it = chatrooms.iterator();
		ChatroomUnit roomunit;

		while (it.hasNext())
		{// 遍历集合
			roomunit = (ChatroomUnit)(it.next());
			System.out.println("Chatroom Iterator here : " + roomunit.getRoomID());
			if(roomunit.getRoomID() == null || id == null){
				System.out.println("Error	: This room has no ID or you want to get a Chatroom without any id, it's very strange!!");
				network.StopNetwork();
				System.exit(-1);
			}
			if(id.equals(roomunit.getRoomID())){
				System.out.println("I find the room, it exist already");
				return roomunit;
			}else
				System.out.println("Unfortunately, this room is not what we're look for.");
		}
		System.out.println("Unfortunately, the room doesn't exist yet, I find nothing here.");
		return null;
	}
	
	public static PeerGroup createNewPeerGroup(String name, String desc, String pwd){
		return null;
	}

	public static PeerGroup createNewPeerGroup(String name, String desc) {
		return null;
	}
}

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
import net.jxta.util.JxtaBiDiPipe;
import noxUI.AdvTable;
import noxUI.Cheyenne;
import noxUI.NoxJListItem;
import noxUI.Chatroom;
/**
 * NoX ���߰�
 * @author shinysky
 */
public class NoxToolkit {
	/**
	 * �����¼�������,
	 * Ŀǰ����
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
	 * ���ڼ������״̬���¼�������
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
	/**
	 * Chatroom��Ԫ�ṹ,
	 * ���ڱ���roomID, outpipe, ��chatroom�Ķ�Ӧ��ϵ.
	 * 
	 * @author shinysky
	 *
	 */
	public class ChatroomUnit{
		private ID roomID = null;
		private JxtaBiDiPipe outbidipipe = null;
		private Chatroom room = null;
		
		public ChatroomUnit(ID id, JxtaBiDiPipe pipe){
			this(id, pipe, null);
		}
		public ChatroomUnit(ID id, JxtaBiDiPipe pipe, Chatroom rm){
			roomID = id;
			outbidipipe = pipe;
			room = rm;
		}
		public ID getRoomID(){
			return roomID;
		}
		public JxtaBiDiPipe getOutPipe(){
			return outbidipipe;
		}
		/**
		 * ����ChatroomUnit��outbidipipe, ��ͬ������Chatroom(�������)��outbidipipe
		 * @param pipe
		 */
		public void setOutPipe(JxtaBiDiPipe pipe){
			outbidipipe = pipe;
			//ͬ��room��outpipe
			if(room != null)
				room.setOutBidipipe(pipe);
		}
		public Chatroom getChatroom(){
			return room;
		}
		public void setChatroom(Chatroom rm){
			room = rm;
		}
	}
	private static JXTANetwork network;
	private static NetworkManager manager;
	private static NetworkConfigurator configer;
	//private static AdvHunter advhunter;
	private static HuntingEventHandler hehandler;
	private static CheckStatusEventHandler cshandler;
	private static Cheyenne cheyenne;
	private static Set<ChatroomUnit> chatrooms;
	private static float Opacity = 100;
	
	public NoxToolkit(){
	}
	
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
	
	public void setCheyenne(Cheyenne chy){
		cheyenne = chy;
	}
	public Cheyenne getCheyenne(){
		return cheyenne;
	}
	public float getOpacity(){
		return Opacity;
	}
	public void setOpacity(float opa){
		Opacity = opa;
	}
	/**
	 * ע��ID��Pipe�Ķ�Ӧ��ϵ
	 * @param id
	 * @param pipe
	 */
	public ChatroomUnit registerChatroomUnit(ID id, JxtaBiDiPipe pipe){
		ChatroomUnit newRoomUnit = new ChatroomUnit(id, pipe);
		chatrooms.add(newRoomUnit);
		return newRoomUnit;
	}
	public ChatroomUnit registerChatroomUnit(ID id, JxtaBiDiPipe pipe, Chatroom room){
		ChatroomUnit newRoomUnit = new ChatroomUnit(id, pipe, room);
		chatrooms.add(newRoomUnit);
		return newRoomUnit;
	}
	/**
	 * ΪID��Ӷ�Ӧ��room
	 * @param id
	 * @param room
	 */
	public void registerChatroom(ID id, Chatroom room){
		Iterator<ChatroomUnit> it = chatrooms.iterator();
		ChatroomUnit roomunit;

		while (it.hasNext())
		{// ��������
			roomunit = (ChatroomUnit)(it.next());
			System.out.println("Chatroom Iterator here : " + roomunit.getRoomID());
			if(roomunit.getRoomID() == null || id == null){
				System.out.println("Error	: This room has no ID or you want to get a Chatroom without any id, it's very strange!!");
				network.StopNetwork();
				System.exit(-1);
			}
			if(roomunit.getChatroom() != null){
				System.out.println("Error: That's bad, the room already exist! It's unusual!");
			}
			if(id.equals(roomunit.getRoomID())){
				System.out.println("I find the ID, now I will set the chatroom");
				roomunit.setChatroom(room);
				return;
			}
		}
		System.out.println("If you see this message, it's bad. Please check the NoxToolkit.registerChatroom()");
	}
	public ChatroomUnit getChatroomUnit(ID id){
		Iterator<ChatroomUnit> it = chatrooms.iterator();
		ChatroomUnit roomunit;

		while (it.hasNext())
		{// ��������
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
}

/*
 * Copyright (c) 2006-2007 Sun Microsystems, Inc.  All rights reserved.
 *
 *  The Sun Project JXTA(TM) Software License
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include the following acknowledgment: "This product includes software
 *     developed by Sun Microsystems, Inc. for JXTA(TM) technology."
 *     Alternately, this acknowledgment may appear in the software itself, if
 *     and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *     not be used to endorse or promote products derived from this software
 *     without prior written permission. For written permission, please contact
 *     Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA", nor may
 *     "JXTA" appear in their name, without prior written permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SUN
 *  MICROSYSTEMS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  JXTA is a registered trademark of Sun Microsystems, Inc. in the United
 *  States and other countries.
 *
 *  Please see the license information page at :
 *  <http://www.jxta.org/project/www/license.html> for instructions on use of
 *  the license in source files.
 *
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many individuals
 *  on behalf of Project JXTA. For more information on Project JXTA, please see
 *  http://www.jxta.org.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 */
package net.nox;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.peer.PeerID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import noxUI.Chatroom;
import xml.XmlMsgFormat;

/**
 * This is the server (receiver) side of the Bi-directional Pipe Tutorial. <p/>
 * This example does the following :
 * <ol>
 * <li>Open a server pipe.</li>
 * <li>Listen for connect requests via {@code accept()}.</li>
 * <li>For each connect request spawn a thread which:
 * <ol>
 * <li>Sends {@code ITERATIONS} messages to the connection.</li>
 * <li>Waits {@code ITERATIONS} responses.</li>
 * </ol>
 * </li>
 * </ol>
 * 
 * Connection wrapper. Once started, it sends ITERATIONS messages and receives a
 * response from the initiator for each message.
 * 
 */
public class ConnectionHandler implements Runnable, PipeMsgListener{

	private final JxtaBiDiPipe outbidipipe;
	/**
	 * 远程发现
	 */
	private boolean remoteFound = false;
	/**
	 * 对方的adv
	 */
	private Advertisement incomingPeerAdv = null;
	/**
	 * 发现ID监听器
	 */
	final DiscoveryListener listener = new DiscoveryListener() {
		@Override
		public void discoveryEvent(DiscoveryEvent event) {
			System.out.println("In ConnectionHandler: Begin discoveryEvent()");
			remoteFound = true;
			Enumeration<Advertisement> remAdvEnum = event.getSearchResults();
			if (remAdvEnum != null) {
				while (remAdvEnum.hasMoreElements()) {
					incomingPeerAdv = (Advertisement) remAdvEnum.nextElement();
					System.out
							.println("Here is a loc adv of the peer who made the incoming call:\n"
									+ incomingPeerAdv);
				}
			}
			System.out.println("In ConnectionHandler: End discoveryEvent()");
		}
	};

	/**
	 * Constructor for the MsgReceiver object
	 * 
	 * @param pipe
	 *            message pipe
	 */
	public ConnectionHandler(JxtaBiDiPipe pipe) {
		this.outbidipipe = pipe;
		outbidipipe.setMessageListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void pipeMsgEvent(PipeMsgEvent event) {
		System.out.println("===Begin ConnectionHandler PipeMsgEvent()===");
		// TODO 处理消息
		// grab the message from the event
		Message msg = event.getMessage();

		System.out.println("Incoming call: " + msg.toString());

		// get the message element named SenderMessage
		MessageElement senderEle = msg.getMessageElement(
		// XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null, XmlMsgFormat.SENDER_ELEMENT_NAME);
		MessageElement senderIDEle = msg.getMessageElement(
		// XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null, XmlMsgFormat.SENDERID_ELEMENT_NAME);

		MessageElement receiverEle = msg.getMessageElement(
		// XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null, XmlMsgFormat.RECEIVER_ELEMENT_NAME);
		MessageElement receiverIDEle = msg.getMessageElement(
		// XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null, XmlMsgFormat.RECEIVERID_ELEMENT_NAME);
		MessageElement timeEle = msg.getMessageElement(
		// XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null, XmlMsgFormat.TIME_ELEMENT_NAME);
		MessageElement msgEle = msg.getMessageElement(
		// XmlMsgFormat.MESSAGE_NAMESPACE_NAME,
				null, XmlMsgFormat.MESSAGE_ELEMENT_NAME);

		System.out.println("Detecting if the msg elements is null");

		if (null == senderEle || receiverEle == null || timeEle == null
				|| msgEle == null) {
			System.out.println("Msg is empty, it's weird.");
			return;
		}
		System.out.println("Incoming call: From: " + senderEle.toString());
		System.out.println("Incoming call: FromID: " + senderIDEle.toString());
		System.out.println("Incoming call: To: " + receiverEle.toString());
		System.out.println("Incoming call: ToID: " + receiverIDEle.toString());
		System.out.println("Incoming call: At: " + timeEle.toString());
		System.out.println("Incoming call: Msg: " + msgEle.toString());

		// Get message
		// TODO 这是在干嘛?
		if (null == senderEle.toString() || receiverEle.toString() == null
				|| timeEle.toString() == null || msgEle.toString() == null) {
			System.out
					.println("Msg.toString() is empty, it's weird even more.");
			return;
		}

		System.out.println("Connection-Handler got Message :"
				+ msgEle.toString());
		// TODO 处理收到的消息
		ID chatroomID;
		try {
			chatroomID = (PeerID) IDFactory.fromURI(new URI(senderIDEle
					.toString()));
			Chatroom room = new NoxToolkit().getChatroom(chatroomID);
			if (room == null)// 不存在
			{
				// room = new Chatroom((PeerItem)listItem);
				// new NoxToolkit().addChatroom(room);
				System.out
						.println("The room doesn't exist, find the peer adv in local cache");
				// TODO 查找该ID的adv, 向用户提示该消息, 根据adv创建对应的Chatroom
				// 在本地查找
				Enumeration<Advertisement> locAdvEnum = new NoxToolkit()
						.getNetworkManager().getNetPeerGroup()
						.getDiscoveryService().getLocalAdvertisements(
								DiscoveryService.PEER, "PID",
								senderIDEle.toString());
				System.out.println("Local Discovery Done!");
				if (locAdvEnum != null) {
					System.out.println("We got some in local cache!:" + locAdvEnum.hasMoreElements());
					while (locAdvEnum.hasMoreElements()) {
						incomingPeerAdv = (Advertisement) locAdvEnum.nextElement();
						System.out
								.println("Here is a loc adv of the peer who made the incoming call:\n"
										+ incomingPeerAdv);
					}
				} 
				if(incomingPeerAdv == null)
				{
					// 远程查找
					System.out
							.println("Can't find the peer's adv in local cache, try to find it remotely...");
					new NoxToolkit().getNetworkManager().getNetPeerGroup()
							.getDiscoveryService().getRemoteAdvertisements(
									null, DiscoveryService.PEER, "PID",
									senderIDEle.toString(), 1, listener);
					int unittime = 500;
					int waittime = 5 * 1000;
					int timecount = waittime / unittime;
					while (!remoteFound && timecount-- > 0) {
						try {
							Thread.sleep(unittime);
							System.out.println("remoteDiscovery timecount:"
									+ timecount);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (!remoteFound) {
						// 仍然没发现
						System.out
								.println("I still can't find the peer's adv, is it a ghost?");
						return;
					}
				}
				if (incomingPeerAdv != null) {
					System.out.println("Find the adv locally.");
					System.out.println("Establishing a chatroom with "
							+ ((PeerAdvertisement) incomingPeerAdv).getName());
					room = new NoxToolkit().getCheyenne().setupChatroomWith(
							(PeerAdvertisement) incomingPeerAdv, outbidipipe);
				}
			}
			if (room != null) {
				room.setOutBidipipe(outbidipipe);
				room.pack();
				room.setVisible(true);
				room.processIncomingMsg(msg, true);
			} else {
				// TODO 说明没有发现该peer的广告, 是陌生人消息;可能是远程发现timeout设得太小了.
				// 需要获取该公告;
				// 然后建立chatroom;
				System.out.println("可能是远程发现timeout设得太小了...");
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("===End ConnectionHandler PipeMsgEvent()===");
	}

	/**
	 * Send a series of messages over a pipe
	 * 
	 * @param bidipipe
	 *            the pipe to send messages over
	 * @throws IOException
	 *             Thrown for errors sending messages.
	 */
	private void sendGreetingMessages(JxtaBiDiPipe bidipipe) throws IOException {
		Message msg;

		System.out.println("Sending message");
		// create the message
		msg = new Message();
		Date date = new Date(System.currentTimeMillis());
		// add a string message element with the current date
		StringMessageElement stns = new StringMessageElement(
				XmlMsgFormat.TIME_ELEMENT_NAME, date.toString(), null);
		String hellomsg = "Say Hello, [F: 100]In BiDiPipe MsgReceiver!\n from "
				+ new NoxToolkit().getNetworkConfigurator().getName();
		StringMessageElement mns = new StringMessageElement(
				XmlMsgFormat.MESSAGE_ELEMENT_NAME, hellomsg, null);

		msg.addMessageElement(null, stns);
		msg.addMessageElement(null, mns);

		bidipipe.sendMessage(msg);
	}

	/**
	 * Main processing method for the ConnectionHandler object
	 */
	public void run() {
		try {
			sendGreetingMessages(outbidipipe);
			// TODO what
		} catch (Throwable all) {
			all.printStackTrace();
		}
		// TODO 根据bidipipe获取remote peerID, 判断是否是好友;
		// 如果是好友就显示消息提示; 不是, 则进行相应处理...
		// PeerID remotePeerID =
		// bidipipe.getRemotePeerAdvertisement().getPeerID();
	}
}

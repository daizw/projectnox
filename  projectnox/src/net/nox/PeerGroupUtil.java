/*
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Sun Microsystems, Inc. for Project JXTA."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *  must not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA",
 *  nor may "JXTA" appear in their name, without prior written
 *  permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: PeerGroupUtil.java,v 1.7 2007/05/28 22:00:51 nano Exp $
 */

package net.nox;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.passwd.PasswdMembershipService;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

/**
 * @author james todd [gonzo at jxta dot org]
 * @version $Id: PeerGroupUtil.java,v 1.7 2007/05/28 22:00:51 nano Exp $
 */

public class PeerGroupUtil {

	public static final String MEMBERSHIP_ID = "NoXUser";

	private static final long MILLISECONDS_IN_A_WEEK = 7 * 24 * 60 * 60 * 1000;

	/**
	 * Create a new PeerGroupAdvertisment from which a new PeerGroup will be
	 * created. <p/> See "Create a Secure Peer Group" in <a
	 * href="http://www.jxta.org/docs/JxtaProgGuide_v2.pdf">Jxta Programmers
	 * Guide</a> for how to create a secure group. Most of the code in this
	 * class is taken direcly from that chapter
	 * 
	 * @param parentGroup
	 *            the parent PeerGroup
	 * @param name
	 *            the name of the new PeerGroup
	 * @param description
	 *            the description of the new PeerGroup
	 * @param password
	 *            the password for the new Peergroup. If it is null or an empty
	 *            string this peer group is not password protected
	 * @return a new PeerGroupAdvertisement
	 */
	public static PeerGroupAdvertisement createPGA(PeerGroup parentGroup,
			String name, String description, String password, long expiration)
			throws Exception {
		return createPGA(parentGroup, name, description, password, expiration, null);
	}

	/**
	 * 创建新的PGA
	 * 
	 * @param parentGroup
	 *            parent group
	 * @param name
	 *            广告的名字
	 * @param description
	 *            描述
	 * @param password
	 *            密码, 为空或为null则表明无需密码认证
	 * @param expiration
	 *            生命期(?)
	 * @param id
	 *            给定的ID, 如果为null, 则自动生成新ID
	 * @return 所生成的PGA
	 * @throws Exception
	 */
	public static PeerGroupAdvertisement createPGA(PeerGroup parentGroup,
			String name, String description, String password, long expiration,
			PeerGroupID id) throws Exception {
		PeerGroupAdvertisement pga;
		ModuleImplAdvertisement mia;
		boolean passProt = (password != null && !password.trim().equals(""));

		System.out.println("in createPGA(): Creating PGA with: " + name + ":" + password);
		if(password.equals(""))
			System.out.println("password == \"\"");
		else if(password == null)
			System.out.println("password == null");
		else
			System.out.println("password == >" + password + "<");
		
		System.out.println("parentGroup:");
		System.out.println(parentGroup.getPeerGroupName());
		System.out.println(parentGroup.getPeerGroupID());
		
		// create the ModuleImplAdvertisement
		mia = parentGroup.getAllPurposePeerGroupImplAdvertisement();

		System.out.println("mia:\n" + mia.toString());
		// 如果有密码, 则在mia中添加密码认证moduleImpl
		if (passProt) {
			System.out.println("pasProt == true : Begin createPasswordModuleImpl()");
			createPasswordModuleImpl(mia);
		}

		// publish it
		parentGroup.getDiscoveryService().publish(mia);
		parentGroup.getDiscoveryService().remotePublish(mia);

		// create the PeerGroupAdvertisment and publish it
		pga = (PeerGroupAdvertisement) AdvertisementFactory
				.newAdvertisement(PeerGroupAdvertisement.getAdvertisementType());
		pga.setPeerGroupID(id != null ? id : IDFactory.newPeerGroupID());
		pga.setName(name);
		pga.setDescription(description);
		pga.setModuleSpecID(mia.getModuleSpecID());

		//向PGA中添加(加密后的)密码
		if (passProt) {
			StructuredTextDocument login = (StructuredTextDocument) StructuredDocumentFactory
					.newStructuredDocument(MimeMediaType.XMLUTF8, "Param");
			String loginString = MEMBERSHIP_ID + ":"
					+ PasswdMembershipService.makePsswd(password) + ":";
			System.out.println("loginString = " + loginString);
			
			TextElement loginElement = login
					.createElement("login", loginString);

			login.appendChild(loginElement);
			pga.putServiceParam(PeerGroup.membershipClassID, login);
		}

		DiscoveryService ds = parentGroup.getDiscoveryService();

		//发布组广告
		ds.publish(pga, expiration != 0 ? expiration
				: 2 * MILLISECONDS_IN_A_WEEK, expiration != 0 ? expiration
				: 2 * MILLISECONDS_IN_A_WEEK);
		ds.remotePublish(pga, expiration != 0 ? expiration
				: 2 * MILLISECONDS_IN_A_WEEK);

		System.out.println("mia final edition:\n" + mia.toString());
		
		return pga;
	}
	
	/**
	 * Updates the ModuleImplAdvertisement of the PeerGroupAdvertisement to
	 * reflect the fact that we want to use the PasswordService in order to
	 * manage the membership in this group
	 * 
	 * @param mia
	 *            the ModuleImplAdvertisement to update
	 */
	private static void createPasswordModuleImpl(ModuleImplAdvertisement mia)
			throws Exception {
		StdPeerGroupParamAdv stdPgParams = new StdPeerGroupParamAdv(mia
				.getParam());
		Map<ModuleClassID, Object> params = stdPgParams.getServices();
		boolean found = false;

		// loop until the MembershipService is found
		for (Iterator<ModuleClassID> pi = params.keySet().iterator(); pi
				.hasNext()
				&& !found;) {
			ModuleClassID serviceID = pi.next();

			if (serviceID.equals(PeerGroup.membershipClassID)) {
				// get the Advertisement for the MembershipService
				ModuleImplAdvertisement memServices = (ModuleImplAdvertisement) params
						.get(serviceID);

				// create a new Advertisement describing the password service
				ModuleImplAdvertisement newMemServices = createPasswordServiceImpl(memServices);

				// update the services hashtable
				params.remove(serviceID);
				params.put(PeerGroup.membershipClassID, newMemServices);
				found = true;

				// and update the Service parameters list for the
				// ModuleImplAdvertisement
				mia.setParam((Element) stdPgParams
						.getDocument(MimeMediaType.XMLUTF8));

				// change the ModuleSpecID since this
				if (!mia.getModuleSpecID().equals(
						PeerGroup.allPurposePeerGroupSpecID)) {
					System.out.println("in createPasswordModuleImpl(): setting moduleSpecID, mia has general MSID");
					mia.setModuleSpecID(IDFactory.newModuleSpecID(mia
							.getModuleSpecID().getBaseClass()));
				} else {
					ID passID = ID.nullID;

					try {
						passID = IDFactory.fromURI(new URI("urn", "jxta:uuid-"
								+ "DeadBeefDeafBabaFeedBabe00000001" + "04"
								+ "06", null));
					} catch (URISyntaxException use) {
						use.printStackTrace();
					}

					System.out.println("in createPasswordModuleImpl(): setting moduleSpecID to deadBeef stuff...");
					mia.setModuleSpecID((ModuleSpecID) passID);
				}
			}
		}
	}

	/**
	 * Create the ModuleImplAdvertisement that describes the PasswordService
	 * that this group is going to use
	 * 
	 * @param template
	 *            the previous ModuleImplAdvertisement that we use as a template
	 * @return the ModuleImplAdvertisement that describes the PasswordService
	 *         that this group is going to use
	 */
	private static ModuleImplAdvertisement createPasswordServiceImpl(
			ModuleImplAdvertisement template) {
		ModuleImplAdvertisement passMember = (ModuleImplAdvertisement) AdvertisementFactory
				.newAdvertisement(ModuleImplAdvertisement
						.getAdvertisementType());

		passMember
				.setModuleSpecID(PasswdMembershipService.passwordMembershipSpecID);
		passMember.setCode(PasswdMembershipService.class.getName());
		passMember.setDescription("Membership Services for NoX");
		passMember.setCompat(template.getCompat());
		passMember.setUri(template.getUri());
		passMember.setProvider(template.getProvider());

		return passMember;
	}
	
	public static boolean joinPeerGroup(PeerGroup satellaPeerGroup, String login,
			String passwd) {
		System.out.println("begin joinPeerGroup()");
		// Get the Heavy Weight Paper for the resume
		// Alias define the type of credential to be provided
		StructuredDocument creds = null;
		try {
			// Create the resume to apply for the Job
			// Alias generate the credentials for the Peer Group
			AuthenticationCredential authCred = new AuthenticationCredential(
					satellaPeerGroup, null, creds);
			// Create the resume to apply for the Job
			// Alias generate the credentials for the Peer Group
			MembershipService membershipService = satellaPeerGroup
					.getMembershipService();
			// Send the resume and get the Job application form
			// Alias get the Authenticator from the Authentication creds
			Authenticator auth = membershipService.apply(authCred);
			// Fill in the Job Application Form
			// Alias complete the authentication
			AuthenticationUtil.completeAuth(auth, login, passwd);
			// Check if I got the Job
			// Alias Check if the authentication that was submitted was
			// accepted.
			if (!auth.isReadyForJoin()) {
				System.out.println("Failure in authentication.");
				System.out
						.println("Group was not joined. Does not know how to complete authenticator");
				return false;
			}
			// I got the Job, Join the company
			// Alias I the authentication I completed was accepted,
			// therefore join the Peer Group accepted.
			membershipService.join(auth);
			return true;
		} catch (Exception e) {
			System.out.println("Failure in authentication.");
			System.out.println("Group was not joined. Login was incorrect.");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获取本地广告
	 * 
	 * @param pg
	 *            (Parent?) PeerGroup
	 * @param name
	 *            广告的名字
	 * @return 发现的所有符合查询条件的广告的List
	 */
	public static List<PeerGroupAdvertisement> getLocalAdvs(PeerGroup pg,
			String name) {
		List<PeerGroupAdvertisement> p = new ArrayList<PeerGroupAdvertisement>();

		try {
			for (Enumeration<Advertisement> gas = pg.getDiscoveryService()
					.getLocalAdvertisements(DiscoveryService.GROUP,
							name != null ? "Name" : null, name); gas
					.hasMoreElements();) {
				Object o = gas.nextElement();

				if (o instanceof PeerGroupAdvertisement) {
					p.add((PeerGroupAdvertisement) o);
				}
			}
		} catch (IOException ioe) {
		}

		return p;
	}
	
	/**
	 * 获取本地广告
	 * 
	 * @param pg
	 *            (Parent?) PeerGroup
	 * @param name
	 *            广告的名字
	 * @return 发现的所有符合查询条件的广告的List
	 */
	public static PeerGroupAdvertisement getLocalAdvByID(PeerGroup pg,
			String id) {
		PeerGroupAdvertisement pga = null;

		try {
			for (Enumeration<Advertisement> gas = pg.getDiscoveryService()
					.getLocalAdvertisements(DiscoveryService.GROUP,
							id != null ? "GID" : null, id); gas.hasMoreElements();) {
				Advertisement adv = gas.nextElement();
				System.out.println("getLocalAdvByID(): got a pga");
				if (adv instanceof PeerGroupAdvertisement) {
					pga = (PeerGroupAdvertisement) adv;
					/*if(pga == null){
						pga = (PeerGroupAdvertisement) adv;
					}else{
						if(Long.parseLong(pga.getDescription())
								<Long.parseLong(((PeerGroupAdvertisement)adv).getDescription())){
							//如果当前正处理的广告比当前广告更新, 则更新
							pga = (PeerGroupAdvertisement) adv;
						}
					}*/
				}
			}
		} catch (IOException ioe) {
		}

		return pga;
	}

	/**
	 * 发现远程广告
	 * 
	 * @param pg
	 *            (Parent?) PeerGroup
	 * @param name
	 *            广告的名字
	 * @param listener
	 *            发现监听器
	 */
	public static void discoverRemoteAdvs(PeerGroup pg, String name,
			DiscoveryListener listener) {
		DiscoveryService s = pg.getDiscoveryService();

		s.getRemoteAdvertisements(null, DiscoveryService.GROUP,
				name != null ? "Name" : null, name, 10, listener);
	}
}

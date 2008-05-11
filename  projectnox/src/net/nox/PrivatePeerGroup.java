package net.nox;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 4cse03
 */
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.document.Element;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.TextElement;
import net.jxta.endpoint.*;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.ID;
import net.jxta.id.IDFactory;
import net.jxta.impl.membership.PasswdMembershipService;
import net.jxta.impl.protocol.*;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.ModuleSpecID;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.impl.peergroup.StdPeerGroupParamAdv;

public class PrivatePeerGroup implements Runnable {
	static Thread PeerGroupthread = new Thread(new PrivatePeerGroup());
	private PeerGroup RootNetPeerGroup = null;
	private PeerGroup ProtectedPeerGroup = null,
			discoveredProtectedPeerGroup = null;
	private final static PeerGroupID ProtectedPeerGroupID = PeerGroupID
			.create(URI
					.create("urn:jxta:uuid-4d6172676572696e204272756e6f202002"));

	public void peergroupcopy(PeerGroup RootNetPeerGroup) {
		this.RootNetPeerGroup = RootNetPeerGroup;
	}

	public void run() {
		/** Creates new RootWS */

		// Starts the JXTA Platform
		if (RootNetPeerGroup != null) {
			System.out.println("JXTA platform Started ...");
		} else {
			System.err
					.println("Failed to start JXTA :RootNetPeerGroup is null");
			System.exit(1);
		}
		// Generate the parameters:
		// login, passwd, peer group name and peer group id
		// for creating the Peer Group
		String login = "ppgroup";
		String passwd = "griffin";
		String groupName = "ProtectedGroup";

		// create The Passwd Authenticated Peer Group
		ProtectedPeerGroup = this.createPeerGroup(RootNetPeerGroup, groupName,
				login, passwd);
		// join the ProtectedPeerGroup Locally
		if (ProtectedPeerGroup != null) {
			System.out.println(" Peer Group Created ...");
			discoveredProtectedPeerGroup = this.discoverLocalPeerGroup(
					RootNetPeerGroup, ProtectedPeerGroupID);
			if (discoveredProtectedPeerGroup != null) {
				System.out.println(" Peer Group Found ...");
				this.joinPeerGroup(discoveredProtectedPeerGroup, login, passwd);
			}
		}
		System.out.println(" Peer Group Joined ...");
		// join the ProtectedPeerGroup Remote
		// Print the Peer Group Adverstisement on sdt out.
		this.printXmlAdvertisement(
				"XML Advertisement for Peer Group Advertisement",
				ProtectedPeerGroup.getPeerGroupAdvertisement());

	}

	private PeerGroup createPeerGroup(PeerGroup RootPeerGroup,
			String groupName, String login, String passwd) {
		// create the Peer Group by doing the following:
		// - Create a Peer Group Module Implementation Advertisement and publish
		// it
		// - Create a Peer Group Adv and publish it
		// - Create a Peer Group from the Peer Group Adv and return this object
		PeerGroup protectedPeerGroup = null;
		PeerGroupAdvertisement ProtectedPeerGroupAdvertisement;

		// Create the PeerGroup Module Implementation Adv
		ModuleImplAdvertisement passwdMembershipModuleImplAdv;
		passwdMembershipModuleImplAdv = this
				.createPasswdMembershipPeerGroupModuleImplAdv(RootPeerGroup);

		// Publish it in the parent peer group
		DiscoveryService rootPeerGroupDiscoveryService = RootPeerGroup
				.getDiscoveryService();
		try {
			rootPeerGroupDiscoveryService.publish(
					passwdMembershipModuleImplAdv, PeerGroup.DEFAULT_LIFETIME,
					PeerGroup.DEFAULT_EXPIRATION);
			rootPeerGroupDiscoveryService
					.remotePublish(passwdMembershipModuleImplAdv,
							PeerGroup.DEFAULT_EXPIRATION);
		} catch (java.io.IOException e) {
			System.err.println("Can't Publish passwdMembershipModuleImplAdv");
			System.exit(1);
		}
		// Now, Create the Peer Group Advertisement
		ProtectedPeerGroupAdvertisement = this.createPeerGroupAdvertisement(
				passwdMembershipModuleImplAdv, groupName, login, passwd);
		// Publish it in the parent peer group
		try {
			rootPeerGroupDiscoveryService.publish(
					ProtectedPeerGroupAdvertisement,
					PeerGroup.DEFAULT_LIFETIME, PeerGroup.DEFAULT_EXPIRATION);
			rootPeerGroupDiscoveryService.remotePublish(
					ProtectedPeerGroupAdvertisement,
					PeerGroup.DEFAULT_EXPIRATION);
		} catch (java.io.IOException e) {
			System.err
					.println("Can't Publish  ProtectedPeerGroupAdvertisement");
			System.exit(1);
		}
		// Finally Create the Peer Group
		if (ProtectedPeerGroupAdvertisement == null) {
			System.err.println("ProtectedPeerGroupAdvertisement is null");
		}
		try {
			protectedPeerGroup = RootPeerGroup
					.newGroup(ProtectedPeerGroupAdvertisement);
		} catch (net.jxta.exception.PeerGroupException e) {
			System.err
					.println("Can't create Protected Peer Group from Advertisement");
			e.printStackTrace();
			return null;
		}

		return protectedPeerGroup;
	}

	private PeerGroupAdvertisement createPeerGroupAdvertisement(
			ModuleImplAdvertisement passwdMembershipModuleImplAdv,
			String groupName, String login, String passwd) {
		PeerGroupAdvertisement ProtectedPeerGroupAdvertisement = (PeerGroupAdvertisement) AdvertisementFactory
				.newAdvertisement(PeerGroupAdvertisement.getAdvertisementType());
		// Instead of creating a new group ID each time, by using the
		// line below
		// ProtectedPeerGroupAdvertisement.setPeerGroupID
		// (IDFactory.newPeerGroupID());
		// I use a fixed ID so that each time I start PrivatePeerGroup,
		// it creates the same Group
		ProtectedPeerGroupAdvertisement.setPeerGroupID(ProtectedPeerGroupID);
		ProtectedPeerGroupAdvertisement
				.setModuleSpecID(passwdMembershipModuleImplAdv
						.getModuleSpecID());
		ProtectedPeerGroupAdvertisement.setName(groupName);
		ProtectedPeerGroupAdvertisement
				.setDescription("Peer Group using Password Authentication");
		// Now create the Structured Document Containing the login and
		// passwd informations. Login and passwd are put into the Param
		// section of the peer Group
		if (login != null) {
			StructuredTextDocument loginAndPasswd = (StructuredTextDocument) StructuredDocumentFactory
					.newStructuredDocument(new MimeMediaType("text/xml"),
							"Param");
			String loginAndPasswdString = login + ":"
					+ PasswdMembershipService.makePsswd(passwd) + ":";
			TextElement loginElement = loginAndPasswd.createElement("login",
					loginAndPasswdString);
			loginAndPasswd.appendChild(loginElement);
			// All Right, now that loginAndPasswdElement
			// (The strucuted document
			// that is the Param Element for The PeerGroup Adv
			// is done, include it in the Peer Group Advertisement
			ProtectedPeerGroupAdvertisement.putServiceParam(
					PeerGroup.membershipClassID, loginAndPasswd);
		}
		return ProtectedPeerGroupAdvertisement;
	}

	private ModuleImplAdvertisement createPasswdMembershipPeerGroupModuleImplAdv(
			PeerGroup RootPeerGroup) {
		// Create a ModuleImpl Advertisement for the Passwd
		// Membership Service Take a allPurposePeerGroupImplAdv
		// ModuleImplAdvertisement parameter to
		// Clone some of its fields. It is easier than to recreate
		// everything from scratch

		// Try to locate where the PasswdMembership is within this
		// ModuleImplAdvertisement.
		// For a PeerGroup Module Impl, the list of the services
		// (including Membership) are located in the Param section
		ModuleImplAdvertisement allPurposePeerGroupImplAdv = null;
		try {
			allPurposePeerGroupImplAdv = RootPeerGroup
					.getAllPurposePeerGroupImplAdvertisement();
		} catch (java.lang.Exception e) {
			System.err
					.println("Can't Execute: getAllPurposePeerGroupImplAdvertisement();");
			System.exit(1);
		}
		ModuleImplAdvertisement passwdMembershipPeerGroupModuleImplAdv = allPurposePeerGroupImplAdv;
		ModuleImplAdvertisement passwdMembershipServiceModuleImplAdv = null;
		StdPeerGroupParamAdv passwdMembershipPeerGroupParamAdv = null;
		// error 1
		passwdMembershipPeerGroupParamAdv = new StdPeerGroupParamAdv(
				allPurposePeerGroupImplAdv.getParam());

		Hashtable allPurposePeerGroupServicesHashtable = new Hashtable(
				passwdMembershipPeerGroupParamAdv.getServices());
		Enumeration allPurposePeerGroupServicesEnumeration = allPurposePeerGroupServicesHashtable
				.keys();
		boolean membershipServiceFound = false;
		// error 1
		while ((!membershipServiceFound)
				&& (allPurposePeerGroupServicesEnumeration.hasMoreElements())) {
			Object allPurposePeerGroupServiceID = allPurposePeerGroupServicesEnumeration
					.nextElement();
			if (allPurposePeerGroupServiceID
					.equals(PeerGroup.membershipClassID)) {
				// allPurposePeerGroupMemershipServiceModuleImplAdv is
				// the all Purpose Membership Service for the all
				// purpose Peer Group Module Impl adv
				ModuleImplAdvertisement allPurposePeerGroupMemershipServiceModuleImplAdv = (ModuleImplAdvertisement) allPurposePeerGroupServicesHashtable
						.get(allPurposePeerGroupServiceID);
				// Create the passwdMembershipServiceModuleImplAdv
				passwdMembershipServiceModuleImplAdv = this
						.createPasswdMembershipServiceModuleImplAdv(allPurposePeerGroupMemershipServiceModuleImplAdv);
				// Remove the All purpose Membership Service implementation
				allPurposePeerGroupServicesHashtable
						.remove(allPurposePeerGroupServiceID);
				// And Replace it by the Passwd Membership Service
				// Implementation
				allPurposePeerGroupServicesHashtable.put(
						PeerGroup.membershipClassID,
						passwdMembershipServiceModuleImplAdv);
				membershipServiceFound = true;
				// Now the Service Advertisements are complete. Let's
				// update the passwdMembershipPeerGroupModuleImplAdv by
				// Updating its param
				passwdMembershipPeerGroupModuleImplAdv
						.setParam((Element) passwdMembershipPeerGroupParamAdv
								.getDocument(MimeMediaType.XMLUTF8));
				// Update its Spec ID This comes from the
				// Instant P2P PeerGroupManager Code (Thanks !!!!)
				if (!passwdMembershipPeerGroupModuleImplAdv.getModuleSpecID()
						.equals(PeerGroup.allPurposePeerGroupSpecID)) {
					passwdMembershipPeerGroupModuleImplAdv
							.setModuleSpecID(IDFactory
									.newModuleSpecID(passwdMembershipPeerGroupModuleImplAdv
											.getModuleSpecID().getBaseClass()));
				} else {
					ID passwdGrpModSpecID = ID.create(URI.create("urn"
							+ "jxta:uuid-" + "DeadBeefDeafBabaFeedBabe00000001"
							+ "04" + "06"));
					passwdMembershipPeerGroupModuleImplAdv
							.setModuleSpecID((ModuleSpecID) passwdGrpModSpecID);
				} // End Else
				membershipServiceFound = true;

			}
		}
		return passwdMembershipPeerGroupModuleImplAdv;
	}

	private ModuleImplAdvertisement createPasswdMembershipServiceModuleImplAdv(
			ModuleImplAdvertisement allPurposePeerGroupMemershipServiceModuleImplAdv) {
		// Create a new ModuleImplAdvertisement for the
		// Membership Service

		ModuleImplAdvertisement passwdMembershipServiceModuleImplAdv = (ModuleImplAdvertisement) AdvertisementFactory
				.newAdvertisement(ModuleImplAdvertisement
						.getAdvertisementType());

		passwdMembershipServiceModuleImplAdv
				.setModuleSpecID(PasswdMembershipService.passwordMembershipSpecID);
		passwdMembershipServiceModuleImplAdv
				.setCode(PasswdMembershipService.class.getName());
		passwdMembershipServiceModuleImplAdv
				.setDescription(" Module Impl Advertisement for the PasswdMembership Service");
		passwdMembershipServiceModuleImplAdv
				.setCompat(allPurposePeerGroupMemershipServiceModuleImplAdv
						.getCompat());
		passwdMembershipServiceModuleImplAdv
				.setUri(allPurposePeerGroupMemershipServiceModuleImplAdv
						.getUri());
		passwdMembershipServiceModuleImplAdv
				.setProvider(allPurposePeerGroupMemershipServiceModuleImplAdv
						.getProvider());

		return passwdMembershipServiceModuleImplAdv;

	}

	private PeerGroup discoverLocalPeerGroup(PeerGroup RootNetPeerGroup,
			PeerGroupID ProtectedPeerGroupID) {
		// First discover the peer group
		// In most cases we should use discovery listeners so that
		// we can do the discovery asynchroneously.
		// Here I won't, for increased simplicity and because
		// The Peer Group Advertisement is in the local cache for sure
		PeerGroup ProtectedpeerGroup;
		DiscoveryService RootNetPeerGroupDiscoveryService = null;
		if (RootNetPeerGroup != null) {
			RootNetPeerGroupDiscoveryService = RootNetPeerGroup
					.getDiscoveryService();
		} else {
			System.err
					.println("Can't join  Peer Group since it's parent is null");
			System.exit(1);
		}
		// Local
		boolean isGroupFound = false;
		Enumeration localPeerGroupAdvertisementEnumeration = null;
		PeerGroupAdvertisement ProtectedPeerGroupAdvertisement = null;
		while (!isGroupFound) {
			try {
				localPeerGroupAdvertisementEnumeration = RootNetPeerGroupDiscoveryService
						.getLocalAdvertisements(DiscoveryService.GROUP, "GID",
								ProtectedPeerGroupID.toString());
			} catch (java.io.IOException e) {
				System.out.println("Can't Discover Local Adv");
			}
			if (localPeerGroupAdvertisementEnumeration != null) {
				while (localPeerGroupAdvertisementEnumeration.hasMoreElements()) {
					PeerGroupAdvertisement pgAdv = null;
					pgAdv = (PeerGroupAdvertisement) localPeerGroupAdvertisementEnumeration
							.nextElement();
					if (pgAdv.getPeerGroupID().equals(ProtectedPeerGroupID)) {
						ProtectedPeerGroupAdvertisement = pgAdv;
						isGroupFound = true;
						break;
					}
				}
			}
			try {
				Thread.sleep(5 * 1000);
			} catch (Exception e) {
			}
		}
		try {
			ProtectedpeerGroup = RootNetPeerGroup
					.newGroup(ProtectedPeerGroupAdvertisement);
		} catch (net.jxta.exception.PeerGroupException e) {
			System.err.println("Can't create Peer Group from Advertisement");
			e.printStackTrace();
			return null;
		}
		return ProtectedpeerGroup;
	}

	private void discoverRemotePeerGroup(PeerGroup RootNetPeerGroup,
			PeerGroupID ProtectedPeerGroupID) {
		// First discover the peer group
		// In most cases we should use discovery listeners so that
		// we can do the discovery asynchroneously.
		// Here I won't, for increased simplicity and because
		// The Peer Group Advertisement is in the local cache for sure
		PeerGroup ProtectedpeerGroup;
		long waittime = 10 * 60 * 1000L;
		DiscoveryService RootNetPeerGroupDiscoveryService = null;
		DiscoveryListener disclisten = new discoverylistener();
		if (RootNetPeerGroup != null) {
			RootNetPeerGroupDiscoveryService = RootNetPeerGroup
					.getDiscoveryService();
		} else {
			System.err
					.println("Can't join  Peer Group since it's parent is null");
			System.exit(1);
		}
		try {
			// Add ourselves as a DiscoveryListener for DiscoveryResponse events
			RootNetPeerGroupDiscoveryService.addDiscoveryListener(disclisten);
			RootNetPeerGroupDiscoveryService.getRemoteAdvertisements(
			// no specific peer (propagate)
					null,
					// Adv type
					DiscoveryService.GROUP,
					// Attribute
					"GID",
					// Value
					ProtectedPeerGroupID.toString(),
					// one advertisement response is all we are looking for
					3,
					// no query specific listener. we are using a global
					// listener
					null);
			while (true) {
				// wait a bit before sending a discovery message
				try {
					System.out.println("Sleeping for :" + waittime);
					Thread.sleep(waittime);
				} catch (Exception e) {
					// ignored
				}
				System.out.println("Sending a Discovery Message");
				// look for any peer
				RootNetPeerGroupDiscoveryService.getRemoteAdvertisements(
				// no specific peer (propagate)
						null,
						// Adv type
						DiscoveryService.ADV,
						// Attribute = name
						"Name",
						// Value = the tutorial
						"Discovery tutorial",
						// one advertisement response is all we are looking for
						1,
						// no query specific listener. we are using a global
						// listener
						null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void joinPeerGroup(PeerGroup ProtectedPeerGroup, String login,
			String passwd) {
		// Get the Heavy Weight Paper for the resume
		// Alias define the type of credential to be provided
		StructuredDocument creds = null;
		try {
			// Create the resume to apply for the Job
			// Alias generate the credentials for the Peer Group
			AuthenticationCredential authCred = new AuthenticationCredential(
					ProtectedPeerGroup, null, creds);

			// Create the resume to apply for the Job
			// Alias generate the credentials for the Peer Group
			MembershipService membershipService = ProtectedPeerGroup
					.getMembershipService();

			// Send the resume and get the Job application form
			// Alias get the Authenticator from the Authentication creds
			Authenticator auth = membershipService.apply(authCred);

			// Fill in the Job Application Form
			// Alias complete the authentication
			completeAuth(auth, login, passwd);

			// Check if I got the Job
			// Alias Check if the authentication that was submitted was
			// accepted.
			if (!auth.isReadyForJoin()) {
				System.out.println("Failure in authentication.");
				System.out
						.println("Group was not joined. Does not know how to complete authenticator");
			}
			// I got the Job, Join the company
			// Alias I the authentication I completed was accepted,
			// therefore join the Peer Group accepted.
			membershipService.join(auth);
		} catch (Exception e) {
			System.out.println("Failure in authentication.");
			System.out.println("Group was not joined. Login was incorrect.");
			e.printStackTrace();
		}
	}

	private void completeAuth(Authenticator auth, String login, String passwd)
			throws Exception {
		Method[] methods = auth.getClass().getMethods();
		Vector authMethods = new Vector();

		// Find out with fields of the application needs to be filled
		// Alias Go through the methods of the Authenticator class and
		// copy them sorted by name into a vector.
		for (int eachMethod = 0; eachMethod < methods.length; eachMethod++) {
			if (methods[eachMethod].getName().startsWith("setAuth")) {
				if (Modifier.isPublic(methods[eachMethod].getModifiers())) {
					// sorted insertion.
					for (int doInsert = 0; doInsert <= authMethods.size(); doInsert++) {
						int insertHere = -1;
						if (doInsert == authMethods.size())
							insertHere = doInsert;
						else {
							if (methods[eachMethod].getName().compareTo(
									((Method) authMethods.elementAt(doInsert))
											.getName()) <= 0)
								insertHere = doInsert;
						} // end else

						if (-1 != insertHere) {
							authMethods.insertElementAt(methods[eachMethod],
									insertHere);
							break;
						} // end if ( -1 != insertHere)
					} // end for (int doInsert=0
				} // end if (modifier.isPublic
			} // end if (methods[eachMethod]
		} // end for (int eachMethod)

		Object[] AuthId = { login };
		Object[] AuthPasswd = { passwd };

		for (int eachAuthMethod = 0; eachAuthMethod < authMethods.size(); eachAuthMethod++) {
			Method doingMethod = (Method) authMethods.elementAt(eachAuthMethod);
			String authStepName = doingMethod.getName().substring(7);
			if (doingMethod.getName().equals("setAuth1Identity")) {
				// Found identity Method, providing identity
				doingMethod.invoke(auth, AuthId);

			} else if (doingMethod.getName().equals("setAuth2_Password")) {
				// Found Passwd Method, providing passwd
				doingMethod.invoke(auth, AuthPasswd);
			}
		}
	}

	private void printXmlAdvertisement(String title, Advertisement adv) {
		// First, Let's print a "nice" Title
		String separator = "";
		for (int i = 0; i < title.length() + 4; i++) {
			separator = separator + "-";
		}
		System.out.println(separator);
		System.out.println("| " + title + " |");
		System.out.println(separator);

		// Now let's print the Advertisement
		System.out.println(adv.toString());

		// Let's end up with a line
		System.out.println(separator);
	}

}

class discoverylistener implements DiscoveryListener {
	public void discoveryEvent(DiscoveryEvent ev) {

	}
}
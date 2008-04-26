package net.nox;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import javax.security.cert.CertificateException;
import javax.swing.JOptionPane;

import net.jxta.discovery.DiscoveryListener;
import net.jxta.exception.PeerGroupException;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.nox.NoxToolkit.CheckStatusEventHandler;
import net.nox.NoxToolkit.HuntingEventHandler;

public class JXTANetwork {

	public static final String Local_Peer_Name = "Local NoX Peer";
	public static final String Local_Network_Manager_Name = "Local NoX Network Manager";

	String locpeername = "";
	NetworkManager TheNetworkManager;
	NetworkConfigurator TheConfig;
	PeerGroup TheNetPeerGroup;
	AdvHunter disocveryClient;
	NoxToolkit toolkit;
	HuntingEventHandler hehandler;
	CheckStatusEventHandler cshandler;

	boolean StopDiscovery = false;

	public JXTANetwork() {
		// Creating the Network Manager
		try {
			System.out.println("Creating the Network Manager");
			String peername = GetPrincipal();

			TheNetworkManager = new NetworkManager(
					NetworkManager.ConfigMode.EDGE, Local_Network_Manager_Name,
					new File(new File(".cache"), peername).toURI());
			System.out.println("Network Manager created");
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}

		// Persisting it to make sure the Peer ID is not re-created each
		// time the Network Manager is instantiated
		TheNetworkManager.setConfigPersistent(true);

		// Since we won't be setting our own relay or rendezvous seed peers we
		// will use the default (public network) relay and rendezvous seeding.
		TheNetworkManager.setUseDefaultSeeds(true);

		TheNetworkManager.registerShutdownHook();

		// Retrieving the Network Configurator
		System.out.println("Retrieving the Network Configurator");
		try {
			TheConfig = TheNetworkManager.getConfigurator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Network Configurator retrieved");

		// Does a local peer configuration exist?
		if (TheConfig.exists()) {

			System.out.println("Local configuration found");
			// We load it
			File LocalConfig = new File(TheConfig.getHome(), "PlatformConfig");
			try {
				System.out.println("Loading found configuration");
				TheConfig.load(LocalConfig.toURI());
				System.out.println("Configuration loaded");
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(-1);
			} catch (CertificateException ex) {
				// An issue with the existing peer certificate has been
				// encountered
				ex.printStackTrace();
				System.exit(-1);
			}
		} else {
			System.out.println("No local configuration found");
			String principal = GetPrincipal();
			TheConfig.setPrincipal(principal);
			TheConfig.setPassword(GetPassword());
			TheConfig.setName(principal);
			TheConfig.setDescription("A NoX Peer");

			TheConfig.setTcpStartPort(9701);
			TheConfig.setTcpEndPort(65530);

			System.out.println("Principal: " + TheConfig.getPrincipal());
			System.out.println("Password : " + TheConfig.getPassword());

			try {
				System.out.println("Saving new configuration");
				TheConfig.save();
				System.out.println("New configuration saved successfully");
			} catch (IOException ex) {
				ex.printStackTrace();
				System.exit(-1);
			}
		}
		/**
		 * ≥ı ºªØNoxToolkit
		 */
		hehandler = new NoxToolkit().new HuntingEventHandler(null);
		cshandler = new NoxToolkit().new CheckStatusEventHandler(null);
		toolkit = new NoxToolkit(this, TheNetworkManager, hehandler, cshandler);
	}

	private String GetPrincipal() {
		return locpeername = (String) JOptionPane.showInputDialog(null,
				"Enter username", "Username", JOptionPane.QUESTION_MESSAGE,
				null, null, "");
	}

	private String GetPassword() {
		return (String) JOptionPane.showInputDialog(null, "Enter password",
				"Password", JOptionPane.QUESTION_MESSAGE, null, null, "");
	}

	public void SeekRendezVousConnection() {
		try {
			System.out.println("Starting JXTA");
			TheNetPeerGroup = TheNetworkManager.startNetwork();
			System.out.println("JXTA Started");

			System.out.println("Peer name	: " + TheNetPeerGroup.getPeerName());
			System.out.println("Peer ID		: "
					+ TheNetPeerGroup.getPeerID().toString());
			System.out.println("Peer Group name	: "
					+ TheNetPeerGroup.getPeerGroupName());
			System.out.println("Peer Group ID	: "
					+ TheNetPeerGroup.getPeerGroupID().toString());
		} catch (PeerGroupException ex) {
			// Cannot initialize peer group
			ex.printStackTrace();
			System.exit(-1);
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
		}

		System.out.println("Waiting for a rendezvous connection for 5 seconds "
				+ "(maximum)");
		boolean connected = TheNetworkManager.waitForRendezvousConnection(5000);
		System.out.println(MessageFormat.format("Connected :{0}", connected));
	}

	public void GoHunting(String peerid, int AdvType, String attribute,
			String value, int threshold, DiscoveryListener listener) {
		disocveryClient = new AdvHunter(TheNetworkManager);
		// new AdvHunter(TheNetworkManager, AdvType);

		long startTime = new Date().getTime();
		long waittime = 5 * 1000L;
		System.out.println("Start Time: " + new Date().getTime());

		StopDiscovery = false;

		while (!StopDiscovery) {
			// startTime = new Date().getTime();
			// look for any peer
			// String peerid, int type, String attribute, String value, int
			// threshold, DiscoveryListener listener, long starttime
			disocveryClient.LookAround(peerid, AdvType, attribute, value,
					threshold, listener, startTime);
			// wait a bit before sending a discovery message
			try {
				System.out.println("Sleeping for :" + waittime);
				Thread.sleep(waittime);
			} catch (Exception e) {
				// ignored
			}
		}
	}

	public void StopHunting() {
		StopDiscovery = true;
	}

	public void StopNetwork() {
		System.out.println("Stopping JXTA");
		TheNetworkManager.stopNetwork();
	}
}
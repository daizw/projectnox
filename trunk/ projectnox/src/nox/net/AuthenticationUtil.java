/*
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or withouta
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of sourcec code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following discalimer in
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
 *  ===================================================tre=================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *
 *  $Id: AuthenticationUtil.java,v 1.12 2007/06/10 21:15:12 nano Exp $
 */

package nox.net;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.endpoint.MessageTransport;
import net.jxta.exception.PeerGroupException;
import net.jxta.exception.ProtocolNotSupportedException;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.logging.Logging;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import nox.ui.AuthenticationPanel;

/**
 * @author james todd [gonzo at jxta dot org]
 * @version $Id: AuthenticationUtil.java,v 1.12 2007/06/10 21:15:12 nano Exp $
 */

public class AuthenticationUtil {

	// private static final String INTERACTIVE_AUTHENTICATOR =
	// "InteractiveAuthentication";
	private static final String STRING_AUTHENTICATOR = "StringAuthentication";
	private static final String AUTHENTICATION = STRING_AUTHENTICATOR;
	private static final String JXTA_TLS_TRANSPORT = "jxtatls";
	// private static final ResourceBundle STRINGS = Resources.getStrings();
	private static final Logger LOG = Logger.getLogger(AuthenticationUtil.class
			.getName());

	public static PeerGroup getTLSPeerGroup() {
		return getTLSPeerGroup(NoxToolkit.getNetworkManager().getNetPeerGroup());
		// return getTLSPeerGroup(g != null ? g.getPeerGroup() : null);
		// return NoxToolkit.getNetworkManager().getNetPeerGroup();
	}

	public static PeerGroup getTLSPeerGroup(PeerGroup pg) {
		if (Logging.SHOW_FINE && LOG.isLoggable(Level.FINE)) {
			LOG.fine("getTLSPeerGroup");
		}

		MessageTransport tls = pg.getEndpointService().getMessageTransport(
				JXTA_TLS_TRANSPORT);
		PeerGroup tlspg = null;

		if (tls != null) {
			tlspg = tls.getEndpointService().getGroup();
		}

		if (Logging.SHOW_INFO && LOG.isLoggable(Level.INFO)) {
			LOG.info("tls group: "
					+ (tlspg != null ? tlspg.getPeerGroupName() : null));
		}

		return tlspg;
	}

	public static boolean isAuthenticated(PeerGroup pg) {
		if (Logging.SHOW_FINE && LOG.isLoggable(Level.FINE)) {
			LOG.fine("isAuthenticated");
		}

		boolean isAuthenticated = false;

		try {
			isAuthenticated = (pg.getMembershipService().getDefaultCredential() != null);
		} catch (PeerGroupException pge) {
			if (Logging.SHOW_WARNING && LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "no default credential", pge);
			}
		}

		if (Logging.SHOW_INFO && LOG.isLoggable(Level.INFO)) {
			LOG.info("is authenticated: " + pg.getPeerGroupName() + " "
					+ isAuthenticated);
		}

		return isAuthenticated;
	}

	public static void completeAuth(Authenticator auth, String login,
			String passwd) throws Exception {
		/*
		auth.setAuth2Identity(login);
		auth.setAuth3_IdentityPassword(passwd);*/
		
		Method[] methods = auth.getClass().getMethods();
		Vector<Method> authMethods = new Vector<Method>();
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
			@SuppressWarnings("unused")
			String authStepName = doingMethod.getName().substring(7);
			if (doingMethod.getName().equals("setAuth1Identity")) {
				// Found identity Method, providing identity
				doingMethod.invoke(auth, AuthId);
				System.out.println("Find the setAuth1Identity(), great!");
			} else if (doingMethod.getName().equals("setAuth2_Password")) {

				// Found Passwd Method, providing passwd
				doingMethod.invoke(auth, AuthPasswd);
				System.out.println("Find the setAuth1Identity(), greater!");
			}
		}
	}

	public static boolean authenticate(PeerGroup pg) {
		return authenticate(pg, null, null);
	}

	public static boolean authenticate(PeerGroup pg, String keyStorePassword,
			String identityPassword) {
		return authenticate(pg, keyStorePassword, identityPassword, true);
	}

	public static boolean authenticate(PeerGroup pg, String keyStorePassword,
			String identityPassword, boolean join) {
		boolean isAuthenticated = isAuthenticated(pg);
		// xxx: assumption that ks pwd == id pwd
		String pwd = "";// v.getConfig() != null ? v.getConfig().getPassword() :
						// null;

		if (pwd != null) {
			if (keyStorePassword == null
					|| keyStorePassword.trim().length() == 0) {
				keyStorePassword = pwd;
			}

			if (identityPassword == null
					|| identityPassword.trim().length() == 0) {
				identityPassword = pwd;
			}
		}

		if (!isAuthenticated) {
			if (keyStorePassword != null && identityPassword != null) {
				MembershipService ms = pg.getMembershipService();
				AuthenticationCredential ac = new AuthenticationCredential(pg,
						AUTHENTICATION, null);
				StringAuthenticator sa = null;

				try {
					sa = (StringAuthenticator) ms.apply(ac);
				} catch (ProtocolNotSupportedException pnse) {
				} catch (PeerGroupException pge) {
				}
				
				/*sa.setAuth1_KeyStorePassword(store_password);
				sa.setAuth2Identity(id);
				sa.setAuth3_IdentityPassword(key_password);*/

				sa.setAuth1_KeyStorePassword(keyStorePassword);
				sa.setAuth2Identity(pg.getPeerID());
				sa.setAuth3_IdentityPassword(identityPassword);

				isAuthenticated = sa.isReadyForJoin();

				if (isAuthenticated && join) {
					try {
						ms.join(sa);
					} catch (PeerGroupException pge) {
						pge.toString();
					}
				}
			} else {
				// 密码为空, 应该怎么办?
				JFrame parent = null;
				AuthenticationPanel ap = new AuthenticationPanel(parent, pg);
				JDialog jd = new JDialog(parent, "Login", true);

				jd.getContentPane().add(ap);
				jd.pack();

				if (parent != null) {
					jd.setLocationRelativeTo((JFrame) parent);
				}

				ap.requestFocus();
				jd.setVisible(true);

				if (!ap.isCanceled()) {
					isAuthenticated = ap.isReadyForJoin();

					if (isAuthenticated && join) {
						try {
							ap.join();
						} catch (PeerGroupException pge) {
						}
					}
				}

				ap = null;
				jd = null;
			}
		}

		return isAuthenticated;
	}

	// public static boolean authenticate(PeerGroup pg) {
	// if (LOG.isEnabledFor(Level.INFO)) {
	// LOG.info("authenticate");
	// }
	//        
	// boolean isAuthenticated = false;
	//        
	// if (! isAuthenticated(pg)) {
	// if (LOG.isEnabledFor(Level.INFO)) {
	// LOG.info("authenticating group: " +
	// pg.getPeerGroupName());
	// }
	//                
	// MembershipService ms = pg.getMembershipService();
	// AuthenticationCredential ac = new AuthenticationCredential(pg,
	// "InteractiveAuthetnication", null);
	// InteractiveAuthenticator ia = null;
	//            
	// try {
	// ia = (InteractiveAuthenticator)ms.apply(ac);
	// } catch (ProtocolNotSupportedException pnse) {
	// if (LOG.isEnabledFor(Level.SEVERE)) {
	// LOG.error("apply membership", pnse);
	// }
	// } catch (PeerGroupException pge) {
	// if (LOG.isEnabledFor(Level.SEVERE)) {
	// LOG.error("apply membership", pge);
	// }
	// }
	//
	// if (ia != null &&
	// ia.interact() &&
	// ia.isReadyForJoin()) {
	// if (LOG.isEnabledFor(Level.INFO)) {
	// LOG.info("joining membership");
	// }
	//                
	// try {
	// ms.join(ia);
	// isAuthenticated = true;
	// } catch (PeerGroupException pge) {
	// if (LOG.isEnabledFor(Level.SEVERE)) {
	// LOG.error("apply membership", pge);
	// }
	// }
	// } else {
	// if (LOG.isEnabledFor(Level.INFO)) {
	// LOG.info("can't interact with membership");
	// }
	// }
	// } else {
	// if (LOG.isEnabledFor(Level.INFO)) {
	// LOG.info("already authenticated");
	// }
	// }
	//        
	// return isAuthenticated;
	// }
}

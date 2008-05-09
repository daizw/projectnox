package net.nox;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;

import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.protocol.PipeAdvertisement;

public class PipeUtil {
	private static final Logger LOG = Logger.getLogger(PipeUtil.class.getName());
	
	private static final int WaitTime = 500;		// Զ�̷��ֵĵȴ�ʱ�䣬����������������
	private static final int MAXRETRIES = 20;		// Զ�̷���ʱ�����Դ���������������������
	
	/**
	 * ��ȡ�ܵ���棬���ȴӱ��ػ����з��ֹܵ���棬���û�з��֣��ٴ�Զ�̷��ֹܵ���棬���û�з��֣���ô�����ܵ���档
	 * @param pg		���ڷ��ֻ򴴽��ܵ����Ľڵ���
	 * @param name		���ڷ��ֻ򴴽��ܵ��������ƣ�������ػ�Զ�̶�û�з��ֹܵ���棬��ô��ʹ�ø����ƴ����ܵ���棬��˸����Ʋ�Ҫʹ��ͨ���
	 * @param type		�����ܵ��������ͣ�Ŀǰ֧��3�ֻ�������
	 * 						JxtaUnicast			���򡢲���ȫ�Ͳ��ɿ�
	 * 						JxtaUnicastSecure	���򡢰�ȫ��ʹ��TLS��
	 * 						JxtaPropagate		������� 
	 * @param pipeId	ָ�����ɹܵ�����PipeId,�����ֵΪ�ջ�null����ôʹ��ϵͳ���ɵ�Id
	 * @param remotePublish	��������ܵ���棬��ô�Ƿ�Զ�̷���
	 * @return			�ڽڵ����ڷ��ֻ򴴽��Ĺ�����
	 */
	public static PipeAdvertisement getPipeAdv(PeerGroup pg, String name, String type, String pipeId, boolean remotePublish) {
		PipeAdvertisement myAdv = null;
		
		try {
			myAdv = findPipeAdv(pg, name);
			
			if(myAdv == null) {
				// We did not find the pipe advertisement, so create one
				LOG.info("Could not find the Pipe Advertisement");
				
				// Create a pipe advertisement
				myAdv = createAdv(pg, name, type, pipeId);
				
				// We have the advertisement; publish it into our local cache or remote peer
				publish(pg, myAdv, remotePublish);
				
				LOG.info("Created the Pipe Advertisement");
			}
		} catch(Exception e) {
			LOG.severe("Could not get pipe Advertisement");
			return null;
		}
		
		return myAdv;
	}
	
	/**
	 * ��ȡ�ܵ���棬���ȴӱ��ػ����з��ֹܵ���棬���û�з��֣��ٴ�Զ�̷��ֹܵ���棬���û�з��֣���ôʹ��ϵͳ���ɵ�Id�����ܵ���档
	 * @param pg		���ڷ��ֻ򴴽��ܵ����Ľڵ���
	 * @param name		���ڷ��ֻ򴴽��ܵ��������ƣ�������ػ�Զ�̶�û�з��ֹܵ���棬��ô��ʹ�ø����ƴ����ܵ���棬��˸����Ʋ�Ҫʹ��ͨ���
	 * @param type		�����ܵ��������ͣ�Ŀǰ֧��3�ֻ�������
	 * 						JxtaUnicast			���򡢲���ȫ�Ͳ��ɿ�
	 * 						JxtaUnicastSecure	���򡢰�ȫ��ʹ��TLS��
	 * 						JxtaPropagate		������� 
	 * @param remotePublish	��������ܵ���棬��ô�Ƿ�Զ�̷���
	 * @return			�ڽڵ����ڷ��ֻ򴴽��Ĺ�����
	 */
	public static PipeAdvertisement getPipeAdv(PeerGroup pg, String name, String type, boolean remotePublish) {
		return getPipeAdv(pg, name, type, null, remotePublish);
	}
	
	/**
	 * ��ȡ�ܵ���棬���ȴӱ��ػ����з��ֹܵ���棬���û�з��֣���ô�����ܵ���档
	 * @param pg		���ڷ��ֻ򴴽��ܵ����Ľڵ���
	 * @param name		���ڷ��ֻ򴴽��ܵ��������ƣ�������ػ�Զ�̶�û�з��ֹܵ���棬��ô��ʹ�ø����ƴ����ܵ���棬��˸����Ʋ�Ҫʹ��ͨ���
	 * @param type		�����ܵ��������ͣ�Ŀǰ֧��3�ֻ�������
	 * 						JxtaUnicast			���򡢲���ȫ�Ͳ��ɿ�
	 * 						JxtaUnicastSecure	���򡢰�ȫ��ʹ��TLS��
	 * 						JxtaPropagate		������� 
	 * @param pipeId	ָ�����ɹܵ�����PipeId,�����ֵΪ�ջ�null����ôʹ��ϵͳ���ɵ�Id
	 * @param remotePublish	��������ܵ���棬��ô�Ƿ�Զ�̷���
	 * @return			�ڽڵ����ڷ��ֻ򴴽��Ĺ�����
	 */
	public static PipeAdvertisement getPipeAdvWithoutRemoteDiscovery(PeerGroup pg, String name, String type, String pipeId, boolean remotePublish) {
        PipeAdvertisement pa = searchLocal(pg, name);

        if (pa == null) {
            pa = createAdv(pg, name, type, pipeId);

            publish(pg, pa, remotePublish);
        }else
        	System.out.println("Find a pipe adv locally, using it.");

        return pa;
	}
	
	/**
	 * ��ȡ�ܵ���棬���ȴӱ��ػ����з��ֹܵ���棬���û�з��֣���ôʹ��ϵͳ���ɵ�Id�����ܵ���档
	 * @param pg		���ڷ��ֻ򴴽��ܵ����Ľڵ���
	 * @param name		���ڷ��ֻ򴴽��ܵ��������ƣ�������ػ�Զ�̶�û�з��ֹܵ���棬��ô��ʹ�ø����ƴ����ܵ���棬��˸����Ʋ�Ҫʹ��ͨ���
	 * @param type		�����ܵ��������ͣ�Ŀǰ֧��3�ֻ�������
	 * 						JxtaUnicast			���򡢲���ȫ�Ͳ��ɿ�
	 * 						JxtaUnicastSecure	���򡢰�ȫ��ʹ��TLS��
	 * 						JxtaPropagate		������� 
	 * @param remotePublish	��������ܵ���棬��ô�Ƿ�Զ�̷���
	 * @return			�ڽڵ����ڷ��ֻ򴴽��Ĺ�����
	 */
	public static PipeAdvertisement getPipeAdvWithoutRemoteDiscovery(PeerGroup pg, String name, String type, boolean remotePublish) {
		return getPipeAdvWithoutRemoteDiscovery(pg, name, type, null, remotePublish);
	}
	
	/**
	 * ͬ����ʽ���ֹܵ����
	 * @param pg	�ڵ��飬�ڸýڵ����ڷ��ֹܵ����
	 * @param name	�ܵ�������ƣ���ʹ��ͨ���
	 * @return		�ܵ����������û���ҵ����ֹ��̷����쳣����ô����null
	 */
	public static PipeAdvertisement findPipeAdv(PeerGroup pg, String name) {
		DiscoveryService discovery = pg.getDiscoveryService();
		
		int count = MAXRETRIES; // Discovery retry count
		
		PipeAdvertisement myAdv = null;
		
		try {
			LOG.info("Attempting to Discover the pipe advertisement");
			
			// Check if we have already published ourselves
			while(count-- > 0) {
				// First, check locally if the advertisement is cached
				myAdv = searchLocal(pg, name);
				
				// If we found our pipe advertisement, we are done
				if(myAdv != null)
					break;

				// We did not find the advertisement locally;
				// send a remote request
				// remote advs will be stored in local cache
				discovery.getRemoteAdvertisements(null, DiscoveryService.ADV, PipeAdvertisement.NameTag, name, 1, null);
				
				// Sleep to allow time for peers to respond to the discovery request
				try {
					Thread.sleep(WaitTime);
				} catch(InterruptedException e) {
					// ignored
				}
			}
		} catch(Exception e) {
			LOG.severe("Could not get pipe Advertisement");
			return null;
		}
		
		if(myAdv != null) {
			LOG.info(myAdv.toString());
		} else {
			LOG.info("myAdv is null.");
		}
		
		return myAdv;
	}
	
	/**
	 * ��������pipeIDͬ����ʽ���ֹܵ����
	 * @param pg �ڵ��飬�ڸýڵ����ڷ��ֹܵ����
	 * @param pipeID Ҫ���ҵĹܵ����Ĺܵ�ID
	 * @return �ܵ����������û���ҵ����ֹ��̷����쳣����ô����null
	 */
	public static PipeAdvertisement findPipeAdvByPipeID(PeerGroup pg, String pipeID) {
		DiscoveryService discovery = pg.getDiscoveryService();
		
		int count = MAXRETRIES; // Discovery retry count
		
		PipeAdvertisement myAdv = null;
		
		try {
			LOG.info("Attempting to Discover the pipe advertisement");
			
			// Check if we have already published ourselves
			while(count-- > 0) {
				// First, check locally if the advertisement is cached
				myAdv = searchLocalByPipeID(pg, pipeID);
				
				// If we found our pipe advertisement, we are done
				if(myAdv != null)
					break;

				// We did not find the advertisement locally;
				// send a remote request
				// remote advs will be stored in local cache
				discovery.getRemoteAdvertisements(null, DiscoveryService.ADV, PipeAdvertisement.IdTag, pipeID, 10, null);
				
				// Sleep to allow time for peers to respond to the discovery request
				try {
					Thread.sleep(WaitTime);
				} catch(InterruptedException e) {
					// ignored
				}
			}
		} catch(Exception e) {
			LOG.severe("Could not get pipe Advertisement");
			return null;
		}
		
		if(myAdv != null) {
			LOG.info(myAdv.toString());
		} else {
			LOG.info("myAdv is null.");
		}
		
		return myAdv;
	}
	
	/**
	 * �����ܵ����
	 * @param pg		���ڴ����ܵ����Ľڵ���
	 * @param name		ʹ�ø����ƴ����ܵ����
	 * @param type		�����ܵ��������ͣ�Ŀǰ֧��3�ֻ�������
	 * 						JxtaUnicast			���򡢲���ȫ�Ͳ��ɿ�
	 * 						JxtaUnicastSecure	���򡢰�ȫ��ʹ��TLS��
	 * 						JxtaPropagate		������� 
	 * @param pipeId	ָ�����ɹܵ�����PipeId,�����ֵΪ�ջ�null����ôʹ��ϵͳ���ɵ�Id
	 * @return			�ڽڵ����ڴ����Ĺ�����
	 */
    public static PipeAdvertisement createAdv(PeerGroup pg, String name, String type, String pipeId) {
    	PipeAdvertisement pa = (PipeAdvertisement) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());

    	try {
			pa.setPipeID((pipeId == null || pipeId.equals(""))?
					IDFactory.newPipeID(pg.getPeerGroupID())
					:(PipeID) IDFactory.fromURI(new URI(pipeId)));
	    	pa.setName(name);
	    	pa.setType(type);
	    	pa.setDescription(new Date().getTime() + "");
		} catch (URISyntaxException e) {
			LOG.warning("a string could not be parsed as a URI reference");
			e.printStackTrace();
		}

    	return pa;
    }
	
    /**
     * ��������������
     * @param pg		���������ܵ����Ľڵ���
     * @param name		���������ܵ��������ƣ���ʹ��ͨ���
     * @return			�ڽڵ����ڴ����Ĺ����󣨷��ط��ֵ����µĹܵ���棩
     */
    public static PipeAdvertisement searchLocal(PeerGroup pg, String name) {
        DiscoveryService discoveryService = pg.getDiscoveryService();
        Enumeration<Advertisement> pas = null;
        try {
            pas = discoveryService.getLocalAdvertisements(DiscoveryService.ADV, PipeAdvertisement.NameTag, name);
        } catch (IOException e) {
            return null;
        }
        PipeAdvertisement curpa = null;
        PipeAdvertisement newestPA = null;
        
        while (pas.hasMoreElements()) {
        	try{
        		curpa = (PipeAdvertisement) pas.nextElement();
                
                if (curpa.getName().equals(name)) {
                	 if(newestPA == null)
                     	newestPA = curpa;
                     else if(Long.parseLong(newestPA.getDescription()) < Long.parseLong(curpa.getDescription()))
                    	 newestPA = curpa;
                }
        	}catch(Exception e){
        		System.out.println("Find a non-pipeAdv, just pass");
        		e.printStackTrace();
        	}
        }
        return newestPA;
    }
    
    /**
     * ��������pipeID��������������
     * @param pg		���������ܵ����Ľڵ���
     * @param pipeID Ҫ���ҵĹܵ����Ĺܵ�ID
     * @return			�ڽڵ����ڴ����Ĺ����󣨷��ط��ֵ����µĹܵ���棩
     */
    public static PipeAdvertisement searchLocalByPipeID(PeerGroup pg, String pipeID) {
        DiscoveryService discoveryService = pg.getDiscoveryService();
        Enumeration<Advertisement> pas = null;
        try {
            pas = discoveryService.getLocalAdvertisements(DiscoveryService.ADV, PipeAdvertisement.IdTag, pipeID);
        } catch (IOException e) {
            return null;
        }
        PipeAdvertisement curpa = null;
        PipeAdvertisement newestPA = null;
        
        while (pas.hasMoreElements()) {
        	try{
        		curpa = (PipeAdvertisement) pas.nextElement();
                
                if (curpa.getID().equals(pipeID)) {
                	 if(newestPA == null)
                     	newestPA = curpa;
                     else if(Long.parseLong(newestPA.getDescription()) < Long.parseLong(curpa.getDescription()))
                    	 newestPA = curpa;
                }
        	}catch(Exception e){
        		System.out.println("Find a non-pipeAdv, just pass");
        		e.printStackTrace();
        	}
        }
        return newestPA;
    }
    
	/**
	 * ���ط������
	 * @param pg ��
	 * @param pa �ܵ����
	 */
    public static void publish(PeerGroup pg, PipeAdvertisement pa) {
        publish(pg, pa, false);
    }

    /**
     * ���ط������, ���remoteΪ����Զ�̷���
     * @param pg ��
     * @param pa �ܵ����
     * @param remote �Ƿ�Զ�̷��� 
     */
    public static void publish(PeerGroup pg, PipeAdvertisement pa, boolean remote) {
        DiscoveryService ds = pg.getDiscoveryService();

        try {
            ds.publish(pa);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (remote) {
             ds.remotePublish(pa, DiscoveryService.ADV);
        }
    }

	public static void flushOldPipeAdvs(PeerGroup pg, String name) {
		DiscoveryService discoveryService = pg.getDiscoveryService();
        Enumeration<Advertisement> pas = null;
        try {
            pas = discoveryService.getLocalAdvertisements(DiscoveryService.ADV, PipeAdvertisement.NameTag, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PipeAdvertisement curpa = null;
        PipeAdvertisement newestPA = null;
        
        while (pas.hasMoreElements()) {
        	try{
        		curpa = (PipeAdvertisement) pas.nextElement();
        		if(newestPA == null)
                 	newestPA = curpa;
                 else if(Long.parseLong(newestPA.getDescription()) < Long.parseLong(curpa.getDescription()))
                 {
                	 discoveryService.flushAdvertisement(newestPA);
                	 newestPA = curpa;
                 }else
                	 discoveryService.flushAdvertisement(curpa);
        	}catch(Exception e){
        		System.out.println("Find a non-pipeAdv, just pass");
        		e.printStackTrace();
        	}
        }
	}
}

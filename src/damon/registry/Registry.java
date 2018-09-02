/*****************************************************************************************
 * Damon : a Distributed AOP Middleware on top of a p2p Overlay Network
 * Copyright (C) 2006-2008 Ruben Mondejar
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *****************************************************************************************/
package damon.registry;

import java.io.*;
import java.util.*;

import rice.p2p.commonapi.NodeHandle;

import damon.core.DamonCore;
import damon.util.Context;
import easypastry.dht.DHTException;
import easypastry.dht.DHTHandler;

/**
 * This class provides decentralized registry's static and generic methods
 * @author Ruben Mondejar <ruben.mondejar@urv.cat>
 * @version 2.0
 */
public class Registry {
  
  private static Naming naming;
  
	private static void init() throws RegistryException, DHTException {		
		
		try {
			DHTHandler dht2 = DamonCore.getDHTHandler(Context.REGISTRY, new RegistryRecordMerge());
			Registry.connect(dht2, DamonCore.getReflection().getNodeHandle());
			Registry.lookup(Context.URL_BASE);
		} catch (DHTException pe) {
			System.out.println("Base remote node root not bound... Binding '"+ Context.URL_BASE + "'");
			Registry.bind(Context.URL_BASE, "damon");
			//pe.printStackTrace();
		}	
	
	}
  
  private static void connect(DHTHandler dht, NodeHandle nh) throws RegistryException, DHTException {
	  naming = new NamingImpl(dht, nh);
	  bindRoot();
  }     
  
  /**
   * Special root ("p2p://") bind method
   * @throws RegistryException If the connection with the event service fails.
   */
  private static void bindRoot() throws RegistryException, DHTException {
	if (naming==null) throw new RegistryException("Registry is not connected");  
    System.out.println ("[DAMON REGISTRY] - Looking for base root...");
    try {
      naming.lookup("p2p://");
    } catch (DHTException re) {
      System.out.println ("[DAMON REGISTRY] - Base root not bound... Binding 'p2p://'");
      naming.bind ("p2p://", "root");
    }    
  }

  /**
   * Binds the specified name to a remote object.
   * @param name Id of the remote object
   * @param env Object properties
   * @throws RemoteException If the connection with the event service fails.
   */
  public static void bind (String url, Serializable value) throws RegistryException, DHTException {
	  if (naming==null) init();
	  naming.bind (url, value);
  }


  /**
   * Unbinds the specified url from a serializable object.
   * @param name url of the serializable object
   * @throws RegistryException If the connection with the event service fails.
   */
  public static void unbind (String url) throws RegistryException, DHTException {
	  if (naming==null) init();
	  naming.unbind (url);
  }


  /**
   * Rebinds the specified url in the registry.
   * @param url of the serializable object
   * @throws RegistryException If the connection with the event service fails.
   */
  public static void rebind (String url, Serializable value) throws RegistryException, DHTException {
	  if (naming==null) init();
	  naming.rebind (url,value);
  }
  

  /**
   * Returns a reference, a stub, for the remote object associated with the specified name.
   * @param name Id of the remote object in the Registry
   * @throws RemoteException If the connection with the event service fails.
   * @return Serializable object
   */
  public static Serializable lookup (String url) throws RegistryException, DHTException {
	if (naming==null) init();
    return naming.lookup (url);
  }


  /**
   * Returns a Collection of the url bound in the registry.
   * The Collection contains a snapshot of the url present in the registry at the time of the call. 
   * @return Collection<String> A list of urls of each child.
   * @throws RegistryException If the connection with the event service fails.
   */
  public static Collection<String> list (String root) throws RegistryException, DHTException {
	  if (naming==null) init();
	  return naming.list (root);
  }
  
  /**
   * Returns a Collection of nodehandle of writers of an object. 
   * @return Collection<NodeHandle> A list of nodehandles.
   * @throws RegistryException If the connection with the event service fails.
   */
  public static Collection<NodeHandle> getWriters(String url) throws RegistryException, DHTException {
	  if (naming==null) init();
	  return naming.getWriters (url);
  }


}

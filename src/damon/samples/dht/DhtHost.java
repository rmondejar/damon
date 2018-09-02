/*******************************************************************************
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
 ******************************************************************************/

package damon.samples.dht;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;

import damon.core.DamonCore;
import damon.util.UID;

/**
 * This class encapsulates the DHT Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class DhtHost {
	  
  private static Hashtable<Object,Object> data = new Hashtable<Object, Object>();
    
  //interactive test
  public void test() throws Exception { 
	
	  BufferedReader dis = new BufferedReader(new InputStreamReader (System.in));
	  String response = "";
	  while(!response.equals("exit")) {	  
	    System.out.println("Enter String to Store :");	 
	    response = dis.readLine();
	    String value = UID.getUID();
	    data.put(response,value);	    	
	    System.out.println("Enter String to Retrieve : ");	 
	    response = dis.readLine(); 
	    value = (String) data.get(response);
	    System.out.println("Value retrieved : "+value);
	  }
	  
  }
	
  public static void main (String[] args) {  
	
    try {
      
      //init
      DamonCore.init("damon-config.xml");                 
      DamonCore.registerGroup("p2p://dht.urv.cat");
            
      DamonCore.getStorage().deploy("damon.samples.dht.distribution.Locator");
      DamonCore.getControl().activateLocally("damon.samples.dht.distribution.Locator");
      
      DamonCore.getStorage().deploy("damon.samples.dht.distribution.Storage");
      DamonCore.getControl().activateLocally("damon.samples.dht.distribution.Storage");
      
      DamonCore.getStorage().deploy("damon.samples.dht.replication.nbs.Replicator");
      DamonCore.getControl().activateLocally("damon.samples.dht.replication.nbs.Replicator");
      
      DamonCore.getStorage().deploy("damon.samples.dht.replication.nbs.ReplicaStore");
      DamonCore.getControl().activateLocally("damon.samples.dht.replication.nbs.ReplicaStore");
      
      
            
      DhtHost host = new DhtHost();  
      host.test();
      System.exit(0);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}

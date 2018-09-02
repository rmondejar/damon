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
package damon.samples.dht.replication.nbs;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.metalevel.aspectwerkz.DistributedMetaAspect;
import damon.util.collections.SetHashtable;

/**
 * Distribute Meta-Aspect ReplicaStore
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://dht.urv.cat")
public class ReplicaStore extends DistributedMetaAspect {
	
    private static SortedMap<Id, Object> replicaData = new TreeMap<Id, Object>();
    private static SetHashtable<NodeHandle,Id> owners = new SetHashtable<NodeHandle,Id>();
	
	public static synchronized void putReplica(Id id, Object value) {
		replicaData.put(id, value);
	}

	public static synchronized Object getReplica(Id id) {
		return replicaData.get(id);
	}
	
	public static synchronized Object removeReplica(Id id) {
		return replicaData.remove(id);
	}
		
	@RemoteAdvice(id = "replicate")    
	public void replicaArrive(RemoteJoinPoint rjp, Id id, Object value) {
		System.out.println("ReplicaStore [replicate] : "+id);		
		putReplica(id,value);
		owners.put(rjp.getOriginator(), id);
		rjp.proceed(id);
	}
    

	/*********************************************************************/
	  
	 
	@RemotePointcut(id = "put", abstraction = Abstractions.HOPPED, synchro = true)
	@RemoteUpdate
	public void ownerCheck(NodeHandle handle, boolean joined) {
		
		System.out.println("ReplicaStore [ownerCheck] : "+owners.keySet());
		
		if (!joined && owners.containsKey(handle)) {
		
			  Set<Id> ids = owners.remove(handle);
			  System.out.println("Detected dead owner : "+handle);
			  for(Id id : ids) {
				  Object value = getReplica(id);
				  System.out.println("Re-insert id : "+id);
				  Object res = null; 
				  while(res==null) {
					  res = super.invoke("ownerCheck",null,id,new Object[]{id,value});
					  if (res==null) try {Thread.sleep(2000);} catch(Exception e) {}
					  else {					    
					    System.out.println("Removing replica, id : "+id);
					    removeReplica(id);
					  }  
				  }
			    
			  }  
		}
				
    }
		
	/**
	 * Forces Update (Crash Case)
	 */
	@RemotePointcut(id = "ownerPing", abstraction = Abstractions.DIRECT, synchro = true)
	@DamonPulse(seconds=11)	
	public void ping() {
		for (NodeHandle nh : owners.keySet()) {
			super.invoke("ping",null,nh,new Object[]{});
		}
	}
	
	@RemoteAdvice(id = "ownerPing") 
	public void pong(RemoteJoinPoint rjp) {		
	  rjp.proceed(true);
	}
	
	
}	
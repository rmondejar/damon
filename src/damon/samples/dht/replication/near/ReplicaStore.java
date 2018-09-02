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
package damon.samples.dht.replication.near;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.metalevel.aspectwerkz.DistributedMetaAspect;
import damon.reflection.ReflectionHandler;
import damon.reflection.thisEndPoint;
import damon.util.Utilities;
import damon.util.collections.SetHashtable;

/**
 * Distribute Meta-Aspect ReplicaStore
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://dht.urv.cat")
public class ReplicaStore extends DistributedMetaAspect {
	
    private static SortedMap<Id, Object> data = new TreeMap<Id, Object>();
    private static SetHashtable<NodeHandle,Id> owners = new SetHashtable<NodeHandle,Id>();
	
	public static synchronized void putValue(Id id, Object value) {
		data.put(id, value);
	}

	public static synchronized Object getValue(Id id) {
		return data.get(id);
	}
	
	public static synchronized Object removeValue(Id id) {
		return data.remove(id);
	}
	
	@RemoteCondition(id = "replicate")    
	public boolean isntReplicated(String key, Object value) {
		Id id =  Utilities.generateHash(key);
		System.out.println("ReplicaStore [isntReplicated] : "+id);
		return getValue(id)==null;		
	}
	
	@RemotePointcut(id = "hasReplica", abstraction = Abstractions.DIRECT)
	@RemoteAdvice(id = "replicate")    
	public void replicaArrive(RemoteJoinPoint rjp, String key, Object value) {
		Id id =  Utilities.generateHash(key);
		System.out.println("ReplicaStore [replicate] : "+id);		
		putValue(id,value);
		owners.put(rjp.getOriginator(), id);
		super.invoke("replicaArrive", null, rjp.getOriginator(), new Object[]{id});
	}
    

	/*********************************************************************/
	  
	 
	@RemotePointcut(id = "put", abstraction = Abstractions.HOPPED, synchro = true)
	@DamonPulse(seconds=11)
	public void ownerCheck() {	
		
		System.out.println("ReplicaStore [ownerCheck] : "+owners.keySet());
		ReflectionHandler reflection = thisEndPoint.getReflectionHandler();
		
		for(NodeHandle nh : owners.keySet()) {
			
			if (!reflection.isAlive(nh)) {
				
			  Set<Id> ids = owners.get(nh);
			  System.out.println("Detecting dead owner : "+nh);
			  //owner without ids? -> remove directly
			  if (ids==null || ids.isEmpty()) {
				  owners.remove(nh);
				  System.out.println("Remove directly dead owner : "+nh);
			  }
			  else {
			    for(Id id : ids) {
				  Object value = getValue(id);
				  System.out.println("Re-insert id : "+id);
				  Object res = super.invoke("ownerCheck",null,id,new Object[]{id,value});
				  if (res!=null) {					  
					  owners.remove(nh,id);
					  System.out.println("Remove now dead owner : "+nh);
					  removeValue(id);
				  }
			    }
			  }  
			}
		}		
    }
}	
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.NodeHandle;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.metalevel.aspectwerkz.DistributedMetaAspect;
import damon.reflection.ReflectionHandler;
import damon.reflection.thisEndPoint;
import damon.samples.dht.distribution.Storage;
import damon.util.collections.SetHashtable;

/**
 * Distributed Meta-Aspect Replicator
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://dht.urv.cat")
public class Replicator extends DistributedMetaAspect {
	
	private final int R_FACTOR = 4;
	private ReflectionHandler rh = thisEndPoint.getReflectionHandler();
	
	private static SetHashtable<NodeHandle,Id> replicators = new SetHashtable<NodeHandle,Id>();
	private static SetHashtable<Id,NodeHandle> replicas = new SetHashtable<Id,NodeHandle>();
	
	@RemotePointcut(id = "replicate", abstraction = Abstractions.MANY, num = R_FACTOR)
	@RemoteMetaPointcut(id = "put", type = Type.AFTER)    
	public void replicate(RemoteJoinPoint rjp, Id id, Object value) {
		System.out.println("Replicator [replicate] : "+id);		
		super.invoke("replicate", null, new Object[]{id,value});
	}
    
	@RemoteAdvice(id = "hasReplica")
	public void hasReplica(RemoteJoinPoint rjp, Id id) {
				
		System.out.println("Replicator [hasReplica] : "+id);	
		replicators.put(rjp.getOriginator(),id);		
		replicas.put(id, rjp.getOriginator());
	}

	/*********************************************************************/
	
	@RemotePointcut(id = "replicate", abstraction = Abstractions.DIRECT)
	@DamonPulse(seconds=11)	
	public void replicaCheck() {
	
		ReflectionHandler reflection = thisEndPoint.getReflectionHandler();
		System.out.println("Replicator [replicaCheck]");
		//check replicators --> if dead -> remove
		for(NodeHandle nh : replicators.keySet()) {
			
			if (!reflection.isAlive(nh)) {
			  Set<Id> ids = replicators.remove(nh);
			  for(Id id : ids) {
				  replicas.remove(id,nh);
				  
			  }
			}  
		}	
		//check replicas --> if # < R_FACTOR --> replicate
		for(Id id : replicas.keySet()) {
			Set<NodeHandle> replicators = replicas.get(id);
			int diff = R_FACTOR - replicators.size();
			if (diff>0) {
				Object value = Storage.getValue(id);
				
				//newers = getNecessaryNb(Set<NodeHandle> current, int max);
				Set<NodeHandle> newers = new HashSet<NodeHandle>();
				newers.addAll(rh.getNeighbours(false));
				newers.removeAll(replicators);
				Iterator<NodeHandle> it = newers.iterator();				
				int max = Math.min(diff, newers.size());
				
				for(int i = 0; i<max; i++) {
				  NodeHandle nh = it.next();
				  super.invoke("replicaCheck",null,nh, new Object[]{id,value});				  
				}  
			}
		}
    }

    
}	
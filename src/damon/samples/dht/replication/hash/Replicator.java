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
package damon.samples.dht.replication.hash;

import rice.p2p.commonapi.Id;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.metalevel.aspectwerkz.DistributedMetaAspect;
import damon.util.Utilities;

/**
 * Distributed Meta-Aspect Replicator
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://dht.urv.cat")
public class Replicator extends DistributedMetaAspect {
	
	private final int R_FACTOR = 3;
			
	@RemotePointcut(id = "put", abstraction = Abstractions.HOPPED)
	@RemoteMetaPointcut(id = "put", type = Type.BEFORE)    
	public void replicate(RemoteJoinPoint rjp, String key, Object value) {

		System.out.println("Replicator [replicate] : "+key);
		for(int i=0;i<R_FACTOR;i++) {
	      Id id =  Utilities.generateHash(key+i);
		  super.invoke("replicate", null, new Object[]{id,value});
		}  
	}
	
	
	@RemotePointcut(id = "get", abstraction = Abstractions.HOPPED, synchro = true)
	@RemoteMetaPointcut(id = "get", type = Type.AFTER, ack = true)    
	public void getReplicas(RemoteJoinPoint rjp, Object value) {
		if (value == null) {
		  String key = (String) rjp.getArgs()[0];
		  System.out.println("Replicator [getReplicas] : "+key);		  
		  int i = 0;
		  while (value==null && i<R_FACTOR) {		    
			Id id =  Utilities.generateHash(key+i);
		    value = super.invoke("put", null, new Object[]{id,value});
            i++;		  
		  }
	      if (value!=null) rjp.setResult(value);
		}  
	}   
    
}	 
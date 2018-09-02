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
package damon.samples.dht.caching;

import rice.p2p.commonapi.Id;
import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.metalevel.aspectwerkz.DistributedMetaAspect;
import damon.util.Utilities;
import damon.util.collections.Cache;

/**
 * Distribute Meta-Aspect Caching
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://dht.urv.cat")
public class Caching extends DistributedMetaAspect {
	
private Cache<Id,Object> cache = new Cache<Id,Object>(100);
	
    @RemoteMetaPointcut(type = Type.AROUND, id = "put")
	public void putTravelling(RemoteJoinPoint rjp, String key, Object value) {
    	System.out.println("Caching [put]");
    	Id id =  Utilities.generateHash(key);
    	cache.putValue(id, value);
	}
    
    @RemoteMetaPointcut(type = Type.AROUND, id = "get")
    @RemoteMetaAdvice(id = "get", abstraction = Abstractions.DIRECT)
	public void getTravelling(RemoteJoinPoint rjp, String key) {
    	System.out.println("Caching [get]");
    	Id id =  Utilities.generateHash(key);
    	Object value = cache.getValue(id);    	
    	if (value!=null) {    		
    		rjp.cancel();
    		super.supplantation("getTravelling", rjp, value);
    	}
	}    
}	
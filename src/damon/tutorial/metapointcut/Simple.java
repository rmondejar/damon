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
package damon.tutorial.metapointcut;

import java.util.Hashtable;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import rice.p2p.commonapi.NodeHandle;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.invokation.aspectwerkz.AspectRemoting;

/**
 * Distributed Aspect using multi abstraction and remote services
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.MULTI, target = "p2p://clockstation.urv.cat")
public class Simple extends AspectRemoting {
	
	private Hashtable<NodeHandle,Long> times = new Hashtable<NodeHandle,Long>();
	
	@RemoteAdvice(id = "hosttime")
	public void storeTime(RemoteJoinPoint rjp, Long value) {
		Long l = (Long) value;
        times.put(rjp.getOriginator(),l);	
	}
		
	@RemotePointcut(id = "hosttime", abstraction = Abstractions.MULTI)
    @SourceHook(source = "damon.tutorial.metapointcut.SourceHooks", method = "getTime", type = Type.AROUND)     
	public Object propagateTime(JoinPoint joinPoint) throws Throwable {
    	
		long time = ((Long) joinPoint.proceed()).longValue();		
    	
    	//communicator primitive
    	invoke("propagateTime", joinPoint, new Object[]{time});
    	
    	return time;
    	
	}
    
}	
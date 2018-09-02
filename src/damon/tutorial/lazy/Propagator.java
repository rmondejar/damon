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
package damon.tutorial.lazy;

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
@DistributedAspect (abstraction = Abstractions.MULTI, target = "p2p://test.urv.cat")
public class Propagator extends AspectRemoting {
	
	private Hashtable<NodeHandle,Integer> states = new Hashtable<NodeHandle,Integer>();
	
	@RemoteAdvice(id = "propagate")
	public void setState(RemoteJoinPoint rjp, int state) {	
		System.out.println("Propagator : remote advice "+state);
        states.put(rjp.getOriginator(),state);        
	}
		
	@RemotePointcut(id = "propagate", abstraction = Abstractions.MULTI, lazy = true)		
	@SourceHook(source = "damon.tutorial.lazy.SourceHooks", method = "getState", type = Type.AROUND)
	public Object getState(JoinPoint joinPoint) throws Throwable {
    	
		Integer state = (Integer) joinPoint.proceed();    	
		System.out.println("Propagator : remote pointcut "+state);
    	invoke("getState", joinPoint, new Object[]{state});
    	
    	return state;
	}
    
}	
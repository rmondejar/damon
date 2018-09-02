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
package damon.tutorial.local;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.invokation.aspectwerkz.AspectRemoting;

/**
 * Distributed Aspect using multi abstraction and remote services
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://test.urv.cat")
public class Synchro extends AspectRemoting {
	
	private Set<String> hosts = new HashSet<String>();
	
	@RemotePointcut(id = "hostname", abstraction = Abstractions.MULTI)		
	@SourceHook(source = "damon.tutorial.local.SourceHooks", method = "getHostName", type = Type.AROUND)
	public Object getHostName(JoinPoint joinPoint) throws Throwable {
    	
		String hostname = (String) joinPoint.proceed();    	
    	super.invoke("getHostName", joinPoint, new Object[]{hostname});
    	System.out.println("> Hosts ("+hosts.size()+") : "+hosts);
    	return hostname;
	}
	
	@RemoteAdvice(id = "hostname")
	public void storeTime(RemoteJoinPoint rjp, String hostname) {
		System.out.println("> Received : "+hostname+" from "+rjp.getOriginator());
        hosts.add(hostname);        
	}
		
	
    
}	
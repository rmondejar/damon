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

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.reflection.thisEndPoint;

/**
 * Distributed Aspect to find another host with a good state
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.MULTI, target = "p2p://test.urv.cat")
public class Resolver extends AspectRemoting {
	
	private int localState;
	
	@SourceHook(source = "damon.tutorial.lazy.SourceHooks", method = "getState", type = Type.AROUND)
	public Object getLocalState(JoinPoint joinPoint) throws Throwable {
    	
		localState = (Integer) joinPoint.proceed();   	
    	return localState;
	}
	
    @RemotePointcut(id = "resolve", abstraction = Abstractions.ANY, synchro = true, lazy = true)
	@SourceHook(source = "damon.tutorial.lazy.SourceHooks", method = "getHost", type = Type.AROUND)
	public Object getHost(JoinPoint joinPoint) throws Throwable {
		
		int threshold = (Integer) thisEndPoint.getParams(joinPoint)[0];		
	    Object result = joinPoint.proceed();	    
		String hostname = (String) super.invoke("getHost", joinPoint,	new Object[] { threshold });
		System.out.println("Resolver : remote pointcut "+threshold+" --> "+hostname);
		if (hostname==null) return result;
		else return hostname;
		
	}
	
	@RemoteCondition(id = "resolve")
	public boolean checkThreshold(int threshold) {		
		System.out.println("Is "+localState+" >= "+threshold+" ? ");
		return localState>=threshold;
	}

	@RemoteAdvice(id = "resolve")
	public void resolveP2PURL(RemoteJoinPoint rjp, int threshold)  {
		System.out.println("Resolver : remote advice "+threshold);
		rjp.proceed(thisEndPoint.getLocalNodeHandle().toString());
	}

}	
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
package damon.tutorial.many;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.invokation.aspectwerkz.AspectRemoting;

/**
 * Distributed Aspect using multi abstraction and synchronized remote services
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.MULTI, target = "p2p://clockstation.urv.cat")
public class Simple extends AspectRemoting {
	
	private long currentTime, lastTime;	
	
	@RemoteAdvice(id = "hosttime")
	public void storeTime(RemoteJoinPoint rjp, Long value) {
		Long otherTime = (Long) value;
		System.out.println("Time Arrived : "+otherTime);
		currentTime = (currentTime + otherTime)/2;		
		rjp.proceed(currentTime);  		
	}
		
	@RemotePointcut(id = "hosttime", abstraction = Abstractions.MANY, num  = 4)
    @SourceHook(source = "damon.tutorial.many.SourceHooks", method = "getTime", type = Type.AROUND)     
	public Object getTime(JoinPoint joinPoint) throws Throwable {
    	
    	lastTime = ((Long) joinPoint.proceed()).longValue();        	    	
    	Object result = invoke("getTime", joinPoint, new Object[]{lastTime});
    	if (result!=null) {
    	  long otherTime = (Long) result;
    	  System.out.println("Time Returned : "+otherTime);
    	  currentTime = (currentTime + otherTime)/2;
    	} 
    	else System.out.println("Time not returned");
    	return currentTime;
	}
    
}	
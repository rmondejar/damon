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
package damon.samples.monitor;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.annotation.*;
import damon.invokation.aspectwerkz.AspectRemoting;

/**
 * Aspect with multi pointcut abstraction for ControlHost
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.MULTI, target = "p2p://controlstation.urv.net")
public class Monitor extends AspectRemoting {
	
	@RemotePointcut(id = "monitor", abstraction = Abstractions.MULTI)
	@SourceHook(source = "damon.samples.monitor.SourceHooks", method = "getAverage", type = Type.AROUND)	
	public Object report(JoinPoint joinPoint) throws Throwable {
				
    	Float average = (Float) joinPoint.proceed();    	
    	super.invoke("resport", joinPoint, new Object[]{average});
    	return average;
    }
       
    
}	
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
package damon.tutorial.metaadvice;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.metalevel.aspectwerkz.DistributedMetaAspect;

/**
 * Distribute meta-aspect with a meta-pointcut abstraction for SimpleHost invokations
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://clockstation.urv.cat")
public class TimeDiff extends DistributedMetaAspect {
	
    @RemoteMetaPointcut(id = "hosttime", type = Type.AFTER)
    @RemoteMetaAdvice(id = "hosttime", abstraction = Abstractions.LOCAL)
	public void timeArrive(RemoteJoinPoint rjp, Long time) {    	
    	long diff = System.currentTimeMillis() - time;
    	System.out.println("TimeDiff [Swapping "+time+" by "+diff+"]");
    	super.supplantation("timeArrive", rjp, diff);
	}
    
    @RemoteMetaPointcut(id = "hosttime", type = Type.AFTER, ack = true)   
	public void lastTimeArrive(RemoteJoinPoint rjp, Long time, Object result) {
    	Long lastTime = (Long) result;
    	long diff = lastTime - time;
    	System.out.println("LastTimeDiff ["+time+"]");    	
	}  
    
}	
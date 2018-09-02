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
package damon.tutorial.redirect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.annotation.*;
import damon.invokation.RemoteJoinPoint;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.reflection.thisEndPoint;

/**
 * Distributed Aspect to find another host with a good state
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
@DistributedAspect (abstraction = Abstractions.LOCAL, target = "p2p://test.urv.cat")
public class Resolver extends AspectRemoting {

	private PrintStream ps;
	
	public Resolver() {
	  try {	
		String filename = thisEndPoint.getReflectionHandler().getNodeHandle().getId().toStringFull();  
		File file = new File(filename+"-log.txt");
		FileOutputStream fos = new FileOutputStream(file);
		ps = new PrintStream(fos);
	  } catch(Exception e) {
		  e.printStackTrace();
	  }
		
	}
    @RemotePointcut(id = "resolve", abstraction = Abstractions.ANY, synchro = false)
	@SourceHook(source = "damon.tutorial.redirect.SourceHooks", method = "getHost", type = Type.AROUND)
	public Object getHost(JoinPoint joinPoint) throws Throwable {
						
	    Object result = joinPoint.proceed();	    
		super.invoke("getHost", joinPoint,	new Object[] { System.currentTimeMillis() });
		return result;
		
	}
	
	@RemoteCondition(id = "resolve")
	public boolean checkTime(long time) {		
		return true;
	}

	@RemoteAdvice(id = "resolve")
	public void resolveP2PURL(RemoteJoinPoint rjp, long time)  {
		//System.out.println("> "+time);
		ps.println(System.currentTimeMillis());		
	}

}	
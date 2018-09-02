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

import org.codehaus.aspectwerkz.annotation.*;
import org.codehaus.aspectwerkz.joinpoint.JoinPoint;

import damon.reflection.thisEndPoint;


/**
 * Source Hook definition for ControlHost
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class SourceHooks {
	
	@Before("execution(* damon.tutorial.lazy.ActiveHost.getState(..))")          
	public void getState(JoinPoint joinPoint) {}
	
	@Before("execution(* damon.tutorial.lazy.ActiveHost.getHost(..)) AND args(threshold)")          
	public void getHost(JoinPoint joinPoint, int threshold) {
		thisEndPoint.addParams(joinPoint,threshold);
	}

}	
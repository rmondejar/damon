/*****************************************************************************************
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
 *****************************************************************************************/
package damon.util;

/**
 * Damon Context constants
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
	
public interface Context {
	
	//props		
	public static final String ASPECT_ENGINE = "aop_engine";
	
	//Registry url container base
	public static final String URL_BASE = "p2p://damon.distributed.aspects";
	public static final String URL_GLOBAL = URL_BASE + "/global";
	
	//Persistence contexts
	public static final String REGISTRY = "REGISTRY";
	public static final String STORAGE = "STORAGE";	
		
	//Remote Pointcut Labels
	public static final String NAME = "name";
	
	public static final String ABSTRACTION = "abstraction";
	
	public static final String NUM_NODES = "num";
	
	public static final String TARGET = "target";
	
	public static final String ID = "id";
	
	public static final String TYPE = "type";
	
	public static final String STATE = "state";
	
	public static final String SINGLETON = "singleton";
	
	public static final String PROXY = "proxy";
	
	public static final String SYNCHRO = "synchro";
	
	public static final String LAZY = "lazy";
	
	public static final String ACK = "ack";
	
	public static final int TIMEOUT = 50;
	
	public static final int DELAY = 100;
	
	public static final int CACHING_TTL = 10000;
	
	
	
	
	

}

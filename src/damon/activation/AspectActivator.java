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
package damon.activation;

import rice.p2p.commonapi.NodeHandle;
import damon.annotation.Abstractions;
import damon.reflection.MetaData;
import damon.registry.RegistryException;
import easypastry.dht.DHTException;

/**
 * Damon AspectManager remote inteface
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */

public interface AspectActivator {

	  public void activate (NodeHandle nh, AspectActivation activation);
	
	  public void activate (Abstractions abstraction, AspectActivation activation) throws RegistryException, DHTException;
  	  
	  public void passivate (NodeHandle nh, AspectActivation activation);
	  
	  public void passivate (Abstractions abstraction, AspectActivation activation) throws RegistryException, DHTException;
	  	  
	  public void activateLocally(String name, boolean isXML, MetaData md) throws Exception;

	  public void passivateLocally(String url, String aspectName);
	  
  	  public void activationArrive(AspectActivation activation);	




}

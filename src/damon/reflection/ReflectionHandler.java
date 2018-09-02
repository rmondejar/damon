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
package damon.reflection;

import java.net.UnknownHostException;
import java.util.Collection;

import damon.registry.RegistryException;
import easypastry.dht.DHTException;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.IdRange;
import rice.p2p.commonapi.NodeHandle;

public interface ReflectionHandler {
 	  
	  public String getHostName() throws UnknownHostException;
	  
	  public NodeHandle getNodeHandle();
	  
	  public boolean isAlive(NodeHandle nh);
	  	  
	  public Collection<NodeHandle> getNeighbours(boolean ordered);
	  
	  public Collection<NodeHandle> getReplicaSet(Id id, int num);
	  
	  public IdRange getRange(NodeHandle handle, int rank, Id lkey);
	  
	  public Collection<NodeHandle> getGroupMembers(String url) throws RegistryException, DHTException;	  
	      
	  public boolean isGroupRoot(String url);
	  
	  public NodeHandle getGroupParent(String url);
	  
	  public Collection<NodeHandle> getGroupChildren(String url);
	    
	  public long getAverageLatency (NodeHandle neighbour);

	  public double getSystemLoadAverage();
	  
	  public int getAvailableProcessors();	  

	  public long getFreeMem();

	  public long getTotalMem();

	  public long getFreeSpace();

	  public long getTotalSpace();
	  
	  public long getProcessCpuTime();
	  
	  public int getNumOfThreads();
}

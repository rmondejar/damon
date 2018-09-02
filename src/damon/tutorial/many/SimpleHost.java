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

import java.util.Date;

import damon.core.DamonCore;

/**
 * This class encapsulates the Simple Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class SimpleHost {
	
  private long initialTime;
  
  public SimpleHost() {
	  long max = 1000000000;
	  initialTime =  (long) (Math.random()*max);
  }
  
  public Long getTime() {	
    return new Long (initialTime + System.currentTimeMillis());
  }
  
  public void showTime(long time) {
	try {		
		System.out.println("Time: "+new Date(time));
		Thread.sleep(5000);
	} catch (InterruptedException e) {}
  }
  
  public static void main (String[] args) {
	  
	SimpleHost host = new SimpleHost();  
    try {
      
      DamonCore.init("damon-config.xml");
      DamonCore.registerGroup("p2p://clockstation.urv.cat");
      
      long time = host.getTime().longValue();
      
      while(time>=0) {
    	  time = host.getTime().longValue();
    	  host.showTime(time);   	  
      }
      
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

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
package damon.test.g5k.fault;

import damon.core.DamonCore;
import damon.test.AbstractTest;

/**
 * This class encapsulates the Sensor Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class SensorHost extends AbstractTest {
	
  private String hostname;
  
  public void init() {
	 this.hostname = DamonCore.getHostName();  
  }
  
  public String getHostName() throws InterruptedException {
	Thread.sleep(15000);
    return hostname;
    
  }
  
  public void test(Object... params) throws Exception {
	  
	 System.out.println("Starting Test ... ");  	   
  	 for(;;) {
  		getHostName();
  	 }
  }
  
  public static void main (String[] args) {  
	
    try {
            
      if (args.length>0) DamonCore.init(args[0], "damon-config.xml");
      else DamonCore.init("damon-config.xml");
      
      DamonCore.registerGroup("p2p://test.urv.cat");
      
      SensorHost host = new SensorHost();      
      host.test();
      
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
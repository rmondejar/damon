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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import damon.core.DamonCore;
import damon.test.AbstractTest;

/**
 * This class encapsulates the Active Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class ActiveHost extends AbstractTest {
	  
  private static int state;
  private static String hostname;
  
  public ActiveHost()  throws UnknownHostException {
	  hostname = InetAddress.getLocalHost().getHostName();
	  state = (int) (Math.random() * 100);
  }
  
  public int getState() {	
    return state;
  }
  
  public String getHost(int threshold) {
	 if (state>threshold) return hostname;
	 else return "no result";
  }
  
  @Override
  public void test(Object... params) throws Exception {
	  
	  BufferedReader dis = new BufferedReader(new InputStreamReader (System.in));
	   	  
  	  System.out.println("Press <Enter> key to start the 10000 getHost(threshold)");
  	  dis.readLine();
  	  for(int i=0; i<10000; i++) {
		  String hostname = getHost(0);
		  //System.out.println("Host : "+hostname);
	  }
  	  
	  System.out.println("Press <Enter> key to start the 10000 getState()");
	  dis.readLine();
  	  for(int i=0; i<10000; i++) {
  		  int state = getState();
  		  //System.out.println("State : "+state);
  	  }
  	
  }
    
  public static void main (String[] args) {  
	
	  try {
	         
	      DamonCore.init("damon-config.xml");      
	      DamonCore.registerGroup("p2p://test.urv.cat");
	      
	      ActiveHost host = new ActiveHost();  
	      host.deploy("damon.tutorial.lazy.Propagator");
	      host.deploy("damon.tutorial.lazy.Resolver");
	      host.test();
	      System.exit(0);
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }

}

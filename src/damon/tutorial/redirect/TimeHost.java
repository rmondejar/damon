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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import damon.core.DamonCore;
import damon.test.AbstractTest;

/**
 * This class encapsulates the Time Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class TimeHost extends AbstractTest {
	  
  private static String hostname;
  
  public TimeHost()  throws UnknownHostException {
	  hostname = InetAddress.getLocalHost().getHostName();
  }
  
  public String getHost() {
	 return hostname;	
  }
  
  @Override
  public void test(Object... params) throws Exception {
	  
	  BufferedReader dis = new BufferedReader(new InputStreamReader (System.in));
	  boolean resolve = (Boolean) params[0];
	  if (resolve) {
	  
	    boolean exit = false;  
	    while (!exit) { 	  
  	      System.out.println("Enter the number of request or exit : ");
  	      String line = dis.readLine();
  	      if (line.charAt(0)=='e') break;
  	      else {
  	        int max = Integer.parseInt(line);
  	        for(int i=0; i<max; i++) {
		      String hostname = getHost();
		      try {
		        Thread.sleep(10);	  
		      } catch(Exception ex) {}
		      if (i==1000) System.out.println("--------->>>");
		      else if (i==3000) System.out.println("<<<---------");
	        }
  	      }
	    }
	  }
	  else {
		  System.out.println("Press <any> key to start the Redirector ");
	  	  dis.readLine();
	  	  deploy("damon.tutorial.redirect.Redirector");
	  	  System.out.println("Press <any> key to end the Redirector ");
	  	  dis.readLine();
	  	  passivate("damon.tutorial.redirect.Redirector");
	  	  System.out.println("Press <any> key to finish");
	  	  dis.readLine(); 
	  }
  	    	
  }
    
  public static void main (String[] args) {  
	
	  try {
		  
		  boolean resolve = true;
		  int num = 1;
		  		  
		  if (args.length>=1 && (args[0].charAt(0)=='n')) resolve = false;
		  if (args.length>=2 ) {
			  num = Integer.parseInt(args[1]);
		  }
		  
		  System.out.println("Host resolve? "+resolve);
	         
	      DamonCore.init("damon-config.xml");      
	      DamonCore.registerGroup("p2p://test.urv.cat");
	      
	      TimeHost host = new TimeHost();      
	      if (resolve) host.deploy("damon.tutorial.redirect.Resolver");
	      else host.deploy("damon.tutorial.redirect.NonResolver");
	    	  
	      host.test(resolve);
 	      System.exit(0);
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }

}

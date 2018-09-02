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

package damon.tutorial.local;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import damon.activation.AspectControl;
import damon.core.AspectStorage;
import damon.core.DamonCore;

/**
 * This class encapsulates the Synchro Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class SynchroHost {
	
  private String hostname;
  
  public SynchroHost() throws UnknownHostException {
	 this.hostname = InetAddress.getLocalHost().getHostName();  
  }
  
  public String getHostName() {	
    return hostname;
  }
  
  public void showHostName() {
	try {		
		System.out.println("Host : "+hostname+" : "+new Date(System.currentTimeMillis()));
		Thread.sleep(10000);
	} catch (InterruptedException e) {}
  }
  
  public void deploy() {
	  try {	      
	    	    
		AspectStorage storage = DamonCore.getStorage();
	    AspectControl control = DamonCore.getControl();   
	    
	    long t0 = System.currentTimeMillis();	    
	    
	    System.out.println ("Deploying damon.tutorial.local.Synchro...");
	    
	    storage.deploy("damon.tutorial.local.Synchro");	    
	    
	    long t1 = System.currentTimeMillis();
	    
	    System.out.println ("Activating damon.tutorial.local.Synchro...");

	    control.activate("damon.tutorial.local.Synchro");
	    
	    long t2 = System.currentTimeMillis();
	 
	    System.out.println ("Deploy time     : "+(t1-t0)+" ms.");
	    System.out.println ("Activation time : "+(t2-t1)+" ms.");
	    System.out.println ("Total time      : "+(t2-t0)+" ms.");
	    	    
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
  
  public static void main (String[] args) {  
	
    try {
            
      if (args.length>0) DamonCore.init(args[0], "damon-config.xml");
      else DamonCore.init("damon-config.xml");
      
      DamonCore.registerGroup("p2p://test.urv.cat");
      
      SynchroHost host = new SynchroHost();  
      host.deploy();
           
      while(host.getHostName()!=null) {    	  
    	  host.showHostName();   	  
      }
      
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

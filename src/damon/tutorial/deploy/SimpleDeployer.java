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

package damon.tutorial.deploy;

import damon.activation.AspectControl;
import damon.core.AspectStorage;
import damon.core.DamonCore;



public class SimpleDeployer {
	
  public static void main (String[] args) {
	  try {
  
		DamonCore.init("damon-config.xml");	
	    	    
		AspectStorage storage = DamonCore.getStorage();
	    AspectControl control = DamonCore.getControl();
	    
	    long t0 = System.currentTimeMillis();
	    
	    //assign name and deploy
	    storage.deploy("simple", "damon.tutorial.deploy.SimpleImpl");
	    
	    long t1 = System.currentTimeMillis();
	    
	    Thread.sleep(3000);
	    
	    long t1_2 = System.currentTimeMillis();

	    control.activate("simple");
	    
	    long t3 = System.currentTimeMillis();
	 
	    System.out.println ("Deploy time     : "+(t1-t0)+" ms.");
	    System.out.println ("Activation time : "+(t3-t1_2)+" ms.");
	    System.out.println ("Total time      : "+((t1-t0)+(t3-t1_2))+" ms.");
	    
	    System.out.println("Press enter to passivate remote aspect");
	    System.in.read();
	    control.passivate("simple");
	    
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
}

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

package damon.tutorial.xml;
import java.io.File;

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
	    	    
	    char s = File.separatorChar;
	    storage.deployXML("simpleXML", ".."+s+"src"+s+"damon"+s+"tutorial"+s+"xml"+s+"SimpleImpl.xml");
	    
	    long t1 = System.currentTimeMillis();

	    control.activateXML("simpleXML");
	    
	    long t2 = System.currentTimeMillis();
	 
	    System.out.println ("Deploy time     : "+(t1-t0)+" ms.");
	    System.out.println ("Activation time : "+(t2-t1)+" ms.");
	    System.out.println ("Total time      : "+(t2-t0)+" ms.");
	    
	    System.out.println("Press enter to passivate remote aspect");
	    System.in.read();
	    control.passivateXML("simpleXML");
	    
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
  }
}

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

package damon.samples.monitor;

import damon.core.DamonCore;

/**
 * This class encapsulates the Monitor Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class ControlHost {
	
  private float average;
  
  public ControlHost() {
	  average = 0.0f;
  }
  
  public Float getAverage() {	
    return new Float (average);
  }
  
  public void incAverage() {
	average += 1.0f;
  }
  
  public void decAverage() {
	average -= 10.0f;
  }
  
  public void showAverage(float average) {
	try {		
		System.out.println("Average: "+average);
		Thread.sleep(1000);
	} catch (InterruptedException e) {}
  }
  
  public static void main (String[] args) {
	  
	ControlHost host = new ControlHost();  
    try {
      
      DamonCore.init("damon-config.xml");
      DamonCore.registerGroup("p2p://controlstation.urv.net");
      
      while(true) {
    	  float average = host.getAverage().floatValue();
    	  host.incAverage();
    	  host.showAverage(average);   	  
      }
      
      //System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

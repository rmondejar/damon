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

package damon.test.g5k.hops;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;

import damon.core.DamonCore;
import damon.test.AbstractTest;

/**
 * This class encapsulates the Monitor Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class MonitorHost extends AbstractTest {
	  
  private static Vector<Object[]> hopped = new Vector<Object[]>();
  private static Vector<Object[]> any = new Vector<Object[]>();
  private static Vector<Object[]> many = new Vector<Object[]>();
  
  private String hostname;
  
  public void init() {
	 this.hostname = DamonCore.getHostName();  
  }
  
  public String getHostName() throws InterruptedException {
	Thread.sleep(5000);
    return hostname;
    
  }
	
  public static void updateData(String abstraction, long millis, int hops) {
	  
	  System.out.println("updateData : "+abstraction+" : "+millis+" ms, "+hops+" hops.");
	  switch (abstraction.charAt(0)) {
	    case 'a' : any.add(new Object[]{millis,hops}); break;
	    case 'h' : hopped.add(new Object[]{millis,hops}); break;
	    case 'm' : many.add(new Object[]{millis,hops}); break;
	  }	  
  }
  
  
  
  //interactive test
  public void test(Object... params) throws Exception {
  	  
	  init();
	  BufferedReader dis = new BufferedReader(new InputStreamReader (System.in));
	  System.out.print("Number : ");	 
	  String response = dis.readLine();
	  System.out.println();
	  int  counter = Integer.parseInt(response);
	  
	  while(counter>0) {	    
	    System.out.println(getHostName()+" status : "+hopped.size());
	    counter--;
	  }
	  
	  generateResultFile();
	  
  }
  

  public void generateResultFile() throws FileNotFoundException {
	  //open file
	  File file = new File("hops.txt");
	  FileOutputStream fos = new FileOutputStream(file);
	  PrintStream ps = new PrintStream(fos);
	  
	  for(int i=0;i<hopped.size();i++) {
		  Object[] hdata = hopped.get(i);
		  Object[] adata = any.get(i);
		  Object[] mdata = many.get(i);
		  String line = hdata[0]+"\t"+adata[0]+"\t"+mdata[0]+"\t"+hdata[1]+"\t"+adata[1]+"\t"+mdata[1];		  
		  ps.println(line);		  
	  }
	  //close file
	  ps.close();
  } 

  
	
  public static void main (String[] args) {  
	
    try {
         
      DamonCore.init("damon-config.xml");      
      DamonCore.registerGroup("p2p://test.urv.cat");
      
      MonitorHost host = new MonitorHost();  
      host.deploy("damon.test.g5k.hops.Monitor");
      host.deploy("damon.test.g5k.hops.Sensor");    
      host.test();
      System.exit(0);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}

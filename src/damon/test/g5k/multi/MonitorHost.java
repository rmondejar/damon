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

package damon.test.g5k.multi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Hashtable;

import damon.core.DamonCore;
import damon.test.AbstractTest;
import damon.util.Calc;

/**
 * This class encapsulates the Monitor Host
 * @author Rubén Mondéjar <ruben.mondejar@urv.cat>
 */
public class MonitorHost extends AbstractTest {
	  
  private static Hashtable<String, int[]> hosts = new Hashtable<String, int[]>();
  private static Hashtable<String, int[]> latencies = new Hashtable<String, int[]>();
  private String hostname;
  
  public void init() {
	 this.hostname = DamonCore.getHostName();  
  }
  
  public String getHostName() throws InterruptedException {
	Thread.sleep(5000);
    return hostname;
    
  }
	
  public static void updateData(String name, int group, int closest, int neighbour, 
		                                     int latgroup, int latclosest, int latneighbour) {
	  
	  if (name!=null) {
		  hosts.put(name, new int[]{group,closest,neighbour});
		  latencies.put(name, new int[]{latgroup,latclosest,latneighbour});
	  }
	  
  }
  
  public void generateResultFile() throws FileNotFoundException {
	  //open file
	  File file = new File("multi.txt");
	  FileOutputStream fos = new FileOutputStream(file);
	  PrintStream ps = new PrintStream(fos);
	  
	  for(String name : hosts.keySet()) {
		  int[] data = hosts.get(name);
		  int[] data2 = latencies.get(name);
		  String line = name+"\t";
		  for(int i: data) line += i+"\t";
		  for(int f: data2) line += f+"\t";
		  ps.println(line);		  
	  }
	  //close file
	  ps.close();
  } 

  
  public void printResume() {
	  
	  //elems
	  System.out.println("number of elements");
	  Collection<int[]> raw = hosts.values();
	  int[][] data = new int[raw.size()][3];	  
	  int i=0;
	  for (int[] values : raw) {
		  data[i] = values;
		  i++;
	  }
	  //max
	  int[] res = Calc.max(data, i, 3);
	  System.out.println("max\t+"+res[0]+"\t"+res[1]+"\t"+res[2]);   
	  //min
	  res = Calc.min(data, i, 3);
	  System.out.println("min\t+"+res[0]+"\t"+res[1]+"\t"+res[2]);	  
	  //avg
	  res = Calc.avg(data, i, 3);
	  System.out.println("avg\t+"+res[0]+"\t"+res[1]+"\t"+res[2]);
	  
	  //resources
	  System.out.println("latencies");
	  Collection<int[]> raw2 = latencies.values();
	  int[][] data2 = new int[raw2.size()][3];	  
	  i=0;
	  for (int[] values : raw2) {
		  data2[i] = values;
		  i++;
	  }
	  //max
	  int[] res2 = Calc.max(data2, i, 3);
	  System.out.println("max\t+"+res2[0]+"\t"+res2[1]+"\t"+res2[2]);   
	  //min
	  res2 = Calc.min(data2, i, 3);
	  System.out.println("min\t+"+res2[0]+"\t"+res2[1]+"\t"+res2[2]);	  
	  //avg
	  res2 = Calc.avg(data2, i, 3);
	  System.out.println("avg\t+"+res2[0]+"\t"+res2[1]+"\t"+res2[2]);	  
	  
  }
  
  //interactive test
  public void test(Object... params) throws Exception {
  	  
	  init();
	  BufferedReader dis = new BufferedReader(new InputStreamReader (System.in));
	  String response = "";
	  while(response.length()<=0 || response.charAt(0)!='y') {	  	  	  
	    System.out.println(getHostName()+" status : "+hosts.size());
	    System.out.println("Print & Exit ? (y/n)");	 
	    response = dis.readLine(); 
	  }
	  generateResultFile();
	  printResume();
  }
	
  public static void main (String[] args) {  
	
    try {
         
      DamonCore.init("damon-config.xml");      
      DamonCore.registerGroup("p2p://test.urv.cat");
      
      MonitorHost host = new MonitorHost();  
      host.deploy("damon.test.g5k.multi.Monitor");
      host.deploy("damon.test.g5k.multi.Sensor");
      host.test();
      System.exit(0);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}

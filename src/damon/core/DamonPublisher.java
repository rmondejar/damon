package damon.core;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;

import damon.util.Communication;

public class DamonPublisher extends Thread {
	
	private Communication comm;
	private boolean close = false;
	
	public DamonPublisher(int port) throws UnknownHostException, IOException {
		//System.out.println("DamonPublisher : init() : "+Thread.currentThread().getContextClassLoader());
		comm = new Communication();
		comm.initPublisher(port);
	}
	
	public void publish(Serializable ser) throws IOException {
		//System.out.println("DamonPublisher : publish("+ser+") : "+Thread.currentThread().getContextClassLoader());
		comm.publish(ser);
	}
	
	public void run() {
		
		while(!close) {
	  	  try {
	  		  //System.out.println("DamonPublisher : waitSubscribers() : "+Thread.currentThread().getContextClassLoader());
	  		  comm.waitSubscribers();	  		  
	  	  } catch (Exception e) {
	  		  
	  	  }
		}  
	}
	
	public void close() {
		close = true;
	}
}
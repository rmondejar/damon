package damon.core;

import java.io.IOException;
import java.net.UnknownHostException;

import damon.invokation.AspectInvokation;
import damon.invokation.InvokationException;
import damon.invokation.aspectwerkz.AspectRemoting;
import damon.util.Communication;
import damon.util.collections.QueueHashtable;

public class DamonProxy extends Thread {//implements damon.invokation.AspectInvoker {
	
	private Communication comm;
	private boolean close = false;
	private QueueHashtable<String,AspectRemoting> ars; //url --> ARs
		
	public DamonProxy(int port) throws UnknownHostException, IOException {
		//System.out.println("DamonProxy : init() : "+Thread.currentThread().getContextClassLoader());
		comm = new Communication();
		comm.initSubscriber(port);
		this.ars = new QueueHashtable<String,AspectRemoting>();
	
	}

	public void add(String url, AspectRemoting ar) {		
		ars.put(url,ar);		
	}
	
	public void invokationArrive(AspectInvokation ai) {
		
		//System.out.println("DamonProxy :  invokationArrive("+ai+") : "+Thread.currentThread().getContextClassLoader());
		
		//boolean doIt = metaInvokationArrive(ai, Type.AFTER);
		//if (doIt) {
		
		for (AspectRemoting ar: ars.get(ai.getSubject())) {
			if (ar!=null)
				try {
					ar.invokeLocally(ai);
				} catch (InvokationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else ars.remove(ar);
		}
		
		//}
	}
	
	/*
    private boolean metaInvokationArrive(AspectInvokation ai, Type type) {
    	
    	//System.out.println("DamonProxy :  metaInvokationArrive("+ai+") : "+Thread.currentThread().getContextClassLoader());
    	boolean accept = true;
    	    	
    	if (!ai.isFake()) {
     	  AspectMetaInvokation ami = new AspectMetaInvokation(ai, type);
    	  Vector<AspectRemoting> arsByURL = ars.get(ai.getSubject());
    	  //System.out.println(ai.getSubject()+">>> "+arsByURL);
    	       	for(AspectRemoting ar : arsByURL) {
	        		if (ar instanceof DistributedMetaAspect) {
	        			DistributedMetaAspect dma = (DistributedMetaAspect) ar;
	        			System.out.println("DP >>> "+dma);
	        			try {
							accept &= !dma.checkMetaPointcuts(ami);
							//System.out.println(">>> ACCEPT? "+accept);
						} catch (InvokationException e) {							
							e.printStackTrace();
						}
	        		}
	        	}	        	
	        }
		
    	return accept;
	} 
	*/ 	
	
	public void run() {
		
		while(!close) {
	  	  try {	  		  
	  		  AspectInvokation ai = (AspectInvokation) comm.subscribe();
	  		//System.out.println("DamonProxy :  subscribe("+ai+") : "+Thread.currentThread().getContextClassLoader());
	          invokationArrive(ai);	  		  
	  		  
	  	  } catch (Exception e) {
	  		  
	  	  }
		}  
	}
	
	public void close() throws IOException {
		close = true;
		comm.close();
	}


}

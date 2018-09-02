package damon.util;

import java.io.*;
import java.net.*;
import java.util.*;

public class Communication {
	 
	
	private ServerSocket ss;
	private Socket socket;
	private Vector<ObjectOutputStream> outs = new Vector<ObjectOutputStream>();	
	private ObjectInputStream ins;
		
	public void initPublisher(int port) throws IOException {
		ss = new ServerSocket(port);	
	}
	
	public void waitSubscribers() throws IOException {		
		socket = ss.accept();
		OutputStream out = socket.getOutputStream();	
		ObjectOutputStream os = new ObjectOutputStream(out);
		outs.add(os);
	}
	

    public void publish(Serializable ser)  {
    	 int counter = 0;
    	 Vector<Integer> errors = new Vector<Integer>();
         for (ObjectOutputStream out : outs) {        	 
        	 try {       		  
       		  out.writeObject(ser);       		  				
			} catch (IOException e) {
			   errors.add(counter);	
			}      
		   counter++;
         }
         
         //remove errors
         for(int error : errors) {
        	 outs.remove(error);
         }
    }
	
    /**************************************/
    
    public void initSubscriber(int port) throws UnknownHostException, IOException {    	      
	  socket = new Socket(InetAddress.getLocalHost(), port);	  
	  InputStream is = socket.getInputStream();	  
	  ins = new ObjectInputStream(is);
	    	  
    }
    
    public Serializable subscribe() throws IOException, ClassNotFoundException {
        return (Serializable) ins.readObject();
    }
    
    /**************************************/
    
    public void close() throws IOException {
      if (ins!=null) ins.close();
      for (ObjectOutputStream out : outs) out.close();
      if (ss!=null) ss.close();
      if (socket!=null) socket.close(); 
    }
    


}

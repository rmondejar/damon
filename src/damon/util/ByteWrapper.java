package damon.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ByteWrapper {

	public static byte[] wrap(Object ser) throws IOException {
		
		  ByteArrayOutputStream bos = new ByteArrayOutputStream();
		  ObjectOutputStream os = new ObjectOutputStream(bos);
		  os.writeObject(ser);
		  os.close();			
				
		  byte[] bytes = bos.toByteArray();
		  return bytes;

	}
	
	public static Object unwrap(byte[] bytes) throws IOException, ClassNotFoundException {
		
		  ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		  ObjectInputStream is = new ObjectInputStream(bis);
		  Object ser = is.readObject();
		  is.close();			
		  
		  return ser;
		
	}
}

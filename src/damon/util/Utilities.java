/*****************************************************************************************
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
 *****************************************************************************************/
package damon.util;

import rice.p2p.commonapi.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * Util methods used in the pastry applications
 *
* @author <a href="mailto: ruben.mondejar@urv.net"> Ruben Mondejar </a>
 */
 
public class Utilities {
		
    //public static Id generateHash (String data) {    	
    //  return generateHash(data.getBytes());      
    //}
    
    public static Id generateHash (Object data) {
    	
    	if (data instanceof String) {
    		return generateHash(((String)data).getBytes());
    	}
    	
    	 ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	try {
    	 
		  ObjectOutputStream os = new ObjectOutputStream(bos);
		  os.writeObject(data);
		  os.close();			
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
		byte[] bytes = bos.toByteArray();
    	    	
        return generateHash(bytes);      
      }
    
    public static Id generateHash (byte[] data) {
      MessageDigest md = null;

      try {
        md = MessageDigest.getInstance("SHA");
      }
      catch (NoSuchAlgorithmException e) {
        System.err.println("No SHA support!");
      }

      md.update(data);
      byte[] digest = md.digest();

      Id newId = rice.pastry.Id.build(digest);

      return newId;
    }
    
    public static String generateStringHash(String data) {
    	return generateHash(data).toStringFull();   	
    	
    }
    
  
}	
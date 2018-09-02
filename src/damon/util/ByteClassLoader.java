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

import java.util.*;

 public class ByteClassLoader extends ClassLoader {
   private static Hashtable<String, Class> cache = new Hashtable<String, Class>();
   byte[] source;
   private ClassLoader contextLoader;

   public ByteClassLoader (byte[] bytes, ClassLoader contextLoader) {
     super();
     source = bytes;
     this.contextLoader = contextLoader;
   }

   public Class loadClass (String name, boolean resolve) throws ClassNotFoundException {
     try {
       return contextLoader.loadClass (name);
     } catch (ClassNotFoundException cnf) {
       return this.findClass (name);
     }
   }

   public Class findClass (String name) {
     if (cache.get (name) != null) {
       return (Class) cache.get (name);
     }
     else {
       byte[] b = loadClassData (name);
       Class c = super.defineClass (name, b, 0, b.length);
       cache.put (name, c);
       return c;
     }
   }

   private byte[] loadClassData (String name) {
     return source;
   }
}

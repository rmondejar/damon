package damon.util;

import damon.annotation.Abstractions;
import damon.annotation.DistributedAspect;
import damon.annotation.Type;
import damon.reflection.MetaData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Hashtable;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class AnnotationParser {

	public static MetaData parse(Class aspectClass) throws IOException, ClassNotFoundException  {
		return parse(aspectClass,ClassLoader.getSystemClassLoader());
	}
	
	public static MetaData parse(Class aspectClass, ClassLoader cl) throws IOException, ClassNotFoundException  {
		
		MetaData metadata = new MetaData();		
		metadata.setName(aspectClass.getName());

		Annotation[] as = aspectClass.getAnnotations();
		if (as.length>0) {
		  DistributedAspect da = (DistributedAspect) as[0];
				
		  metadata.setAbstraction(da.abstraction());
		  metadata.setGroup(da.target());
		  metadata.setNumNodes(da.num());
		  metadata.setStateful(da.stateful());
		  metadata.setSingleton(da.singleton());
		  metadata.setProxy(da.proxy());		  
		
		  ClassReader cr = null;
		  try {
			cr = new ClassReader(aspectClass.getName());
		  } catch (IOException e) {				
			cl.loadClass(aspectClass.getName());
			String res = aspectClass.getName().replace('.', File.separatorChar)+ ".class";
			InputStream is = cl.getResourceAsStream(res);			
			if (is!=null) cr = new ClassReader(readClass(is));			
		  }
          ClassNode cn = new ClassNode();
          cr.accept(cn,0);
        
          for (Object o : cn.methods) {
        	MethodNode mn = (MethodNode) o;
        	if (mn.visibleAnnotations!=null) {
        	  for (Object o2 : mn.visibleAnnotations) {
        		AnnotationNode an = (AnnotationNode) o2;
        		//System.out.println(mn.name+" -> "+an.desc);
        		     
        		if (an.desc.equals("Ldamon/annotation/RemoteUpdate;")) {            			
					metadata.addRemoteUpdate(mn.name);
				}
        		else if (an.desc.equals("Ldamon/annotation/RemoteCondition;")) {
            		Hashtable<String,Object> rc = parseValues(an);
					String id = (String) rc.get("id");					
					metadata.addRemoteCondition(mn.name, id);
				}
				else if (an.desc.equals("Ldamon/annotation/RemoteAdvice;")) {
					Hashtable<String,Object> ra = parseValues(an);
					String id = (String) ra.get("id");	
					metadata.addRemoteAdviceOrMethod(mn.name, id);
					
				} else if (an.desc.equals("Ldamon/annotation/RemoteMethod;")) {
					Hashtable<String,Object> rm = parseValues(an);
					String id = (String) rm.get("id");					
					metadata.addRemoteAdviceOrMethod(mn.name, id);
					
				} else if (an.desc.equals("Ldamon/annotation/RemotePointcut;")) {
					Hashtable<String,Object> rpc = parseValues(an);
					String id = (String) rpc.get("id");					
					Abstractions abstraction = (Abstractions) rpc.get("abstraction");
					int numNodes = 1;
					if (rpc.containsKey("num")) numNodes = (Integer) rpc.get("num"); // MANY abstraction
					String target = da.target(); // distributed aspect group
					if (rpc.containsKey("target")) target = (String) rpc.get("target");
					boolean synchro = false;
					if (rpc.containsKey("synchro")) synchro = (Boolean) rpc.get("synchro");
					boolean lazy = false;
					if (rpc.containsKey("lazy")) lazy = (Boolean) rpc.get("lazy");	
					metadata.addRemotePointcutOrInvocation(mn.name, id, abstraction, numNodes, target, synchro, lazy);
					
				} else if (an.desc.equals("Ldamon/annotation/RemoteInvocation;")) {
					Hashtable<String,Object> ri = parseValues(an);
					String id = (String) ri.get("id");					
					Abstractions abstraction = (Abstractions) ri.get("abstraction");
					int numNodes = 1;
					if (ri.containsKey("num")) numNodes = (Integer) ri.get("num"); // MANY abstraction
					String target = da.target(); // distributed aspect group
					if (ri.containsKey("target")) target = (String) ri.get("target");
					boolean synchro = false;
					if (ri.containsKey("synchro")) synchro = (Boolean) ri.get("synchro");
					boolean lazy = false;
					if (ri.containsKey("lazy")) lazy = (Boolean) ri.get("lazy");	
					metadata.addRemotePointcutOrInvocation(mn.name, id, abstraction, numNodes, target, synchro, lazy);
					
				} else if (an.desc.equals("Ldamon/annotation/RemoteMetaPointcut;")) {
					Hashtable<String,Object> rmpc = parseValues(an);
					String id = (String) rmpc.get("id");	
					String target = da.target(); // distributed aspect group
					if (rmpc.containsKey("target")) target = (String) rmpc.get("target");				
					Type type = (Type) rmpc.get("type");					
					boolean ack = false;
					if (rmpc.containsKey("ack")) ack = (Boolean) rmpc.get("ack");
					
					metadata.addRemoteMetaPointcut(mn.name, type, id, target, ack);
					
				} else if (an.desc.equals("Ldamon/annotation/RemoteMetaAdvice;")) {		
					Hashtable<String,Object> rma = parseValues(an);
					String id = (String) rma.get("id");					
					Abstractions abstraction = (Abstractions) rma.get("abstraction");
					int numNodes = 1;
					if (rma.containsKey("num")) numNodes = (Integer) rma.get("num"); // MANY abstraction
					String target = da.target(); // distributed aspect group
					if (rma.containsKey("target")) target = (String) rma.get("target");
					boolean synchro = false;
					if (rma.containsKey("synchro")) synchro = (Boolean) rma.get("synchro");
					boolean lazy = false;
					if (rma.containsKey("lazy")) lazy = (Boolean) rma.get("lazy");									
					metadata.addRemoteMetaAdvice(mn.name, id, abstraction, numNodes, target, synchro, lazy);
				  }
			   } 
        	}		
		}	
		}
		else System.out.println("Warning No Distributed Aspect : "+aspectClass.getName());

		return metadata;
	}
        
    private static byte[] readClass(InputStream is) throws IOException {
    	byte[] b = new byte[is.available()];
        int len = 0;
        while (true) {
            int n = is.read(b, len, b.length - len);
            if (n == -1) {
                if (len < b.length) {
                    byte[] c = new byte[len];
                    System.arraycopy(b, 0, c, 0, len);
                    b = c;
                }
                return b;
            }
            len += n;
            if (len == b.length) {
                byte[] c = new byte[b.length + 1000];
                System.arraycopy(b, 0, c, 0, len);
                b = c;
            }
        }

	}

	public static Hashtable<String,Object> parseValues(AnnotationNode an) {
    	
       List values = an.values;
              
       Hashtable<String,Object> vdata = new Hashtable<String,Object>();
       if (values!=null) {
         for (int i=0;i<values.size();i+=2) {
    	   
    	   String key = (String) values.get(i);
    	   //String value = (String) values.get(i+1);
    	   Object value = values.get(i+1);    	   
    	   
    	   if (key.equals("abstraction")) {
    		   String[] enumera = (String[]) value;    		           	           	
    		   vdata.put(key, getAbstractionCode(enumera[1]));
    	   }
    	   else if (key.equals("type")) {
    		   String[] enumera = (String[]) value;
    		   vdata.put(key, getTypeCode((String) enumera[1]));
    	   }
    	   /*
    	   else if (key.equals("num")) {
    		   vdata.put(key, (Integer) value));
    	   }
    	   else if (key.equals("synchro") || key.equals("ack") || key.equals("lazy")) {
    		   vdata.put(key, Boolean.parseBoolean((String) value));
    	   }
    	   */
    	   else vdata.put(key, value);
         }  
       }
       
       return vdata;
    }
    
    private static Type getTypeCode(String type) {
		
		for(Type t : Type.values()) {
			if (type.equals(t.toString())) {
				return t;
			}
		}		
		return Type.values()[0];
	}

	private static Abstractions getAbstractionCode(String abstraction) {
		
		for(Abstractions abs : Abstractions.values()) {
			if (abstraction.equals(abs.toString())) {
				return abs;
			}
		}		
		return Abstractions.values()[0];
	}

}

package damon.aspectwerkz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import damon.annotation.Type;
import damon.util.AnnotationParser;

public class AnnotationExtractor {
	 
	
    private static AnnotationInfo readAnnotations(Class c, ClassLoader cl) throws Exception {
	    	
    	AnnotationInfo info = new AnnotationInfo();
    	
    	ClassReader cr = null;
		try {
			cr = new ClassReader(c.getName());
		} catch (IOException e) {		
			cl.loadClass(c.getName());
			  String res = c.getName().replace('.', File.separatorChar)+ ".class";
				InputStream is = cl.getResourceAsStream(res);
				//System.out.println("Swap 1 : "+res+" -> "+is);
			    //System.out.println("reading is : "+is);
			    if (is!=null) cr = new ClassReader(readClass(is));			
		}
        ClassNode cn = new ClassNode();
        cr.accept(cn,0);
        
        for (Object o : cn.methods) {
        	MethodNode mn = (MethodNode) o;   	

        	if (mn.visibleAnnotations!=null) {
        	  for (Object o2 : mn.visibleAnnotations) {
        		
        		AnnotationNode an = (AnnotationNode) o2;
        		Hashtable<String,Object> values = AnnotationParser.parseValues(an);
        		    								
            	if (an.desc.equals("Ldamon/annotation/SourceHook;")) {
            	
					String sourceClass = (String) values.get("source");
					String sourceMethod = (String) values.get("method");
					Type type = (Type) values.get("type");
					//info.putAspectData(c.getName(),mn.name,source,method);					
					readAWAnnotations(sourceClass,sourceMethod,type,cl,mn.name,info);					    								
				}
            	if (an.desc.equals("Ldamon/annotation/DamonPulse;")) {    		
            		int seconds = (Integer) values.get("seconds");									
					info.putPulse(mn.name, seconds);					    
				}
            } 
          }	  
        }	
        return info;
    }

    private static void readAWAnnotations(final String sourceClass, final String sourceMethod, final Type type, ClassLoader cl, String originalMethod, AnnotationInfo info) throws Exception {
      	    	
    	ClassReader cr = null;
		try {
			cr = new ClassReader(sourceClass);
		} catch (IOException e) {		
			cl.loadClass(sourceClass);
			  String res = sourceClass.replace('.', File.separatorChar)+ ".class";
				InputStream is = cl.getResourceAsStream(res);
				//System.out.println("Swap 1 : "+res+" -> "+is);
			    //System.out.println("reading is : "+is);
			    if (is!=null) cr = new ClassReader(readClass(is));			
		}
        ClassNode cn = new ClassNode();
        cr.accept(cn,0);
        
        //System.out.println("Source : "+source+" method : "+method);
        //System.out.println("#METHODS : "+cn.methods.size());
        for (Object o : cn.methods) {
        	MethodNode mn = (MethodNode) o;
        	
        	if (sourceMethod.equals(mn.name) && mn.visibleAnnotations!=null) {
        	//	System.out.println("#ANNOTS : "+mn.visibleAnnotations.size());
        	  for (Object o2 : mn.visibleAnnotations) {
        		
        		AnnotationNode an = (AnnotationNode) o2;
        		//System.out.println("METHOD : "+mn.name);
        		//System.out.println("ANNOTI : "+an.desc);
        		List values = an.values;
        		int i=0;
        		for(;!values.get(i).equals("value");i++){}
        		i++;
        		//int i=0;
        		//while (!values.get(i).equals("value")) {i++;}
        		String value = (String) values.get(i);
        		String pointcut = cutPointcutDef(value);
        		        		
        		Type previousType = Type.NULL;
				
        		//System.out.println("aV : "+an.desc);
				    								
            	if (an.desc.equals("Lorg/codehaus/aspectwerkz/annotation/Before;")) {
				    previousType = Type.BEFORE;    								    
				    //value = before.value();									
				}
            	if (an.desc.equals("Lorg/codehaus/aspectwerkz/annotation/After;")) {    								    
				    previousType = Type.AFTER;
				    //value = after.value();
				}
            	if (an.desc.equals("Lorg/codehaus/aspectwerkz/annotation/Around;")) {    								    
				    previousType = Type.AROUND;
				    //value = around.value();
				}
				
				if (!type.equals(Type.NULL)) previousType = type; //change it!
				
				switch(previousType) {
				  case BEFORE : info.putBefore(originalMethod,pointcut); 
					  //System.out.println("BEFORE : "+source+" + "+method+" -> "+value);
				  break;
				  case AFTER : info.putAfter(originalMethod,pointcut);
					  //System.out.println("AFTER : "+source+" + "+method+" -> "+value);
				  break;
				  case AROUND : info.putAround(originalMethod,pointcut); 
					//  System.out.println("AROUND : "+source+" + "+method+" -> "+value);					  
				  break;								  
				}        		
        	  }
        	}  
        }	
    }
    
    private static String cutPointcutDef(String pointcut) {
		//System.out.println("real : "+pointcut);
		StringTokenizer st = new StringTokenizer(pointcut," ");
		String pointcut2 = st.nextToken();
		//System.out.println("ini : "+pointcut2);
		while (st.hasMoreElements()) {
		
			String token = st.nextToken();		   
			   if (token.equals("AND")) {
				   String nextToken = st.nextToken();
				   if (!nextToken.startsWith("args")) {
					   //System.out.println("good : "+token+" "+nextToken+" ");
					   pointcut2 += " "+token+" "+nextToken; 	      
				   }
				   //else System.out.println("bad : "+token+" "+nextToken+" ");
			   }			
			   else {
				   //System.out.println("good : "+token+" ");
				   pointcut2 += " "+token; 
			   }
		}
		//System.out.println("final : "+pointcut2);
		return pointcut2;
	}
    
    public static byte[] readClass(InputStream is) throws IOException {
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
  
	
	public static void printAnnotations(Class c) throws ClassNotFoundException {
		System.out.println("Annotations Class <"+c.getName()+"> : ");		
		for (Method m : c.getMethods()) {
			Annotation[] ans = m.getAnnotations();
			if (ans.length>0) {			  
			  for (Annotation a : ans) {
				System.out.println(""+a);				
			  }
			  System.out.println("----- "+m);
			}  
		}
	}
		
    public static synchronized AnnotationInfo extract(Class c) {
		return extract(c,ClassLoader.getSystemClassLoader());
	}
	
	public static synchronized AnnotationInfo extract(Class c,ClassLoader cl) {
		
		try {	  
				
		  AnnotationInfo info = readAnnotations(c,cl);	  		  		   
		  return info;
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;

	}
	
    
}

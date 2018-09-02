package damon.aspectwerkz;

import java.util.Hashtable;
import java.util.Vector;

import org.jdom.*;
import org.jdom.output.XMLOutputter;

public class DocumentCreator {
	
	public static String createXML(Class aspectClass, AnnotationInfo info) {
				
		  Hashtable<String,Vector<String[]>> advices = info.getAdvices();
		  Element aspect = new Element("aspect");
		  aspect.setAttribute("class", aspectClass.getName());
		  
		  int num = 0;
		  for (String methodName : advices.keySet()) {
			  Vector<String[]> pairs = advices.get(methodName);
			  for (String[] pair : pairs) {
				  Element pointcut = new Element("pointcut");
				  pointcut.setAttribute("name", "pc"+num);
				  pointcut.setAttribute("expression", pair[1]);				  
				  aspect.addContent(pointcut);
				  
				  Element advice = new Element("advice");
				  advice.setAttribute("name", methodName);
				  advice.setAttribute("type", pair[0]);
				  advice.setAttribute("bind-to", "pc"+num);
				  aspect.addContent(advice);
				  
				  num++;
			  }  
		  }
		  Document doc = new Document(aspect);
		  XMLOutputter serializer = new XMLOutputter();		
		  return serializer.outputString(doc);
		  
	}

}

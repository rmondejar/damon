package damon.util;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import damon.annotation.Abstractions;
import damon.annotation.Type;
import damon.reflection.MetaData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class XMLParser {

	public static void generate(String className, String xmlFileName) {

	}

	public static void test(String xml, String xsd) {
		try {
			File xmlFile = new File(xml);
			File xsdFile = new File(xsd);

			SAXBuilder builder = new SAXBuilder(true);
			builder.setFeature(
					"http://apache.org/xml/features/validation/schema", true);

			builder.setProperty("http://apache.org/xml/properties/schema"
					+ "/external-noNamespaceSchemaLocation", xsdFile.toURI()
					.toString());

			builder.build(xmlFile);

			System.out.println("Successfully validated");
		} catch (Exception cause) {
			System.err.println(cause.toString());
		}
	}

	public static MetaData parse(File file) throws JDOMException,
	IOException {
		return parse(new FileInputStream(file));
	}
	
	public static MetaData parse(InputStream is) throws JDOMException,
			IOException {

		Document d = new SAXBuilder().build(is);
		Element root = d.getRootElement();
		Element e = root;

		MetaData metadata = new MetaData();
		String name = e.getChildText(Context.NAME);
		metadata.setName(name);

		String abstraction = e.getChildText(Context.ABSTRACTION);
		metadata.setAbstraction(getAbstractionCode(abstraction));
		String target = e.getChildText(Context.TARGET);
		metadata.setGroup(target);

		String numNodes = e.getChildText(Context.NUM_NODES);
		if (numNodes != null) {
			metadata.setNumNodes(Integer.parseInt(numNodes));
		}
		String state = e.getChildText(Context.STATE);
		if (state != null) {
			if (state.equals("STATEFUL"))
				metadata.setStateful(true);
		}
		
		String singleton = e.getChildText(Context.SINGLETON);
		if (singleton != null) {
			if (singleton.equals("SINGLETON"))
				metadata.setSingleton(true);
		}
		
		String proxy = e.getChildText(Context.PROXY);
		if (proxy != null) {
			if (proxy.equals("PROXY"))
				metadata.setProxy(true);
		}

		List<Element> list = e.getChildren();
		for (Element elem : list) {
			if (elem.getName().equals("offered")) {

				name = elem.getChildText(Context.NAME);
				String id = elem.getChildText(Context.ID);
				abstraction = elem.getChildText(Context.ABSTRACTION);
				target = e.getChildText(Context.TARGET);
				
				if (numNodes != null) metadata.setNumNodes(Integer.parseInt(numNodes));
				else numNodes = "1";
				
				boolean synchro = false;
				String synchroText = e.getChildText(Context.SYNCHRO);
				if (synchroText != null && (synchroText.equals("SYNCHRONIZED"))) synchro = true;

				boolean lazy = false;
				String lazyText = e.getChildText(Context.SYNCHRO);
				if (lazyText != null && (lazyText.equals("LAZY"))) lazy = true;

				System.out.println("offered : "+name);
				
				metadata.addRemotePointcutOrInvocation(name, id,
						getAbstractionCode(abstraction), Integer
								.parseInt(numNodes), target, synchro, lazy);
			} else if (elem.getName().equals("required")) {

				name = elem.getChildText(Context.NAME);
				String id = elem.getChildText(Context.ID);

				System.out.println("required : "+name);
				metadata.addRemoteAdviceOrMethod(name, id);
			}

			else if (elem.getName().equals("meta-pointcut")) {

				name = elem.getChildText(Context.NAME);
				String id = elem.getChildText(Context.ID);
				String type = elem.getChildText(Context.TYPE);
				
				boolean ack = false;
				String ackText = e.getChildText(Context.ACK);
				if (ackText != null && (ackText.equals("ACK"))) ack = true;

				System.out.println("meta-pointcut : "+name);
				metadata.addRemoteMetaPointcut(name, getTypeCode(type),
						id, target,ack);
			}
			
			else if (elem.getName().equals("meta-advice")) {

				name = elem.getChildText(Context.NAME);
				String id = elem.getChildText(Context.ID);
				abstraction = elem.getChildText(Context.ABSTRACTION);
				target = e.getChildText(Context.TARGET);
				
				if (numNodes != null) metadata.setNumNodes(Integer.parseInt(numNodes));
				else numNodes = "1";
				
				boolean synchro = false;
				String synchroText = e.getChildText(Context.SYNCHRO);
				if (synchroText != null && (synchroText.equals("SYNCHRONIZED"))) synchro = true;

				boolean lazy = false;
				String lazyText = e.getChildText(Context.SYNCHRO);
				if (lazyText != null && (lazyText.equals("LAZY"))) lazy = true;

				System.out.println("meta-advice : "+name);
				
				metadata.addRemoteMetaAdvice(name, id,
						getAbstractionCode(abstraction), Integer
								.parseInt(numNodes), target, synchro, lazy);
			}
		}

		return metadata;
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

	public static void main(String[] args) {
		// test("Locator.xml","distributed-aspect.xsd");

		try {

			parse(new FileInputStream(new File("Locator.xml")));
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

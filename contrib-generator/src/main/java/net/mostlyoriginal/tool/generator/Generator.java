package net.mostlyoriginal.tool.generator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.mostlyoriginal.tool.generator.common.JarReflectionsComponentCollectionStrategy;
import net.mostlyoriginal.tool.generator.model.Component;
import net.mostlyoriginal.tool.generator.model.Factory;
import net.mostlyoriginal.tool.generator.model.Method;
import net.mostlyoriginal.tool.generator.model.Parameter;
import org.reflections.Reflections;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Set;

/**
 * @author Daan van Yperen
 */
public class Generator {

	public static void main( String[] args)
	{

		Component[] components = new JarReflectionsComponentCollectionStrategy(new File("E:\\GitHub\\arktrail\\desktop\\build\\libs\\desktop-1.0.jar")).getComponents();

		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(Generator.class, "/");
		try {
			Template template = cfg.getTemplate("Factory.java.ftl");

			Factory factory = new Factory("Ship");
			for (Component component : components) {
				Collections.sort(component.getMethods());
				factory.getComponents().add(component);
			}

			Collections.sort(factory.getComponents());

			/*
			Method pos = new Method("pos", false);
			pos.parameters.add(new Parameter("float","x"));
			pos.parameters.add(new Parameter("float","y"));
			factory.methods.add(pos);

			Method sprite = new Method("sprite", true);
			sprite.parameters.add(new Parameter("String","id"));
			factory.methods.add(sprite);*/

			// Console output
			Writer out = new OutputStreamWriter(System.out);
			template.process(factory, out);
			out.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
	}
}

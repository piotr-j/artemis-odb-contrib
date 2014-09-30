package net.mostlyoriginal.tool.generator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.mostlyoriginal.tool.generator.model.Component;
import net.mostlyoriginal.tool.generator.model.Factory;
import net.mostlyoriginal.tool.generator.model.Method;
import net.mostlyoriginal.tool.generator.model.Parameter;

import java.io.*;

/**
 * @author Daan van Yperen
 */
public class Generator {

	public static void main( String[] args)
	{
		Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(Generator.class,"/");
		try {
			Template template = cfg.getTemplate("Factory.java.ftl");

			Factory factory = new Factory("Ship");
			factory.components.add(new Component("net.mostlyoriginal.api.basic.Pos", "Pos"));
			factory.components.add(new Component("net.mostlyoriginal.api.basic.Angle", "Angle"));
			factory.components.add(new Component("net.mostlyoriginal.api.basic.Sprite", "Sprite"));

			Method pos = new Method("pos", false);
			pos.parameters.add(new Parameter("float","x"));
			pos.parameters.add(new Parameter("float","y"));
			factory.methods.add(pos);

			Method sprite = new Method("sprite", true);
			sprite.parameters.add(new Parameter("String","id"));
			factory.methods.add(sprite);

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

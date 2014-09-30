package net.mostlyoriginal.tool.generator.common;

import net.mostlyoriginal.tool.generator.model.Component;
import net.mostlyoriginal.tool.generator.model.Method;
import net.mostlyoriginal.tool.generator.model.Parameter;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Daan van Yperen
 */
public class JarReflectionsComponentCollectionStrategy implements ComponentCollectionStrategy {

	private final URLClassLoader urlClassLoader;

	public JarReflectionsComponentCollectionStrategy(File file) {
		try {
			urlClassLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, System.class.getClassLoader());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Component[] getComponents() {

		Reflections reflections = new Reflections(urlClassLoader);
		Set<Class<? extends com.artemis.Component>> subTypes = reflections.getSubTypesOf(com.artemis.Component.class);

		ArrayList<Component> components = new ArrayList<Component>(subTypes.size());
		for (Class<? extends com.artemis.Component> subType : subTypes) {

			// resolve methods.
			components.add(wrapComponent(subType));
		}

		return components.toArray(new Component[components.size()]);
	}

	protected Component wrapComponent(Class<? extends com.artemis.Component> subType) {
		final Component component = new Component(subType);

		if (subType.getConstructors().length > 0) {

			// use constructors. add one per constructor.
			int index=0;
			for (Constructor<?> constructor : subType.getConstructors()) {
				Method pos = new Method(component.getSimpleName().toLowerCase(), false);
				pos.setConstructor(true);
				for (Class<?> aClass : constructor.getParameterTypes()) {
					pos.getParameters().add(new Parameter(aClass.getSimpleName(), "arg"+index++));
				}

				if (pos.getParameters().size() > 0) {
					component.getMethods().add(pos);
				}
			}

		}

		Method pos = new Method(component.getSimpleName().toLowerCase(), false);
		// fallback to fields
		for (Field field : subType.getDeclaredFields()) {
				if ( java.lang.reflect.Modifier.isStatic(field.getModifiers())
					 || !java.lang.reflect.Modifier.isPublic(field.getModifiers())
			         || java.lang.reflect.Modifier.isFinal(field.getModifiers()))
				continue;
				pos.getParameters().add(new Parameter(field.getType().getSimpleName(), field.getName()));
		}
		if (pos.getParameters().size() > 0) {
			component.getMethods().add(pos);
		}

		return component;
	}
}

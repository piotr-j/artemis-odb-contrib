package net.mostlyoriginal.tool.generator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daan van Yperen
 */
public class Component {
	public String name;
	public String simpleName;
	public List<Method> methods = new ArrayList<Method>();

	public Component() {
	}

	public Component(Class clazz) {
		name = clazz.getName();
		simpleName = clazz.getSimpleName();
	}

	public Component(String name, String simpleName) {
		this.name = name;
		this.simpleName = simpleName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSimpleName() {
		return simpleName;
	}

	public void setSimpleName(String simpleName) {
		this.simpleName = simpleName;
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(List<Method> methods) {
		this.methods = methods;
	}
}

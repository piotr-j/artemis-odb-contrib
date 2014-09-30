package net.mostlyoriginal.tool.generator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Daan van Yperen
 */
public class Factory {

	public String type;
	public List<Method> methods = new ArrayList<>();
	public List<Component> components = new ArrayList<>();

	public Factory() {
	}

	public Factory(String type) {
		this.type = type;
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(List<Method> methods) {
		this.methods = methods;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}

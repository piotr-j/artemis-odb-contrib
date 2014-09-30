package net.mostlyoriginal.tool.generator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Daan van Yperen
 */
public class Factory {

	public String type;
	public List<Component> components = new ArrayList<>();

	public Factory() {
	}

	public Factory(String type) {
		this.type = type;
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

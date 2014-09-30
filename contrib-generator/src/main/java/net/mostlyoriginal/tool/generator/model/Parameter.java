package net.mostlyoriginal.tool.generator.model;

/**
 * @author Daan van Yperen
 */
public class Parameter {
	public String name;
	public String type;

	public Parameter() {
	}

	public Parameter(String type, String name) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}

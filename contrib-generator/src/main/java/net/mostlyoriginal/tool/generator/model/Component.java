package net.mostlyoriginal.tool.generator.model;

/**
 * @author Daan van Yperen
 */
public class Component {
	public String name;
	public String simpleName;

	public Component() {
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
}

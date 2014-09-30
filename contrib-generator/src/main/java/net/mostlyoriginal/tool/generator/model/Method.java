package net.mostlyoriginal.tool.generator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daan van Yperen
 */
public class Method {
	public String name;
	public Boolean sticky = false;
	public List<Parameter> parameters = new ArrayList<>();

	public Method() {
	}

	public Method(String name, Boolean sticky) {
		this.name = name;
		this.sticky = sticky;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public Boolean getSticky() {
		return sticky;
	}

	public void setSticky(Boolean sticky) {
		this.sticky = sticky;
	}
}

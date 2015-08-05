package edu.cmu.sei.eraces.aadl.model;

import java.util.ArrayList;
import java.util.List;

import org.osate.aadl2.instance.ComponentInstance;

public class ReportItem {
	private Category category;
	private String justification;
	private List<ComponentInstance> relatedComponents;

	public ReportItem() {
		this.relatedComponents = new ArrayList<ComponentInstance>();
		this.category = Category.UNKNOWN;
	}
}

package edu.cmu.sei.eraces.aadl.model;

import java.util.ArrayList;
import java.util.List;

import org.osate.aadl2.NamedElement;

public class ReportItem implements Comparable {
	private Category category;
	private String justification;
	private Severity severity;
	private List<NamedElement> relatedElements;

	public ReportItem() {
		this.relatedElements = new ArrayList<NamedElement>();
		this.category = Category.UNKNOWN;
		this.severity = Severity.NORMAL;
	}

	public Severity getSeverity() {
		return this.severity;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setSeverity(Severity s) {
		this.severity = s;
	}

	public void setCategory(Category c) {
		this.category = c;
	}

	public void setJustification(String j) {
		this.justification = j;
	}

	public String getJustification() {
		return this.justification;
	}

	public void addRelatedElement(NamedElement ci) {
		this.relatedElements.add(ci);
	}

	public void setRelatedElements(List<NamedElement> l) {
		this.relatedElements = l;
	}

	public List<NamedElement> getRelatedElements() {
		return this.relatedElements;
	}

	public int compareTo(Object arg) {
		if (arg instanceof ReportItem) {
			ReportItem other = (ReportItem) arg;
			if ((other.getJustification().equalsIgnoreCase(this.justification))
					&& (other.getSeverity() == this.getSeverity()) && (other.getCategory() == this.getCategory())) {
				return 0;
			}
		}
		return -1;
	}

	public boolean equals(Object arg) {
		return (compareTo(arg) == 0);
	}
}

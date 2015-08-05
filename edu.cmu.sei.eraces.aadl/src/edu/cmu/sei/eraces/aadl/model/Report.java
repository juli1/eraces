package edu.cmu.sei.eraces.aadl.model;

import java.util.ArrayList;
import java.util.List;

public class Report {

	private List<ReportItem> items;

	public Report() {
		this.items = new ArrayList<ReportItem>();
	}

	public void addItem(ReportItem item) {
		this.items.add(item);
	}

	public void exportExcel() {

	}

	public void exportCSV() {

	}
}

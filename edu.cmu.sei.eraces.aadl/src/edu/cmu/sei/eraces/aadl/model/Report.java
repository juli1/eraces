package edu.cmu.sei.eraces.aadl.model;

import java.util.ArrayList;
import java.util.List;

import org.osate.aadl2.NamedElement;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.WriteToFile;

public class Report {
	private SystemInstance system;
	private List<ReportItem> items;

	public Report(SystemInstance si) {
		this.items = new ArrayList<ReportItem>();
		this.system = si;
	}

	public void addItem(ReportItem item) {
		for (ReportItem it : this.items) {
			if (it.equals(item)) {
				return;
			}
		}
		this.items.add(item);
	}

	public void export() {
		exportExcel();
		exportCSV();
	}

	public void exportExcel() {

	}

	public void exportCSV() {
		WriteToFile csvReport = new WriteToFile("ERACES", this.system);
		csvReport.addOutputNewline("Category, Severity,Justification,Related Components");
		for (ReportItem ri : this.items) {
			csvReport.addOutput(ri.getCategory().toString());
			csvReport.addOutput(",");
			csvReport.addOutput(ri.getSeverity().toString());
			csvReport.addOutput(",");
			csvReport.addOutput(ri.getJustification().toString());
			csvReport.addOutput(",");
			for (NamedElement ne : ri.getRelatedElements()) {
				csvReport.addOutput(ne.getName());
			}
			csvReport.addOutput("\n");
		}
		csvReport.setFileExtension("csv");
		csvReport.saveToFile();
	}
}

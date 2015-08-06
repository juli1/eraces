package edu.cmu.sei.eraces.aadl.logic;

import java.util.ArrayList;
import java.util.List;

import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.ConnectionInstance;
import org.osate.aadl2.instance.ConnectionInstanceEnd;
import org.osate.aadl2.instance.FeatureInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.xtext.aadl2.properties.util.GetProperties;

import edu.cmu.sei.eraces.aadl.model.Category;
import edu.cmu.sei.eraces.aadl.model.Report;
import edu.cmu.sei.eraces.aadl.model.ReportItem;
import edu.cmu.sei.eraces.aadl.util.Utils;

public class OptimizationLogic {
	private SystemInstance systemInstance;
	private Report report;

	public OptimizationLogic(SystemInstance si) {
		this.systemInstance = si;
		this.report = new Report(si);
	}

	public Report getReport() {
		return (this.report);
	}

	public void process() {
		processComponent(systemInstance);
		for (ConnectionInstance ci : systemInstance.getAllConnectionInstances()) {
			this.processConnection(ci);
		}
	}

	/**
	 * Check for any shared variable accessed by several threads in the same process.
	 * 
	 * @param processInstance - the process containing the data and other threads
	 * @param dataInstance    - the shared data
	 */
	public void checkSharedData(ComponentInstance processInstance, ComponentInstance dataInstance) {
		List<NamedElement> threads;
		threads = new ArrayList<NamedElement>();

		System.out.println("[checkSharedData] on data: " + dataInstance);

		for (ConnectionInstance ci : processInstance.getConnectionInstances()) {
			ConnectionInstanceEnd destination = ci.getDestination();
			ConnectionInstanceEnd source = ci.getSource();
			ComponentInstance componentDestination = Utils.getComponent(destination);
			ComponentInstance componentSource = Utils.getComponent(source);
			System.out.println("[checkSharedData] source     : " + componentSource);
			System.out.println("[checkSharedData] destination: " + componentDestination);
			if (componentSource == dataInstance) {
				threads.add(componentDestination);
			}
			if (componentDestination == dataInstance) {
				threads.add(componentSource);
			}
		}

		if (threads.size() > 0) {
			ReportItem item = new ReportItem();
			item.setRelatedElements(threads);
			item.setCategory(Category.SCOPE);
			item.setJustification("Component share the same global variable - could be replaced by connections");
			report.addItem(item);

		}
	}

	/**
	 * Check for all potential issue with shared data in the following process
	 * @param processInstance - the process to investigate
	 */
	public void checkProcessSharedVariables(ComponentInstance processInstance) {
		System.out.println("[checkProcessSharedVariables] " + processInstance.getName());

		for (ComponentInstance subcomponent : processInstance.getComponentInstances()) {
			if (subcomponent.getCategory() == ComponentCategory.DATA) {
				checkSharedData(processInstance, subcomponent);
			}
		}
	}

	public void checkComponentsPeriod(ComponentInstance source, ComponentInstance destination) {
		ReportItem item = new ReportItem();

		if ((GetProperties.getPeriodinMS(source) == 0) && (GetProperties.getPeriodinMS(destination) == 0)) {
			return;
		}

		if (GetProperties.getPeriodinMS(source) == 0) {
			item.setJustification("No period specification while the destination has one");
			item.setCategory(Category.TASK);
			item.addRelatedElement(source);
			report.addItem(item);
			return;
		}

		if (GetProperties.getPeriodinMS(destination) == 0) {
			item.setJustification("No period specification while the source has one");
			item.setCategory(Category.TASK);
			item.addRelatedElement(destination);
			report.addItem(item);
			return;
		}

		if (GetProperties.getPeriodinMS(destination) > GetProperties.getPeriodinMicroSec(source)) {
			item.setJustification("Destination has slower period than the source");
			item.setCategory(Category.TASK);
			item.addRelatedElement(destination);
			item.addRelatedElement(source);
			report.addItem(item);
			return;
		}
	}

	public void checkQueueSizes(FeatureInstance source, FeatureInstance destination) {
		ReportItem item = new ReportItem();

		if (GetProperties.getQueueSize(source) != GetProperties.getQueueSize(destination)) {
			item.setJustification("Source and connection features does not have the same queue_size");
			item.setCategory(Category.TASK);
			item.addRelatedElement(destination);
			item.addRelatedElement(source);
			report.addItem(item);
		}
	}

	/**
	 * Process a connection instance and initiates all check
	 * that could be done.
	 * @param connection
	 */
	public void processConnection(ConnectionInstance connection) {

		/**
		 * In the following, we will process connections and check
		 * for unconsistent connection. In particular:
		 *   1. Connection with receiver at a faster rate than the sender
		 *   2. Queue dimension that does not match
		 */
		System.out.println("[processConnection] " + connection.getName());
		FeatureInstance featureSource = null;
		FeatureInstance featureDestination = null;
		ConnectionInstanceEnd destination = connection.getDestination();
		ConnectionInstanceEnd source = connection.getSource();

		if (connection.getDestination() instanceof FeatureInstance) {
			featureDestination = (FeatureInstance) connection.getDestination();
		}

		if (connection.getSource() instanceof FeatureInstance) {
			featureSource = (FeatureInstance) connection.getSource();
		}

		ComponentInstance componentSource = Utils.getComponent(source);
		ComponentInstance componentDestination = Utils.getComponent(destination);

		checkComponentsPeriod(componentSource, componentDestination);
		checkQueueSizes(featureSource, featureDestination);
		System.out.println("[processConnection] component source=" + componentSource);
		System.out.println("[processConnection] component dest  =" + componentDestination);
	}

	public void checkSubcomponentsConnections(ComponentInstance componentInstance) {
		System.out.println("[checkSubcomponentsConnections] " + componentInstance.getName());

	}

	public void processComponent(ComponentInstance componentInstance) {

		System.out.println("[processComponent] " + componentInstance.getName());

		/**
		 * Checks specific to a process, especially for the shared data
		 * pattern.
		 */
		if (componentInstance.getCategory() == ComponentCategory.PROCESS) {
			checkProcessSharedVariables(componentInstance);
			checkSubcomponentsConnections(componentInstance);
		}

		for (FeatureInstance feature : componentInstance.getFeatureInstances()) {
			System.out.println(" -> feature: " + feature.getName());
		}

		for (ComponentInstance subcomponent : componentInstance.getComponentInstances()) {
			processComponent(subcomponent);
		}

	}
}

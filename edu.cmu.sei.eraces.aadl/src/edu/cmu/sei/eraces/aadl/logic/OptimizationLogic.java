package edu.cmu.sei.eraces.aadl.logic;

import java.util.ArrayList;
import java.util.List;

import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.ConnectionInstance;
import org.osate.aadl2.instance.ConnectionInstanceEnd;
import org.osate.aadl2.instance.FeatureInstance;
import org.osate.aadl2.instance.SystemInstance;

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
		List<ComponentInstance> threads;
		threads = new ArrayList<ComponentInstance>();

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
			item.setRelatedComponent(threads);
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

	public void checkSubcomponentsConnections(ComponentInstance componentInstance) {
		System.out.println("[checkSubcomponentsConnections] " + componentInstance.getName());

	}

	public void processConnection(ConnectionInstance connection) {

		/**
		 * In the following, we will process connections and check
		 * for unconsistent connection. In particular:
		 *   1. Connection with receiver at a faster rate than the sender
		 *   2. Queue dimension that does not match
		 */
		System.out.println("[processConnection] " + connection.getName());

		ConnectionInstanceEnd destination = connection.getDestination();
		ConnectionInstanceEnd source = connection.getSource();
		ComponentInstance componentSource = Utils.getComponent(source);
		ComponentInstance componentDestination = Utils.getComponent(destination);
		System.out.println("[processConnection] component source=" + componentSource);
		System.out.println("[processConnection] component dest  =" + componentDestination);
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

package edu.cmu.sei.eraces.aadl.logic;

import java.util.ArrayList;
import java.util.List;

import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.DataPort;
import org.osate.aadl2.DataSubcomponentType;
import org.osate.aadl2.EnumerationLiteral;
import org.osate.aadl2.EventDataPort;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.ConnectionInstance;
import org.osate.aadl2.instance.ConnectionInstanceEnd;
import org.osate.aadl2.instance.FeatureCategory;
import org.osate.aadl2.instance.FeatureInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.errorreporting.AnalysisErrorReporterManager;
import org.osate.aadl2.modelsupport.errorreporting.MarkerAnalysisErrorReporter;
import org.osate.xtext.aadl2.properties.util.GetProperties;

import edu.cmu.sei.eraces.aadl.Activator;
import edu.cmu.sei.eraces.aadl.model.Category;
import edu.cmu.sei.eraces.aadl.model.Report;
import edu.cmu.sei.eraces.aadl.model.ReportItem;
import edu.cmu.sei.eraces.aadl.model.Severity;
import edu.cmu.sei.eraces.aadl.util.Utils;

public class OptimizationLogic {
	private SystemInstance systemInstance;
	private Report report;
	private AnalysisErrorReporterManager errManager;

	public OptimizationLogic(SystemInstance si) {
		this.systemInstance = si;
		this.report = new Report(si);

		this.errManager = new AnalysisErrorReporterManager(new MarkerAnalysisErrorReporter.Factory(
				Activator.ERACES_MARKER));

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
	 * Check Pattern number 1
	 * Check for any shared variable accessed by several threads in the same process.
	 * 
	 * @param processInstance - the process containing the data and other threads
	 * @param dataInstance    - the shared data
	 */
	public void checkSharedDataAccess(ComponentInstance processInstance, ComponentInstance dataInstance) {
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
			String msg = "Component share the same global variable - could be replaced by connections";
			report(threads, msg, Category.SCOPE, Severity.MAJOR);
		}
	}

	/**
	 * Check Pattern2 - all shared data must have concurrency access protocol
	 * @param processInstance
	 * @param dataInstance
	 */
	public void checkSharedDataConcurrency(ComponentInstance processInstance, ComponentInstance dataInstance) {
		if (GetProperties.getConcurrencyControlProtocol(dataInstance) == null) {
			String msg = "Shared data must have a concurrency control protocol";
			report(dataInstance, msg, Category.UNKNOWN, Severity.MAJOR);
		}
	}

	/**
	 * Check for all potential issue with shared data in the following process
	 * This checks pattern 1 and pattern 2
	 * @param processInstance - the process to investigate
	 */
	public void checkProcessSharedVariables(ComponentInstance processInstance) {
//		System.out.println("[checkProcessSharedVariables] " + processInstance.getName());

		for (ComponentInstance subcomponent : processInstance.getComponentInstances()) {
			if (subcomponent.getCategory() == ComponentCategory.DATA) {
				checkSharedDataAccess(processInstance, subcomponent);
				checkSharedDataConcurrency(processInstance, subcomponent);
			}
		}
	}

	/**
	 * Check Pattern 3 - Harmonic tasks in the same process
	 * @param processInstance - the process to investigate
	 */
	public void checkHarmonicTasks(ComponentInstance processInstance) {
//		System.out.println("[checkProcessSharedVariables] " + processInstance.getName());

		for (ComponentInstance subcomponent : processInstance.getComponentInstances()) {
			if (subcomponent.getCategory() == ComponentCategory.THREAD) {
				double period1 = GetProperties.getPeriodinMS(subcomponent);
				for (ComponentInstance subcomponent2 : processInstance.getComponentInstances()) {
					if (subcomponent2.getCategory() == ComponentCategory.THREAD) {
						double period2 = GetProperties.getPeriodinMS(subcomponent2);
						if (period1 == period2) {
							List<NamedElement> nes;
							nes = new ArrayList<NamedElement>();
							nes.add(subcomponent);
							nes.add(subcomponent2);
							String msg = "Shared data must have a concurrency control protocol";
							report(nes, msg, Category.UNKNOWN, Severity.MAJOR);
						}
					}
				}
			}

		}
	}

	/**
	 * Check Pattern 7 - check naming policy
	 * @param relatedData - the component on which we check the naming policy
	 */
	public void checkNamingPolicy(ComponentInstance ci) {
		String[] prefixes = { "is_", "do_", "has_" };

		for (String prefix : prefixes) {
			for (FeatureInstance fi : ci.getFeatureInstances()) {
				if (fi.getName().startsWith(prefix)) {
					String msg = "Uses an invalid prefix (" + prefix + ")";
					report(fi, msg, Category.UNKNOWN, Severity.MAJOR);
				}
			}
		}
	}

	/**
	 * Check Pattern 4 - Use of specific data types
	 * @param relatedData - the dataType to check
	 */
	public void checkDataType(DataSubcomponentType relatedData) {
		System.out.println("[checkDataType] " + relatedData.getName());
		EnumerationLiteral representation = GetProperties.getDataRepresentation(relatedData);
		String representationName = representation.getName();

		String[] representationOK = { "struct", "union", "enum", "array" };
		for (String s : representationOK) {
			if (representationName.equalsIgnoreCase(s)) {
				return;
			}
		}

		if (representationName.equalsIgnoreCase("integer")) {
			if (GetProperties.getDataIntegerRange(relatedData) == null) {
				String msg = "Data uses an unconstrained type, use Integer_Range to specify range ";
				report(relatedData, msg, Category.UNKNOWN, Severity.MAJOR);
			}
			return;
		}

		if (representationName.equalsIgnoreCase("float")) {
			if (GetProperties.getDataRealRange(relatedData) == null) {
				String msg = "Data uses an unconstrained type, use Real_Range to specify range";
				report(relatedData, msg, Category.UNKNOWN, Severity.MAJOR);
			}
			return;
		}

		if ((representationName.equalsIgnoreCase("character")) || (representationName.equalsIgnoreCase("boolean"))) {
			if (GetProperties.getDataRealRange(relatedData) == null) {
				String msg = "Data uses a generic type, must be refined into a specific type";
				report(relatedData, msg, Category.UNKNOWN, Severity.MAJOR);
			}
			return;
		}

		String msg = "Does not specify its representation";
		report(relatedData, msg, Category.UNKNOWN, Severity.MAJOR);
	}

	/**
	 * Check Pattern 6 - sampling ports components period
	 * @param source
	 * @param destination
	 */
	public void checkComponentsPeriod(ComponentInstance source, ComponentInstance destination) {
		double sourcePeriod;
		double destinationPeriod;

		sourcePeriod = GetProperties.getPeriodinMS(source);
		destinationPeriod = GetProperties.getPeriodinMS(destination);

		if (sourcePeriod == 0) {
			String msg = "No period specification while the destination has one";
			report(source, msg, Category.TASK, Severity.MAJOR);
			return;
		}

		if (destinationPeriod == 0) {
			String msg = "No period specification while the source has one";
			report(destination, msg, Category.TASK, Severity.MAJOR);
			return;
		}

		if (destinationPeriod > sourcePeriod) {
			String msg = "Destination has slower period than the source";
			report(destination, msg, Category.TASK, Severity.MAJOR);
			return;
		}
	}

	/**
	 * Check Pattern 5 - queue dimensions
	 * @param source
	 * @param destination
	 */
	public void checkQueueSizes(FeatureInstance source, FeatureInstance destination) {
		long destinationQueueSize = GetProperties.getQueueSize(destination);
		ComponentInstance componentSource = Utils.getComponent(source);
		ComponentInstance componentDestination = Utils.getComponent(destination);

		if (destinationQueueSize == 0) {
			destinationQueueSize = 1;
		}

		double sourcePeriod = GetProperties.getPeriodinMS(componentSource);
		double destinationPeriod = GetProperties.getPeriodinMS(componentDestination);
		double normalizedPeriodDestination = destinationPeriod / destinationQueueSize;

		if (normalizedPeriodDestination > sourcePeriod) {
			String msg = "Source send data too fast - receiver will drop packets. Considering changing the queue size";

			report(destination, msg, Category.UNKNOWN, Severity.MAJOR);
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

		/**
		 * Check Pattern 6
		 */
		if (featureSource.getCategory() == FeatureCategory.DATA_PORT) {
			checkComponentsPeriod(componentSource, componentDestination);
		}

		/**
		 * Check Pattern 5
		 */
		if (featureSource.getCategory() == FeatureCategory.EVENT_DATA_PORT) {
			checkQueueSizes(featureSource, featureDestination);
		}

		/**
		 * Check Pattern 4
		 */
		if ((featureSource.getCategory() == FeatureCategory.EVENT_DATA_PORT)
				|| (featureSource.getCategory() == FeatureCategory.DATA_PORT)) {
			if (featureSource.getCategory() == FeatureCategory.EVENT_DATA_PORT) {
				System.out.println("[processConnection] featureSource=" + featureSource);
				EventDataPort p = (EventDataPort) featureSource.getFeature();
				checkDataType(p.getDataFeatureClassifier());
			}

			if (featureSource.getCategory() == FeatureCategory.DATA_PORT) {
				System.out.println("[processConnection] featureSource=" + featureSource);
				DataPort p = (DataPort) featureSource.getFeature();
				checkDataType(p.getDataFeatureClassifier());

			}

		}
//		System.out.println("[processConnection] component source=" + componentSource);
//		System.out.println("[processConnection] component dest  =" + componentDestination);
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
			checkHarmonicTasks(componentInstance);
			checkNamingPolicy(componentInstance);
		}

		if (componentInstance.getCategory() == ComponentCategory.THREAD) {
			checkNamingPolicy(componentInstance);
		}

		for (FeatureInstance feature : componentInstance.getFeatureInstances()) {
			System.out.println(" -> feature: " + feature.getName());
		}

		for (ComponentInstance subcomponent : componentInstance.getComponentInstances()) {
			processComponent(subcomponent);
		}
	}

	public void report(List<NamedElement> elements, String message, Category category, Severity severity) {
		ReportItem item = new ReportItem();
		String msg = "Component share the same global variable - could be replaced by connections";
		item.setRelatedElements(elements);
		item.setCategory(Category.SCOPE);
		item.setJustification(msg);
		report.addItem(item);
		for (NamedElement ne : elements) {
			errManager.error(ne, msg);
		}
	}

	public void report(NamedElement element, String message, Category category, Severity severity) {
		ArrayList<NamedElement> els = new ArrayList<NamedElement>();
		report(els, message, category, severity);
	}

}

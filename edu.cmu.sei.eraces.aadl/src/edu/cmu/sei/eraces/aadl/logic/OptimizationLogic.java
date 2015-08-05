package edu.cmu.sei.eraces.aadl.logic;

import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.FeatureInstance;
import org.osate.aadl2.instance.SystemInstance;

public class OptimizationLogic {
	private SystemInstance systemInstance;

	public OptimizationLogic(SystemInstance si) {
		this.systemInstance = si;
	}

	public void process() {
		process(systemInstance);
	}

	public void process(ComponentInstance componentInstance) {
		System.out.println("Component: " + componentInstance.getName());
		for (FeatureInstance feature : componentInstance.getFeatureInstances()) {
			System.out.println(" -> feature: " + feature.getName());
		}

		for (ComponentInstance subcomponent : componentInstance.getComponentInstances()) {
			process(subcomponent);
		}

	}

}

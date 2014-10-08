package edu.cmu.sei.eraces.util;

import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;

public class Utils {

	public static ComponentInstance findComponentInstance(String componentName, SystemInstance system) {

		for (ComponentInstance ci : system.getAllComponentInstances()) {
			if (ci.getName().equalsIgnoreCase(componentName)) {
				return ci;
			}
		}
		return null;
	}
}

package edu.cmu.sei.eraces.aadl.util;

import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.ConnectionInstanceEnd;
import org.osate.aadl2.instance.FeatureInstance;

public class Utils {

	public static ComponentInstance getComponent(ConnectionInstanceEnd end) {
		ComponentInstance result;
		result = null;

		if (end instanceof ComponentInstance) {
			result = (ComponentInstance) end;
		}

		if (end instanceof FeatureInstance) {
			result = ((FeatureInstance) end).getContainingComponentInstance();
		}

		return result;
	}

}

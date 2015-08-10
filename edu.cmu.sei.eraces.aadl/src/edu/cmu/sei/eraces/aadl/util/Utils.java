package edu.cmu.sei.eraces.aadl.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
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

//	public static void addMarker(NamedElement ne, String message) {
//		IResource res = getResourceFromEObject(ne);
//		try {
//			IMarker marker = res.createMarker(IMarker.PROBLEM);
//			marker.setAttribute(IMarker.MESSAGE, message);
//			marker.setAttribute(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
//			marker.setAttribute(Activator.ERACES_MARKER, "true");
//		} catch (CoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("[Utils] addMarker exception");
//		}
//	}

	public static IResource getResourceFromEObject(EObject obj) {
		URI uri = obj.eResource().getURI();
		// assuming platform://resource/project/path/to/file
		String project = uri.segment(1);
		IPath path = new Path(uri.path()).removeFirstSegments(2);
		return ResourcesPlugin.getWorkspace().getRoot().getProject(project).findMember(path);
	}

}

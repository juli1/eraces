package edu.cmu.sei.eraces.deployer;

import java.util.HashMap;

import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.Device;
import org.osate.aadl2.DeviceImplementation;
import org.osate.aadl2.Element;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.ProcessImplementation;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.util.OsateDebug;

/**
 * Deploy the function over the platform. The main function is
 * generateDeployedModel which generates a new aadl package with everything in it.
 * @author julien
 *
 */
public class Deployer {

	/**
	 * Contain either a mapping from the platform to function or function
	 * to platform. Anyway, it helps to know what are the association
	 * between the platform and the functions.
	 */
	private HashMap<ComponentInstance, ComponentInstance> platformToFunction;
	private HashMap<ComponentInstance, ComponentInstance> functionToPlatform;
	private SystemInstance functionSystem;
	private SystemInstance platformSystem;

	public Deployer(SystemInstance fs, SystemInstance ps, HashMap<ComponentInstance, ComponentInstance> ptof,
			HashMap<ComponentInstance, ComponentInstance> ftop) {
		functionSystem = fs;
		platformSystem = ps;
		functionToPlatform = ftop;
		platformToFunction = ptof;
	}

	private void processElement(Element element) {
//		OsateDebug.osateDebug("Deployer", "element=" + element);

		if (element instanceof ProcessImplementation) {
			OsateDebug.osateDebug("Deployer", "process=" + element);

			ProcessImplementation process = (ProcessImplementation) element;
			for (Subcomponent threadSubcomponent : process.getAllSubcomponents()) {

				/**
				 * For now, we only consider thread inside a process.
				 */
				if (threadSubcomponent.getClassifier().getCategory() != ComponentCategory.THREAD) {
					continue;
				}

				OsateDebug.osateDebug("Deployer", "sub=" + threadSubcomponent);

				for (ComponentInstance functionInstance : functionToPlatform.keySet()) {
					ComponentInstance correspondingPlatformComponent = functionToPlatform.get(functionInstance);

					/** 
					 * We found the thread associated with a function.
					 */
					if (threadSubcomponent == correspondingPlatformComponent.getSubcomponent()) {

						process.getAllSubcomponents().remove(threadSubcomponent);
						process.getChildren().remove(threadSubcomponent);
						process.getMembers().remove(threadSubcomponent);
						threadSubcomponent.setRefined(threadSubcomponent);
						threadSubcomponent.setName("blablabla2");
//						process.eContents().remove(threadSubcomponent);
//						process.notify();
						OsateDebug.osateDebug("Deployer", "FOUND association of " + threadSubcomponent.getName()
								+ " with function " + functionInstance.getName());

					}

					OsateDebug.osateDebug("Deployer", "ci=" + functionInstance.getSubcomponent());

				}
			}
		}

		if ((element instanceof Device) || (element instanceof DeviceImplementation)) {

		}

		for (Element el : element.getChildren()) {
			processElement(el);
		}

	}

	public AadlPackage generateDeployedModel() {
		AadlPackage aadlPkgOriginal;

		aadlPkgOriginal = (AadlPackage) platformSystem.getComponentClassifier().eContainer().eContainer();
		aadlPkgOriginal.setName(aadlPkgOriginal.getName() + "::deployed");

		for (NamedElement el : aadlPkgOriginal.getPublicSection().getMembers()) {
			processElement(el);
		}

		OsateDebug.osateDebug("Deployer", "obj=" + aadlPkgOriginal);
		return aadlPkgOriginal;
	}
}

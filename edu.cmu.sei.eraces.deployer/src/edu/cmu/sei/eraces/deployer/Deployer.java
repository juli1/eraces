package edu.cmu.sei.eraces.deployer;

import java.util.ConcurrentModificationException;
import java.util.HashMap;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.ComponentCategory;
import org.osate.aadl2.ComponentType;
import org.osate.aadl2.Device;
import org.osate.aadl2.DeviceImplementation;
import org.osate.aadl2.Element;
import org.osate.aadl2.NamedElement;
import org.osate.aadl2.ProcessImplementation;
import org.osate.aadl2.Subcomponent;
import org.osate.aadl2.ThreadSubcomponent;
import org.osate.aadl2.ThreadType;
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
	private AadlPackage aadlPkgDeployed;
	private ThreadType currentThread;

	public Deployer(SystemInstance fs, SystemInstance ps, HashMap<ComponentInstance, ComponentInstance> ptof,
			HashMap<ComponentInstance, ComponentInstance> ftop) {
		functionSystem = fs;
		platformSystem = ps;
		functionToPlatform = ftop;
		platformToFunction = ptof;
	}

	private ThreadType createNewThreadClassifier(final ComponentType threadType, Classifier functionClassifier) {
		ThreadType newThreadClassifier = null;
		final EClass claz;

		claz = threadType.eClass();
//		OsateDebug.osateDebug("Deployer", "eclass=" + threadType.getContainingClassifier().getClass());

		try {
			final TransactionalEditingDomain domain = TransactionalEditingDomain.Registry.INSTANCE
					.getEditingDomain("org.osate.aadl2.ModelEditingDomain");
//			
//			final TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(systemFunction);

			domain.getCommandStack().execute(new RecordingCommand(domain) {
				public void doExecute() {
					currentThread = (ThreadType) aadlPkgDeployed.getPublicSection().createOwnedClassifier(claz);

					currentThread.setName("thread_" + functionSystem.getName());
					currentThread.createOwnedExtension().setExtended(threadType);

				}
			});
		} catch (OperationCanceledException exception) {
			OsateDebug.osateDebug("Deployer", "Cancelled");
			// if here, model did not change because it was interrupted
		}
		newThreadClassifier = currentThread;
//		newThreadClassifier = (ThreadType) aadlPkgDeployed.getPublicSection().createOwnedClassifier(claz);
//		newThreadClassifier.setName("newthreadbla");
//		

//		newThreadClassifier = Aadl2Factory.eINSTANCE.createThreadType();

//		newThreadClassifier = Aadl2Factory.eINSTANCE.createThreadType();

//		newThreadClassifier.setName("blefwefwfablabla");
//
//		newThreadClassifier.setName("thread_" + functionSystem.getName());
//		newThreadClassifier.createOwnedExtension().setExtended(threadType);
//		if (functionClassifier != null) {
//			newThreadClassifier.getAllFeatures().addAll(functionClassifier.getAllFeatures());
//		}
//
//		newThreadClassifier.getAllPropertyAssociations().addAll(threadType.getAllPropertyAssociations());

//		aadlPkgDeployed.getOwnedPublicSection().getOwnedClassifiers().add(newThreadClassifier);
//		aadlPkgDeployed.getOwnedPublicSection().getChildren().add(newThreadClassifier);

//		aadlPkgDeployed.getOwnedPublicSection().getMembers().add(newThreadClassifier);

		return newThreadClassifier;
//		return null;
	}

	private void processElement(Element element) {
//		OsateDebug.osateDebug("Deployer", "element=" + element);

		if (element instanceof ProcessImplementation) {
			OsateDebug.osateDebug("Deployer", "process=" + element);

			final ProcessImplementation process = (ProcessImplementation) element;
			for (final Subcomponent threadSubcomponent : process.getAllSubcomponents()) {

				/**
				 * For now, we only consider thread inside a process.
				 */
				if ((threadSubcomponent.getClassifier() != null)
						&& (threadSubcomponent.getClassifier().getCategory() != ComponentCategory.THREAD)) {
					continue;
				}

				OsateDebug.osateDebug("Deployer", "sub=" + threadSubcomponent);

				for (ComponentInstance functionInstance : functionToPlatform.keySet()) {
					ComponentInstance correspondingPlatformComponent = functionToPlatform.get(functionInstance);

					/** 
					 * We found the thread associated with a function.
					 */
					if (threadSubcomponent == correspondingPlatformComponent.getSubcomponent()) {
						ComponentType originalThreadType = threadSubcomponent.getComponentType();

						OsateDebug.osateDebug("Deployer", "classifier="
								+ functionInstance.getSubcomponent().getClassifier());

						final ThreadType newThreadType = createNewThreadClassifier(originalThreadType, functionInstance
								.getSubcomponent().getClassifier());

						try {
							final TransactionalEditingDomain domain = TransactionalEditingDomain.Registry.INSTANCE
									.getEditingDomain("org.osate.aadl2.ModelEditingDomain");
//							
//							final TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(systemFunction);

							domain.getCommandStack().execute(new RecordingCommand(domain) {
								public void doExecute() {

//									aadlPkgDeployed.getPublicSection().createOwnedClassifier(Thread.class);

									final ThreadSubcomponent ts = process.createOwnedThreadSubcomponent();
									ts.setThreadSubcomponentType(newThreadType);
									ts.setName("bla");
									threadSubcomponent.setRefined(ts);

								}
							});
						} catch (OperationCanceledException exception) {
							OsateDebug.osateDebug("Deployer", "Cancelled");
							// if here, model did not change because it was interrupted
						}

//						ThreadType newThreadType = createNewThreadClassifier(originalThreadType, functionInstance
//								.getSubcomponent().getClassifier());

//						aadlPkgDeployed.getPublicSection().createOwnedClassifier(Thread.class);

//						ThreadSubcomponent ts = process.createOwnedThreadSubcomponent();
//						ts.setThreadSubcomponentType(newThreadType);
//						threadSubcomponent.setRefined(ts);
//						threadSubcomponent.setName("blablabla2");
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

	public synchronized AadlPackage generateDeployedModel() {
		AadlPackage aadlPkgOriginal;
		OsateDebug.osateDebug("Deployer", "platformsystem=" + platformSystem.getComponentClassifier());
		aadlPkgOriginal = (AadlPackage) platformSystem.getComponentClassifier().eContainer().eContainer();
//		aadlPkgDeployed = EcoreUtil.copy(aadlPkgOriginal);
		aadlPkgDeployed = aadlPkgOriginal;
//		aadlPkgDeployed.setName(aadlPkgOriginal.getName() + "::deployed");
		final AadlPackage foobar = aadlPkgDeployed;
		try {
			final TransactionalEditingDomain domain = TransactionalEditingDomain.Registry.INSTANCE
					.getEditingDomain("org.osate.aadl2.ModelEditingDomain");
//			
//			final TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(systemFunction);

			domain.getCommandStack().execute(new RecordingCommand(domain) {
				public void doExecute() {
					aadlPkgDeployed.setName(foobar.getName() + "::deployed");

				}
			});
		} catch (OperationCanceledException exception) {
			OsateDebug.osateDebug("Deployer", "Cancelled");
			// if here, model did not change because it was interrupted
		}

		try {

			for (NamedElement el : aadlPkgDeployed.getPublicSection().getMembers()) {
				processElement(el);
			}

		} catch (ConcurrentModificationException cme) {
			OsateDebug.osateDebug("Deployer", "Concurrent exception");
		}

		OsateDebug.osateDebug("Deployer", "obj=" + aadlPkgDeployed);
		return aadlPkgDeployed;
	}
}

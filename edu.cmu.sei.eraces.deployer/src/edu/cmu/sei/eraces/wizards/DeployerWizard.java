package edu.cmu.sei.eraces.wizards;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.xtext.serializer.impl.Serializer;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.AadlPackage;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.util.Aadl2ResourceImpl;
import org.osate.aadl2.util.OsateDebug;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.cmu.sei.eraces.deployer.Deployer;
import edu.cmu.sei.eraces.util.Utils;

public class DeployerWizard extends Wizard implements INewWizard {

	// workbench selection when the wizard was started
	protected IStructuredSelection selection;

	// the workbench instance
	protected IWorkbench workbench;

	private DeployerWizardMainPage mainPage;
	private DeployerWizardBinder binderPage;

	private Injector injector;
	private Serializer serializer;

	public DeployerWizard() {

		super();

		injector = Guice.createInjector(new org.osate.xtext.aadl2.Aadl2RuntimeModule());
		serializer = injector.getInstance(Serializer.class);
	}

	public DeployerWizardBinder getBinderPage() {
		return this.binderPage;
	}

	public void addPages() {
		/**
		 * The main page is the one where the system 
		 * instances are selected.
		 */
		mainPage = new DeployerWizardMainPage("mainpage");
		addPage(mainPage);

		/**
		 * The binder is where the association are defined.
		 */
		binderPage = new DeployerWizardBinder("binderpage");
		addPage(binderPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
	}

	public boolean canFinish() {
		return true;
	}

	/**
	 * Get the result of the wizard and save it to a new
	 * AADL file.
	 */
	public boolean performFinish() {
		Table bindingTable;
		SystemInstance systemFunction;
		SystemInstance systemPlatform;

		/**
		 * Map each function to its corresponding platform component.
		 */
		HashMap<ComponentInstance, ComponentInstance> functionsToPlatformComponent;

		/**
		 * Opposite of the other, map the platform component to its associated function.
		 */
		HashMap<ComponentInstance, ComponentInstance> platformComponentToFunction;

		systemFunction = mainPage.getSystemFunctions();
		systemPlatform = mainPage.getSystemPlatform();

		/**
		 * The table contains the association of function/platform component. We
		 * use it to retrieve the association.
		 */
		bindingTable = binderPage.getTable();

		functionsToPlatformComponent = new HashMap<ComponentInstance, ComponentInstance>();
		platformComponentToFunction = new HashMap<ComponentInstance, ComponentInstance>();

		for (int itemIndex = 0; itemIndex < bindingTable.getItemCount(); itemIndex++) {
			TableItem item;
			String functionName;
			String platformComponentName;

			item = bindingTable.getItem(itemIndex);

			/**
			 * Each table item has two data elements associated: function and platform,
			 * each one containing the name of the selected component.
			 */
			functionName = (String) item.getData("function");
			platformComponentName = ((CCombo) item.getData("platform")).getText();

//			OsateDebug.osateDebug("DeploymentWizard", "Function Name=" + functionName);
//			OsateDebug.osateDebug("DeploymentWizard", "Platform Comp=" + platformComponentName);

			functionsToPlatformComponent.put(Utils.findComponentInstance(functionName, systemFunction),
					Utils.findComponentInstance(platformComponentName, systemPlatform));
			platformComponentToFunction.put(Utils.findComponentInstance(platformComponentName, systemPlatform),
					Utils.findComponentInstance(functionName, systemFunction));
		}

		Aadl2ResourceImpl aadlSystemPlatformResource = (Aadl2ResourceImpl) systemPlatform.eResource();

		final IFile deployedModel = ResourceUtil.getFile(aadlSystemPlatformResource).getProject()
				.getFile("deployed.aadl");
		Deployer deployer = new Deployer(systemFunction, systemPlatform, platformComponentToFunction,
				functionsToPlatformComponent);
		AadlPackage deployedPackage = deployer.generateDeployedModel();
		injector = Guice.createInjector(new org.osate.xtext.aadl2.Aadl2RuntimeModule());
		serializer = injector.getInstance(Serializer.class);
		final String textModel = serializer.serialize(deployedPackage);
//		OsateDebug.osateDebug("tostring=" + s);

		if (deployedModel.exists()) {
			try {
				deployedModel.delete(true, null);
			} catch (CoreException e) {
				OsateDebug.osateDebug("DeployerWizard", "Cannot delete old model");

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			final TransactionalEditingDomain domain = TransactionalEditingDomain.Registry.INSTANCE
					.getEditingDomain("org.osate.aadl2.ModelEditingDomain");

			domain.getCommandStack().execute(new RecordingCommand(domain) {
				public void doExecute() {
					try {
						deployedModel.create(new ByteArrayInputStream(textModel.getBytes()), IResource.NONE, null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
		} catch (OperationCanceledException exception) {
			OsateDebug.osateDebug("Deployer", "Cancelled");
			// if here, model did not change because it was interrupted
		}

		return false;
	}
}

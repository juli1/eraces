package edu.cmu.sei.eraces.wizards;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.util.Aadl2ResourceImpl;
import org.osate.aadl2.util.OsateDebug;

import edu.cmu.sei.eraces.util.Utils;

public class DeployerWizard extends Wizard implements INewWizard {

	// workbench selection when the wizard was started
	protected IStructuredSelection selection;

	// the workbench instance
	protected IWorkbench workbench;

	private DeployerWizardMainPage mainPage;
	private DeployerWizardBinder binderPage;

	public DeployerWizard() {
		super();
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

		OsateDebug.osateDebug("resource=" + aadlSystemPlatformResource);

		String systemFilePath = URI.decode(aadlSystemPlatformResource.getURI().path());
		IFile systemFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(systemFilePath));
		IPath newFilePath = systemFile.getParent().getParent().getFullPath().append("deployed.aadl");

		OsateDebug.osateDebug("path=" + newFilePath);

//		MessageDialog.openInformation(workbench.getActiveWorkbenchWindow().getShell(), "bli", "bla");
		return false;
	}
}

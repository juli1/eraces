package edu.cmu.sei.eraces.wizards;

import java.util.HashMap;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
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

		mainPage = new DeployerWizardMainPage("mainpage");
		addPage(mainPage);
		binderPage = new DeployerWizardBinder("binderpage");
		addPage(binderPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
	}

	public boolean canFinish() {
		return true;
	}

	public boolean performFinish() {
		Table bindingTable;
		SystemInstance systemFunction;
		SystemInstance systemPlatform;
		HashMap<ComponentInstance, ComponentInstance> functionsToPlatformComponent;
		HashMap<ComponentInstance, ComponentInstance> platformComponentToFunction;

		systemFunction = mainPage.getSystemFunctions();
		systemPlatform = mainPage.getSystemPlatform();

		bindingTable = binderPage.getTable();

		functionsToPlatformComponent = new HashMap<ComponentInstance, ComponentInstance>();
		platformComponentToFunction = new HashMap<ComponentInstance, ComponentInstance>();

		for (int itemIndex = 0; itemIndex < bindingTable.getItemCount(); itemIndex++) {
			TableItem item;
			String functionName;
			String platformComponentName;

			item = bindingTable.getItem(itemIndex);

			functionName = (String) item.getData("function");
			platformComponentName = ((CCombo) item.getData("platform")).getText();

			OsateDebug.osateDebug("DeploymentWizard", "Function Name=" + functionName);
			OsateDebug.osateDebug("DeploymentWizard", "Platform Comp=" + platformComponentName);

			functionsToPlatformComponent.put(Utils.findComponentInstance(functionName, systemFunction),
					Utils.findComponentInstance(platformComponentName, systemPlatform));
			platformComponentToFunction.put(Utils.findComponentInstance(platformComponentName, systemPlatform),
					Utils.findComponentInstance(functionName, systemFunction));
		}

//		MessageDialog.openInformation(workbench.getActiveWorkbenchWindow().getShell(), "bli", "bla");
		return false;
	}

}

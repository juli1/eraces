package edu.cmu.sei.eraces.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

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
//		planePage = new PlanePage("");
//		addPage(planePage);
//		carPage = new CarPage("");
//		addPage(carPage);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
	}

	public boolean canFinish() {
		return true;
	}

	public boolean performFinish() {
		MessageDialog.openInformation(workbench.getActiveWorkbenchWindow().getShell(), "bli", "bla");
		return true;
	}

}

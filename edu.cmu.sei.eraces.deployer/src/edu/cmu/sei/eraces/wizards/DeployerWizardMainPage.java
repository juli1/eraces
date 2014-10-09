package edu.cmu.sei.eraces.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.modelsupport.modeltraversal.TraverseWorkspace;
import org.osate.aadl2.modelsupport.resources.OsateResourceUtil;

public class DeployerWizardMainPage extends WizardPage implements Listener {
	private Combo functionSystem;
	private Combo platformSystem;
	private List<SystemInstance> systems;

	protected DeployerWizardMainPage(String pageName) {

		super(pageName);

		Iterator<IFile> iteratorFile;
		HashSet<IFile> instanceFiles;

		setTitle("Select Models");
		setDescription("Select the functional and platform models");

		systems = new ArrayList<SystemInstance>();

		/**
		 * Get all instances files in the workspace.
		 */
		instanceFiles = TraverseWorkspace.getInstanceModelFilesInWorkspace();
		iteratorFile = instanceFiles.iterator();

		while (iteratorFile.hasNext()) {
			IFile file;
			IResource ires;
			file = iteratorFile.next();
			Resource res;
			EObject target;

			ires = file;
			res = OsateResourceUtil.getResource(ires);
			target = res.getContents().get(0);

			if (target instanceof SystemInstance) {
//				OsateDebug.osateDebug("system=" + target);
				systems.add((SystemInstance) target);
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		String[] listSystems;
		Composite composite;
		GridData gd;
		GridLayout gl;

		composite = new Composite(parent, SWT.NULL);
		gl = new GridLayout();

		listSystems = new String[systems.size()];

		for (int i = 0; i < systems.size(); i++) {
			listSystems[i] = systems.get(i).getName();
		}

		gl.numColumns = 2;
		gl.makeColumnsEqualWidth = false;
		composite.setLayout(gl);

		new Label(composite, SWT.NONE).setText("System Functions:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		functionSystem = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		functionSystem.setLayoutData(gd);
		functionSystem.setItems(listSystems);

		new Label(composite, SWT.NONE).setText("Platform Functions:");
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		platformSystem = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		platformSystem.setLayoutData(gd);
		platformSystem.setItems(listSystems);

		setControl(composite);
	}

	@Override
	public void handleEvent(Event event) {

	}

	/**
	 * Retrieve the system instance that was selected
	 * for all the function components.
	 * @return
	 */
	public SystemInstance getSystemFunctions() {
		for (SystemInstance si : systems) {
			if (si.getName().equalsIgnoreCase(functionSystem.getText())) {
				return si;
			}
		}
		return null;
	}

	/**
	 * Retrieve the system instance that was selected for all the
	 * platform components.
	 * @return
	 */
	public SystemInstance getSystemPlatform() {
		for (SystemInstance si : systems) {
			if (si.getName().equalsIgnoreCase(platformSystem.getText())) {
				return si;
			}
		}
		return null;
	}

	/**
	 * Prepare the binder page with the selected system instances
	 * for the functions and platform components.
	 */
	public IWizardPage getNextPage() {
		DeployerWizardBinder dwb;

		dwb = ((DeployerWizard) getWizard()).getBinderPage();
		dwb.setSystemPlatform(getSystemPlatform());
		dwb.setSystemFunctions(getSystemFunctions());
		dwb.loadTable();
		return dwb;

	}

}

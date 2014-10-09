package edu.cmu.sei.eraces.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;

public class DeployerWizardBinder extends WizardPage implements Listener {
	private Combo functionSystem;
	private Combo platformSystem;
	private SystemInstance systemFunctions;
	private SystemInstance systemPlatform;
	private Table table;

	protected DeployerWizardBinder(String pageName) {
		super(pageName);
		setTitle("Select Models");
		setDescription("Select the functional and platform models");
	}

	public void setSystemFunctions(SystemInstance si) {
		this.systemFunctions = si;
	}

	public void setSystemPlatform(SystemInstance si) {
		this.systemPlatform = si;
	}

	public Table getTable() {
		return this.table;
	}

	@Override
	public void createControl(Composite parent) {
		GridData gd;
		Composite composite;
		GridLayout gl;

		String[] vals = { "blia" };
		composite = new Composite(parent, SWT.NULL);
		gl = new GridLayout();

		gl.numColumns = 1;
		gl.makeColumnsEqualWidth = false;
		composite.setLayout(gl);

		table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setHeaderVisible(true);
		String[] titles = { "Function", "Platform Component" };

		for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
			TableColumn column = new TableColumn(table, SWT.NULL);
			column.setText(titles[loopIndex]);
		}

		for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
			table.getColumn(loopIndex).pack();
		}

		setControl(composite);
	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub

	}

	/**
	 * Load Table fills the table with the content
	 * of the system and platform system instances.
	 */
	public void loadTable() {
		List<String> platformComponents;

		if (this.systemFunctions == null) {
			return;
		}

		if (this.systemPlatform == null) {
			return;
		}

		platformComponents = new ArrayList<String>();

		for (ComponentInstance ci : this.systemPlatform.getAllComponentInstances()) {
			/* Just remove the main system instance */
			if (ci instanceof SystemInstance) {
				continue;
			}
			platformComponents.add(ci.getName());
		}

		for (ComponentInstance ci : this.systemFunctions.getAllComponentInstances()) {

			/* Just remove the main system instance */
			if (ci instanceof SystemInstance) {
				continue;
			}

			TableItem item = new TableItem(table, SWT.NULL);

			TableEditor editor;
			editor = new TableEditor(table);
			Text text = new Text(table, SWT.NONE);
			text.setText(ci.getName());
			editor.grabHorizontal = true;
			editor.setEditor(text, item, 0);
			/**
			 * Associate the function name with the key
			 * "function" in the TableItem. It will then
			 * help to get the function name later on.
			 */
			item.setData("function", ci.getName());

			editor = new TableEditor(table);
			CCombo combo = new CCombo(table, SWT.NONE);

			/**
			 * We create a combo that contains
			 * all the platform components.
			 */
			for (String s : platformComponents) {
				combo.add(s);
			}

			editor.grabHorizontal = true;
			editor.setEditor(combo, item, 1);

			/**
			 * Associate the platform component name with the key
			 * "platform" in the TableItem. It will then
			 * help to get the platform component name later on.
			 */
			item.setData("platform", combo);

			table.setBounds(25, 25, 220, 200);
		}
	}

}

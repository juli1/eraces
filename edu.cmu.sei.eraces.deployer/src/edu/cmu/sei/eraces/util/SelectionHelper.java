package edu.cmu.sei.eraces.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.xtext.resource.EObjectAtOffsetHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.osate.aadl2.Classifier;
import org.osate.aadl2.Element;
import org.osate.aadl2.SystemImplementation;

public class SelectionHelper {
	private static EObjectAtOffsetHelper eObjectAtOffsetHelper = new EObjectAtOffsetHelper();

	public static ISelection getSelection() {
		IWorkbench wb;
		IWorkbenchPart part;
		IWorkbenchPage page;
		IWorkbenchWindow win;
		IEditorPart activeEditor;
		ISelection selection;

		wb = PlatformUI.getWorkbench();
		win = wb.getActiveWorkbenchWindow();
		page = win.getActivePage();
		part = page.getActivePart();
		activeEditor = page.getActiveEditor();

		if (activeEditor == null) {
			throw new RuntimeException("No active editor");
		}

		if (part instanceof ContentOutline) {
			selection = ((ContentOutline) part).getSelection();
		} else {
			selection = getXtextEditor().getSelectionProvider().getSelection();
		}

		return selection;
	}

	public static EObject getEObjectFromSelection(final ISelection selection) {
		return getXtextEditor().getDocument().readOnly(new IUnitOfWork<EObject, XtextResource>() {
			public EObject exec(XtextResource resource) throws Exception {
				EObject targetElement = null;
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection) selection;
					Object eon = ss.getFirstElement();
					if (eon instanceof EObjectNode) {
						targetElement = ((EObjectNode) eon).getEObject(resource);
					}
				} else {
					targetElement = eObjectAtOffsetHelper.resolveElementAt(resource,
							((ITextSelection) selection).getOffset());
				}

				return targetElement;
			}
		});
	}

	public static SystemImplementation getSelectedSystemImplementation() {
		return getSelectedSystemImplementation(getSelection());
	}

	public static SystemImplementation getSelectedSystemImplementation(ISelection selection) {
		EObject selectedObject;

		selectedObject = getEObjectFromSelection(selection);

		if (selectedObject instanceof SystemImplementation) {
			return (SystemImplementation) selectedObject;
		}

		if (selectedObject instanceof Element) {
			Element aadlObject = (Element) selectedObject;
			Classifier containingClassifier = aadlObject.getContainingClassifier();
			if (containingClassifier instanceof SystemImplementation) {
				return (SystemImplementation) containingClassifier;
			}
		}

		return null;
	}

	public static XtextEditor getXtextEditor() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IEditorPart activeEditor = page.getActiveEditor();
		if (activeEditor == null) {
			throw new RuntimeException("Unexpected case. Unable to get active editor");
		}

		XtextEditor xtextEditor = (XtextEditor) activeEditor.getAdapter(XtextEditor.class);
		if (xtextEditor == null) {
			throw new RuntimeException("Unexpected case. Unable to get Xtext editor");
		}

		return xtextEditor;
	}
}

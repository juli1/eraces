package edu.cmu.sei.eraces.aadl;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initialize defaults value for preferences of the plug-in
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(PreferenceConstants.ERACES_PREF_GLOBVAR, true);
		store.setDefault(PreferenceConstants.ERACES_PREF_SUBPROGRAMS, true);

	}

}

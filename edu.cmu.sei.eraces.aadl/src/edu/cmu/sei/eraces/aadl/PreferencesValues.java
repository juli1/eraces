package edu.cmu.sei.eraces.aadl;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Accessors for preference value
 * @author hugues
 *
 */
public class PreferencesValues {

	public static boolean getPrefGlobVar() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return (store.getBoolean(PreferenceConstants.ERACES_PREF_GLOBVAR));
	}

	public static boolean getPrefSubprogram() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return (store.getBoolean(PreferenceConstants.ERACES_PREF_GLOBVAR));
	}

}

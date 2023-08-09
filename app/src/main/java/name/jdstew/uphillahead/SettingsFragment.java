/**
 * Copyright 2023 Jeffrey D. Stewart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package name.jdstew.uphillahead;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import java.text.NumberFormat;

/**
 * The SettingsFragment class is a container for the Preferences items within the Settings Activity.
 *
 * @since 1.0
 * @author Jeff Stewart, jeffrey.d.stew@gmail.com
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SeekBarPreference exaggerationSbp = findPreference(getString(R.string.exaggeration_pref_key));
        exaggerationSbp.setSummary("x" + exaggerationSbp.getValue());
        exaggerationSbp.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary("x" + newValue);
            return true; // because we can't change the value of the preference to a float
        });

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumIntegerDigits(1);

        SeekBarPreference paceSbp = findPreference(getString(R.string.pace_pref_key));
        paceSbp.setSummary(nf.format(paceSbp.getValue() * Config.PACE_PREFS_MULTIPLIER));
        paceSbp.setOnPreferenceChangeListener((preference, newValue) -> {
            final float paceVal = Float.parseFloat(String.valueOf(newValue));
            preference.setSummary(nf.format(paceVal / 10.0f));
            return true; // true indicates that the preferences of this slider should be updated
        });
    }
}
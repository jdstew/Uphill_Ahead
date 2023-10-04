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
        exaggerationSbp.setSummary("vertical change " + exaggerationSbp.getValue() + " times horizontal");
        exaggerationSbp.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary("vertical change " + newValue + " times horizontal");

            return true; // because we can't change the value of the preference to a float
        });

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setMinimumIntegerDigits(1);

        SeekBarPreference paceSbp = findPreference(getString(R.string.pace_pref_key));
        float paceMultiplier = paceSbp.getValue() * Config.PACE_PREFS_MULTIPLIER;
        double paceKmHr = Calcs.getPaceAtSlope(0.0) * paceMultiplier * 1_000.0;
        String paceDisplayedKmHr = Calcs.getDisplayedDist(paceKmHr, Config.SYSTEM_METRIC);
        String paceDisplayedMiHr = Calcs.getDisplayedDist(paceKmHr, Config.SYSTEM_IMPERIAL);
        paceSbp.setSummary(nf.format(paceMultiplier) + "(" + paceDisplayedMiHr + " or " + paceDisplayedKmHr + " per hr)");

        paceSbp.setOnPreferenceChangeListener((preference, newValue) -> {
            float paceVal = Float.parseFloat(String.valueOf(newValue)) * Config.PACE_PREFS_MULTIPLIER;
            double pace = Calcs.getPaceAtSlope(0.0) * paceVal * 1_000.0;
            String paceKPH = Calcs.getDisplayedDist(pace, Config.SYSTEM_METRIC);
            String paceMPH = Calcs.getDisplayedDist(pace, Config.SYSTEM_IMPERIAL);
            preference.setSummary(nf.format(paceVal) + "(" + paceKPH + " or " + paceMPH + " per hr)");
            return true; // true indicates that the preferences of this slider should be updated
        });
    }
}
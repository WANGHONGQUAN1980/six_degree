package whq.projects.sd.profile.dialog

import androidx.preference.PreferenceDialogFragmentCompat
import whq.projects.sd.profile.*
import whq.projects.sd.profile.preference.DateSelectPreference
import whq.projects.sd.profile.preference.LocationSelectPreference
import whq.projects.sd.profile.preference.RelationFavouritePreference
import whq.projects.sd.profile.preference.SettingPreference

abstract class PreferenceDialog : PreferenceDialogFragmentCompat() {
    abstract fun getNewValue(): String
    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            if (preference is DateSelectPreference || preference is LocationSelectPreference || preference is RelationFavouritePreference) {
                val settingPreferenceFragment = this.targetFragment as? SettingPreference
                val newValue = getNewValue()
                settingPreferenceFragment?.save(preference, newValue)
                //preference.summary = newValue
            }
        }
    }
}
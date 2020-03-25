package whq.projects.sd.profile

import androidx.preference.PreferenceDialogFragmentCompat
import whq.projects.sd.profile.preference.SettingPreference

fun PreferenceDialogFragmentCompat.oldValue(): List<String>? {
    val settingPreferenceFragment = this.targetFragment as? SettingPreference
    return settingPreferenceFragment?.get(preference.key)?.split("/")
}

fun PreferenceDialogFragmentCompat.saveValue(newValue: String) {
    val settingPreferenceFragment = this.targetFragment as? SettingPreference
    settingPreferenceFragment?.save(preference, newValue)
    updateSummary(preference.key)
}

fun PreferenceDialogFragmentCompat.saveValue(key:String,newValue: String) {
    val settingPreferenceFragment = this.targetFragment as? SettingPreference
    settingPreferenceFragment?.save(key, newValue)
    updateSummary(key)
}

fun PreferenceDialogFragmentCompat.updateSummary(key: String?) {
    val settingPreferenceFragment = this.targetFragment as? SettingPreference
    settingPreferenceFragment?.updateSummary(key!!)
}


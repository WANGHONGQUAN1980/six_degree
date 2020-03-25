package whq.projects.sd.profile

import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import whq.projects.entities.PROFILE_KEYS
import whq.projects.entities.PROFILE_KEYS.*
import whq.projects.sd.profile.dialog.DatePreferenceDialog
import whq.projects.sd.profile.dialog.EditTextPromptYearPreferenceDialog
import whq.projects.sd.profile.dialog.LocationPreferenceDialog
import whq.projects.sd.profile.dialog.RelationFavouritePreferenceDialog
import whq.projects.sd.profile.preference.*

fun SettingPreference.save(preference: Preference, value: String) {
    preferenceManager.sharedPreferences.edit().putString(preference.key, value).apply()
}

fun SettingPreference.save(preferenceKey: String, value: String) {
    preferenceManager.sharedPreferences.edit().putString(preferenceKey, value).apply()
}

fun SettingPreference.get(key: String): String? {
    return preferenceManager.sharedPreferences.getString(key, null)
}

fun SettingPreference.getYears(profile_key: PROFILE_KEYS): List<String> {
    val ruxueMap = mapOf(
        you_er_yuan_school to (3 to 6),
        small_school to (6 to 9),
        middle_school to (10 to 15),
        high_school to (13 to 18),
        under_graduate_school to (16 to 21),
        post_graduate_school to (19 to 24),
        doctoral_school to (21 to 26),
        work_ever to (18 to 60),
        work_current to (18 to 60)
    )
    val range: Pair<Int, Int> = ruxueMap[profile_key] ?: error("getYears ${profile_key}")
    val pair: Pair<Int, Int> = get(born_date.name)?.run {
        val year = split('/', ignoreCase = true, limit = 3).first().toInt()
        range.first + year to range.second + year
    } ?: (range.first + 1950 to range.second + 2013)
    return pair.first.rangeTo(pair.second).toList().map { it.toString() }
}

fun SettingPreference.updateSummary(key: String) {
    findPreference(key)?.also {
        if (key == gender.toString()) {
            it.summary = if (preferenceManager.sharedPreferences.getBoolean(key, true)) "我是男生" else "我是女生"
        } else if (key == message_need_invite.toString()) {
            it.summary = if (preferenceManager.sharedPreferences.getBoolean(key, false)) "别人加好友需要邀请" else "别人加好友不需要邀请"
        } else {
            it.summary = preferenceManager.sharedPreferences.getString(key, "未设置")
        }
    }
}

fun SettingPreference.newInstance(preference: Preference): DialogFragment {
    val dialogFragmentCompat = when (preference) {
        is DateSelectPreference -> DatePreferenceDialog()
        is RelationFavouritePreference -> RelationFavouritePreferenceDialog()
        is LocationSelectPreference -> LocationPreferenceDialog()
        is EditTextPromptYearPreference -> EditTextPromptYearPreferenceDialog()
        else -> throw Exception("PreferenceDialog newInstance")
    }
    dialogFragmentCompat.arguments = bundleOf("key" to preference.key)
    dialogFragmentCompat.setTargetFragment(this, 0)
    return dialogFragmentCompat
}
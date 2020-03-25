package whq.projects.sd.profile.preference

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.AndroidViewModel
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.reactivex.Completable
import whq.projects.entities.*
import whq.projects.entities.PROFILE_KEYS.*
import whq.projects.sd.R
import whq.projects.sd.entities.ProfileLocation
import whq.projects.sd.profile.newInstance
import whq.projects.sd.profile.updateSummary
import whq.projects.utilities.*
import whq.projects.utilities.User.currentUser
import whq.projects.utilities.rxjava.io_io
import whq.projects.utilities.rxjava.w_io

class SettingPreferenceVM : AndroidViewModel(AppCommon.instance) {
    var changedSetting: Boolean = false
    var changedConfig: Boolean = false
    var profile: Profile = currentUser.value!!.toProfile().copy(location = ProfileLocation(), school = ProfileSchool(), work = ProfileWork())
    var userConfig: UserConfig = UserConfig(message_need_invite = false)
}

class SettingPreference : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        this.updateSummary(key!!)
        val viewModel = viewModel(SettingPreferenceVM::class.java)
        val preferences = preferenceManager.sharedPreferences
        val profileKey = kotlin.runCatching { valueOf(key) }.getOrNull()
        fun SharedPreferences.sv() = getString(key, null)
        fun uy(): UnitYear {
            val (name, year) = preferences.sv().split("/")
            return UnitYear(name = name, year = year.toInt())
        }
        viewModel.run {
            profile.run {
                preferences.run {
                    profileKey?.run {
                        if (this == message_need_invite || this == relation_favourite) changedConfig = true else changedSetting = true
                        when (this) {
                            gender -> {
                                val v = getBoolean(key, false)
                                basic.gender = GenderType.normalize(v)
                            }
                            message_need_invite -> userConfig = userConfig.copy(message_need_invite = getBoolean(key, false))
                            relation_favourite -> {
                                val (qinshu, tongshi, pengyou, tongxue) = sv().split("/").map { it.toInt() }
                                userConfig = userConfig.copy(qinshu_score = qinshu, tongshi_score = tongshi, pengyou_score = pengyou, tongxue_score = tongxue)
                            }
                            nick_name -> basic.nickName = sv()
                            born_date -> basic.born_date = sv()
                            zhu_zhi -> location!!.zhu_zhi = sv()
                            ji_guan -> location!!.ji_guan = sv()
                            PROFILE_KEYS.work -> location!!.work = sv()
                            jiu_du -> location!!.jiu_du = sv()
                            you_er_yuan_school -> school!!.you_er_yuan_school = uy()
                            small_school -> school!!.small_school = uy()
                            middle_school -> school!!.middle_school = uy()
                            high_school -> school!!.high_school = uy()
                            under_graduate_school -> school!!.under_graduate_school = uy()
                            post_graduate_school -> school!!.post_graduate_school = uy()
                            doctoral_school -> school!!.doctoral_school = uy()
                            work_current -> work!!.work_current = uy()
                            work_ever -> work!!.work_ever = uy()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doAfterLogged(ChatActivity.SETTING_PREFERENCE) {
            preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        }
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.settings)
        updateAll()
    }

    private fun updateAll() {
        enumValues<PROFILE_KEYS>().map { it.name }.forEach { k -> this.updateSummary(k) }
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        saveToServer()
    }

    @SuppressLint("CheckResult")
    private fun saveToServer() {
        viewModel(SettingPreferenceVM::class.java).run {
            Completable.concat(
                mapOf(
                    WhqService.serviceInstance().saveContactInfo(profile) to changedSetting,
                    WhqService.serviceInstance().saveConfig(userConfig, currentUser.value!!.mobileMd5) to changedConfig
                ).filter { it.value }.keys
            ).w_io(
                this@SettingPreference,
                promptBeforeExecute = R.string.save_success,
                success = {
                    changedSetting = false
                    changedConfig = false
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        try {
            if (preference is ChineseDialogPreference) {
                Log.d("Setting", "${preference.key} is ChineseDialogPreference")
                val dialogFragment = parentFragmentManager.findFragmentByTag(preference.key) as? DialogFragment
                if (dialogFragment != null) {
                    Log.d("Setting", "${preference.key} is exist")
                    parentFragmentManager.beginTransaction().remove(dialogFragment).commit()
                }
                parentFragmentManager.run {
                    val newInstance = newInstance(preference)
                    Log.d("Setting", "${preference.key} is showing")
                    newInstance.show(this, preference.key)
                }
            }
        } catch (e: Exception) {
            Log.d("Settings", "onDisplayPreferenceDialog ${preference?.key}")
            e.printStackTrace()
        }
    }
}


object GenderType {
    fun normalize(v: Boolean) = normalize(v.toString())
    fun normalize(v: String) = if (setOf("true", "男").contains(v.toLowerCase())) "MALE" else "FEMALE"
}
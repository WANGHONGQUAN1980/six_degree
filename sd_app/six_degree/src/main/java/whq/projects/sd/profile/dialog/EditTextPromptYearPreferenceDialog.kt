package whq.projects.sd.profile.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.preference.PreferenceDialogFragmentCompat
import cn.forward.androids.views.StringScrollPicker
import whq.projects.entities.PROFILE_KEYS
import whq.projects.entities.PROFILE_KEYS.*
import whq.projects.sd.R
import whq.projects.sd.profile.preference.SettingPreference
import whq.projects.sd.profile.getYears
import whq.projects.sd.profile.oldValue
import whq.projects.sd.profile.preference.EditTextPromptYearPreference
import whq.projects.sd.profile.saveValue
import whq.projects.utilities.WhqService
import whq.projects.utilities.adapters.ArrayAdapterMiddle
import whq.projects.utilities.rxjava.w
import whq.projects.utilities.w


@Suppress("UNCHECKED_CAST")
class EditTextPromptYearPreferenceDialog : PreferenceDialogFragmentCompat() {
    private var autoCompleteTextView: AppCompatAutoCompleteTextView? = null
    private var year: AppCompatTextView? = null
    private var years: StringScrollPicker? = null
    private var searchPrompt: AppCompatTextView? = null

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val preference = preference
            if (preference is EditTextPromptYearPreference) {
                val text = this.autoCompleteTextView?.text?.toString()
                val selectedYear = this.years?.selectedItem?.toString()
                if (text != null && !TextUtils.isEmpty(text) && selectedYear != null) {
                    this.saveValue("${text.replace("/", "")}/$selectedYear")
                }
            }
        }
    }

    override fun onBindDialogView(view: View?) {
        if (preference is EditTextPromptYearPreference) {
            this.searchPrompt = view?.findViewById(R.id.searchPrompt)
            this.searchPrompt?.setOnClickListener {
                val keyword = this.autoCompleteTextView?.text?.toString()!!
                if (keyword.length >= 2) when (val level = enumValueOf<PROFILE_KEYS>(preference.key)) {
                    you_er_yuan_school,
                    small_school,
                    middle_school,
                    high_school,
                    under_graduate_school,
                    post_graduate_school,
                    doctoral_school -> {
                        WhqService.serviceInstance().getSchoolNames(
                            level.toString(),
                            keyword
                        )
                            .w(
                                this@EditTextPromptYearPreferenceDialog,
                                promptBeforeExecute = R.string.in_reading,
                                success = {
                                    it.body()?.run {
                                        autoCompleteTextView?.also {
                                            it.setAdapter(ArrayAdapterMiddle(context!!, this))
                                            it.showDropDown()
                                        }
                                    }
                                }
                            )
                    }
                    work_current,
                    work_ever -> WhqService.serviceInstance().getWorkUnits(keyword)
                        .w(
                            this@EditTextPromptYearPreferenceDialog,
                            promptBeforeExecute = R.string.in_reading,
                            success = {
                                it.body()?.run {
                                    autoCompleteTextView?.also {
                                        it.setAdapter(ArrayAdapterMiddle(context!!, this))
                                        it.showDropDown()
                                    }
                                }
                            }
                        )
                }
            }
            val settingPreferenceFragment = this.targetFragment as? SettingPreference
            this.autoCompleteTextView = view?.findViewById(R.id.autoCompleteTextView)
            this.year = view?.findViewById(R.id.year)
            this.years = view?.findViewById(R.id.years)
            autoCompleteTextView?.threshold = 0
            if (preference.key.endsWith("school")) {
                year?.text = "入学年份"
            } else if (preference.key == PROFILE_KEYS.work_current.toString() || preference.key == PROFILE_KEYS.work_ever.toString())
                year?.text = "入职年份"
            this.years?.data = settingPreferenceFragment?.getYears(enumValueOf(preference.key))
            oldValue()?.get(0).also { this.autoCompleteTextView?.setText(it) }
        } else {
            super.onBindDialogView(view)
        }
    }
}
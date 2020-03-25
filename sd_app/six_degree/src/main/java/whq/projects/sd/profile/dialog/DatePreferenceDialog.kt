package whq.projects.sd.profile.dialog

import android.view.View
import cn.forward.androids.views.StringScrollPicker
import whq.projects.sd.R
import whq.projects.sd.profile.preference.DateSelectPreference
import whq.projects.sd.profile.preference.SettingPreference
import whq.projects.sd.profile.get
import whq.projects.sd.profile.save
import java.util.*

class DatePreferenceDialog : PreferenceDialog() {
    private var month_data: List<String> = 1.rangeTo(12).map { it.toString() }
    private var year_data: List<String> = 1950.rangeTo(2050).map { it.toString() }
    private lateinit var view_third: StringScrollPicker
    private lateinit var view_second: StringScrollPicker
    private lateinit var view_first: StringScrollPicker
    override fun getNewValue(): String {
        return arrayOf(this.view_first.selectedItem, this.view_second.selectedItem, this.view_third.selectedItem)
            .joinToString("/")
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            if (preference is DateSelectPreference) {
                val newValue =
                    arrayOf(this.view_first.selectedItem, this.view_second.selectedItem, this.view_third.selectedItem)
                        .joinToString("/")
                val settingPreferenceFragment = this.targetFragment as? SettingPreference
                settingPreferenceFragment?.save(preference, newValue)
                preference.summary = newValue
            }
        }
    }

    override fun onBindDialogView(view: View?) {
        if (preference is DateSelectPreference) {
            val settingPreferenceFragment = this.targetFragment as? SettingPreference
            val old_values: List<String>? = settingPreferenceFragment?.get(preference.key)?.split("/")
            this.view_first = view?.findViewById(R.id.view_first)!!
            this.view_second = view.findViewById(R.id.view_second)!!
            this.view_third = view.findViewById(R.id.view_third)!!
            fun udpateDates() {
                val selected_month = month_data[view_second.selectedPosition].toInt()
                val instance = Calendar.getInstance()
                instance.set(Calendar.YEAR, year_data[view_first.selectedPosition].toInt())
                instance.set(Calendar.MONTH, selected_month - 1)
                val max_day = instance.getActualMaximum(Calendar.DAY_OF_MONTH)
                val date_data = 1.rangeTo(max_day).map { it.toString() }
                view_third.data = date_data
                view_third.selectedPosition = date_data.indexOf(old_values?.get(2) ?: "15")
            }
            view_second.setOnSelectedListener { _, _ ->
                udpateDates()
            }
            view_first.setOnSelectedListener { _, _ ->
                udpateDates()
            }
            view_first.data = year_data
            view_first.selectedPosition = year_data.indexOf(old_values?.get(0) ?: "1980")
            view_second.data = month_data
            view_second.selectedPosition = month_data.indexOf(old_values?.get(1) ?: "5")
        } else {
            super.onBindDialogView(view)
        }
    }
}
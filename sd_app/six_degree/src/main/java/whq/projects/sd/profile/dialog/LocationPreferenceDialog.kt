package whq.projects.sd.profile.dialog

import android.view.View
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import whq.projects.sd.profile.preference.LocationSelectPreference
import whq.projects.utilities.getAdministrativeData
import whq.projects.utilities.locationPrompt

class LocationPreferenceDialog : PreferenceDialog() {
    var tv: AppCompatAutoCompleteTextView? = null
    override fun getNewValue(): String {
        return tv?.text?.toString() ?: ""
    }

    override fun onBindDialogView(view: View?) {
        if (preference is LocationSelectPreference) {
            (view as? AppCompatAutoCompleteTextView)?.run {
                tv = this
                locationPrompt(getAdministrativeData(requireContext())) {}
            }
        } else {
            super.onBindDialogView(view)
        }
    }
}
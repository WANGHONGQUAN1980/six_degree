package whq.projects.sd.profile.dialog

import android.view.View
import androidx.appcompat.widget.AppCompatSeekBar
import whq.projects.sd.R
import whq.projects.sd.profile.preference.SettingPreference
import whq.projects.sd.profile.get
import whq.projects.sd.profile.preference.RelationFavouritePreference

class RelationFavouritePreferenceDialog : PreferenceDialog() {
    private lateinit var tongxue_view: AppCompatSeekBar
    private lateinit var pengyou_view: AppCompatSeekBar
    private lateinit var tongshi_view: AppCompatSeekBar
    private lateinit var qinshu_view: AppCompatSeekBar

    override fun getNewValue(): String = listOf(qinshu_view, tongshi_view, pengyou_view, tongxue_view).map { it.progress }.joinToString("/")

    override fun onBindDialogView(view: View?) {
        if (preference is RelationFavouritePreference) {
            val settingPreferenceFragment = this.targetFragment as? SettingPreference
            val (qinshu_v: Int, tongshi_v: Int, pengyou_v: Int, tongxue_v: Int) = settingPreferenceFragment?.get(preference.key)?.split("/")?.toList()?.map { it.toInt() } ?: listOf(10, 9, 8, 7)
            view?.findViewById<AppCompatSeekBar>(R.id.qinshu_value)?.run {
                this@RelationFavouritePreferenceDialog.qinshu_view = this
                this.progress = qinshu_v
            }
            view?.findViewById<AppCompatSeekBar>(R.id.tongshi_value)?.run {
                this@RelationFavouritePreferenceDialog.tongshi_view = this
                this.progress = tongshi_v
            }
            view?.findViewById<AppCompatSeekBar>(R.id.pengyou_value)?.run {
                this@RelationFavouritePreferenceDialog.pengyou_view = this
                this.progress = pengyou_v
            }
            view?.findViewById<AppCompatSeekBar>(R.id.tongxue_value)?.run {
                this@RelationFavouritePreferenceDialog.tongxue_view = this
                this.progress = tongxue_v
            }
        } else {
            super.onBindDialogView(view)
        }
    }
}
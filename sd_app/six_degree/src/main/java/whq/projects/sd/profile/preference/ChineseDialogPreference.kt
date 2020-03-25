package whq.projects.sd.profile.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference

abstract class ChineseDialogPreference(val res_id: Int, context: Context?, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {
    override fun getDialogLayoutResource(): Int {
        return res_id
    }

    override fun getNegativeButtonText(): CharSequence {
        return "取消"
    }

    override fun getPositiveButtonText(): CharSequence {
        return "确定"
    }
}
package whq.projects.sd.bindingConversions

import android.view.View
import androidx.databinding.BindingConversion

@BindingConversion
fun bool2Int(value: Boolean): Int = if (value) View.VISIBLE else View.GONE

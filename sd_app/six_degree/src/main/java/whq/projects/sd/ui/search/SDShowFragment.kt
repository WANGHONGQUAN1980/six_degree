package whq.projects.sd.ui.search

import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.fragment_show_one_person.*
import whq.projects.chat.callbacks.SDContactItFragmentCallback
import whq.projects.entities.SdType
import whq.projects.sd.R
import whq.projects.sd.vm.SdFragmentVM
import whq.projects.utilities.BaseFragment
import whq.projects.utilities.toggle
import whq.projects.utilities.viewModel

class SDShowFragment : BaseFragment(R.layout.sd_show_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.menuProcess { drawerLayout.toggle(GravityCompat.END) }
        super.onViewCreated(view, savedInstanceState)
        val model = viewModel(SdFragmentVM::class.java)
        SdFragment(this, model, view, SdType.FAV_ED).bind(SDContactItFragmentCallback.callback(requireActivity()))
    }
}
package whq.projects.sd.ui.search


import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.fragment_add_relations.*
import kotlinx.android.synthetic.main.zupu_toolbar.*
import whq.projects.chat.callbacks.SDContactItFragmentCallback
import whq.projects.entities.SdType
import whq.projects.sd.R
import whq.projects.sd.vm.ZupuFragmentVM
import whq.projects.utilities.*
import whq.projects.utilities.User.currentUser
import whq.projects.utilities.rxjava.w
import java.util.concurrent.TimeUnit

class ZupuFragment : BaseFragment(R.layout.fragment_zupu) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        doAfterLogged(ChatActivity.ZUPU_FRAGMENT) {
            menuProcess { drawerLayout.toggle(GravityCompat.END) }
            val model = viewModel(ZupuFragmentVM::class.java)
            SdFragment(this, model, view, SdType.ZUPU).bind(SDContactItFragmentCallback.callback(requireActivity()))
            model.page_loaded.w(
                this,
                action = {
                    show.performClick()
                })
            model.degree.observe(viewLifecycleOwner, Observer {
                show.text = "${it}度搜索"
            })
            max_degree.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    model.degree.value = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
            RxView.clicks(show)
                .throttleFirst(10, TimeUnit.SECONDS)
                .subscribe {
                    WhqService.serviceInstance().getZupu(currentUser.value!!.mobileMd5, degree = max_degree.progress)
                        .w(
                            this,
                            promptBeforeExecute = R.string.in_calculation,
                            success = {
                                model.currentRelationsNeo.value = it.body()
                            }
                        )
                }
        }
    }
}

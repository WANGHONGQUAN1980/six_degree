package whq.projects.sd.ui.search


import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.core.view.GravityCompat
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.fragment_add_relations.*
import kotlinx.android.synthetic.main.show_person_toolbar.*
import whq.projects.chat.callbacks.SDContactItFragmentCallback
import whq.projects.entities.MOBILEMD5_NAME
import whq.projects.entities.SdRelationType
import whq.projects.entities.SdType
import whq.projects.sd.R
import whq.projects.sd.vm.ShowOnePersonFragmentVM
import whq.projects.utilities.*
import whq.projects.utilities.User.currentUser
import whq.projects.utilities.adapters.ArrayAdapterMiddle
import whq.projects.utilities.rxjava.w
import java.util.concurrent.TimeUnit

class ShowOnePersonFragment : BaseFragment(R.layout.fragment_show_one_person) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doAfterLogged(ChatActivity.SHOW_ONE_PERSON) {
            menuProcess { drawerLayout.toggle(GravityCompat.END) }
            val model = viewModel(ShowOnePersonFragmentVM::class.java)
            SdFragment(this, model, view, SdType.ONE).bind(SDContactItFragmentCallback.callback(requireActivity()))
            fun getNetwork() {
                val mobileMd5: String = model.for_who.value?.mobileMd5 ?: currentUser.value!!.mobileMd5
                val map = mapOf(
                    SdRelationType.tongxue to tongxue.isChecked,
                    SdRelationType.tongshi to tongshi.isChecked,
                    SdRelationType.qinshu to qinshu.isChecked,
                    SdRelationType.pengyou to pengyou.isChecked,
                    SdRelationType.locateat to locateat.isChecked
                )
                val groups: Set<SdRelationType> = map.filter { it.value }.keys
                WhqService.serviceInstance().somebodyRelations(mobileMd5, (if (groups.isEmpty()) map.keys else groups).joinToString(",") { it.chineseName }, max_degree.progress)
                    .w(
                        this,
                        promptBeforeExecute = R.string.in_reading,
                        errorPromptAfterExecute = R.string.server_error,
                        success = {
                            it.body()?.run {
                                model.currentRelationsNeo.value = this
                            }
                        }
                    )
            }
            (arguments?.getString(DATA_KEY)?.parse() ?: currentUser.value!!).run {
                model.for_who.value = this
                for_who.setText(this.nickName)
                getNetwork()
            }
            search_prompt.setOnClickListener {
                RxView.clicks(it)
                    .throttleFirst(2, TimeUnit.SECONDS)
                    .subscribe {
                        WhqService.serviceInstance().getPersonNames(for_who.text.toString())
                            .w(
                                this,
                                promptBeforeExecute = R.string.in_reading,
                                success = {
                                    val data: List<MOBILEMD5_NAME> = it.body()!!.map {
                                        val (nickName, mobileMd5) = it.split("/")
                                        MOBILEMD5_NAME(mobileMd5 = mobileMd5, nickName = nickName)
                                    }
                                    val adapter = ArrayAdapterMiddle(this@ShowOnePersonFragment.context!!, data.map { it.nickName })
                                    for_who.setAdapter(adapter)
                                    for_who.showDropDown()
                                    for_who.setOnItemClickListener { _, _, position, _ ->
                                        model.for_who.value = data[position]
                                    }
                                }
                            )
                    }
            }

            max_degree.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    view_my_net.text = "${progress}度搜索"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
            RxView.clicks(view_my_net)
                .throttleFirst(3, TimeUnit.SECONDS)
                .subscribe {
                    getNetwork()
                }
        }

    }
}

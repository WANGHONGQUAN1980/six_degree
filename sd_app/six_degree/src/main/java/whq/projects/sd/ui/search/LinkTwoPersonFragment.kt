package whq.projects.sd.ui.search


import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.fragment_link_two_person.*
import kotlinx.android.synthetic.main.link_two_toolbar.*
import whq.projects.chat.callbacks.SDContactItFragmentCallback
import whq.projects.sd.R
import whq.projects.utilities.adapters.ArrayAdapterMiddle
import whq.projects.entities.MOBILEMD5_NAME
import whq.projects.entities.SdType
import whq.projects.sd.vm.LinkTwoPersonFragmentVM
import whq.projects.utilities.*
import whq.projects.utilities.rxjava.w
import java.util.concurrent.TimeUnit


class LinkTwoPersonFragment : BaseFragment(R.layout.fragment_link_two_person) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        doAfterLogged(ChatActivity.LINK_TWO_PERSON_FRAGMENT) {
            super.menuProcess { drawerLayout.toggle(GravityCompat.END) }
            val model = viewModel(LinkTwoPersonFragmentVM::class.java)
            model.fromPerson.value?.run { fromPerson.setText(this.nickName ?: "") }
            model.toPerson.value?.run { toPerson.setText(this.nickName ?: "") }
            model.degree.value?.run {
                link_two_person.text = "${model.degree.value ?: 3}度搜索"
                max_degree.progress = this
            }
            SdFragment(this, model, view, SdType.TWO).bind(SDContactItFragmentCallback.callback(requireActivity()))
            listOf<AppCompatImageView>(search_prompt, search_prompt_second)
                .forEach {
                    val isFirst = it.id == R.id.search_prompt
                    val completeTextView: AppCompatAutoCompleteTextView = if (isFirst) fromPerson else toPerson
                    RxView.clicks(it)
                        .throttleFirst(2, TimeUnit.SECONDS)
                        .subscribe {
                            WhqService.serviceInstance().getPersonNames(completeTextView.text.toString())
                                .enqueue(callback { response ->
                                    val data: List<MOBILEMD5_NAME> = response.body()!!.map {
                                        val (nickName, mobileMd5) = it.split("/")
                                        MOBILEMD5_NAME(mobileMd5 = mobileMd5, nickName = nickName)
                                    }
                                    val adapter = ArrayAdapterMiddle(this@LinkTwoPersonFragment.context!!, data.map { it.nickName })
                                    completeTextView.setAdapter(adapter)
                                    completeTextView.showDropDown()
                                    completeTextView.setOnItemClickListener { _, _, position, _ ->
                                        if (isFirst) {
                                            model.fromPerson.value = data[position]
                                        } else {
                                            model.toPerson.value = data[position]
                                        }
                                    }
                                })
                        }
                }
            max_degree.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    model.degree.value = progress
                    link_two_person.text = "${progress}度搜索"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
            RxView.clicks(link_two_person)
                .throttleFirst(3, TimeUnit.SECONDS)
                .subscribe {
                    if (model.fromPerson.hasValue() && model.toPerson.hasValue() && model.degree.hasValue()) {
                        WhqService.serviceInstance().theyRelations(model.fromPerson.value!!.mobileMd5, model.toPerson.value!!.mobileMd5, model.degree.value!!)
                            .w(
                                this,
                                promptBeforeExecute = R.string.in_calculation,
                                errorPromptAfterExecute = R.string.server_error,
                                success = {
                                    model.currentRelationsNeo.value = it.body()
                                })

                    }
                }
        }
    }
}

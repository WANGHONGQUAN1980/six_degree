package whq.projects.sd.ui.recommend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.ListFragment
import whq.projects.chat.callbacks.SDContactItFragmentCallback
import whq.projects.chat.fragments.PlugInContactIt
import whq.projects.chat.vm.ContactItFragmentVM
import whq.projects.entities.MOBILEMD5_NAME
import whq.projects.sd.R
import whq.projects.utilities.*
import whq.projects.utilities.User.currentUser
import whq.projects.utilities.rxjava.w

class ContentMyFocusFragment : ListFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_focus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        doAfterLogged(ChatActivity.CONTENT_MY_FOCUS_FRAGMENT) {
            super.onViewCreated(view, savedInstanceState)
            val model = viewModel(ContactItFragmentVM::class.java)
            PlugInContactIt(this, model, view).bind(SDContactItFragmentCallback.callback(requireActivity()))
            listView.choiceMode = AbsListView.CHOICE_MODE_SINGLE
            listView.setSelector(R.drawable.fragment_listselector)
            updateData()
        }
    }

    private fun updateData() {
        WhqService.serviceInstance()
            .myFocusedPeople(currentUser.value!!.mobileMd5)
            .w(
                this@ContentMyFocusFragment,
                promptBeforeExecute = R.string.in_reading,
                success = {
                    it.body()?.run {
                        this@ContentMyFocusFragment.listView.adapter = object : BaseAdapter() {
                            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                                return (convertView ?: LayoutInflater.from(this@ContentMyFocusFragment.requireContext()).inflate(R.layout.imageview_txt_highlight, parent, false))
                                    .also {
                                        it.findViewById<AppCompatImageView>(R.id.img).setImageDrawable(resources.drawable(R.drawable.person))
                                        it.findViewById<AppCompatTextView>(R.id.tv).text = getItem(position).nickName
                                        it.findViewById<AppCompatTextView>(R.id.option).setOnClickListener {
                                            listView.setItemChecked(position, true)
                                            (listView.adapter.getItem(listView.checkedItemPosition) as? MOBILEMD5_NAME)?.run {
                                                val model = viewModel(ContactItFragmentVM::class.java)
                                                model.selectedUser.value = this
                                            }
                                        }
                                    }
                            }

                            override fun getItem(position: Int): MOBILEMD5_NAME {
                                val split: List<String> = this@run[position].split('/', ignoreCase = true, limit = 2)
                                val name: String = split[0]
                                val mobileMd5: String = split[1]
                                return MOBILEMD5_NAME(mobileMd5, name)
                            }

                            override fun getItemId(position: Int): Long = position.toLong()

                            override fun getCount(): Int = this@run.size
                        }
                    }
                }
            )
    }
}
package whq.projects.sd.contacts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.contact_group.*
import whq.projects.chat.getDisplayName
import whq.projects.chat.vm.ContactSelectVM
import whq.projects.entities.*
import whq.projects.sd.R
import whq.projects.sd.utils.ImageUtils
import whq.projects.sd.utils.bindRelations
import whq.projects.sd.vm.ContactsVM
import whq.projects.sd.vm.ProfileRelation
import whq.projects.utilities.*
import whq.projects.utilities.adapters.ArrayAdapterMiddle
import whq.projects.utilities.adapters.pagerAdapter
import whq.projects.utilities.db.roomDb
import whq.projects.utilities.rxjava.w

@Suppress("UNCHECKED_CAST")
class GroupContacts : BaseFragment(R.layout.contact_group, menu_res = null) {
    companion object {
        fun changeRelation(fragment: Fragment, relationFromName: String, changeRelation: ChangeRelation) {
            val removed: List<Pair<String, String>> = changeRelation.run {
                relationFromName.split(",").zip(relationFromId.split(",")).toMap().filterKeys { it != changeRelation.relationToName }
            }.toList()
            val fromNew = removed.map { it.first }.joinToString(",")
            val fromRIdNew = removed.map { it.second }.joinToString(",")
            if (fromNew.isNullOrEmpty()) {
                fragment.toast("您选择的是相同类型")
            } else {
                WhqService.serviceInstance().changeRelation(changeRelation.copy(relationFromId = fromRIdNew)).w(
                    fragment,
                    promptBeforeExecute = R.string.change_relation,
                    promptAfterExecute = R.string.changed_relation_prompt,
                    errorPromptAfterExecute = R.string.change_relation_error
                )
            }
        }

        fun saveRelation(fragment: Fragment, currentRelation: String, relationTo: String, relationMobile: RelationMobile, toUser: Profile, currretnUser: MOBILEMD5_NAME) {
            if (currentRelation != relationTo) {
                roomDb().relations().insert(listOf(relationMobile))
                    .w(
                        fragment,
                        so = SO.IO_MAIN,
                        fail = {
                            roomDb().persons().insertPersons(listOf(toUser))
                                .w(
                                    fragment,
                                    success = {
                                        roomDb().relations().insert(listOf(relationMobile)).w(fragment)
                                    }
                                )
                        }
                    )
                WhqService.serviceInstance().saveRelations(
                    ProfileRelations(
                        persons = listOf(currretnUser.toProfile(), toUser.hiddenName()),
                        relations = listOf(relationMobile)
                    )
                ).w(fragment, promptBeforeExecute = R.string.process_save_relations)
            } else fragment.toast("您选择的是相同类型")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_CONTACTS_GROUP -> viewModel(ContactsVM::class.java).currentForWho.value = data!!.getStringExtra(DATA_KEY).parse()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val model: ContactsVM = viewModel(ContactsVM::class.java)
        bindRelations(to_group_first, to_group_second)
        for_who.setOnClickListener {
            viewModel(ContactSelectVM::class.java).run {
                selectedContacts.value = null
                selectedContacts.observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                        model.currentForWho.value = it.first()
                    }
                })
                findNavController().navigate(R.id.contactsSelectFragment, bundleOf(DATA_KEY to REQUEST_CODE_CONTACTS_GROUP, SHOW_TO_GLOBAL to false))
            }
        }
        model.currentForWho.observe(viewLifecycleOwner, Observer<MOBILEMD5_NAME> { person ->
            for_who.text = person.getDisplayName()
        })

        model.filteredCurrentContacts
            .observe(viewLifecycleOwner, Observer<List<ProfileRelation>> { contacts ->
                contacts_view_pager.clearOnPageChangeListeners()
                empty.visibleFor(contacts.isEmpty())
                if (contacts.isNotEmpty()) {
                    contacts_view_pager.adapter = pagerAdapter(contacts, R.layout.contact_page_view) { view, item ->
                        view.findViewById<ImageView>(R.id.img).setImageBitmap(ImageUtils.getNamesBitmap(listOf(item.profile.toMobileName().getDisplayName())))
                        view.findViewById<TextView>(R.id.name).text = item.profile.basic.nickName
                        view.findViewById<TextView>(R.id.mobileMd5).text = item.profile.basic.mobileMd5
                        view.findViewById<TextView>(R.id.relation).text = item.relation.chineseName
                    }
                    contacts_view_pager.addOnPageChangeListener(pageSelected {
                        current_index_count.text = "${it + 1}/${contacts.size}"
                    })
                    current_index_count.text = "1/${contacts.size}"
                } else {
                    contacts_view_pager.adapter = null
                }
            })
        model.currentRelations.observe(viewLifecycleOwner, Observer {
            val groupCounts: List<Pair<SdRelationType, Int>> = it.groupBy { SdRelationType.firstLevel(SdRelationType.parse(it.relation)!!) }
                .mapValues { it.value.size }
                .toList().sortedBy { it.second }

            val maxCount = groupCounts.maxBy { it.second }
            every_group_count.adapter = object : BaseAdapter() {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val data = groupCounts[position]
                    return (convertView ?: LayoutInflater.from(context).inflate(R.layout.rectangle_graph_item, parent, false)).apply {
                        this.findViewById<AppCompatTextView>(R.id.item_name).text = data.first.chineseName
                        this.findViewById<View>(R.id.line).also {
                            it.layoutParams = (it.layoutParams as ConstraintLayout.LayoutParams).apply {
                                this.matchConstraintPercentWidth = (maxCount?.second?.run { data.second.toFloat() / this } ?: 0f) * 0.8f
                            }
                        }
                        this.findViewById<AppCompatTextView>(R.id.label).text = "${data.second}人"
                    }
                }

                override fun getItem(position: Int) = groupCounts[position]

                override fun getItemId(position: Int) = groupCounts[position].first.chineseName.hashCode().toLong()

                override fun getCount() = groupCounts.size
            }
        })
        RxView.clicks(move)
            .filter { contacts_view_pager.currentItem != -1 }
            .subscribe {
                model.currentForWho.value?.also { currretnUser ->
                    val relation = model.filteredCurrentContacts.value!![contacts_view_pager.currentItem]
                    val currentRelation = relation.relation.chineseName
                    val relationTo = kotlin.runCatching { to_group_second.selectedItem.toString() }.getOrNull() ?: to_group_first.selectedItem.toString()
                    val toUser = relation.profile
                    val relationMobile = RelationMobile(from_mobile_md5 = currretnUser.mobileMd5, to_mobile_md5 = toUser.basic.mobileMd5, relation = relationTo)
                    saveRelation(this@GroupContacts, currentRelation, relationTo, relationMobile, toUser, currretnUser)
                }
            }
        groups_current.adapter = ArrayAdapterMiddle(this.requireContext(), SdRelationType.firstLevels().filter { it != SdRelationType.article && it != SdRelationType.locateat })
        groups_current.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.searchCondition.postValue(SearchCondition(relationType = groups_current.selectedItem as SdRelationType, keyword = ""))
            }
        }
    }


}


package whq.projects.sd.ui.search


import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.GravityCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.add_relations_toolbar.*
import kotlinx.android.synthetic.main.fragment_add_relations.*
import whq.projects.chat.callbacks.SDContactItFragmentCallback
import whq.projects.entities.ProfileKeys
import whq.projects.entities.SdRelationType
import whq.projects.entities.SdType
import whq.projects.entities.TOGGLE
import whq.projects.sd.R
import whq.projects.sd.utils.FileUtils
import whq.projects.sd.utils.Graph
import whq.projects.sd.vm.AddRelationsFragmentVM
import whq.projects.sd.vm.ContactsVM
import whq.projects.utilities.*
import whq.projects.utilities.db.roomDb
import whq.projects.utilities.rxjava.showProcessBar
import whq.projects.utilities.rxjava.w
import java.io.InputStreamReader


class AddRelationsFragment : BaseFragment(R.layout.fragment_add_relations) {
    override fun onDestroyView() {
        val model = viewModel(AddRelationsFragmentVM::class.java)
        model.page_loaded.postValue(false)
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        doAfterLogged(ChatActivity.ADD_RELATIONS_FRAGMENT) {
            super.menuProcess { drawerLayout.toggle(GravityCompat.END) }
            val model = viewModel(AddRelationsFragmentVM::class.java)
            val context = this@AddRelationsFragment.requireContext()
            showProcessBar(R.string.read_hlm)
            if (model.relations.value == null) {
                ioThread {
                    model.relations.postValue(SpannableStringBuilder(InputStreamReader(context.assets.open("relations_hlm.txt")).readText()))
                }
            }
            SdFragment(this, model, view, SdType.SD).bind(SDContactItFragmentCallback.callback(requireActivity()))
            model.relations.zipAnother(model.page_loaded) { r, pl -> Pair(r, pl) }.distinctUntilChanged().observe(viewLifecycleOwner, Observer {
                val (r, pl) = it
                if (pl) {
                    setupRelation()
                    tv_relation.setText(r, TextView.BufferType.EDITABLE)
                    showProcessBar("解析关系并展示......")
                    FileUtils.parseRelations(it.toString().split('\n'))?.run {
                        allNames.addAll(this.persons.mapNotNull { it.basic.nickName })
                        allMobiles.addAll(this.persons.mapNotNull { it.basic.mobile })
                        model.currentRelationsNeo.value = this.hiddenRealName().toNeo4jData()
                    }
                }
            })
            tv_help.movementMethod = ScrollingMovementMethod.getInstance()
            model.toggle.distinctUntilChanged().observe(viewLifecycleOwner, Observer {
                it?.run {
                    when (it) {
                        TOGGLE.SHOW_TEXT -> {
                            tv_relation_container.visibleFor(true)
                            tv_help.hiddenFor(true)
                            webviewer.hiddenFor(true)
                            text.hiddenFor(true)
                            network.visibleFor(true)
                            help_btn.visibleFor(true)
                            sd_fragment_toolbar_include.hiddenFor(true)
                        }
                        TOGGLE.SHOW_NET -> {
                            tv_relation_container.hiddenFor(true)
                            tv_help.hiddenFor(true)
                            webviewer.visibleFor(true)
                            text.visibleFor(true)
                            network.hiddenFor(true)
                            help_btn.visibleFor(true)
                            sd_fragment_toolbar_include.visibleFor(true)
                        }
                        TOGGLE.SHOW_HELP -> {
                            tv_relation_container.hiddenFor(true)
                            tv_help.visibleFor(true)
                            tv_help.text = model.helpString
                            webviewer.hiddenFor(true)
                            text.visibleFor(true)
                            network.visibleFor(true)
                            help_btn.hiddenFor(true)
                            sd_fragment_toolbar_include.hiddenFor(true)
                        }
                    }
                }
            })
            model.toggle.value = TOGGLE.SHOW_NET
            text.setOnClickListener {
                model.toggle.value = TOGGLE.SHOW_TEXT
            }
            network.setOnClickListener {
                if (model.relations.value != tv_relation.text)
                    model.relations.value = tv_relation.text
                model.toggle.value = TOGGLE.SHOW_NET
            }
            help_btn.setOnClickListener {
                model.toggle.value = TOGGLE.SHOW_HELP
            }
            viewModel(ContactsVM::class.java).contactsAllFromSdSim.observe(viewLifecycleOwner, Observer {
                allNames.addAll(it.mapNotNull { it.toMobileName().nickName })
                allMobiles.addAll(it.mapNotNull { it.basic.mobile })
            })
            save.setOnClickListener {
                val relationLines = tv_relation.text.toString().split("\n")
                FileUtils.parseRelations(relationLines)?.hiddenRealName()?.run {
                    WhqService.serviceInstance().saveRelations(this)
                        .mergeWith(roomDb().persons().insertPersons(this.persons))
                        .w(
                            this@AddRelationsFragment,
                            promptAfterExecute = R.string.toast_success,
                            promptBeforeExecute = R.string.process_save_relations
                        )
                } ?: toast("输入格式不正确")
            }
        }
    }

    val allNames: MutableSet<String> = mutableSetOf()
    val allMobiles: MutableSet<String> = mutableSetOf()
    private fun setupRelations() {
        var prev_start = 0
        var prev_end = 0
        val innerTypes = Graph.validRelationNames()
        val profileKeys = ProfileKeys.map.values.flatMap { it.split("/") }
        var prevItems = listOf<MenuPrompt>()
        fun Collection<String>.bindPrompts(groupId: Int) {
            val sorted = sortedWith(comparatorChinese)
            prompts.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.input_prompt, parent, false)) {}
                }

                override fun getItemCount(): Int = sorted.size
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    sorted[position].run {
                        holder.itemView.setOnLongClickListener {
                            if (tv_relation.text == null)
                                tv_relation.setText("")
                            val item = prevItems[position]
                            val tail = when (item.groupId) {
                                MenuPrompt.name_first -> ":"
                                MenuPrompt.property -> "="
                                MenuPrompt.user -> ","
                                MenuPrompt.relation -> " "
                                MenuPrompt.name_second -> if (prev_start == tv_relation.length() - 1) "\n" else ""
                                else -> throw Exception("popupMenu.setOnMenuItemClickListener")
                            }
                            tv_relation.text!!.replace(prev_start, prev_end, "${item.name}${tail}")
                            true
                        }
                        holder.itemView.findViewById<AppCompatTextView>(R.id.text).text = this
                    }
                }
            }
            prevItems = sorted.mapIndexed { idx, v -> MenuPrompt(groupId, idx, v) }
        }
        tv_relation.setOnClickListener {
            if (tv_relation.text.isNullOrEmpty()) {
                allNames.bindPrompts(MenuPrompt.name_first)
            }
        }
        val charsSeparator = " :=,\t\n".toCharArray()
        tv_relation.doOnTextChanged { text, start, _, after ->
            tv_relation.layout?.run {
                val lineNumber = getLineForOffset(start)
                text?.run {
                    val startIndex = lastIndexOfAny(charsSeparator, start - 1, true) + 1
                    val endIndex = start + after
                    prev_start = startIndex
                    prev_end = endIndex
                    val subSequence = subSequence(startIndex, endIndex).trim().toString()
                    val lineTextBefore = substring(getLineStart(lineNumber), start)
                    lineTextBefore.lastOrNull { charsSeparator.contains(it) }.run {
                        when (this) {
                            ' ', '\t' -> {
                                val findAll = "[\t ]+".toRegex().findAll(lineTextBefore)
                                when (findAll.toList().size) {
                                    1 ->
                                        innerTypes.filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.relation)
                                    2 ->
                                        allNames.filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.name_second)
                                    else -> Unit
                                }
                            }
                            ':', ',' ->
                                profileKeys.filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.property)
                            '=' -> {
                                val words = "\\b.+?\\b".toRegex().findAll(text).map { it.value }.filterNot { "\\s+".toRegex().matches(it) }.toList().distinct()
                                words.union(allMobiles).filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.user)
                            }
                            null ->
                                allNames.filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.name_first)
                        }
                    }
                }
            }
        }

    }

    private fun setupRelation() {
        var prev_start = 0
        var prev_end = 0
        val innerTypes = SdRelationType.allLevels().map { it.chineseName }
        val profileKeys = ProfileKeys.map.values.flatMap { it.split("/") }
        var prevItems = listOf<MenuPrompt>()
        fun Collection<String>.bindPrompts(groupId: Int) {
            val sorted = sortedWith(comparatorChinese)
            prompts.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    return object : RecyclerView.ViewHolder(LayoutInflater.from(requireContext()).inflate(R.layout.input_prompt, parent, false)) {}
                }

                override fun getItemCount(): Int = sorted.size
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    sorted[position].run {
                        holder.itemView.setOnLongClickListener {
                            if (tv_relation.text == null)
                                tv_relation.setText("")
                            val item = prevItems[position]
                            val tail = when (item.groupId) {
                                MenuPrompt.name_first -> ":"
                                MenuPrompt.property -> "="
                                MenuPrompt.user -> ","
                                MenuPrompt.relation -> " "
                                MenuPrompt.name_second -> if (prev_start == tv_relation.length() - 1) "\n" else ""
                                else -> throw Exception("popupMenu.setOnMenuItemClickListener")
                            }
                            tv_relation.text!!.replace(prev_start, prev_end, "${item.name}${tail}")
                            true
                        }
                        holder.itemView.findViewById<AppCompatTextView>(R.id.text).text = this
                    }
                }
            }
            prevItems = sorted.mapIndexed { idx, v -> MenuPrompt(groupId, idx, v) }
        }
        tv_relation.setOnClickListener {
            if (tv_relation.text.isNullOrEmpty()) {
                allNames.bindPrompts(MenuPrompt.name_first)
            }
        }
        val charsSeparator = " :=,\t\n".toCharArray()
        tv_relation.doOnTextChanged { text, start, _, after ->
            tv_relation.layout?.run {
                val lineNumber = getLineForOffset(start)
                text?.run {
                    val startIndex = lastIndexOfAny(charsSeparator, start - 1, true) + 1
                    val endIndex = start + after
                    prev_start = startIndex
                    prev_end = endIndex
                    val subSequence = subSequence(startIndex, endIndex).trim().toString()
                    val lineTextBefore = substring(getLineStart(lineNumber), start)
                    lineTextBefore.lastOrNull { charsSeparator.contains(it) }.run {
                        when (this) {
                            ' ', '\t' -> {
                                val findAll = "[\t ]+".toRegex().findAll(lineTextBefore)
                                when (findAll.toList().size) {
                                    1 ->
                                        innerTypes.filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.relation)
                                    2 ->
                                        allNames.filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.name_second)
                                    else -> Unit
                                }
                            }
                            ':', ',' ->
                                profileKeys.filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.property)
                            '=' -> {
                                val words = "\\b.+?\\b".toRegex().findAll(text).map { it.value }.filterNot { "\\s+".toRegex().matches(it) }.toList().distinct()
                                words.union(allMobiles).filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.user)
                            }
                            null ->
                                allNames.filter { it != subSequence && it.containByOrder(subSequence) }.bindPrompts(MenuPrompt.name_first)
                        }
                    }
                }
            }
        }

    }
}

data class MenuPrompt(val groupId: Int, val itemId: Int, val name: String) {
    companion object {
        val user = 1
        val name_first = 2
        val relation = 3
        val property = 4
        val name_second = 5
    }
}
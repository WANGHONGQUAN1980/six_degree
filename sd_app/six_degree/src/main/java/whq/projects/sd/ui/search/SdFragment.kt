package whq.projects.sd.ui.search

import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.gson.Gson
import whq.projects.chat.callbacks.ContactItFragmentCallback
import whq.projects.chat.fragments.PlugInContactIt
import whq.projects.chat.isCurrentUser
import whq.projects.entities.*
import whq.projects.sd.R
import whq.projects.sd.propertiesToDisplay
import whq.projects.sd.utils.deleteRelationDialog
import whq.projects.sd.utils.updateRelationDialog
import whq.projects.sd.vm.SdFragmentVM
import whq.projects.utilities.*
import whq.projects.utilities.User.currentUser
import whq.projects.utilities.adapters.ArrayAdapterMiddle
import whq.projects.utilities.db.roomDb
import whq.projects.utilities.rxjava.w
import whq.projects.utilities.rxjava.w_main_ok
import java.io.StringReader

class SdFragment(val fragment: BaseFragment, override val model: SdFragmentVM, layout: View, val sdType: SdType) : PlugInContactIt(fragment, model, layout) {
    override fun bind(callback: ContactItFragmentCallback?, hideMap: List<Int>) {
        super.bind(callback, hideMap)
        bindSd()
    }

    private fun bindSd() {
        val webView = layout.findViewById<WebView>(R.id.webviewer)
        webView.initPage().loaded {
            model.page_loaded.postValue(true)
        }
        val writerComment = layout.findViewById<AppCompatTextView>(R.id.writer)
        val search_text = layout.findViewById<AppCompatAutoCompleteTextView>(R.id.search_text)
        val properties = layout.findViewById<AppCompatTextView>(R.id.properties)
        val zoomIn = layout.findViewById<AppCompatTextView>(R.id.zoomIn)
        val zoomOut = layout.findViewById<AppCompatTextView>(R.id.zoomOut)
        val double_groups = layout.findViewById<RadioGroup>(R.id.double_groups)
        val fav = layout.findViewById<AppCompatTextView>(R.id.fav)
        val toolbar = layout.findViewById<ConstraintLayout>(R.id.toolbar)
        model.maxWindow.observe(lifecycleOwner, Observer { toolbar.hiddenFor(it) })
        fun enableControls(enable: Boolean) {
            fav.isEnabled = enable
            zoomIn.isEnabled = enable
            zoomOut.isEnabled = enable
            search_text.isEnabled = enable
            double_groups.isEnabled = enable
        }
        enableControls(false)
        fav.setOnClickListener {
            model.currentRelationsNeo.value?.run {
                fragment.showDialog(
                    caption = "收藏下来以便随时(即便离线)查看",
                    alertType = AlertType.Input,
                    inputText = "在这里输入名称",
                    okProcess = { d, e ->
                        if (e.text?.isNotEmpty() == true) {
                            roomDb().relations().favInsert(SdFav(e.text?.toString() ?: "我的收藏", currentUser.value!!.mobileMd5, sdType, this)).w(fragment, errorPromptAfterExecute = R.string.fav_error)
                            d.dismiss()
                        }
                    })
            }
        }
        double_groups.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.tongxue_d -> model.double_type.value = SdRelationType.tongxue
                R.id.tongshi_d -> model.double_type.value = SdRelationType.tongshi
                R.id.qinshu_d -> model.double_type.value = SdRelationType.qinshu
                R.id.pengyou_d -> model.double_type.value = SdRelationType.pengyou
                R.id.locateat_d -> model.double_type.value = SdRelationType.locateat
            }
        }
        val activity = (lifecycleOwner as BaseFragment).requireActivity()
        model.currentRelationsNeo.zipAnother(model.page_loaded) { a, b -> Pair(a, b) }.distinctUntilChanged().observe(lifecycleOwner, Observer {
            val (neo, pl) = it
            if (pl) {
                neo?.parse<Neo4jData>()?.run {
                    val nodes = this.results.first().data.first().graph.nodes
                    val emptyNodes = nodes.isEmpty()
                    fav.isEnabled = sdType != SdType.FAV_ED && !emptyNodes
                    enableControls(!emptyNodes)
                    webView.loadJavascript("updateWithNeo4jData('${this.mergeRelations().toJson()}');")
                    if (emptyNodes) {
                        fragment.toast("他们没有关系~")
                    }
                    nodes.map { MOBILEMD5_NAME(it.id, it.name) }.run {
                        search_text.setAdapter(ArrayAdapterMiddle(fragment.requireContext(), this, { obj, k ->
                            obj.nickName?.containsWithPinyin(k) ?: false
                        }))
                    }
                } ?: fragment.toast("它的关系好简单~")
            }
        })
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun userNameClick(mobileMd5: String, name: String, data: String) {
                activity.runOnUiThread {
                    val parser = Parser.default()
                    val json: JsonObject = parser.parse(StringReader(data)) as JsonObject
                    model.selectedUserData.value = json.obj("properties")?.run { this.mapValues { it.value.toString() } } ?: mapOf()
                    model.selectedUser.value = MOBILEMD5_NAME(mobileMd5, name)
                }
            }

            @JavascriptInterface
            fun doubleClick(mobileMd5: String, x: Float, y: Float) {
                activity.runOnUiThread {
                    model.double_type.value?.run {
                        this.englishName.run {
                            WhqService.serviceInstance().getRelation1Level(mobileMd5, this)
                                .w(
                                    fragment,
                                    promptBeforeExecute = R.string.process_relation_expand,
                                    success = {
                                        it.body()?.run data@{
                                            val neo4jGraphItem: Neo4jData = Gson().fromJson(this@data, Neo4jData::class.java)
                                            if (neo4jGraphItem.results.first().data.first().graph.nodes.isNotEmpty()) {
                                                webView.loadJavascript("update('${this@data}',$x,$y);")
                                            }
                                        }
                                    }
                                )
                        }
                    }
                }
            }

            @JavascriptInterface
            fun relationClick(mobileMd5: String, name: String) {
                activity.runOnUiThread {
                    model.selectedRelationWriter.value = MOBILEMD5_NAME(mobileMd5, name)
                }
            }

            @JavascriptInterface
            fun relationDoubleClick(
                baseType: String, type: String, rId: String,
                sourceMobileMd5: String, sourceNickName: String, sourceGender: String?, sourceBornDate: String?,
                targetMobileMd5: String, targetNickName: String, targetGender: String?, targetBornDate: String?,
                updaterMobileMd5: String?, updaterNickName: String?
            ) {
                if (!setOf("tongxue", "tongshi", "qin_shu", "friend").contains(baseType)) return
                activity.runOnUiThread {
                    model.selectedRelation.value = ChangeInfo(
                        from = Person(mobileMd5 = sourceMobileMd5, nickName = sourceNickName, gender = sourceGender, born_date = sourceBornDate),
                        to = Person(mobileMd5 = targetMobileMd5, nickName = targetNickName, gender = targetGender, born_date = targetBornDate),
                        relationTypeId = RelationTypeId(type, rId),
                        updater = MOBILEMD5_NAME(mobileMd5 = updaterMobileMd5 ?: currentUser.value!!.mobileMd5, nickName = updaterNickName)
                    )
                }
            }
        }, "android")
        model.selectedRelationWriter.observe(lifecycleOwner, Observer {
            writerComment.visibleFor(it != null)
            writerComment.text = "此关系由 ${it.nickName} 指定"
        })
        model.selectedRelation.zipToPair(User.currentUser).observe(lifecycleOwner, Observer {
            it?.run {
                val (r, _) = it
                if (r.from.mobileMd5.isCurrentUser() || r.to.mobileMd5.isCurrentUser()) {
                    fragment.deleteRelationDialog(r)
                } else {
                    fragment.updateRelationDialog(r)
                }
                model.selectedRelation.value = null
            }
        })
        model.selectedUser.zipToPair(model.selectedUserData).distinctUntilChanged().observe(lifecycleOwner, Observer {
            val (su, sud) = it
            properties.visibleFor(sud.isNotEmpty())
            val propertiesNeo = propertiesToDisplay(sud)
            properties.text = propertiesNeo.map { "${it.key}=${it.value}" }.joinToString("\n")
            WhqService.serviceInstance().getConfig(su.mobileMd5).w_main_ok(
                fragment,
                promptBeforeExecute = R.string.read_profile,
                success = { properties_config ->
                    properties.text = (propertiesNeo.plus(properties_config).map { "${it.key}=${it.value}" }.joinToString("\n"))
                })
        })
        search_text.setOnItemClickListener { _, _, position, _ ->
            (search_text.adapter.getItem(position) as? MOBILEMD5_NAME)?.run {
                webView.loadJavascript("focus('${this.mobileMd5}');")
            }
        }
        zoomIn.setOnClickListener { webView.loadJavascript("zoomTimes(true);") }
        zoomOut.setOnClickListener { webView.loadJavascript("zoomTimes(false);") }
    }
}


data class ChangeInfo(val from: Person, val to: Person, val relationTypeId: RelationTypeId, val updater: MOBILEMD5_NAME)
data class RelationTypeId(val type: String, val id: String)
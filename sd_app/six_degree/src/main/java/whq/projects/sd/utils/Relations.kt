package whq.projects.sd.utils

import android.graphics.Color
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import cn.forward.androids.views.StringScrollPicker
import whq.projects.entities.ChangeRelation
import whq.projects.sd.R
import whq.projects.sd.contacts.GroupContacts
import whq.projects.sd.ui.search.ChangeInfo
import whq.projects.utilities.WhqService
import whq.projects.utilities.adapterSimple
import whq.projects.utilities.rxjava.w

fun Fragment.deleteRelationDialog(changeInfo: ChangeInfo) {
    val context = requireContext()
    LayoutInflater.from(context).inflate(R.layout.sd_dialog_delete_relation, null, false).also {
        it.findViewById<ListView>(R.id.relationInfo).adapterSimple(
            mapOf(
                "关系" to changeInfo.relationTypeId.type,
                "从TA" to (changeInfo.from.nickName ?: "昵称未指定"),
                "到TA" to (changeInfo.to.nickName ?: "昵称未指定"),
                "指定" to (changeInfo.updater.nickName ?: "昵称未指定")
            )
        )
        AlertDialog.Builder(context).setCustomTitle(
            LayoutInflater.from(context).inflate(R.layout.sd_alert_title, null).also {
                it.findViewById<AppCompatTextView>(R.id.title).text = "查看 OR 删除关系"
            }
        )
            .setView(it)
            .setPositiveButton("删除") { d, _ ->
                WhqService.serviceInstance().deleteRelationOne(changeInfo.from.mobileMd5, changeInfo.to.mobileMd5, changeInfo.relationTypeId.type)
                    .w(
                        this,
                        promptBeforeExecute = R.string.delete_relation,
                        promptAfterExecute = R.string.delete_success,
                        errorPromptAfterExecute = R.string.server_error,
                        success = {
                            d.dismiss()
                        },
                        fail = {
                            d.dismiss()
                        }
                    )
            }
            .setNegativeButton("取消") { d, _ -> d.cancel() }
            .show()
            .also {
                it.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                it.window?.setTitleColor(Color.parseColor("#1976D2"))
            }
    }
}

fun Fragment.updateRelationDialog(changeInfo: ChangeInfo) {
    val context = this.requireContext()
    LayoutInflater.from(context).inflate(R.layout.sd_dialog_update_relation, null, false).also {
        it.findViewById<ListView>(R.id.relationInfo).adapterSimple(
            mapOf(
                "关系" to changeInfo.relationTypeId.type,
                "从TA" to (changeInfo.from.nickName ?: "昵称未指定"),
                "到TA" to (changeInfo.to.nickName ?: "昵称未指定"),
                "指定" to (changeInfo.updater.nickName ?: "昵称未指定")
            )
        )
        val first = it.findViewById<StringScrollPicker>(R.id.to_group_first)
        val second = it.findViewById<StringScrollPicker>(R.id.to_group_second)
        bindRelations(first, second)
        AlertDialog.Builder(context).setCustomTitle(
            LayoutInflater.from(context).inflate(R.layout.sd_alert_title, null).also {
                it.findViewById<AppCompatTextView>(R.id.title).text = "查看 OR 修改关系"
            }
        )
            .setView(it)
            .setPositiveButton("修改") { d, _ ->
                val relationTo = kotlin.runCatching { second.selectedItem.toString() }.getOrNull() ?: first.selectedItem.toString()
                GroupContacts.changeRelation(
                    this,
                    changeInfo.relationTypeId.type, ChangeRelation(
                        from = changeInfo.from,
                        to = changeInfo.to,
                        relationFromId = changeInfo.relationTypeId.id,
                        relationToName = relationTo
                    )
                )
                d.dismiss()
            }
            .setNegativeButton("取消") { d, _ -> d.cancel() }
            .show()
            .also {
                it.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                it.window?.setTitleColor(Color.parseColor("#1976D2"))
            }
    }
}
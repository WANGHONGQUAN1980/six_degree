package whq.projects.sd.utils

import cn.forward.androids.views.StringScrollPicker
import whq.projects.entities.SdRelationType

fun bindRelations(first: StringScrollPicker, second: StringScrollPicker) {
    fun updateSecondRelationTypes(pickerFirst: StringScrollPicker, secondPicker: StringScrollPicker) {
        secondPicker.data = SdRelationType.parse(pickerFirst.selectedItem.toString())!!.children.filter { it.is_public }.sortedBy { it.sortIndex }.map { it.chineseName }
    }
    first.data = SdRelationType.firstLevels().map { it.chineseName }
    first.setOnSelectedListener { _, _ -> updateSecondRelationTypes(first, second) }
    updateSecondRelationTypes(first, second)
}
package whq.projects.sd

import whq.projects.entities.ContentSearchCondition
import whq.projects.entities.PROFILE_KEYS
import whq.projects.utilities.AppCommon
import whq.projects.utilities.db.RoomDb
import whq.projects.utilities.hms

fun zero_search(): ContentSearchCondition {
    return ContentSearchCondition(
        0, 1, 0, 10,
        AppCommon.sharePreferences().getString(PROFILE_KEYS.relation_favourite.toString(), "10/9/8/7")
    )
}


fun propertiesToDisplay(properties: Map<String, String>): Map<String, String> {
    return properties.filterKeys { !setOf("writer", "updater", "mobileMd5", "id").contains(it) }
        .map {
            when (it.key) {
                "updaterName" -> "最后更新" to it.value
                "writerName" -> "最早写入" to it.value
                "updated" -> "更新日期" to it.value.toLong().hms()
                "created" -> "创建日期" to it.value.toLong().hms()
                "gender" -> "性别" to it.value
                "province" -> "省份" to it.value
                "city" -> "城市" to it.value
                "district" -> "区县" to it.value
                "name" -> "名称" to it.value
                "nick_name" -> "昵称" to it.value
                else -> it.key to it.value
            }
        }.toMap()
}
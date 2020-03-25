package whq.projects.sd.utils

import org.dom4j.io.SAXReader
import whq.projects.utilities.AppCommon
import java.io.InputStreamReader

object Graph {
    val reader = SAXReader()
    fun validPersonProperties(): List<String> = listOf("手机号", "性别", "昵称", "备注")
    fun validRelationNames(): List<String> {
        return reader.read(InputStreamReader(AppCommon.instance.assets.open("graph.xml"))).rootElement
            .selectNodes("relations/*/@name")
            .map { it.toString() }
    }
}
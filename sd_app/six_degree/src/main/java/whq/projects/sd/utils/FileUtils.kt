package whq.projects.sd.utils

import android.content.Context
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import androidx.lifecycle.MutableLiveData
import whq.projects.entities.*
import whq.projects.sd.entities.ProfileLocation
import java.io.FileOutputStream
import whq.projects.entities.PROFILE_KEYS.*
import java.io.InputStreamReader
import java.util.regex.Pattern
import whq.projects.utilities.*
import whq.projects.utilities.md5

object FileUtils {
    @Throws(Exception::class)
    fun savaFileToSD(toFileName: String, filecontent: ByteArray) {
        if (Environment.getExternalStorageState() == MEDIA_MOUNTED) {
            val output = FileOutputStream(toFileName)
            output.write(filecontent)
            output.close()
        }
    }

    fun readRelations(context: Context, file: String): MutableLiveData<ProfileRelations> {
        val data = MutableLiveData<ProfileRelations>()
        Thread(Runnable { data.postValue(readRelations(InputStreamReader(context.assets.open(file)))) }).start()
        return data
    }

    private fun readRelations(reader: InputStreamReader): ProfileRelations? {
        return parseRelations(reader.readLines())
    }


    /*fun parseRelationList(text: String): ProfileRelations? {
        val (entities, relations) = text.split(Pattern.compile("\n")).partition { it.contains('=') }
        val relationNames = Graph.validRelationNames().toSet()
        val validRelations = relations.mapNotNull {
            val splits = it.split(Pattern.compile("\\s+"))
            if (splits.size == 3 && relationNames.contains(splits[1])) splits else null
        }
        val validNames = validRelations.flatMap { it.filterIndexed { index, s -> index != 1 } }.toSet()
        val persons = entities.mapNotNull {
            val splits = it.split(Pattern.compile("\\s+"))
            if (splits.exists { validNames.contains(it) }) {
                val (properties, name) = splits.partition { it.contains('=') }
                val map = properties.map { val (key, value) = it.split("="); key to value }.toMap()
                if (map.containsKey("手机号")) {
                    val mobile = map.getValue("手机号")
                    if (mobile.isMobileOrSys())
                        Person(mobile = mobile, mobileMd5 = mobile.md5(), nickName = map.getOrElse("昵称", { mobile.takeLast(4) }), gender = map["性别"], beizhu = map["备注"]).toProfile()
                    else null
                } else null
            } else null
        }
        val dic = persons.map { it.basic.nickName!! to it }.toMap()
        return ProfileRelations(persons = persons, relations = validRelations.map {
            val (from, relation, to) = it
            RelationMobile(from_mobile_md5 = dic.getValue(from).basic.mobileMd5, to_mobile_md5 = dic.getValue(to).basic.mobileMd5, relation = relation)
        })
    }
*/
    fun parseRelations(lines: List<String>, ignoreCheck: Boolean = false): ProfileRelations? {
        val relations = mutableListOf<RelationName>()
        val persons = mutableListOf<Profile>()
        lines.filter { it.isNotEmpty() }.forEach { line ->
            var split = line.split(Pattern.compile("\\s+"))
            if (split.size == 3) {
                val (from, relationType, to) = split
                split = relationType.split(':')
                val relationName = split[0]
                if (SdRelationType.parse(relationName) != null) {
                    val properties = if (split.size == 2) split[1].split(',').mapNotNull {
                        val kvs = it.split('=')
                        if (kvs.size == 2) kvs[0] to kvs[1] else null
                    }.toMap() else mapOf()
                    relations.add(RelationName(from = from, relationsType = relationName, to = to, properties = properties))
                }
            } else if (split.size == 1 && line.contains(Property.mobile.displayName)) {
                split = line.split(':')
                if (split.size == 2) {
                    val (name, properties) = split
                    val propertyMap: Map<PROFILE_KEYS, String> = properties.split(',')
                        .mapNotNull {
                            val kvs = it.split('=')
                            if (kvs.size == 2) ProfileKeys.chineseToKey(kvs[0])?.run { this to kvs[1] } else null
                        }.toMap().plus(mapOf(nick_name to name))
                    propertyMap[mobile]?.also {
                        if (ignoreCheck || it.matches("^1[03456789]\\d{9}\$".toRegex())) {
                            val basic = Person(mobileMd5 = it.md5())
                            val location = ProfileLocation()
                            val school = ProfileSchool()
                            val pwork = ProfileWork()
                            val info = Profile(basic, location, school, pwork)
                            propertyMap.entries.forEach {
                                val value = it.value
                                when (it.key) {
                                    beizhu -> basic.beizhu = value
                                    mobile -> basic.mobileMd5 = value.md5()
                                    nick_name -> basic.nickName = value
                                    zhu_zhi -> location.zhu_zhi = value
                                    ji_guan -> location.ji_guan = value
                                    work -> location.work = value
                                    jiu_du -> location.jiu_du = value

                                    you_er_yuan_school -> school.you_er_yuan_school = school.you_er_yuan_school?.copy(name = value) ?: UnitYear(name = value)
                                    small_school -> school.small_school = school.small_school?.copy(name = value) ?: UnitYear(name = value)
                                    middle_school -> school.middle_school = school.middle_school?.copy(name = value) ?: UnitYear(name = value)
                                    high_school -> school.high_school = school.high_school?.copy(name = value) ?: UnitYear(name = value)
                                    under_graduate_school -> school.under_graduate_school = school.under_graduate_school?.copy(name = value) ?: UnitYear(name = value)
                                    post_graduate_school -> school.post_graduate_school = school.post_graduate_school?.copy(name = value) ?: UnitYear(name = value)
                                    doctoral_school -> school.doctoral_school = school.doctoral_school?.copy(name = value) ?: UnitYear(name = value)
                                    work_current -> pwork.work_current = pwork.work_current?.copy(name = value) ?: UnitYear(name = value)
                                    work_ever -> pwork.work_ever = pwork.work_ever?.copy(name = value) ?: UnitYear(name = value)
                                    you_er_yuan_school_year -> school.you_er_yuan_school = school.you_er_yuan_school?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())
                                    small_school_year -> school.small_school = school.small_school?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())
                                    middle_school_year -> school.middle_school = school.middle_school?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())

                                    high_school_year -> school.high_school = school.high_school?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())
                                    under_graduate_school_year -> school.under_graduate_school = school.under_graduate_school?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())
                                    post_graduate_school_year -> school.post_graduate_school = school.post_graduate_school?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())
                                    doctoral_school_year -> school.doctoral_school = school.doctoral_school?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())
                                    work_current_year -> pwork.work_current = pwork.work_current?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())
                                    work_ever_year -> pwork.work_ever = pwork.work_ever?.copy(year = value.toInt()) ?: UnitYear(year = value.toInt())
                                    else ->
                                        throw Exception("invalid profile key")
                                }
                            }
                            persons.add(info)
                        }
                    }
                }
            }
        }
        return if (persons.isNotEmpty()) {
            val nickNames = persons.map { it.basic.nickName ?: "" }.toSet()
            val dic = persons.map { it.basic.nickName to it.basic.mobileMd5 }.toMap()
            val validRelations = relations.filter { nickNames.contains(it.from) && nickNames.contains(it.to) }
            if (validRelations.isNotEmpty()) ProfileRelations(
                persons.distinctBy { it.basic.mobileMd5 },
                validRelations.map { RelationMobile(from_mobile_md5 = dic.getValue(it.from), to_mobile_md5 = dic.getValue(it.to), relation = it.relationsType, property = getProperty(it.properties)) }.distinctBy {
                    it.from_mobile_md5 to it.to_mobile_md5
                }) else null
        } else null
    }

    private fun getProperty(properties: Map<String, String>): RelationProperty? {
        val validProperties = properties.keys.filter { it == "单位" || it == "年份" }
        return if (validProperties.isNotEmpty()) {
            val result = RelationProperty()
            result.unit = properties["单位"]
            properties["年份"]?.toIntOrNull()?.run {
                result.year = this
            }
            result
        } else null
    }
}
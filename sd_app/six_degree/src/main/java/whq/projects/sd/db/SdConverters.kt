package whq.projects.sd.db

import androidx.room.TypeConverter
import kotlinx.serialization.enumFromName
import whq.projects.entities.EventType
import whq.projects.entities.MOBILEMD5_NAME
import whq.projects.entities.SdType
import whq.projects.entities.messageBodies.MessageBody
import whq.projects.utilities.parse
import whq.projects.utilities.toJson

class SdConverters {
    @TypeConverter
    fun str2list(str: String) = str.split(",").toList()

    @TypeConverter
    fun list2str(list: List<String>) = list.joinToString(",")


    @TypeConverter
    fun messageType2str(messageType: EventType): String {
        return messageType.toString()
    }

    @TypeConverter
    fun str2messageType(str: String?): EventType? {
        return str?.run { EventType.valueOf(this) }
    }

    @TypeConverter
    fun str2idName(str: String?): MOBILEMD5_NAME? {
        return str?.run { str.parse() }
    }

    @TypeConverter
    fun message2str(messageBody: MessageBody?): String? {
        return messageBody?.toJson()
    }

    @TypeConverter
    fun sdType2str(sdType: SdType): String {
        return sdType.name
    }

    @TypeConverter
    fun str2sdType(sdType: String): SdType {
        return enumFromName(SdType::class, sdType)
    }
}

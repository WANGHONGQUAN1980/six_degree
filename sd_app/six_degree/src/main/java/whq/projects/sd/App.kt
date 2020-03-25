package whq.projects.sd

import androidx.lifecycle.ViewModel
import whq.projects.chat.vm.*
import whq.projects.sd.profile.preference.SettingPreferenceVM
import whq.projects.sd.vm.*
import whq.projects.utilities.AppCommon
import whq.projects.utilities.BaseActivityVM

@Suppress("UNCHECKED_CAST")
/**
 *
 * 王洪权:    通讯录：秦华，梁飞，    亲属：程会敏，    同事：邓兴启，    其他：楚老师
 *
创建群组：
1、relation表中的非对方判为敌人直接添加不需申请度友，即上述任何人均可直接创建群组
2、组员均同步这个组，且将所有组员插入relation表
3、全局搜的人，对方有两个选项：加为度友同时进群/全部拒绝/敌人关系
4、可以解散、全部禁言、个别禁言、授权部分人填加成员、删除成员、公告、改群名
解散群组：
1、同步至各成员，删除本地GroupMember的相应关系
更改昵称：
1、个人更改昵称，同步至服务器account\neo4j中\
2、每人限更改3次
添加成员:
1、relation非对方敌人可添加，全局搜的人权限同上
2、同步成员至所有成员申请度友
敌人关系，双方同步
其他关系：只存本地不上传服务器
relation表
/推荐人字段/类别～渠道:建组的人～建组，null～邀请，null～被邀请，null~通讯录，自己判为敌人，对方判为敌人
申请度友: 只是放置persons表中
 */
class App : AppCommon() {
    override fun appNameChinese(): String {
        return "六度"
    }

    override fun appNameEnglish(): String {
        return "six_degree"
    }

    override fun <T : ViewModel?> factoryMap(modelClass: Class<T>): T {
        return when (modelClass) {
            SettingPreferenceVM::class.java -> SettingPreferenceVM() as T
            BaseActivityVM::class.java -> BaseActivityVM() as T
            ContactSelectVM::class.java -> ContactSelectVM(this@App) as T
            ContactsVM::class.java -> ContactsVM(this@App) as T
            ContentOtherFragmentVM::class.java -> ContentOtherFragmentVM(this@App) as T
            BaseActivityVM::class.java -> BaseActivityVM() as T
            ContentPublishFragmentVM::class.java -> ContentPublishFragmentVM(this@App) as T
            AddRelationsFragmentVM::class.java -> AddRelationsFragmentVM(this@App) as T
            LinkTwoPersonFragmentVM::class.java -> LinkTwoPersonFragmentVM(this@App) as T
            ShowOnePersonFragmentVM::class.java -> ShowOnePersonFragmentVM(this@App) as T
            SdFragmentVM::class.java -> SdFragmentVM(this@App) as T
            ZupuFragmentVM::class.java -> ZupuFragmentVM(this@App) as T
            ContentListFragmentVM::class.java -> ContentListFragmentVM(this@App) as T
            ContactItFragmentVM::class.java -> ContactItFragmentVM(this@App) as T
            MessageGroupsVM::class.java -> MessageGroupsVM(this@App) as T
            MessageSessionVM::class.java -> MessageSessionVM(this@App) as T
            ContactSelectVM::class.java -> ContactSelectVM(this@App) as T
            else -> throw Exception("factoryMap:${modelClass.name}")
        }
    }
}


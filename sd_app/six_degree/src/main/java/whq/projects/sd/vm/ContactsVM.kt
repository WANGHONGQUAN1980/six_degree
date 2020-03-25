package whq.projects.sd.vm

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.*
import whq.projects.entities.*
import whq.projects.sd.R
import whq.projects.utilities.*
import whq.projects.utilities.User.currentUser
import whq.projects.utilities.db.roomDb
import whq.projects.utilities.rxjava.w_io

class ContactsVM(application: Application) : AndroidViewModel(application) {
    val currentForWho: MutableLiveData<MOBILEMD5_NAME> = MutableLiveData()
    val searchCondition: MutableLiveData<SearchCondition> = MutableLiveData(SearchCondition(SdRelationType.contracts, ""))
    private val allRelations: LiveData<List<RelationMobile>> = roomDb().relations().getAllRelations().map { it.filterNot { SdRelationType.black.chineseName == it.relation && it.source != currentUser.value!!.mobileMd5 } }
    val currentRelations: LiveData<List<RelationMobile>> = zipLiveData(currentForWho, allRelations) { for_who, all ->
        all.filter {
            val mobileMd5 = for_who.mobileMd5
            it.from_mobile_md5 == mobileMd5 || it.to_mobile_md5 == mobileMd5
        }
    }
    val contactsAllFromSdSim: LiveData<List<Profile>> = ContactReader.getPhoneContacts(application.baseContext)
    private val contactsAll: LiveData<List<Profile>> = roomDb().persons().getPersons()
    var filteredCurrentContacts: LiveData<List<ProfileRelation>> = Transformations.map(zipLiveData(currentForWho, searchCondition, contactsAll, allRelations)) {
        val itsRelations = allRelations.value!!.filter { it.from_mobile_md5 == currentForWho.value!!.mobileMd5 || it.to_mobile_md5 == currentForWho.value!!.mobileMd5 }
        contactsAll.value!!.mapNotNull { profile: Profile ->
            val nameLike = profile.basic.nickName?.containsWithPinyin(searchCondition.value!!.keyword) ?: false
            if (nameLike) {
                itsRelations
                    .find { (it.to_mobile_md5 == profile.basic.mobileMd5 || it.from_mobile_md5 == profile.basic.mobileMd5) && SdRelationType.parse(it.relation)!!.firstLevel().chineseName == searchCondition.value!!.relationType.chineseName }
                    ?.run { ProfileRelation(profile, SdRelationType.parse(relation)!!) }
            } else null
        }
            .filter { it.profile.basic.mobileMd5 != currentForWho.value!!.mobileMd5 }
    }

    fun initialize(activity: Activity) {
        currentUser.observeForever {
            if (it != null) {
                roomDb().persons().insertPersons(listOf(it.toProfile())).doOnComplete {
                    currentForWho.postValue(it)
                    syncContacts(activity)
                }
            }
        }
    }

    fun syncContacts(activity: Activity) {
        val sharedPreferences: SharedPreferences = AppCommon.sharePreferences()
        val key = "sync_contacts_${currentUser.value!!.mobileMd5}"
        if (!sharedPreferences.getBoolean(key, false)) {
            contactsAllFromSdSim.observeForever { profiles ->
                roomDb().persons().insertPersons(profiles)
                    .w_io(
                        activity,
                        promptBeforeExecute = R.string.in_sync_contacts,
                        success = {
                            roomDb().relations().insert(profiles.map {
                                RelationMobile(
                                    from_mobile_md5 = currentUser.value!!.mobileMd5,
                                    to_mobile_md5 = it.basic.mobileMd5,
                                    relation = SdRelationType.contracts.chineseName,
                                    source = MOBILEMD5_NAME.SYSTEM_NICKNAME
                                )
                            }).w_io(activity)
                            sharedPreferences.edit(commit = true) {
                                this.putBoolean(key, true)
                            }
                        }
                    )
            }
        }
    }
}

data class ProfileRelation(val profile: Profile, val relation: SdRelationType)
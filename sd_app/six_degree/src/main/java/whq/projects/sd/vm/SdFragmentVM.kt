package whq.projects.sd.vm

import android.app.Application
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import whq.projects.chat.vm.ContactItFragmentVM
import whq.projects.entities.MOBILEMD5_NAME
import whq.projects.entities.SdRelationType
import whq.projects.sd.ui.search.ChangeInfo

open class SdFragmentVM(application: Application) : ContactItFragmentVM(application) {
    var currentRelationsNeo: MediatorLiveData<String> = MediatorLiveData()
    val selectedUserData: MutableLiveData<Map<String, String>> = MutableLiveData()
    val selectedRelationWriter: MutableLiveData<MOBILEMD5_NAME> = MutableLiveData()
    val selectedRelation: MutableLiveData<ChangeInfo> = MutableLiveData()
    val double_type: MutableLiveData<SdRelationType> = MutableLiveData()
    val maxWindow: MutableLiveData<Boolean> = MutableLiveData()
    val page_loaded: MutableLiveData<Boolean> = MutableLiveData()
}
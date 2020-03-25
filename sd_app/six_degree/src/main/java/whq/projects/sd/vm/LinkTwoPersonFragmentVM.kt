package whq.projects.sd.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import whq.projects.entities.MOBILEMD5_NAME

class LinkTwoPersonFragmentVM(application: Application) : SdFragmentVM(application) {
    val fromPerson: MutableLiveData<MOBILEMD5_NAME> = MutableLiveData()
    val toPerson: MutableLiveData<MOBILEMD5_NAME> = MutableLiveData()
    val degree: MutableLiveData<Int> = MutableLiveData()

    init {
        fromPerson.value = MOBILEMD5_NAME.JIA_BAOYU
        toPerson.value = MOBILEMD5_NAME.LIN_DAIYU
        degree.value = 4
    }
}
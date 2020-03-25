package whq.projects.sd.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import whq.projects.entities.MOBILEMD5_NAME

class ShowOnePersonFragmentVM(application: Application) : SdFragmentVM(application) {
    val for_who: MutableLiveData<MOBILEMD5_NAME> = MutableLiveData()
}
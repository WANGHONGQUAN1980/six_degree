package whq.projects.sd.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData

class ZupuFragmentVM(application: Application) : SdFragmentVM(application) {
    val degree: MutableLiveData<Int> = MutableLiveData()
}
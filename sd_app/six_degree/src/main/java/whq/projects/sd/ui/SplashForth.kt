package whq.projects.sd.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import whq.projects.sd.MainActivity
import whq.projects.sd.R

class SplashForth : Fragment(R.layout.flash_forth) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener {
            requireActivity().run {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }
}
package whq.projects.sd.ui.search

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.search_fragment.*
import whq.projects.sd.R
import whq.projects.utilities.BaseFragment

class SearchFragment : BaseFragment(R.layout.search_fragment,menu_res = null) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        add_relations_container.setOnClickListener {
            findNavController().navigate(R.id.add_relations)
        }
        link_two_person_container.setOnClickListener {
            findNavController().navigate(R.id.link_two_person)
        }
        show_one_person_container.setOnClickListener {
            findNavController().navigate(R.id.show_one_person)
        }
        zupu_container.setOnClickListener {
            findNavController().navigate(R.id.zupu)
        }
        fav_container.setOnClickListener {
            findNavController().navigate(R.id.favList)
        }
    }
}
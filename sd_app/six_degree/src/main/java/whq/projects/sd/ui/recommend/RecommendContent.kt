package whq.projects.sd.ui.recommend

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.recommend_content.*
import whq.projects.entities.ContentListType
import whq.projects.sd.R
import whq.projects.utilities.*

class RecommendContent : BaseFragment(R.layout.recommend_content, menu_res = null) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        other_container.setOnClickListener {
            findNavController().navigate(R.id.content_other_fragment)
        }
        my_container.setOnClickListener {
            findNavController().navigate(
                R.id.content_list_fragment, bundleOf(
                    TYPE_KEY to ContentListType.Published.name
                )
            )
        }
        publish_container.setOnClickListener {
            findNavController().navigate(R.id.content_publish_fragment)
        }
        to_me_container.setOnClickListener {
            findNavController().navigate(
                R.id.content_list_fragment, bundleOf(
                    TYPE_KEY to ContentListType.Relevant.name
                )
            )
        }
        to_focus_container.setOnClickListener {
            findNavController().navigate(R.id.content_my_focus_fragment)
        }
        to_favourite_container.setOnClickListener {
            findNavController().navigate(
                R.id.content_list_fragment, bundleOf(
                    TYPE_KEY to ContentListType.Favourite.name
                )
            )
        }
        to_search_container.setOnClickListener {
            findNavController().navigate(
                R.id.content_list_fragment, bundleOf(
                    TYPE_KEY to ContentListType.Search.name
                )
            )
        }
    }
}
package whq.projects.sd.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.fragment.app.ListFragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import whq.projects.entities.SdFav
import whq.projects.entities.SdType
import whq.projects.sd.R
import whq.projects.utilities.*
import whq.projects.utilities.User.currentUser
import whq.projects.utilities.db.roomDb
import whq.projects.utilities.rxjava.io_main

class FavListFragment : ListFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        doAfterLogged(ChatActivity.FAV_LIST_FRAGMENT) {
            super.setEmptyText("这里空空如也")
            roomDb().relations().favGet(currentUser.value!!.mobileMd5).observe(viewLifecycleOwner, Observer<List<SdFav>?> { favs ->
                super.setListAdapter(object : BaseAdapter() {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                        val context = this@FavListFragment.requireContext()
                        return (convertView ?: LayoutInflater.from(context).inflate(R.layout.sd_fav_item, parent, false))
                            .also {
                                val fav = favs!![position]
                                it.findViewById<AppCompatImageView>(R.id.type).setImageDrawable(
                                    context.resources.getDrawable(
                                        when (fav.sdType) {
                                            SdType.SD -> R.drawable.ac_unit
                                            SdType.TWO -> R.drawable.group_blue_24
                                            SdType.ONE -> R.drawable.flare
                                            SdType.ZUPU -> R.drawable.hierachies
                                            SdType.FAV_ED -> TODO()
                                        }
                                    )
                                )
                                it.findViewById<AppCompatTextView>(R.id.name).text = fav.name
                                it.findViewById<AppCompatImageView>(R.id.delete).setOnClickListener {
                                    roomDb().relations().favDelete(fav.name).io_main().subscribe({
                                        toast("你已经取消收藏")
                                        notifyDataSetChanged()
                                    }, {
                                        toast("发生错误")
                                    })
                                }
                                it.findViewById<AppCompatImageView>(R.id.detail).setOnClickListener {
                                    findNavController().navigate(
                                        R.id.favShow, bundleOf(
                                            DATA_KEY to fav.neoJson
                                        )
                                    )
                                }
                            }
                    }

                    override fun getItem(position: Int) = favs?.get(position)

                    override fun getItemId(position: Int) = position.toLong()

                    override fun getCount() = favs!!.size
                })
            })
        }
    }
}
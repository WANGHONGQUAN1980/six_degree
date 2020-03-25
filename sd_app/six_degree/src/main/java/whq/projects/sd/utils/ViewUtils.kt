package whq.projects.sd.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TabHost
import android.widget.TextView
import whq.projects.sd.R

fun TabHost.TabSpec.tabView(context: Context, tabhost: TabHost, text: String, drawable: Int): TabHost.TabSpec {
    val inflate = LayoutInflater.from(context).inflate(R.layout.tab_imageview_txt, tabhost, false)
    inflate.findViewById<TextView>(R.id.tv).text = text
    inflate.findViewById<ImageView>(R.id.img).setImageDrawable(context.resources.getDrawable(drawable, null))
    this.setIndicator(inflate)
    return this
}
package whq.projects.sd

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.splash.*
import whq.projects.sd.ui.SplashFirst
import whq.projects.sd.ui.SplashForth
import whq.projects.sd.ui.SplashSecond
import whq.projects.sd.ui.SplashThird
import whq.projects.utilities.Initialize
import whq.projects.utilities.isFirstStart
import whq.projects.utilities.showPrivatePolicy

class SplashActivity : AppCompatActivity(R.layout.splash) {
    val fragments = listOf(SplashFirst(), SplashSecond(), SplashThird(), SplashForth())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFirstStart()) {
            showPrivatePolicy()
            showSplashPages()
        } else {
            startPrimaryActivity()
        }
    }

    fun showSplashPages() {
        val indicators = listOf(first, second, third, forth)
        skip.setOnClickListener {
            startPrimaryActivity()
        }
        pager.adapter = object : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment = fragments[position]
            override fun getCount(): Int = fragments.size
        }
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                indicators.withIndex().forEach {
                    it.value.setImageDrawable(resources.getDrawable(if (it.index == position) R.drawable.remove else R.drawable.remove_black, null))
                }
            }
        })
    }

    fun startPrimaryActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
package whq.projects.sd

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.allenliu.badgeview.BadgeFactory
import com.allenliu.badgeview.BadgeView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_main.*
import whq.projects.chat.Chat
import whq.projects.chat.getDisplayName
import whq.projects.entities.*
import whq.projects.sd.utils.FileUtils
import whq.projects.utilities.*
import whq.projects.utilities.User.currentUser
import whq.projects.utilities.db.roomDb
import whq.projects.utilities.rxjava.*

class MainActivity : Initialize(R.layout.activity_main) {
    override fun permissions(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.INSTALL_PACKAGES
        )
    }

    var newCountBadge: BadgeView? = null
    private fun initData(file: String, isHLM: Boolean) {
        val sharedPreferences: SharedPreferences = AppCommon.sharePreferences()
        val key = "initialized_persons_${if (isHLM) "hlm" else "me"}"
        if (sharedPreferences.getBoolean(key, false)) {
            Chat.initialize(this)
        } else {
            FileUtils.readRelations(this, file).observe(this, Observer { relations ->
                roomDb().persons().insertPersons(relations.persons)
                    .w_main(this, success = {
                        if (isHLM) {
                            Chat.initHLMMessages(this)
                        }
                        roomDb().relations().insert(relations.relations.map { it.copy(source = MOBILEMD5_NAME.SYSTEM_NICKNAME) })
                            .w_main(this, success = {
                                sharedPreferences.edit(commit = true) { this.putBoolean(key, true) }
                                Chat.initialize(this)
                            })
                    })
            })
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
        initialized()
    }

    private var configuration: AppBarConfiguration? = null

    fun initialized() {
        currentUser.distinctUntilChanged().observeForever {
            Log.d("Main", "initialized distinctUntilChanged")
            initData("relations_me.txt", isHLM = false)
            showUserName(it.nickName ?: "无名氏")
            val nickNameControl = navigationView.getHeaderView(0).findViewById<TextInputEditText>(R.id.nickName)
            val nickNameTv = navigationView.getHeaderView(0).findViewById<AppCompatTextView>(R.id.nickName_tv)
            nickNameControl.setText(it.getDisplayName())
            nickNameTv.text = it.getDisplayName()

        }
    }

    @SuppressLint("SdCardPath")
    fun initialize() {
        configuration = AppBarConfiguration(setOf(R.id.loginFragment, R.id.registerFragment, R.id.group_contacts, R.id.messages_group, R.id.recommend_content, R.id.search_fragment, R.id.setting_preference), drawerLayout)
        bottom_nav.setupWithNavController(nav_host_fragment.findNavController())
        setupActionBarWithNavController(this, nav_host_fragment.findNavController(), configuration!!)
        Chat.newMessageCount.observeForever {
            if (it != null && it > 0)
                getBadge().setBadgeCount(it).show()
            else
                getBadge().gone()
        }
        setLeftNav()
        Log.d("main_activity", "initilize done")
    }

    private fun setLeftNav() {
        navigationView.menu.findItem(R.id.version).title = "当前版本:${VersionInfo.localVersion(AppCommon.instance).versionName}"
        navigationView.getHeaderView(0).findViewById<AppCompatImageView>(R.id.app_img).setImageBitmap(ImageUtils.getTextBitmap(this, VersionInfo.getApplicationName(AppCommon.instance)))
        val nickNameTe = navigationView.getHeaderView(0).findViewById<TextInputEditText>(R.id.nickName)
        val nickNameTv = navigationView.getHeaderView(0).findViewById<AppCompatTextView>(R.id.nickName_tv)
        val nickNameOk = navigationView.getHeaderView(0).findViewById<AppCompatTextView>(R.id.update_nick_name_ok)
        val nickNameUpdate = navigationView.getHeaderView(0).findViewById<AppCompatTextView>(R.id.update_nick_name)
        nickNameTv.visibleFor(true)
        nickNameTe.visibleFor(false)
        nickNameUpdate.setOnClickListener {
            nickNameTe.visibleFor(true)
            nickNameTv.visibleFor(false)
            nickNameOk.visibleFor(true)
            nickNameUpdate.visibleFor(false)
        }
        nickNameOk.setOnClickListener {
            nickNameTe.visibleFor(false)
            nickNameTv.visibleFor(true)
            nickNameOk.visibleFor(false)
            nickNameUpdate.visibleFor(true)
            if (nickNameTe.length() > 0) {
                val nickNameNew = nickNameTe.text.toString()
                WhqService.serviceInstance().updateNickName(nickNameNew)
                    .w(
                        this,
                        promptBeforeExecute = R.string.in_updating_nickname,
                        success = {
                            if (it.isSuccessful) {
                                if (it.body() == true) {
                                    currentUser.value = MOBILEMD5_NAME(currentUser.value!!.mobileMd5, nickNameNew)
                                    roomDb().persons().updateNickName(currentUser.value!!.mobileMd5, nickNameNew).w_io(this)
                                    AppCommon.sharePreferences().run p@{
                                        this.getString(AUTO_LOGIN_ACCOUNT, null)?.run {
                                            this.parse<MobileNamePasswordMd5>().run m@{
                                                if (this.mobileMd5 == currentUser.value!!.mobileMd5) {
                                                    this@p.edit {
                                                        putString(AUTO_LOGIN_ACCOUNT, this@m.copy(nickName = nickNameNew).toJson())
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    hideProcessBar(R.string.nickname_exists)
                                }
                            } else hideProcessBar(R.string.nickname_exists)
                        }
                    )
            }
        }
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.exit -> finishAndRemoveTask()
                R.id.register -> nvg(R.id.registerFragment, Authenticate.clientBundle(Authenticate.client_type_sd, R.id.search_fragment))
                R.id.login -> nvg(R.id.loginFragment, Authenticate.clientBundle(Authenticate.client_type_sd, R.id.search_fragment))
                R.id.contact -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:${R.string.sys_email}") }
                    intent.resolveActivity(this.packageManager)?.run {
                        startActivity(intent)
                    } ?: this.showDialog(getString(R.string.contact_title), alertType = AlertType.Show, showContent = "您没有安装邮件")
                }
                R.id.bug -> nvg(R.id.content_publish_fragment_bug, bundleOf(ARTICLE_TYPE to ArticleType.BUG))
                R.id.share -> {
                    SdMetadata.metadata.value!!.apkDownloadUrl.run url@{
                        startActivityForResult(Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT, """
                                |[六度]分享链接:
                                |${this@url}""".trimMargin()
                            )
                            type = "text/plain"
                        }, REQUEST_CODE_SHARE)
                    }
                }
                R.id.private_policy -> showPrivatePolicy()
                R.id.home -> SdMetadata.metadata.value?.run { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(this.homeUrl))) }
                R.id.winux_cmd -> SdMetadata.metadata.value?.run { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://sixdegree.ren:8081/download/winux_cmd.apk"))) }
                R.id.out_door -> SdMetadata.metadata.value?.run { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://sixdegree.ren:8081/download/out_door.apk"))) }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
    override fun homePage(): String {
        return SEARCH_FRAGMENT
    }

    override fun navControler(): NavController {
        return nav_host_fragment.findNavController()
    }

    override fun navIds(): Map<String, Int> {
        return mapOf(
            ADD_RELATIONS_FRAGMENT to R.id.add_relations,
            FAV_LIST_FRAGMENT to R.id.favList,
            LINK_TWO_PERSON_FRAGMENT to R.id.link_two_person,
            SHOW_ONE_PERSON_FRAGMENT to R.id.show_one_person,
            ZUPU_FRAGMENT to R.id.zupu,
            CONTENT_MY_FOCUS_FRAGMENT to R.id.content_my_focus_fragment,

            CONTENT_LIST_FRAGMENT to R.id.content_list_fragment,
            CONTENT_OTHER_FRAGMENT to R.id.content_other_fragment,
            MESSAGES_GROUP to R.id.messages_group,
            MESSAGE_SESSION to R.id.message_session,
            SHOW_ONE_PERSON to R.id.show_one_person,
            LOGIN to R.id.loginFragment,
            REGISTER to R.id.registerFragment,
            SUCCESS to R.id.successFragment,
            SEARCH_FRAGMENT to R.id.search_fragment,
            CONTENT_PUBLISH_FRAGMENT to R.id.content_publish_fragment,
            GROUP_CONTACTS to R.id.group_contacts,
            CONTACTS_SELECT to R.id.contactsSelectFragment,
            RECOMMEND_CONTENT to R.id.recommend_content,

            SETTING_PREFERENCE to R.id.setting_preference
        )
    }


    private fun getBadge(): BadgeView {
        return newCountBadge ?: BadgeFactory.createRoundRect(this)
            .setTextColor(resources.color(R.color.md_white_1000))
            .apply {
                bind(bottom_nav.getChildAt(0))
            }
            .setWidthAndHeight(25, 20)
            .setTextSize(12)
            .setBadgeGravity(Gravity.START or Gravity.TOP)
            .setSpace(2, 2)
            .apply { gone() }
            .also {
                newCountBadge = it
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return nav_host_fragment.findNavController().navigateUp(configuration!!)
    }
}

fun MainActivity.nvg(fragment: Int, bundle: Bundle) {
    nav_host_fragment.findNavController().navigate(fragment, bundle)
}


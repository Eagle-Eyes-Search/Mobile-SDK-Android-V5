package dji.simpleV5

import PermissionHelper
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dji.sampleV5.aircraft.R
import dji.simpleV5.utils.*
import dji.v5.common.utils.GeoidManager
import dji.v5.utils.common.LogUtils
import dji.v5.utils.common.StringUtils
import dji.v5.ux.core.communication.DefaultGlobalPreferences
import dji.v5.ux.core.communication.GlobalPreferencesManager
import dji.v5.ux.core.util.UxSharedPreferencesUtil
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class DJIMainActivity : AppCompatActivity() {

    private val tag: String = LogUtils.getTag(this)
    private val baseMainActivityVm: BaseMainActivityVm by viewModels()
    private val msdkInfoVm: MSDKInfoVm by viewModels()
    private val msdkManagerVM: MSDKManagerVM by globalViewModels()
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val disposable = CompositeDisposable()

    private lateinit var permissionHandler: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (shouldFinishActivity()) return

        // Set full-screen view
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        initMSDKInfoView()
        observeSDKManager()

        // Initialize the PermissionHandler
        permissionHandler = PermissionHelper(this)

        // Check and request permissions
        permissionHandler.checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        permissionHandler.checkAndRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        disposable.dispose()
    }

    private fun shouldFinishActivity(): Boolean {
        return if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
            finish()
            true
        } else false
    }

    private fun initMSDKInfoView() {
        msdkInfoVm.msdkInfo.observe(this) {
            text_view_version.text = StringUtils.getResStr(R.string.sdk_version, "${it.SDKVersion} ${it.buildVer}")
            text_view_product_name.text = StringUtils.getResStr(R.string.product_name, it.productType.name)
            text_view_package_product_category.text = StringUtils.getResStr(R.string.package_product_category, it.packageProductCategory)
            text_view_is_debug.text = StringUtils.getResStr(R.string.is_sdk_debug, it.isDebug)
        }

        view_base_info.setOnClickListener {
            baseMainActivityVm.doPairing { showToast(it) }
        }
    }

    private fun observeSDKManager() {
        msdkManagerVM.lvRegisterState.observe(this) { resultPair ->
            val statusText = if (resultPair.first) {
                showToast("Register Success")
                StringUtils.getResStr(this, R.string.registered).also { msdkInfoVm.initListener() }
            } else {
                showToast("Register Failure: ${resultPair.second}")
                StringUtils.getResStr(this, R.string.unregistered)
            }
            text_view_registered.text = StringUtils.getResStr(R.string.registration_status, statusText)
            if (resultPair.first) handler.postDelayed({ prepareUxActivity() }, 5000)
        }

        msdkManagerVM.lvProductConnectionState.observe(this) {
            showToast("Product: ${it.second}, ConnectionState: ${it.first}")
        }

        msdkManagerVM.lvProductChanges.observe(this) {
            showToast("Product: $it Changed")
        }

        msdkManagerVM.lvInitProcess.observe(this) {
            showToast("Init Process event: ${it.first.name}")
        }

        msdkManagerVM.lvDBDownloadProgress.observe(this) {
            showToast("Database Download Progress: ${it.first}/${it.second}")
        }
    }

    private fun showToast(content: String) {
        ToastUtils.showToast(content)
    }

    private fun <T> enableDefaultLayout(cl: Class<T>) {
        enableShowCaseButton(default_layout_button, cl)
    }

    private fun <T> enableShowCaseButton(view: View, cl: Class<T>) {
        view.isEnabled = true
        view.setOnClickListener {
            Intent(this, cl).also { startActivity(it) }
        }
    }

    private fun prepareUxActivity() {
        UxSharedPreferencesUtil.initialize(this)
        GlobalPreferencesManager.initialize(DefaultGlobalPreferences(this))
        GeoidManager.getInstance().init(this)
        enableDefaultLayout(SimplePilotingActivity::class.java)
    }

}
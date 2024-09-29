package dji.simpleV5

import PermissionHelper
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dji.simpleV5.dji_sdk5_utils.MSDKManagerVM2
import dji.simpleV5.dji_sdk5_utils.globalViewModels
//import dji.v5.utils.common.LogUtils
//import dji.v5.utils.common.StringUtils
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*


/*
This is an adaptation od DJIMainActivity from the sample app
https://github.com/dji-sdk/Mobile-SDK-Android-V5/blob/dev-sdk-main/SampleCode-V5/android-sdk-v5-sample/src/main/java/dji/sampleV5/aircraft/DJIMainActivity.kt

DJI-specific functionality has been factored out to MSDKManagerVM2.kt

 */

class ConnectionActivity : AppCompatActivity() {

    private val tag: String = "ConnectionActivity"
//    private val baseMainActivityVm: BaseMainActivityVm by viewModels()
//    private val msdkInfoVm: MSDKInfoVm by viewModels()
//    private val msdkManagerVM: ISDKManager by globalViewModels<MSDKManagerVM2>()
    private val msdkManagerVM: IMSDKManager by globalViewModels<MSDKManagerVM2>()
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

//        initMSDKInfoView()
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

//    private fun initMSDKInfoView() {
//
//
//
////        msdkInfoVm.msdkInfo.observe(this) {
////            val summaryText = "SDK Version: ${msdkInfoVm.msdkInfo.value?.SDKVersion} ${msdkInfoVm.msdkInfo.value?.buildVer}" +
////                    "\nProduct Name: ${msdkInfoVm.msdkInfo.value?.productType?.name}" +
////                    "\nPackage Product Category: ${msdkInfoVm.msdkInfo.value?.packageProductCategory}" +
////                    "\nIs SDK Debug: ${msdkInfoVm.msdkInfo.value?.isDebug}"
////            text_view_msdk_info.text = summaryText
////        }
//
////        view_base_info.setOnClickListener {
//////            baseMainActivityVm.doPairing { showToast(it) }
////        }
//    }

    private fun updateInfoDisplay() {
        val productInfo = msdkManagerVM.systemState.value
        val summaryText = "SDK Version: ${productInfo?.sdkVersion} build: ${productInfo?.buildVersion}" +
                "\nProduct Name: ${productInfo?.productType}" +
                "\nConnection State: ${msdkManagerVM.productConnectionState.value?.second}" +
                "\nRegistered: ${msdkManagerVM.registrationStatus.value?.second}"
        text_view_msdk_info.text = summaryText
    }

    private fun observeSDKManager() {
        msdkManagerVM.registrationStatus.observe(this) { (isRegistered, statusString) ->
//            val statusText = if (isRegistered) {"Register Success"
////                StringUtils.getResStr(this, R.string.registered).also { msdkInfoVm.initListener() }
//            } else {
//                "Register Failure: ${isRegistered}"
////                StringUtils.getResStr(this, R.string.unregistered)
//            }
//            text_view_registered.text = statusString
            updateInfoDisplay()

            if (isRegistered) handler.postDelayed({ openCockpitDoor() }, 5000)
        }

        msdkManagerVM.productConnectionState.observe(this) {
            showToast("Product: ${it.second}, ConnectionState: ${it.first}")
            // Sho the product connection state
//            text_view_msdk_info.text = "Product: ${it.second}, ConnectionState: ${it.first}"
            updateInfoDisplay()
        }

        msdkManagerVM.systemState.observe(this) {
            showToast("System State: ${it.sdkVersion}")
            updateInfoDisplay()
        }

//        msdkManagerVM.lvProductChanges.observe(this) {
////            showToast("Product: $it Changed")
//            updateInfoDisplay()
//        }
//
//        msdkManagerVM.lvInitProcess.observe(this) {
//            showToast("Init Process event: ${it.first.name}")
//        }
//
//        msdkManagerVM.lvDBDownloadProgress.observe(this) {
//            showToast("Database Download Progress: ${it.first}/${it.second}")
//        }
    }

    private fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }

//    private fun <T> enableDefaultLayout(cl: Class<T>) {
//        enableShowCaseButton(default_layout_button, cl)
//    }

//    private fun <T> enableShowCaseButton(view: View, cl: Class<T>) {
//        view.isEnabled = true
//        view.setOnClickListener {
//            Intent(this, cl).also { startActivity(it) }
//        }
//    }

    private fun openCockpitDoor() {
        default_layout_button.isEnabled = true
        default_layout_button.setOnClickListener {
//            showToast("Opening Cockpit Door...")
            it.setOnClickListener {
                Intent(this, SimplePilotingActivity::class.java).also { startActivity(it) }
            }
        }
    }

}
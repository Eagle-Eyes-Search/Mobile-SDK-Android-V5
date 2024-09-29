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
    private val msdkManagerVM: IMSDKManager by globalViewModels<MSDKManagerVM2>()
    private val handler: Handler = Handler(Looper.getMainLooper())

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

        // Create observers that update the UI in response to changes in SDK
        msdkManagerVM.registrationStatus.observe(this) { (isRegistered, statusString) ->
            updateInfoDisplay()
            if (isRegistered) handler.postDelayed({ openCockpitDoor() }, 5000)
        }
        msdkManagerVM.productConnectionState.observe(this) {
            showToast("Product: ${it.second}, ConnectionState: ${it.first}")
            updateInfoDisplay()
        }
        msdkManagerVM.systemState.observe(this) {
            showToast("System State: ${it.sdkVersion}")
            updateInfoDisplay()
        }

        // Initialize the PermissionHandler and request permissions
        permissionHandler = PermissionHelper(this).also { it.checkAndRequestPermissions()  }
//        permissionHandler.checkAndRequestPermissions()

    }

    override fun onResume() {
        super.onResume()
        permissionHandler.checkAndRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)

    }

    private fun shouldFinishActivity(): Boolean {
        return if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
            finish()
            true
        } else false
    }

    private fun updateInfoDisplay() {
        val productInfo = msdkManagerVM.systemState.value
        val summaryText = "SDK Version: ${productInfo?.sdkVersion} build: ${productInfo?.buildVersion}" +
                "\nProduct Name: ${productInfo?.productType}" +
                "\nConnection State: ${msdkManagerVM.productConnectionState.value?.second}" +
                "\nRegistered: ${msdkManagerVM.registrationStatus.value?.second}"
        text_view_msdk_info.text = summaryText
    }

    private fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }

    private fun openCockpitDoor() {
        default_layout_button.isEnabled = true
        default_layout_button.setOnClickListener {
            it.setOnClickListener {
                Intent(this, SimplePilotingActivity::class.java).also { startActivity(it) }
            }
        }
    }

}
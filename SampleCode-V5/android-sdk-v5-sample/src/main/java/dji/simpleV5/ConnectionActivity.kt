package dji.simpleV5

import DJIPermissionHelper
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dji.simpleV5.dji_sdk5_utils.DjiSdk5Manager
import dji.simpleV5.dji_sdk5_utils.globalViewModels
import dji.v5.ux.sample.showcase.defaultlayout.DefaultLayoutActivity
//import dji.v5.utils.common.LogUtils
//import dji.v5.utils.common.StringUtils
import kotlinx.android.synthetic.main.activity_connection.simple_cockpit_button
import kotlinx.android.synthetic.main.activity_connection.fancy_cockpit_button
import kotlinx.android.synthetic.main.activity_connection.intermediate_cockpit_button
import kotlinx.android.synthetic.main.activity_connection.text_view_msdk_info


/*
This is an adaptation od DJIMainActivity from the sample app
https://github.com/dji-sdk/Mobile-SDK-Android-V5/blob/dev-sdk-main/SampleCode-V5/android-sdk-v5-sample/src/main/java/dji/sampleV5/aircraft/DJIMainActivity.kt

DJI-specific functionality has been factored out to MSDKManagerVM2.kt

 */

class ConnectionActivity : AppCompatActivity() {

    private val tag: String = "ConnectionActivity"
    private val msdkManagerVM: IMSDKManager by globalViewModels<DjiSdk5Manager>()
    private lateinit var permissionHandler: DJIPermissionHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        if (shouldFinishActivity()) return

        simple_cockpit_button.setOnClickListener {Intent(this, SimplePilotingActivity::class.java).also { startActivity(it) }}
        intermediate_cockpit_button.setOnClickListener {Intent(this, VideoAndLocationPilotingActivity::class.java).also { startActivity(it) }}
        fancy_cockpit_button.setOnClickListener {Intent(this, DefaultLayoutActivity::class.java).also { startActivity(it) }}


        // Set full-screen view
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Create observers that update the UI in response to changes in SDK
        msdkManagerVM.registrationStatus.observe(this) { (isRegistered, statusString) ->
            updateInfoDisplay()
            if (isRegistered){

                simple_cockpit_button.postDelayed({
                    simple_cockpit_button.isEnabled = true
                    intermediate_cockpit_button.isEnabled = true
                    fancy_cockpit_button.isEnabled = true
                  } , 2000)
            }  // Any view will do
        }
        msdkManagerVM.productConnectionState.observe(this) {
            showToast(it.message)
            updateInfoDisplay()
        }
        msdkManagerVM.systemState.observe(this) {
            showToast("System State: ${it.sdkVersion}")
            updateInfoDisplay()
        }

        // Initialize the PermissionHandler and request permissions
        permissionHandler = DJIPermissionHelper(this).also { it.checkAndRequestPermissions()  }
//        permissionHandler.checkAndRequestPermissions()
        log("onCreate done")
    }

    fun log(message: String) = Log.d(tag, message)

    override fun onResume() {
        super.onResume()
        permissionHandler.checkAndRequestPermissions()
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
//                "\nConnection State: ${msdkManagerVM.productConnectionState.value?.second}" +
                "\nRegistered: ${msdkManagerVM.registrationStatus.value?.message}"
        text_view_msdk_info.text = summaryText
    }

    private fun showToast(content: String) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }

//    private fun openCockpitDoor(activityClass: Class<*> = SimplePilotingActivity::class.java) {
//        default_layout_button.isEnabled = true
//        default_layout_button.setOnClickListener {
//            it.setOnClickListener {
//                Intent(this, activityClass).also { startActivity(it) }
//            }
//        }
//    }

}
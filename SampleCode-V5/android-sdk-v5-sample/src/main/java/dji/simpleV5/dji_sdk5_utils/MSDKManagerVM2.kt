package dji.simpleV5.dji_sdk5_utils

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dji.simpleV5.IMSDKManager
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.common.utils.GeoidManager
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback
import dji.v5.ux.core.communication.DefaultGlobalPreferences
import dji.v5.ux.core.communication.GlobalPreferencesManager
import dji.v5.ux.core.util.UxSharedPreferencesUtil

class MSDKManagerVM2 : ViewModel(), IMSDKManager {
    // The data is held in livedata mode, but you can also save the results of the sdk callbacks any way you like.
    val lvRegisterState = MutableLiveData<Pair<Boolean, IDJIError?>>()
    val lvProductConnectionState = MutableLiveData<Pair<Boolean, Int>>()
    val lvProductChanges = MutableLiveData<Int>()
    val lvInitProcess = MutableLiveData<Pair<DJISDKInitEvent, Int>>()
    val lvDBDownloadProgress = MutableLiveData<Pair<Long, Long>>()


    override val registrationStatus: MutableLiveData<Pair<Boolean, String>> by lazy {
        MutableLiveData<Pair<Boolean, String>>()
    }
    override val productConnectionState: MutableLiveData<Pair<Boolean, String>> by lazy {
        MutableLiveData<Pair<Boolean, String>>()
    }


    override fun initMobileSDK(appContext: Context) {
        // Initialize and set the sdk callback, which is held internally by the sdk until destroy() is called
        SDKManager.getInstance().init(appContext, object : SDKManagerCallback {
            override fun onRegisterSuccess() {
                lvRegisterState.postValue(Pair(true, null))
                registrationStatus.postValue(Pair(true, "Registration Successful"))

                // Ok - initialize a bunch of stuff - God only knows what this does - got it from the sample:
                // https://github.com/dji-sdk/Mobile-SDK-Android-V5/blob/dbbd5ad95347039ba48e04011f2531cd89c9d1d7/SampleCode-V5/android-sdk-v5-sample/src/main/java/dji/sampleV5/aircraft/DJIAircraftMainActivity.kt#L20
                UxSharedPreferencesUtil.initialize(appContext)
                GlobalPreferencesManager.initialize(DefaultGlobalPreferences(appContext))
                GeoidManager.getInstance().init(appContext)

            }

            override fun onRegisterFailure(error: IDJIError) {
                lvRegisterState.postValue(Pair(false, error))
                registrationStatus.postValue(Pair(false, "Registration Failed - ${error.description()}"))
            }

            override fun onProductDisconnect(productId: Int) {
                lvProductConnectionState.postValue(Pair(false, productId))
                productConnectionState.postValue(Pair(false, "Product Disconnected"))
            }

            override fun onProductConnect(productId: Int) {
                lvProductConnectionState.postValue(Pair(true, productId))
                productConnectionState.postValue(Pair(true, "Product Connected"))
            }

            override fun onProductChanged(productId: Int) {
                lvProductChanges.postValue(productId)
                productConnectionState.postValue(Pair(true, "Product Changed"))
            }

            override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                lvInitProcess.postValue(Pair(event, totalProcess))
                // Don't forget to call the registerApp()
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    SDKManager.getInstance().registerApp()
                }
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                lvDBDownloadProgress.postValue(Pair(current, total))
            }
        })
    }

    fun destroyMobileSDK() {
        SDKManager.getInstance().destroy()
    }

}
package dji.simpleV5

import android.content.Context
import androidx.lifecycle.MutableLiveData


data class DroneSDKInfo(
    val sdkVersion: String,
    var buildVersion: String?= null,
//    var isDebug: Boolean = false
//    var packageProductCategory: PackageProductCategory? = null
    var productType: String? = null,
    var networkInfo: String? = null,
    var countryCode: String? = null,
    var firmwareVer: String? = null,
)


interface IMSDKManager{

    val registrationStatus: MutableLiveData<Pair<Boolean, String>>
    val productConnectionState: MutableLiveData<Pair<Boolean, String>>

    fun initMobileSDK(appContext: Context)

    fun getDroneSDKInfo(): DroneSDKInfo

}




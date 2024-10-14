package dji.simpleV5

import android.content.Context
import androidx.lifecycle.MutableLiveData


data class SystemState(
    val sdkVersion: String,
    var buildVersion: String?= null,
//    var isDebug: Boolean = false
//    var packageProductCategory: PackageProductCategory? = null
    var productType: String? = null,
    var networkInfo: String? = null,
    var countryCode: String? = null,
    var firmwareVer: String? = null,
)


data class ConnectionState(
    val isConnected: Boolean,
    val customMessage: String?,  // Message describing the connection state (e.g. "Connected", "Disconnected")
    val additionalInfo: String? = null
) {
    val message = customMessage ?: if (isConnected) "Connected" else "Disconnected"
}


interface IMSDKManager{

    val registrationStatus: MutableLiveData<ConnectionState>
    val productConnectionState: MutableLiveData<ConnectionState>
    val systemState: MutableLiveData<SystemState>

    fun initMobileSDK(appContext: Context)

}




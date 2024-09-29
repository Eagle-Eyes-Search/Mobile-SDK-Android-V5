package dji.simpleV5

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dji.sdk.keyvalue.value.product.ProductType
import dji.simpleV5.dji_sdk5_utils.data.DEFAULT_STR
import dji.v5.common.register.PackageProductCategory
import dji.v5.utils.inner.SDKConfig


data class MSDKInfo(val SDKVersion: String = DEFAULT_STR) {
    var buildVer: String? = DEFAULT_STR
//    var isDebug: Boolean = false
//    var packageProductCategory: PackageProductCategory? = null
    var productType: String = "Unknown"
    var networkInfo: String? = null
    var countryCode: String? = null
    var firmwareVer: String? = null
}


interface IMSDKManager{

    val registrationStatus: MutableLiveData<Pair<Boolean, String>>
    val productConnectionState: MutableLiveData<Pair<Boolean, String>>
    val msdkInfo: MutableLiveData<String>


    fun initMobileSDK(appContext: Context)

}




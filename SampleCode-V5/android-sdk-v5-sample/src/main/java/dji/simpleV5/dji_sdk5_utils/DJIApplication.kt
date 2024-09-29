package dji.simpleV5.dji_sdk5_utils

import android.app.Application
import android.content.Context
import dji.simpleV5.IMSDKManager

/**
 * Class Description
 *
 * @author Hoker
 * @date 2022/3/1
 *
 * Copyright (c) 2022, DJI All Rights Reserved.
 */
open class DJIApplication : Application() {

//    private val msdkManagerVM2: MSDKManagerVM by globalViewModels()
    private val msdkManagerVM: IMSDKManager by globalViewModels<MSDKManagerVM2>()

    override fun onCreate() {
        super.onCreate()

        // Ensure initialization is called first
        msdkManagerVM.initMobileSDK(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        com.cySdkyc.clx.Helper.install(this)
    }

}

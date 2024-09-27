package dji.simpleV5.dji_sdk5_utils

import androidx.lifecycle.ViewModel
import dji.v5.utils.common.LogUtils

/**
 * Class Description
 *
 * @author Hoker
 * @date 2021/7/5
 *
 * Copyright (c) 2021, DJI All Rights Reserved.
 */
open class DJIViewModel : ViewModel() {
    val toastResult
        get() = DJIToastUtil.dJIToastLD

    val logTag = LogUtils.getTag(this)

}
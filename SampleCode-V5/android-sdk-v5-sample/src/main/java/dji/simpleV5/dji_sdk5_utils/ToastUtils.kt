package dji.simpleV5.dji_sdk5_utils

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import dji.v5.utils.common.ContextUtil
import java.lang.ref.WeakReference

object ToastUtils {
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var toastRef: WeakReference<Toast>? = null

    fun showToast(msg: String) {
        showLongToast(msg)
    }

    fun showLongToast(msg: String) {
        showToast(msg, Toast.LENGTH_LONG)
    }

    fun showShortToast(msg: String) {
        showToast(msg, Toast.LENGTH_SHORT)
    }

    @Synchronized
    fun showToast(msg: String, duration: Int) {
        handler.post {
            toastRef?.let {
                it.get()?.cancel()
                it.clear()
            }
            toastRef = null;
            val toast = Toast.makeText(ContextUtil.getContext(), msg, duration)
            toastRef = WeakReference(toast)
            toast.show()
        }
    }

}

object DJIToastUtil {
    var dJIToastLD: MutableLiveData<DJIToastResult>? = null
}

class DJIToastResult(var isSuccess: Boolean, var msg: String? = null) {

    companion object {
        fun success(msg: String? = null): DJIToastResult {
            return DJIToastResult(true, "success ${msg ?: ""}")
        }

        fun failed(msg: String): DJIToastResult {
            return DJIToastResult(false, msg)
        }
    }
}

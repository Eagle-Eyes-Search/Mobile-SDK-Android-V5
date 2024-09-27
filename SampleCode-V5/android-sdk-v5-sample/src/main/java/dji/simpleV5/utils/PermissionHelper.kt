package dji.simpleV5.utils

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import dji.v5.utils.common.PermissionUtil

class PermissionHelper(private val context: Context) {

    fun getPermissionArray(): Array<String> {
        val permissions = arrayListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.KILL_BACKGROUND_PROCESSES,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addAll(listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            ))
        } else {
            permissions.addAll(listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }

        return permissions.toArray(arrayOf())
    }

    fun checkPermissions(): Boolean {
        val permissions = getPermissionArray()
        return permissions.all { permission ->
//            ContextCompat.checkSelfPermission(context, permission) == PermissionUtil.PERMISSION_GRANTED
            PermissionUtil.isPermissionGranted(context, permission)
        }
    }

    fun requestPermissions(launcher: ActivityResultLauncher<Array<String>>) {
        launcher.launch(getPermissionArray())
    }
}

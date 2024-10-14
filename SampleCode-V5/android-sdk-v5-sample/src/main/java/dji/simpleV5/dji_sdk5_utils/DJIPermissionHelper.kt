import android.Manifest
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dji.v5.utils.common.PermissionUtil


/** Helper class to request permissions needed for dji sdk */
class DJIPermissionHelper(
    private val activity: AppCompatActivity,
    private val onPermissionsGranted: () -> Unit = {}
) {
    private val permissionArray = getPermissionArray()

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.all { it.value }) {
                onPermissionsGranted()
            } else {
                requestPermissions()
            }
        }

    fun checkAndRequestPermissions() {
        if (arePermissionsGranted()) {
            onPermissionsGranted()
        } else {
            requestPermissions()
        }
    }

    private fun arePermissionsGranted(): Boolean {
        return permissionArray.all { PermissionUtil.isPermissionGranted(activity, it) }
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(permissionArray.toTypedArray())
    }

    private fun getPermissionArray(): List<String> {
        return mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.KILL_BACKGROUND_PROCESSES,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                addAll(
                    listOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_AUDIO
                    )
                )
            } else {
                addAll(
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }
}

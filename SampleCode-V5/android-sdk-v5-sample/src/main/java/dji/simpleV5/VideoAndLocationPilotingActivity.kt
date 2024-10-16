package dji.simpleV5

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dji.sdk.keyvalue.value.common.Attitude
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.sdk.keyvalue.value.common.LocationCoordinate2D
import dji.simpleV5.dji_sdk5_utils.SimpleDJIVideoWidget
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun Double?.formatAsLatLong(): String = this?.let { "%.6f".format(it) } ?: "?"
fun Double?.formatAsAltitude(): String = this?.let { "%.2f".format(it) } ?: "?"
fun Double?.formatAsAttitude(): String = this?.let { "%.2f".format(it) } ?: "?"


/**
 * Simple
 */
class VideoAndLocationPilotingActivity : AppCompatActivity() {

    private lateinit var primaryFpvWidget: SimpleDJIVideoWidget
    private lateinit var locationText: TextView
    private val djiSdk5DroneAccessor: DjiSdk5DroneAccessor by lazy { DjiSdk5DroneAccessor() }

    fun log(message: String) = Log.d("VideoAndLocationPilotingActivity", message)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_and_location_piloting)

        primaryFpvWidget = findViewById(R.id.widget_primary_fpv)
        primaryFpvWidget.updateVideoSource(ComponentIndexType.LEFT_OR_MAIN)

        // Initialize the location text view and set up a flow to update it with the aircraft's location
        locationText = findViewById(R.id.textLocationInfo)
        locationText.text = "Awaiting Location..."

        // Get the flows from the DjiFlowProvider
        djiSdk5DroneAccessor.recalibrateGimbal()  // Recalibrate the gimbal mainly to make sure the yaw offset at North is 0

        // Use the custom combineNullable function
        lifecycleScope.launch {
            combineNullable(
                lifecycleScope,
                djiSdk5DroneAccessor.getAircraftLocationFlow(),
                djiSdk5DroneAccessor.getAircraftAttitudeFlow(),
                djiSdk5DroneAccessor.getGimbalAttitudeFlow(),
                djiSdk5DroneAccessor.getHomePointFlow(),
            )
                .catch { error ->
                    locationText.text = error.message ?: "Error updating status"
                }
//                .collectLatest { (location: LocationCoordinate3D, aircraftAttitude: Attitude, gimbalAttitude: Attitude, homePoint: LocationCoordinate2D) ->
                .collectLatest { (location, aircraftAttitude, gimbalAttitude, homePoint) ->
                    val status =
                        "Lat: ${location?.latitude.formatAsLatLong()}, Lon: ${location?.longitude.formatAsLatLong()}, Alt: ${location?.altitude.formatAsAltitude()}\n" +
                        "Aircraft: Pitch: ${aircraftAttitude?.pitch.formatAsAttitude()}, Roll: ${aircraftAttitude?.roll.formatAsAttitude()}, Yaw: ${aircraftAttitude?.yaw.formatAsAttitude()}\n" +
                        "Gimbal: Pitch: ${gimbalAttitude?.pitch.formatAsAttitude()}, Roll: ${gimbalAttitude?.roll.formatAsAttitude()}, Yaw: ${gimbalAttitude?.yaw.formatAsAttitude()}\n" +
                        "Home: Lat: ${homePoint?.latitude.formatAsLatLong()}, Lon: ${homePoint?.longitude.formatAsLatLong()}"
                    log(status)
                    locationText.text = status
                }
        }
    }
}

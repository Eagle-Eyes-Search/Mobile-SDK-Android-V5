package dji.simpleV5

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.simpleV5.dji_sdk5_utils.SimpleDJIVideoWidget

class VideoAndLocationPilotingActivity : AppCompatActivity() {

    private lateinit var primaryFpvWidget: SimpleDJIVideoWidget
    private lateinit var locationText: TextView

    // Simplified LocationWidgetModel
    private lateinit var locationWidgetModel: LocationWidgetModelv2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_and_location_piloting)

        primaryFpvWidget = findViewById(R.id.widget_primary_fpv)
        primaryFpvWidget.updateVideoSource(ComponentIndexType.LEFT_OR_MAIN)

        locationText = findViewById(R.id.textLocationInfo)
        locationText.text = "Awaiting Location..."

        // Initialize and subscribe to location updates
        locationWidgetModel = LocationWidgetModelv2().also {
            it.subscribe { result ->
                updateLocation(result)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unsubscribe to prevent memory leaks
        locationWidgetModel.unsubscribe()
    }

    private fun updateLocation(locationResult: Result<LocationWidgetModelv2.Location>) {
        locationResult.fold(
            onSuccess = { location ->
                val latitude = String.format("%.6f", location.latitude)
                val longitude = String.format("%.6f", location.longitude)
                locationText.text = "Lat: $latitude, Lon: $longitude"
            },
            onFailure = { exception ->
                locationText.text = exception.message
            }
        )
    }
}

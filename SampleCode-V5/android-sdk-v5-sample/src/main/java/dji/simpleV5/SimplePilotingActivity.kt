package dji.simpleV5
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.simpleV5.dji_sdk5_utils.SimpleDJIVideoWidget
import dji.v5.ux.core.widget.fpv.FPVWidget

class SimplePilotingActivity : AppCompatActivity() {

    private lateinit var primaryFpvWidget: SimpleDJIVideoWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_piloting)

        primaryFpvWidget = findViewById(R.id.widget_primary_fpv)
        // Set the video source to the main camera
        primaryFpvWidget.updateVideoSource(ComponentIndexType.LEFT_OR_MAIN)
//        primaryFpvWidget.overlay.r

    }
}

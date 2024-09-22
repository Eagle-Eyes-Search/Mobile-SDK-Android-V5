package dji.simpleV5

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dji.v5.ux.R
import dji.v5.ux.core.widget.fpv.FPVWidget
import dji.sdk.keyvalue.value.common.ComponentIndexType

class SimplePilotingActivity : AppCompatActivity() {

    private lateinit var primaryFpvWidget: FPVWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_piloting)

        primaryFpvWidget = findViewById(R.id.widget_primary_fpv)
        // Set the video source to the main camera
        primaryFpvWidget.updateVideoSource(ComponentIndexType.LEFT_OR_MAIN)
    }
}

package dji.simpleV5.dji_sdk5_utils

import android.content.Context
import android.util.AttributeSet
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.constraintlayout.widget.ConstraintLayout
import dji.sdk.keyvalue.value.common.CameraLensType
import dji.sdk.keyvalue.value.common.ComponentIndexType
import dji.v5.manager.interfaces.ICameraStreamManager
import dji.v5.utils.common.LogPath
import dji.v5.utils.common.LogUtils
import dji.v5.ux.R
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.widget.ConstraintLayoutWidget
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.module.FlatCameraModule
import dji.v5.ux.core.widget.fpv.FPVStreamSourceListener
import dji.v5.ux.core.widget.fpv.FPVWidgetModel
import io.reactivex.rxjava3.core.Flowable

private const val TAG = "FPVWidget"

open class SimpleDJIVideoWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayoutWidget<SimpleDJIVideoWidget.ModelState>(context, attrs, defStyleAttr) {

    private lateinit var fpvSurfaceView: SurfaceView
    private var surface: Surface? = null
    private var width = -1
    private var height = -1

    private val cameraSurfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            surface = holder.surface
            LogUtils.i(LogPath.SAMPLE, "surfaceCreated: ${widgetModel.getCameraIndex()}")
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            this@SimpleDJIVideoWidget.width = width
            this@SimpleDJIVideoWidget.height = height
            LogUtils.i(LogPath.SAMPLE, "surfaceChanged: ${widgetModel.getCameraIndex()}, width:$width, height:$height")
            updateCameraStream()
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            width = 0
            height = 0
            LogUtils.i(LogPath.SAMPLE, "surfaceDestroyed: ${widgetModel.getCameraIndex()}")
            removeSurfaceBinding()
        }
    }

    val widgetModel: FPVWidgetModel = FPVWidgetModel(
        DJISDKModel.getInstance(), ObservableInMemoryKeyedStore.getInstance(), FlatCameraModule()
    )

    init {
        if (!isInEditMode) {
            // Initialize the view using the abstract initView method
            initView(context, attrs, defStyleAttr)
            // Add the SurfaceHolder callback
            fpvSurfaceView.holder.addCallback(cameraSurfaceCallback)
        } else {
            // For design time preview
            fpvSurfaceView = SurfaceView(context)
        }
    }

    override fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        // Inflate the layout and initialize the fpvSurfaceView
        inflate(context, R.layout.uxsdk_widget_fpv, this)
        fpvSurfaceView = findViewById(R.id.surface_view_fpv)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode) {
            widgetModel.setup()
        }
    }

    override fun onDetachedFromWindow() {
        if (!isInEditMode) {
            widgetModel.cleanup()
        }
        super.onDetachedFromWindow()
    }

    override fun reactToModelChanges() {
        // Implement necessary reactions here if any
    }

    fun updateVideoSource(source: ComponentIndexType) {
        LogUtils.i(LogPath.SAMPLE, "updateVideoSource", source, this)
        widgetModel.updateCameraSource(source, CameraLensType.UNKNOWN)
        updateCameraStream()
        fpvSurfaceView.invalidate()
    }

    fun setOnFPVStreamSourceListener(listener: FPVStreamSourceListener) {
        widgetModel.streamSourceListener = listener
    }

    fun setSurfaceViewZOrderOnTop(onTop: Boolean) {
        fpvSurfaceView.setZOrderOnTop(onTop)
    }

    fun setSurfaceViewZOrderMediaOverlay(isMediaOverlay: Boolean) {
        fpvSurfaceView.setZOrderMediaOverlay(isMediaOverlay)
    }

    private fun updateCameraStream() {
        removeSurfaceBinding()
        surface?.let {
            widgetModel.putCameraStreamSurface(
                it,
                width,
                height,
                ICameraStreamManager.ScaleType.CENTER_INSIDE
            )
        }
    }

    private fun removeSurfaceBinding() {
        if (width <= 0 || height <= 0 || surface == null) {
            surface?.let {
                widgetModel.removeCameraStreamSurface(it)
            }
        }
    }

    override fun getWidgetStateUpdate(): Flowable<ModelState> {
        return super.getWidgetStateUpdate()
    }

    sealed class ModelState
}

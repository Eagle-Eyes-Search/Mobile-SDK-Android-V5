package dji.simpleV5


import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.value.common.LocationCoordinate2D
import dji.sdk.keyvalue.key.KeyTools
import dji.simpleV5.dji_sdk5_utils.DjiSdk5Manager
import io.reactivex.rxjava3.core.Flowable
import dji.v5.ux.core.base.DJISDKModel
import dji.v5.ux.core.base.WidgetModel
import dji.v5.ux.core.communication.ObservableInMemoryKeyedStore
import dji.v5.ux.core.util.DataProcessor
import dji.v5.utils.common.LocationUtil
import dji.v5.ux.core.widget.location.LocationWidgetModel.LocationState.ProductDisconnected


class LocationWidgetModelv2 {

    data class Location(val latitude: Double, val longitude: Double)

    private val keyValueManager = DjiSdk5Manager.getInstance().keyValueManager
    private val aircraftLocationKey = FlightControllerKey.KeyAircraftLocation.create()
    private val locationSubject = PublishSubject.create<Result<Location>>()
    private val disposables = CompositeDisposable()

    /**
     * Subscribe to location updates with a callback.
     */
    fun subscribe(onLocationUpdate: (Result<Location>) -> Unit) {
        // Listen to the aircraft location key
        keyValueManager.listen(
            aircraftLocationKey,
            this
        ) { _, newValue ->
            val locationCoordinate2D = newValue as? LocationCoordinate2D
            if (locationCoordinate2D != null &&
                LocationUtil.checkLatitude(locationCoordinate2D.latitude) &&
                LocationUtil.checkLongitude(locationCoordinate2D.longitude)
            ) {
                val location = Location(locationCoordinate2D.latitude, locationCoordinate2D.longitude)
                locationSubject.onNext(Result.success(location))
            } else {
                locationSubject.onNext(Result.failure(Exception("Location Unavailable")))
            }
        }

        // Observe the locationSubject on the main thread
        disposables.add(
            locationSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onLocationUpdate)
        )
    }

    /**
     * Unsubscribe from location updates.
     */
    fun unsubscribe() {
        // Stop listening to the key and dispose of subscriptions
        keyValueManager.cancelListen(aircraftLocationKey, this)
        disposables.clear()
    }
}

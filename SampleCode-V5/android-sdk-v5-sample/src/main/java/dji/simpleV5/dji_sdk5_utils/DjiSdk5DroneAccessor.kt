package dji.simpleV5

import android.util.Log
import dji.sdk.keyvalue.key.DJIKeyInfo
import dji.sdk.keyvalue.key.FlightControllerKey
import dji.sdk.keyvalue.key.KeyTools
import dji.sdk.keyvalue.key.co_w
import dji.sdk.keyvalue.value.common.Attitude
import dji.sdk.keyvalue.value.common.EmptyMsg
import dji.sdk.keyvalue.value.common.LocationCoordinate2D
import dji.sdk.keyvalue.value.common.LocationCoordinate3D
import dji.v5.common.callback.CommonCallbacks.CompletionCallbackWithParam
import dji.v5.common.error.IDJIError
import dji.v5.manager.KeyManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * A utility class to subscribe to DJIKeyInfo keys and expose their values as Kotlin Flows.
 * This class handles thread management and cleanup internally, reducing the burden on the caller.
 *
 * Example Usage:
 * ```
 * val djiSdk5DroneAccessor = DjiSdk5DroneAccessor()
 * lifecycleScope.launch {
 *     djiSdk5DroneAccessor.getAircraftLocationFlow()
 *         .collect { location ->
 *             println("Aircraft Location: (${location.latitude}, ${location.longitude}, ${location.altitude})")
 *         }
 * }
 * ```
 *
 * @param observeDispatcher The dispatcher on which emissions should be observed (default: Dispatchers.Main).
 */
class DjiSdk5DroneAccessor(
) {

    private val tag = "DjiDroneAccessor"
    private val djiKeyManager = KeyManager.getInstance()

    /**
     * Subscribes to a DJIKeyInfo key and returns a Flow emitting its values.
     *
     * @param T The type of the data emitted by the key.
     * @param key The DJIKeyInfo key to subscribe to.
     * @return A Flow emitting values of type T from the key.
     */
    fun <T> subscribe(
        key: DJIKeyInfo<T>,
        dispatcher: CoroutineDispatcher = Dispatchers.IO

    ): Flow<T> = callbackFlow {
        val observer = Any()  // Unique observer to track this subscription
        val actualKey = KeyTools.createKey(key)

        // Start listening to the key using DJI's KeyManager
        djiKeyManager.listen(actualKey, observer) { _, newValue ->
            @Suppress("UNCHECKED_CAST")
            trySend(newValue as T).isSuccess  // Emit the new value to the flow
        }

        // Handle cancellation and cleanup when the flow is closed
        awaitClose {
            djiKeyManager.cancelListen(actualKey, observer)
        }
    }.flowOn(dispatcher)  // Switch upstream flow to the specified dispatcher

    fun recalibrateGimbal() {
        djiKeyManager.performAction(KeyTools.createKey(co_w.KeyGimbalCalibrate), object : CompletionCallbackWithParam<EmptyMsg> {
            override fun onSuccess(result: EmptyMsg) {
                println("Gimbal yaw recalibration started")
            }

            override fun onFailure(error: IDJIError) {
                println("Gimbal yaw recalibration failed: ${error.description()}")
            }
        })
    }

    /** Returns a Flow emitting the aircraft's 3D location coordinates.
     *   latitude, longitude are in degrees
     *   altitude is in meters above home-point
     */
    fun getAircraftLocationFlow(): Flow<LocationCoordinate3D> =
        subscribe(FlightControllerKey.KeyAircraftLocation3D)

    /** Returns a Flow emitting the aircraft's attitude (pitch, roll, yaw).
     *    pitch is in degrees up from horizontal
     *    roll is in degrees right from vertical
     *    yaw is in degrees clockwise from North
     */
    fun getAircraftAttitudeFlow(): Flow<Attitude> =
        subscribe(FlightControllerKey.KeyAircraftAttitude)

    /** Returns a Flow emitting the gimbal's attitude (pitch, roll, yaw).
     *    pitch is in degrees up from horizontal
     *    roll is in degrees right from vertical
     *    yaw is in degrees clockwise from North*
     *
     *    *Note - Be sure to call recalibrateGimbal() before subscribing to this flow, otherwise
     *    the yaw will have some arbitrary offset
     *    https://sdk-forum.dji.net/hc/en-us/articles/9125759407769-Chapter-12-Gimbal
     */
    fun getGimbalAttitudeFlow(): Flow<Attitude> =
        subscribe(co_w.KeyGimbalAttitude)

    fun getHomePointFlow(): Flow<LocationCoordinate2D> =
        subscribe(FlightControllerKey.KeyHomeLocation)

    fun log(message: String) = Log.d("DjiFlowProvider", message)

}



/**
 * Combines two non-nullable flows into one nullable flow that emits whenever any of the input flows emit.
 * If a flow hasn't emitted yet, its value in the pair will be `null`.
 *
 * @param scope The CoroutineScope in which to launch coroutines for collecting the input flows.
 * @param flowA The first flow to combine.
 * @param flowB The second flow to combine.
 * @return A Flow emitting pairs of nullable values from the input flows whenever any of them emit.
 */
fun <A, B> combineNullable(
    scope: CoroutineScope,
    flowA: Flow<A>,
    flowB: Flow<B>
): Flow<Pair<A?, B?>> = channelFlow {
    var latestA: A? = null
    var latestB: B? = null

    // Collect from flowA
    scope.launch {
        flowA.collect { a ->
            latestA = a
            send(latestA to latestB)
        }
    }

    // Collect from flowB
    scope.launch {
        flowB.collect { b ->
            latestB = b
            send(latestA to latestB)
        }
    }

    // Ensure the flow stays active until it's cancelled
    awaitClose {
        // Clean-up if necessary
    }
}


/**
 * Combines three non-nullable flows into one nullable flow that emits whenever any of the input flows emit.
 * If a flow hasn't emitted yet, its value in the Triple will be `null`.
 *
 * @param scope The CoroutineScope in which to launch coroutines for collecting the input flows.
 * @param flowA The first flow to combine.
 * @param flowB The second flow to combine.
 * @param flowC The third flow to combine.
 * @return A Flow emitting Triples of nullable values from the input flows whenever any of them emit.
 */
fun <A, B, C> combineNullable(
    scope: CoroutineScope,
    flowA: Flow<A>,
    flowB: Flow<B>,
    flowC: Flow<C>
): Flow<Triple<A?, B?, C?>> = channelFlow {
    var latestA: A? = null
    var latestB: B? = null
    var latestC: C? = null

    // Collect from flowA
    scope.launch {
        flowA.collect { a ->
            latestA = a
            send(Triple(latestA, latestB, latestC))
        }
    }

    // Collect from flowB
    scope.launch {
        flowB.collect { b ->
            latestB = b
            send(Triple(latestA, latestB, latestC))
        }
    }

    // Collect from flowC
    scope.launch {
        flowC.collect { c ->
            latestC = c
            send(Triple(latestA, latestB, latestC))
        }
    }

    // Ensure the flow stays active until it's cancelled
    awaitClose {
        // Clean-up if necessary
    }
}


data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Combines four non-nullable flows into one nullable flow that emits whenever any of the input flows emit.
 * If a flow hasn't emitted yet, its value in the Quadruple will be `null`.
 *
 * @param scope The CoroutineScope in which to launch coroutines for collecting the input flows.
 * @param flowA The first flow to combine.
 * @param flowB The second flow to combine.
 * @param flowC The third flow to combine.
 * @param flowD The fourth flow to combine.
 * @return A Flow emitting Quadruples of nullable values from the input flows whenever any of them emit.
 */
fun <A, B, C, D> combineNullable(
    scope: CoroutineScope,
    flowA: Flow<A>,
    flowB: Flow<B>,
    flowC: Flow<C>,
    flowD: Flow<D>
): Flow<Quadruple<A?, B?, C?, D?>> = channelFlow {
    var latestA: A? = null
    var latestB: B? = null
    var latestC: C? = null
    var latestD: D? = null

    // Collect from flowA
    scope.launch {
        flowA.collect { a ->
            latestA = a
            send(Quadruple(latestA, latestB, latestC, latestD))
        }
    }

    // Collect from flowB
    scope.launch {
        flowB.collect { b ->
            latestB = b
            send(Quadruple(latestA, latestB, latestC, latestD))
        }
    }

    // Collect from flowC
    scope.launch {
        flowC.collect { c ->
            latestC = c
            send(Quadruple(latestA, latestB, latestC, latestD))
        }
    }

    // Collect from flowD
    scope.launch {
        flowD.collect { d ->
            latestD = d
            send(Quadruple(latestA, latestB, latestC, latestD))
        }
    }

    // Ensure the flow stays active until it's cancelled
    awaitClose {
        // Clean-up if necessary
    }
}

package dji.simpleV5.dji_sdk5_utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch



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


/**
 * A data class representing a tuple of four values.
 */
data class Quadruple<A, B, C, D>(
    val first: A?,
    val second: B?,
    val third: C?,
    val fourth: D?
)


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
package reactivecircus.flowbinding.lifecycle

import androidx.annotation.CheckResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import reactivecircus.flowbinding.common.checkMainThread

/**
 * Create a [Flow] of [Lifecycle.Event]s on the [Lifecycle] instance.
 *
 * Note: Created flow keeps a strong reference to the [Lifecycle] instance
 * until the coroutine that launched the flow collector is cancelled.
 *
 * Example of usage:
 *
 * ```
 * lifecycle.events()
 *     .filter { it == Lifecycle.Event.ON_CREATE }
 *     .onEach { event ->
 *          // handle Lifecycle.Event.ON_CREATE event
 *     }
 *     .launchIn(uiScope)
 * ```
 */
@CheckResult
@OptIn(ExperimentalCoroutinesApi::class)
public fun Lifecycle.events(): Flow<Lifecycle.Event> = callbackFlow {
    checkMainThread()
    val observer = object : LifecycleObserver {
        @Suppress("UNUSED_PARAMETER")
        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        fun onEvent(owner: LifecycleOwner, event: Lifecycle.Event) {
            trySend(event)
        }
    }
    addObserver(observer)
    awaitClose { removeObserver(observer) }
}

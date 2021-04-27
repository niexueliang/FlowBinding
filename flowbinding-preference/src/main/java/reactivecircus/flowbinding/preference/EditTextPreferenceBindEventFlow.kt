@file:Suppress("MatchingDeclarationName")

package reactivecircus.flowbinding.preference

import android.widget.EditText
import androidx.annotation.CheckResult
import androidx.preference.EditTextPreference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import reactivecircus.flowbinding.common.checkMainThread

/**
 * Create a [Flow] of bind events on the [EditTextPreference] instance.
 *
 * Note: Created flow keeps a strong reference to the [EditTextPreference] instance
 * until the coroutine that launched the flow collector is cancelled.
 *
 * Example of usage:
 *
 * ```
 * editTextPreference.editTextBindEvents()
 *     .onEach { event ->
 *          // handle edit text preference bind event
 *     }
 *     .launchIn(uiScope)
 * ```
 */
@CheckResult
@OptIn(ExperimentalCoroutinesApi::class)
public fun EditTextPreference.editTextBindEvents(): Flow<EditTextBindEvent> = callbackFlow {
    checkMainThread()
    val listener = EditTextPreference.OnBindEditTextListener {
        trySend(EditTextBindEvent(it))
    }
    setOnBindEditTextListener(listener)
    awaitClose { setOnBindEditTextListener(null) }
}.conflate()

public data class EditTextBindEvent(public val editText: EditText)

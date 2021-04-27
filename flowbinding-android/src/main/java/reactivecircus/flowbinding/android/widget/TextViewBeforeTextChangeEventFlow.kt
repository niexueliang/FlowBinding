@file:Suppress("MatchingDeclarationName")

package reactivecircus.flowbinding.android.widget

import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.annotation.CheckResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import reactivecircus.flowbinding.common.InitialValueFlow
import reactivecircus.flowbinding.common.asInitialValueFlow
import reactivecircus.flowbinding.common.checkMainThread

/**
 * Create a [InitialValueFlow] of before text change events on the [TextView] instance.
 *
 * Note: Created flow keeps a strong reference to the [TextView] instance
 * until the coroutine that launched the flow collector is cancelled.
 *
 * Example of usage:
 *
 * ```
 * textView.beforeTextChanges()
 *     .onEach { event ->
 *          // handle text view before text change event
 *     }
 *     .launchIn(uiScope)
 * ```
 */
@CheckResult
@OptIn(ExperimentalCoroutinesApi::class)
public fun TextView.beforeTextChanges(): InitialValueFlow<BeforeTextChangeEvent> =
    callbackFlow {
        checkMainThread()
        val listener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                trySend(
                    BeforeTextChangeEvent(
                        view = this@beforeTextChanges,
                        text = s,
                        start = start,
                        count = count,
                        after = after
                    )
                )
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable) = Unit
        }

        addTextChangedListener(listener)
        awaitClose { removeTextChangedListener(listener) }
    }
        .conflate()
        .asInitialValueFlow {
            BeforeTextChangeEvent(
                view = this@beforeTextChanges,
                text = text,
                start = 0,
                count = 0,
                after = 0
            )
        }

public data class BeforeTextChangeEvent(
    public val view: TextView,
    public val text: CharSequence,
    public val start: Int,
    public val count: Int,
    public val after: Int
)

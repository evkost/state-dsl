/*
 * Copyright 2023 Murat Kostoev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evkost.statedsl.processor.core.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode

/**
 * Scope for validating symbol
 * @see check
 * @see assert
 */
internal open class ValidationScope<T: KSNode>(
    open val element: T,
    private val logger: KSPLogger
) {
    private var valid = true

    private var nextRound = false

    inline fun check(lambda: T.() -> Boolean): Checked = Checked(lambda(element))

    inline fun checkNot(lambda: T.() -> Boolean): Checked  = Checked(!lambda(element))

    inline fun checkNotNull(lambda: T.() -> Any?): Checked  = Checked(lambda(element) != null)

    inline fun checkNull(lambda: T.() -> Any?): Checked  = Checked(lambda(element) == null)

    infix fun Checked.orInfo(message: String): Unit = onFalseValue { logger.info(message, element) }

    infix fun Checked.orWarn(message: String): Unit = onFalseValue { logger.warn(message, element) }

    infix fun Checked.orError(message: String): Unit = onFalseValue { logger.error(message, element) }

    infix fun Checked.orException(throwable: Throwable): Unit = onFalseValue { logger.exception(throwable) }

    infix fun Checked.affect(isAffecting: Boolean): Checked = apply {
        if (isAffecting) {
            valid = valid && value
        }
    }

    private inline fun Checked.onFalseValue(lambda: () -> Unit): Unit {
        if (!value) lambda()
    }

    fun validate(): ValidationResult {
        if (nextRound) {
            return ValidationResult.NextRound(element)
        }
        return if (valid) ValidationResult.Valid(element) else ValidationResult.Invalid(element)
    }

    @JvmInline
    value class Checked(
        val value: Boolean
    )
}

internal inline fun <T : KSNode> ValidationScope<T>.assert(lambda: T.() -> Boolean) = check(lambda) affect true

internal inline fun <T : KSNode> ValidationScope<T>.assertNot(lambda: T.() -> Boolean) = checkNot(lambda) affect true

internal inline fun <T : KSNode> ValidationScope<T>.assertNotNull(lambda: T.() -> Any?) = checkNotNull(lambda) affect true

internal inline fun <T : KSNode> ValidationScope<T>.assertNull(lambda: T.() -> Any?) = checkNull(lambda) affect true

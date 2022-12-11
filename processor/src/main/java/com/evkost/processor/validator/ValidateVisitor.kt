/*
 * Copyright 2022 Murat Kostoev
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

package com.evkost.processor.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor

/**
 * Class for validating any symbol for generator visitor
 * @see com.evkost.processor.generator.GenerateVisitor
 */
internal abstract class ValidateVisitor<T>(
    private val logger: KSPLogger
) : KSEmptyVisitor<T, Validated<ValidateVisitor<T>>>() {
    override fun defaultHandler(node: KSNode, data: T): Validated<ValidateVisitor<T>> {
        return Invalid(node)
    }

    /**
     * @param element element that should be validated
     * @param lambda scope that you should validate symbol
     * @return validated symbol
     * @see Validated
     * @see ValidationScope
     */
    protected inline fun <V: KSNode, P: ValidateVisitor<T>> validate(
        element: V,
        lambda: ValidationScope<V>.() -> Unit
    ): Validated<P> =
        ValidationScope(element, logger).apply(lambda).validate()
}

internal fun <T, V: ValidateVisitor<T>> Sequence<KSNode>.validateBy(visitor: V, data: T) =
    map { it.accept(visitor, data) }

/**
 * Scope for validating symbol
 * @see check
 * @see assert
 */
internal open class ValidationScope<T: KSNode>(
    open val element: T,
    private val logger: KSPLogger
) {
    var valid = true
        protected set

    infix fun Asserted.info(message: String) = onNonAssert { logger.info(message, element) }
    infix fun Asserted.warn(message: String) = onNonAssert { logger.warn(message, element) }
    infix fun Asserted.error(message: String) = onNonAssert { logger.error(message, element) }
    infix fun Asserted.exception(throwable: Throwable) = onNonAssert { logger.exception(throwable) }

    inline fun check(lambda: T.() -> Boolean) = assertOf { lambda(element) }
    inline fun checkNot(lambda: T.() -> Boolean) = assertNotOf { lambda(element) }
    inline fun checkNotNull(lambda: T.() -> Any?) = assertNotNullOf { lambda(element) }
    inline fun checkNull(lambda: T.() -> Any?) = assertNullOf { lambda(element) }

    inline fun assert(lambda: T.() -> Boolean) = check(lambda) affect true
    inline fun assertNot(lambda: T.() -> Boolean) = checkNot(lambda) affect true
    inline fun assertNotNull(lambda: T.() -> Any?) = checkNotNull(lambda) affect true
    inline fun assertNull(lambda: T.() -> Any?) = checkNull(lambda) affect true

    infix fun Asserted.affect(affectsValid: Boolean): Asserted {
        if (affectsValid) {
            valid = valid && value
        }

        return this
    }

    fun <V: ValidateVisitor<*>> validate(): Validated<V> = validated(element) { valid }
}

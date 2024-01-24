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

package com.evkost.statedsl.processor.core.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor

/**
 * Class for validating any symbol for generator visitor
 * @see com.evkost.processor.generator.GenerateVisitor
 */
internal abstract class Validator<D>(
    private val logger: KSPLogger
) : KSEmptyVisitor<D, ValidationResult>() {
    override fun defaultHandler(node: KSNode, data: D): ValidationResult {
        return ValidationResult.Invalid(node)
    }

    /**
     * @param element element that should be validated
     * @param lambda scope that you should validate symbol
     * @return validated symbol
     * @see ValidationResult
     * @see ValidationScope
     */
    protected inline fun <V: KSNode> validate(
        element: V, lambda: ValidationScope<V>.() -> Unit
    ): ValidationResult = ValidationScope(element, logger).apply(lambda).validate()
}

internal fun Validator<Unit>.isValid(node: KSNode) =
    node.accept(this, Unit) is ValidationResult.Valid

internal fun <T> Validator<T>.isValid(node: KSNode, data: T) =
    node.accept(this, data) is ValidationResult.Valid

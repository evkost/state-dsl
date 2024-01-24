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

package com.evkost.statedsl.processor.generation.extension.flow

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.evkost.statedsl.extension.flow.InlineUpdate
import com.evkost.statedsl.processor.core.validator.*
import com.evkost.statedsl.processor.core.validator.Validator
import com.evkost.statedsl.processor.core.validator.ValidationResult
import com.evkost.statedsl.processor.core.validator.assert
import com.evkost.statedsl.processor.core.validator.assertNot
import com.evkost.statedsl.processor.hasModifier
import com.evkost.statedsl.processor.generation.dslbuilder.DslBuilderValidator


private object Errors {
    const val MUST_HAVE_PARENT_CLASS = "Property must have parent class"
}

internal class InlineUpdateValidator(
    private val dslBuilderValidator: DslBuilderValidator,
    private val logger: KSPLogger
) : Validator<Unit>(logger) {
    override fun defaultHandler(node: KSNode, data: Unit): ValidationResult {
        logger.error("Only property can be annotated with ${InlineUpdate::class.simpleName} annotation", node)

        return super.defaultHandler(node, data)
    }

    override fun visitPropertyDeclaration(
        property: KSPropertyDeclaration,
        data: Unit
    ): ValidationResult = validate(property) {
        assertNotNull { parentDeclaration } orError Errors.MUST_HAVE_PARENT_CLASS

        assert { parentDeclaration?.let { dslBuilderValidator.isValid(it) } ?: false } orError Errors.MUST_HAVE_PARENT_CLASS

        assertNot { hasModifier(Modifier.PRIVATE) } orError "Property that annotated with ${InlineUpdate::class.simpleName} annotation cannot be private"
    }
}
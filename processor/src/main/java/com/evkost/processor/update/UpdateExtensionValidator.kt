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

package com.evkost.processor.update

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.evkost.core.annotation.StateDsl
import com.evkost.core.annotation.Updatable
import com.evkost.processor.hasAnnotation
import com.evkost.processor.hasModifier
import com.evkost.processor.validator.ValidateVisitor
import com.evkost.processor.validator.Validated

internal class UpdateExtensionValidator(private val logger: KSPLogger) : ValidateVisitor<Unit>(logger) {
    override fun defaultHandler(node: KSNode, data: Unit): Validated<ValidateVisitor<Unit>> {
        logger.error("Only property can be annotated with ${Updatable::class.simpleName} annotation", node)

        return super.defaultHandler(node, data)
    }

    override fun visitPropertyDeclaration(
        property: KSPropertyDeclaration,
        data: Unit
    ): Validated<ValidateVisitor<Unit>> = validate(property) {
        assert { parentDeclaration?.hasAnnotation(StateDsl::class) ?: false } error
                "Property that annotated with ${Updatable::class.simpleName} annotation cannot be in the class that not annotated ${StateDsl::class.simpleName} annotation"

        assertNot { hasModifier(Modifier.PRIVATE) } error "Property that annotated with ${Updatable::class.simpleName} annotation cannot be private"
    }
}
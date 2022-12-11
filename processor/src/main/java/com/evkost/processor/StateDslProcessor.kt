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

package com.evkost.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.evkost.core.annotation.InnerState
import com.evkost.core.annotation.StateDsl
import com.evkost.core.annotation.Updatable
import com.evkost.processor.builder.BuilderGenerator
import com.evkost.processor.builder.BuilderValidator
import com.evkost.processor.generator.generateBy
import com.evkost.processor.update.StateExtensionsGenerator
import com.evkost.processor.update.UpdateExtensionGenerator
import com.evkost.processor.update.UpdateExtensionValidator
import com.evkost.processor.validator.Valid
import com.evkost.processor.validator.validateBy

class StateDslProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor{
    private val builderValidator = BuilderValidator(logger)
    private val builderGenerator = BuilderGenerator(codeGenerator)
    private val stateExtensionsGenerator = StateExtensionsGenerator(codeGenerator)
    private val updateExtensionValidator = UpdateExtensionValidator(logger)
    private val updateExtensionGenerator = UpdateExtensionGenerator(codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbols(StateDsl::class)
            .validateBy(builderValidator, Unit)
            .filterIsInstance<Valid<BuilderValidator>>()
            .run {
                generateBy(builderGenerator, Unit)
                generateBy(stateExtensionsGenerator, Unit)
            }

        resolver.getSymbols(InnerState::class)
            .validateBy(builderValidator, Unit)
            .filterIsInstance<Valid<BuilderValidator>>()
            .generateBy(builderGenerator, Unit)

        resolver.getSymbols(Updatable::class)
            .filterIsInstance<KSPropertyDeclaration>()
            .validateBy(updateExtensionValidator, Unit)
            .filterIsInstance<Valid<UpdateExtensionValidator>>()
            .generateBy(updateExtensionGenerator, Unit)

        return emptyList()
    }
}
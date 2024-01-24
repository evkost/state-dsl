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

package com.evkost.statedsl.processor

import com.evkost.statedsl.core.builder.DslBuilder
import com.evkost.statedsl.extension.flow.FlowCreate
import com.evkost.statedsl.extension.flow.FlowUpdate
import com.evkost.statedsl.extension.flow.InlineUpdate
import com.evkost.statedsl.processor.core.validator.ValidationResult
import com.evkost.statedsl.processor.generation.dslbuilder.DslBuilderGenerator
import com.evkost.statedsl.processor.generation.dslbuilder.DslBuilderValidator
import com.evkost.statedsl.processor.generation.extension.flow.FlowCreateGenerator
import com.evkost.statedsl.processor.generation.extension.flow.FlowCreateValidator
import com.evkost.statedsl.processor.generation.extension.flow.FlowUpdateGenerator
import com.evkost.statedsl.processor.generation.extension.flow.FlowUpdateValidator
import com.evkost.statedsl.processor.generation.extension.flow.InlineUpdateGenerator
import com.evkost.statedsl.processor.generation.extension.flow.InlineUpdateValidator
import com.evkost.statedsl.processor.generation.extension.generateExtensions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

class StateDslProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor{
    private val dslBuilderValidator = DslBuilderValidator(logger)
    private val dslBuilderGenerator = DslBuilderGenerator(codeGenerator)
    private val flowCreateValidator = FlowCreateValidator(logger)
    private val flowCreateGenerator = FlowCreateGenerator()
    private val flowUpdateValidator = FlowUpdateValidator(logger)
    private val flowUpdateGenerator = FlowUpdateGenerator()
    private val inlineUpdateValidator = InlineUpdateValidator(dslBuilderValidator, logger)
    private val inlineUpdateGenerator = InlineUpdateGenerator()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbols(DslBuilder::class)
            .map { dslBuilderValidator(it) }
            .filterIsInstance<ValidationResult.Valid>()
            .forEach { dslBuilderGenerator(it.symbol) }

        val flowCreateExtensions = resolver.getSymbols(FlowCreate::class)
            .map { flowCreateValidator(it) }
            .filterIsInstance<ValidationResult.Valid>()
            .map { flowCreateGenerator(it.symbol) }
            .filterNotNull()
            .toList()

        val flowUpdateExtensions = resolver.getSymbols(FlowUpdate::class)
            .map { flowUpdateValidator(it) }
            .filterIsInstance<ValidationResult.Valid>()
            .map { flowUpdateGenerator(it.symbol) }
            .filterNotNull()
            .toList()

        val inlineUpdateExtensions = resolver.getSymbols(InlineUpdate::class)
            .map { inlineUpdateValidator(it) }
            .filterIsInstance<ValidationResult.Valid>()
            .map { inlineUpdateGenerator(it.symbol) }
            .filterNotNull()
            .toList()

        codeGenerator.generateExtensions(listOf(flowCreateExtensions, flowUpdateExtensions, inlineUpdateExtensions).flatten())

        return emptyList()
    }
}

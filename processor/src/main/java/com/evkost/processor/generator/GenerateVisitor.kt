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

package com.evkost.processor.generator

import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import com.evkost.processor.validator.Valid
import com.evkost.processor.validator.ValidateVisitor

internal abstract class GenerateVisitor<V: ValidateVisitor<*>, T> : KSDefaultVisitor<T, Unit>() {
    override fun defaultHandler(node: KSNode, data: T) {}

    fun generate(valid: Valid<V>, data: T) {
        valid.value.accept(this, data)
    }
}

internal fun <T, V: ValidateVisitor<*>> Sequence<Valid<V>>.generateBy(generator: GenerateVisitor<V, T>, data: T) =
    forEach { generator.generate(it, data) }
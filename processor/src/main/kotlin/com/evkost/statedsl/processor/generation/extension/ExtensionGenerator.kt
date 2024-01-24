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

package com.evkost.statedsl.processor.generation.extension

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo

data class ExtensionSpecsCreationInfo(
    val parentClassName: ClassName,
    val createdSpecs: List<Taggable>
)

internal abstract class ExtensionGenerator<T> : KSEmptyVisitor<T, ExtensionSpecsCreationInfo?>() {
    override fun defaultHandler(node: KSNode, data: T): ExtensionSpecsCreationInfo? = null
}

internal fun CodeGenerator.generateExtensions(creationInfoList: List<ExtensionSpecsCreationInfo>) {
    val parentClassNameToSpecsMap = creationInfoList.groupBy { info -> info.parentClassName }
    for ((className, specsCreationInfo) in parentClassNameToSpecsMap) {
        val file = FileSpec.builder(
            packageName = className.packageName,
            fileName = "StateDsl_${className.simpleName}DslExtension"
        )

        val extensionSymbols = specsCreationInfo.map { it.createdSpecs }.flatten()
        for (symbol in extensionSymbols) {
            when (symbol) {
                is TypeSpec -> file.addType(symbol)
                is FunSpec -> file.addFunction(symbol)
                is PropertySpec -> file.addProperty(symbol)
                else -> throw NotImplementedError()
            }
        }
        file.build().writeTo(this, false)
    }
}



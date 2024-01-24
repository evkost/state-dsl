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

import com.evkost.statedsl.core.builder.DslBuilder
import com.evkost.statedsl.processor.generation.extension.ExtensionGenerator
import com.evkost.statedsl.processor.generation.extension.ExtensionSpecsCreationInfo
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.evkost.statedsl.processor.hasAnnotation
import com.evkost.statedsl.processor.uppercaseName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import kotlinx.coroutines.flow.MutableStateFlow

internal class InlineUpdateGenerator : ExtensionGenerator<Unit>() {

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit): ExtensionSpecsCreationInfo {
        super.visitPropertyDeclaration(property, data)

        val propertyType = property.type.resolve()
        val propertyName = property.simpleName.asString()

        val packageName = property.packageName.asString()
        val functionName = "update${property.uppercaseName}"

        val parentClassName = ClassName(
            packageName = property.parentDeclaration!!.packageName.asString(),
            property.parentDeclaration!!.simpleName.asString()
        )

        val factoryFunctionMemberName = MemberName(
            packageName = packageName,
            simpleName = parentClassName.simpleName
        )

        val typeClassName = propertyType.toClassName().copy(nullable = propertyType.isMarkedNullable)

        val typeBuilderClassName = ClassName(
            packageName = propertyType.declaration.packageName.asString(),
            "${propertyType.declaration.simpleName.asString()}Builder"
        )
        val functionParameterName = "function"

        val updateStateFlowMember = MemberName("kotlinx.coroutines.flow", "update")

        val updateExtensionFunction = FunSpec.builder(functionName).apply {
            addModifiers(KModifier.INLINE)
            receiver(MutableStateFlow::class.asTypeName().parameterizedBy(parentClassName))
            beginControlFlow("return %M", updateStateFlowMember)
            beginControlFlow("%M(it)", factoryFunctionMemberName)

            if (propertyType.declaration.hasAnnotation(DslBuilder::class)) {
                addParameter(functionParameterName, LambdaTypeName.get(
                    receiver = typeBuilderClassName,
                    returnType = UNIT
                ))
                addStatement("%N(%N)",propertyName,functionParameterName)
            } else {
                addParameter(functionParameterName, LambdaTypeName.get(
                    receiver = null,
                    typeClassName,
                    returnType = typeClassName
                ))
                addStatement("%N·= %N(%N)", propertyName, functionParameterName, propertyName)
            }

            endControlFlow()
            endControlFlow()

        }.build()

        return ExtensionSpecsCreationInfo(parentClassName, listOf(updateExtensionFunction))
    }

}
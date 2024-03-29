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

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.evkost.core.annotation.InnerState
import com.evkost.processor.generator.GenerateVisitor
import com.evkost.processor.hasAnnotation
import com.evkost.processor.uppercaseName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.flow.MutableStateFlow

internal class UpdateExtensionGenerator(
    private val codeGenerator: CodeGenerator
) : GenerateVisitor<UpdateExtensionValidator, Unit>() {

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        super.visitPropertyDeclaration(property, data)

        val propertyType = property.type.resolve()
        val propertyName = property.simpleName.asString()

        val packageName = property.packageName.asString()
        val functionName = "update${property.uppercaseName}"

        val parentClassName = ClassName(
            packageName = property.parentDeclaration!!.packageName.asString(),
            property.parentDeclaration!!.simpleName.asString()
        )

        val parentBuilderClassName = ClassName(
            packageName = property.parentDeclaration!!.packageName.asString(),
            "${property.parentDeclaration!!.simpleName.asString()}Builder"
        )

        val typeClassName = propertyType.toClassName().copy(nullable = propertyType.isMarkedNullable)

        val typeBuilderClassName = ClassName(
            packageName = propertyType.declaration.packageName.asString(),
            "${propertyType.declaration.simpleName.asString()}Builder"
        )

        val updateStateFlowMember = MemberName("kotlinx.coroutines.flow", "update")

        val updateExtensionFunction = FunSpec.builder(functionName).apply {
            receiver(MutableStateFlow::class.asTypeName().parameterizedBy(parentClassName))
            beginControlFlow("return %M", updateStateFlowMember)
            beginControlFlow("%T(it).apply", parentBuilderClassName)

            if (propertyType.declaration.hasAnnotation(InnerState::class)) {
                addParameter("lambda", LambdaTypeName.get(
                    receiver = typeBuilderClassName,
                    typeClassName,
                    returnType = UNIT
                ))
                beginControlFlow("%N", propertyName)
                addStatement("lambda(it.%N)", propertyName)
                endControlFlow()
            } else {
                addParameter("lambda", LambdaTypeName.get(
                    receiver = null,
                    typeClassName,
                    returnType = typeClassName
                ))
                addStatement("%N·= lambda(it.%N)", propertyName, propertyName)
            }

            endControlFlow()
            addStatement(".build()")
            endControlFlow()

        }.build()

        val file = FileSpec.builder(packageName, "Update${property.uppercaseName}From${parentClassName.simpleName}Extension")
            .addFunction(updateExtensionFunction)
            .build()

        file.writeTo(codeGenerator, false)
    }

}
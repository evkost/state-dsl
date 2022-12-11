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
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.evkost.processor.builder.BuilderValidator
import com.evkost.processor.generator.GenerateVisitor
import com.evkost.processor.lowercaseName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class StateExtensionsGenerator(
    private val codeGenerator: CodeGenerator
) : GenerateVisitor<BuilderValidator, Unit>() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)

        val classDeclarationName = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()

        val className = ClassName(
            packageName = packageName,
            classDeclarationName
        )

        val builderClassName = ClassName(
            packageName = packageName,
            "${classDeclarationName}Builder"
        )

        val updateStateFlowMember = MemberName("kotlinx.coroutines.flow", "update")
        val mutableStateFlowType = MutableStateFlow::class.asTypeName()

        val updateExtensionFunction = FunSpec.builder("updateState").apply {
            receiver(mutableStateFlowType.parameterizedBy(className))

            addParameter(
                "lambda", LambdaTypeName.get(
                    receiver = builderClassName,
                    className,
                    returnType = UNIT
                )
            )

            beginControlFlow("return %M", updateStateFlowMember)
            addStatement("%T(it).apply·{·lambda(it)·}.build()", builderClassName)
            endControlFlow()

        }.build()

        val anyType = Any::class.asTypeName().copy(nullable = true)

        val stateDelegateFunction = FunSpec.builder("${classDeclaration.lowercaseName}Flow").apply {
            addParameter(
                ParameterSpec.builder(
                    "lambda", LambdaTypeName.get(
                        receiver = builderClassName,
                        returnType = UNIT
                    )
                )
                    .defaultValue("{}")
                    .build()
            )
            beginControlFlow("return object·:·%T",
                ReadOnlyProperty::class.asTypeName().parameterizedBy(
                    anyType,
                    mutableStateFlowType.parameterizedBy(className)
                )
            )
            addStatement(
                "\t\tval state·= %T(%T().apply(lambda).build())",
                mutableStateFlowType, builderClassName
            )

            addStatement(
                "\t\toverride·fun·getValue(thisRef:·%T, property:·%T<*>): %T·= state",
                anyType,
                KProperty::class.asTypeName(),
                mutableStateFlowType.parameterizedBy(className)
            )
            endControlFlow()
        }.build()

        val file = FileSpec.builder(
            packageName,
            "Update${className.simpleName}Extension"
        )
            .addFunction(stateDelegateFunction)
            .addFunction(updateExtensionFunction)
            .build()

        file.writeTo(codeGenerator, false)
    }

}
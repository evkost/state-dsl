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

package com.evkost.processor.builder

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.evkost.core.annotation.InnerState
import com.evkost.core.annotation.StateDslMarker
import com.evkost.processor.generator.GenerateVisitor
import com.evkost.processor.hasAllDefaults
import com.evkost.processor.hasAnnotation
import com.evkost.processor.lowercaseName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

private typealias PropertyWithParameter = Pair<PropertySpec, KSValueParameter>

internal class BuilderGenerator(
    private val codeGenerator: CodeGenerator
) : GenerateVisitor<BuilderValidator, Unit>() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)

        val className = classDeclaration.simpleName.asString()
        val classLowercaseName = classDeclaration.lowercaseName

        val packageName = classDeclaration.packageName.asString()
        val builderName = "${className}Builder"

        val builderType = TypeSpec.classBuilder(builderName).apply {
            addAnnotation(StateDslMarker::class)
//            addModifiers(
//                classDeclaration.modifiers
//                    .mapNotNull { it.toKModifier() }
//                    .filter { modifier -> listOf(KModifier.INTERNAL, KModifier.PUBLIC).any { it == modifier} }
//            )

            val parameters = classDeclaration.primaryConstructor!!.parameters

            if (classDeclaration.hasAllDefaults == true) {
                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(proxyClassParameterOf(classDeclaration))
                        .build()
                )

                parameters.forEach { addProperty(initializedProperty(it, classLowercaseName)) }
            } else {
                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(nullableProxyClassParameterOf(classDeclaration))
                        .build()
                )

                parameters.forEach {
                    addProperty(
                        nullableInitializedProperty(
                            it,
                            classLowercaseName
                        )
                    )
                }
            }

            val propertyWithParameter = propertySpecs.zip(parameters)

            propertyWithParameter.forEach {
                if (it.second.type.resolve().declaration.hasAnnotation(InnerState::class)) {
                    addFunction(innerStateDslFunction(it))
                }
            }

            addFunction(buildFunction(classDeclaration.toClassName(), propertyWithParameter))
        }.build()

        val file = FileSpec.builder(packageName, builderName)
            .addType(builderType)
            .build()

        file.writeTo(codeGenerator, false)
    }

    private fun proxyClassParameterOf(symbol: KSClassDeclaration): ParameterSpec {
        val parameterName = symbol.simpleName.asString().replaceFirstChar { it.lowercase() }
        return ParameterSpec.builder(
            name = parameterName,
            type = symbol.toClassName()
        )
            .defaultValue("%T()", symbol.toClassName())
            .build()
    }

    private fun nullableProxyClassParameterOf(symbol: KSClassDeclaration): ParameterSpec {
        val parameterName = symbol.simpleName.asString().replaceFirstChar { it.lowercase() }
        return ParameterSpec.builder(
            name = parameterName,
            type = symbol.toClassName().copy(nullable = true)
        )
            .defaultValue("null", symbol.toClassName(), parameterName)
            .build()
    }

    private fun initializedProperty(
        parameter: KSValueParameter,
        proxyPropertyName: String
    ): PropertySpec =
        PropertySpec.builder(
            name = parameter.name!!.asString(),
            type = parameter.type.toTypeName()
        )
            .mutable()
            .initializer("%N.%N", proxyPropertyName, parameter.name!!.asString())
            .build()

    private fun nullableInitializedProperty(
        parameter: KSValueParameter,
        proxyPropertyName: String
    ): PropertySpec =
        PropertySpec.builder(
            name = parameter.name!!.asString(),
            type = parameter.type.toTypeName().copy(nullable = true)
        )
            .mutable()
            .initializer("%N?.%N", proxyPropertyName, parameter.name!!.asString())
            .build()

    private fun innerStateDslFunction(
        parameter: PropertyWithParameter,
    ): FunSpec {
        val typeClassName = parameter.first.type as ClassName
        val builderClassName = ClassName(
            packageName = typeClassName.packageName,
            typeClassName.simpleName + "Builder"
        )

        return FunSpec.builder(parameter.first.name)
            .addParameter(
                name = "lambda",
                type = LambdaTypeName.get(
                    receiver = builderClassName,
                    returnType = UNIT
                )
            )
            .apply {
                if ((parameter.second.type.resolve().declaration as KSClassDeclaration).hasAllDefaults == true && parameter.first.type.isNullable) {
                    addStatement(
                        "this.%N·= %T(%N ?: %T()).apply(%N).build()",
                        parameter.first.name,
                        builderClassName,
                        parameter.first.name,
                        typeClassName.copy(nullable = false),
                        "lambda"
                    )
                } else {
                    addStatement(
                        "this.%N·= %T(%N).apply(%N).build()",
                        parameter.first.name,
                        builderClassName,
                        parameter.first.name,
                        "lambda"
                    )
                }
            }
            .build()
    }

    private fun buildFunction(
        symbolClassName: ClassName,
        parameters: List<PropertyWithParameter>
    ): FunSpec =
        FunSpec.builder("build")
            .apply {
                if (parameters.all { !it.first.type.isNullable || it.second.type.resolve().isMarkedNullable}) {
                    addStatement("return %T(%L)", symbolClassName, parameters.joinToString { it.first.name })
                } else {
                    addStatement(
                        "return %T(\n\t%L\n)", symbolClassName,
                        parameters.joinToString(",\n\t") {
                            if (!it.first.type.isNullable || it.second.type.resolve().isMarkedNullable) {
                                it.first.name
                            } else {
                                "checkNotNull(${it.first.name})·{·\"Property·${it.first.name}·must·not·be·null\"·}"
                            }
                        }
                    )
                }
            }.build()
}
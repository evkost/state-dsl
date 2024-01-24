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

package com.evkost.statedsl.processor.generation.dslbuilder

import com.evkost.statedsl.core.builder.Builder
import com.evkost.statedsl.core.builder.DslBuilder
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.evkost.statedsl.core.builder.DslBuilderMarker
import com.evkost.statedsl.processor.core.generator.Generator
import com.evkost.statedsl.processor.hasAllDefaults
import com.evkost.statedsl.processor.hasAnnotation
import com.evkost.statedsl.processor.lowercaseName
import com.evkost.statedsl.processor.withPrefix
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

internal class DslBuilderGenerator(
    private val codeGenerator: CodeGenerator
) : Generator<Unit>() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        super.visitClassDeclaration(classDeclaration, data)

        val classLowercaseName = classDeclaration.lowercaseName
        val primaryConstructor = classDeclaration.primaryConstructor!!
        val primaryConstructorParameters = primaryConstructor.parameters

        val builderSpec = TypeSpec.classBuilder(builderName(classDeclaration)).apply {
            addAnnotation(DslBuilderMarker::class)
            addSuperinterface(Builder::class.asClassName().parameterizedBy(classDeclaration.toClassName()))

            if (primaryConstructor.hasAllDefaults) {
                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(proxyClassParameterOf(classDeclaration))
                        .build()
                )

                for (parameter in primaryConstructorParameters) {
                    addProperty(initializedProperty(parameter, classLowercaseName))
                }
            } else {
                primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(nullableProxyClassParameterOf(classDeclaration))
                        .build()
                )

                for (parameter in primaryConstructorParameters) {
                    addProperty(nullableInitializedProperty(parameter, classLowercaseName))
                }
            }

            val propertyWithParameter = propertySpecs zip primaryConstructorParameters
            for ((builderProperty, primaryConstructorParameter) in propertyWithParameter) {
                val typeDeclaration = primaryConstructorParameter.type.resolve().declaration as KSClassDeclaration
                if (typeDeclaration.hasAnnotation(DslBuilder::class)) {
                    addFunction(
                        innerStateDslFunction(typeDeclaration, builderProperty)
                    )
                }
            }

            addFunction(buildFunction(classDeclaration.toClassName(), propertySpecs, primaryConstructor.hasAllDefaults))
        }.build()

        val stateDslFunction = stateDslFunction(
            symbolDeclaration = classDeclaration,
            isBuilderAcceptingNullable = !primaryConstructor.hasAllDefaults
        )

        FileSpec.builder(fileClassName(classDeclaration))
            .addType(builderSpec)
            .addFunction(stateDslFunction)
            .build()
            .writeTo(
                codeGenerator = codeGenerator,
                aggregating = false
            )
    }

    private fun builderName(classDeclaration: KSClassDeclaration): String {
        val className = classDeclaration.simpleName.asString()

        return "${className}Builder"
    }

    private fun fileClassName(classDeclaration: KSClassDeclaration): ClassName {
        val className = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()

        return ClassName(
            packageName = packageName,
            simpleNames = listOf(withPrefix(className))
        )
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
        symbolDeclaration: KSClassDeclaration,
        property: PropertySpec,
    ): FunSpec {
        val functionParameterName = "function"
        val typeClassName = property.type as ClassName

        val builderClassName = ClassName(
            packageName = typeClassName.packageName,
            builderName(symbolDeclaration)
        )

        val stateDslFunctionMemberName = MemberName(
            packageName = typeClassName.packageName,
            typeClassName.simpleName
        )

        return FunSpec.builder(property.name)
            .addModifiers(KModifier.INLINE)
            .addParameter(
                name = functionParameterName,
                type = LambdaTypeName.get(
                    receiver = builderClassName,
                    returnType = UNIT
                )
            )
            .addStatement(
                "this.%N·= %M(%N, %N)",
                property.name,
                stateDslFunctionMemberName,
                property.name,
                functionParameterName
            )
            .build()
    }

    private fun stateDslFunction(
        symbolDeclaration: KSClassDeclaration,
        isBuilderAcceptingNullable: Boolean
    ): FunSpec {
        val symbolClassName = symbolDeclaration.toClassName()

        val symbolParameterName = symbolDeclaration.lowercaseName
        val lambdaParameterName = "function"

        val builderClassName = ClassName(
            packageName = symbolClassName.packageName,
            builderName(symbolDeclaration)
        )

        val symbolParameterSpec = ParameterSpec.builder(
            name = symbolParameterName,
            type = symbolDeclaration.toClassName().copy(nullable = true)
        ).defaultValue("null").build()

        val funBuilder = FunSpec.builder(symbolClassName.simpleName)
            .returns(symbolClassName)
            .addModifiers(KModifier.INLINE)
            .addParameter(symbolParameterSpec)
            .addParameter(
                name = lambdaParameterName,
                type = LambdaTypeName.get(
                    receiver = builderClassName,
                    returnType = UNIT
                )
            )

        if (isBuilderAcceptingNullable) {
            funBuilder.addStatement(
                "return %T(%N).apply(%N).build()",
                builderClassName,
                symbolParameterName,
                lambdaParameterName
            )
        } else {
            funBuilder.addStatement(
                "return %T(%N ?: %T()).apply(%N).build()",
                builderClassName,
                symbolParameterName,
                symbolClassName,
                lambdaParameterName
            )
        }

        return funBuilder.build()
    }

    private fun buildFunction(
        builderClassName: ClassName,
        properties: List<PropertySpec>,
        check: Boolean
    ): FunSpec {
        val funBuilder = FunSpec.builder("build")
            .returns(builderClassName)
            .addModifiers(KModifier.OVERRIDE)

        if (properties.all { !it.type.isNullable }) {
            funBuilder.addStatement("return %T(%L)", builderClassName, properties.joinToString { it.name })
        } else {
            funBuilder.addStatement(
                "return %T(\n\t%L\n)",
                builderClassName,
                properties.joinToString(",\n\t") { property ->
                    if (!property.type.isNullable || check) {
                        property.name
                    } else {
                        "checkNotNull(${property.name})·{·\"Property·${property.name}·must·not·be·null\"·}"
                    }
                }
            )
        }

        return funBuilder.build()
    }
}
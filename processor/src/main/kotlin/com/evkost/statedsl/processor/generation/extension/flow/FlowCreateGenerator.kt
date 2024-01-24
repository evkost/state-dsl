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

import com.evkost.statedsl.processor.generation.extension.ExtensionGenerator
import com.evkost.statedsl.processor.generation.extension.ExtensionSpecsCreationInfo
import com.evkost.statedsl.processor.lowercaseName
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import kotlinx.coroutines.flow.MutableStateFlow

internal class FlowCreateGenerator : ExtensionGenerator<Unit>() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): ExtensionSpecsCreationInfo {
        super.visitClassDeclaration(classDeclaration, data)

        val classDeclarationName = classDeclaration.simpleName.asString()
        val packageName = classDeclaration.packageName.asString()

        val className = classDeclaration.toClassName()
        val builderClassName = ClassName(
            packageName = packageName,
            "${classDeclarationName}Builder"
        )

        val factoryFunctionMemberName = MemberName(
            packageName = packageName,
            simpleName = classDeclarationName
        )

        val functionParameterName = "function"

        val mutableStateFlowType = MutableStateFlow::class.asTypeName()

        val stateDelegateFunction = FunSpec.builder("${classDeclaration.lowercaseName}Flow").apply {
            addParameter(
                ParameterSpec.builder(
                    functionParameterName,
                    LambdaTypeName.get(
                        receiver = builderClassName,
                        returnType = UNIT
                    )
                )
                    .defaultValue("{}")
                    .build()
            )
            addModifiers(KModifier.INLINE)
            returns(mutableStateFlowType.parameterizedBy(className))
            addStatement(
                "return %T(%M(null, %N))",
                mutableStateFlowType, factoryFunctionMemberName, functionParameterName
            )
        }.build()

        return ExtensionSpecsCreationInfo(className, listOf(stateDelegateFunction))
    }

}
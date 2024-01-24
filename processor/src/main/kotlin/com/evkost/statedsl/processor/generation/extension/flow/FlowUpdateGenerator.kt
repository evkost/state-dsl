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
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.evkost.statedsl.processor.lowercaseName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal class FlowUpdateGenerator : ExtensionGenerator<Unit>() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): ExtensionSpecsCreationInfo {
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

        val factoryFunctionMemberName = MemberName(
            packageName = packageName,
            simpleName = classDeclarationName
        )

        val functionParameterName = "function"

        val updateStateFlowMember = MemberName("kotlinx.coroutines.flow", "update")
        val mutableStateFlowType = MutableStateFlow::class.asTypeName()

        val updateExtensionFunction = FunSpec.builder("updateState").apply {
            receiver(mutableStateFlowType.parameterizedBy(className))
            addModifiers(KModifier.INLINE)
            addParameter(
                functionParameterName,
                LambdaTypeName.get(
                    receiver = builderClassName,
                    returnType = UNIT
                )
            )

            beginControlFlow("return %M", updateStateFlowMember)
            addStatement("%M(it, %N)", factoryFunctionMemberName, functionParameterName)
            endControlFlow()
        }.build()

        return ExtensionSpecsCreationInfo(className, listOf(updateExtensionFunction))
    }

}
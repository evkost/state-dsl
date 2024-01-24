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

import com.evkost.statedsl.processor.core.validator.*
import com.evkost.statedsl.processor.core.validator.Validator
import com.evkost.statedsl.processor.core.validator.ValidationResult
import com.evkost.statedsl.processor.core.validator.assertNot
import com.evkost.statedsl.processor.core.validator.assertNotNull
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.evkost.statedsl.processor.hasAllDefaults
import com.evkost.statedsl.processor.hasModifier

private object Errors {
    const val CLASS_REQUIRED= "Builder can be created only for classes"
    const val CLASS_IS_ABSTRACT = "Cannot create builder for abstract class"
    const val CLASS_IS_COMPANION_OBJECT = "Cannot create builder for private class"
    const val PRIVATE_CLASS= "Cannot create builder for private class"
    const val INTERNAL_CLASS = "Cannot create builder for internal class (will be added in future)"
    const val SEALED_CLASS = "Cannot create builder for sealed class (will be added in future)"
    const val PRIMARY_CONSTRUCTOR_REQUIRED = "Cannot create builder for class, that hasn't primary constructor"
    const val PRIVATE_PRIMARY_CONSTRUCTOR = "Cannot create builder for class, that has private primary constructor"
    const val COMMON_CLASS_REQUIRED = "Builder can be created only for common classes"
    const val PRIVATE_CONSTRUCTOR_PROPERTY = "Cannot create builder for class, that has private property in primary constructor"
    const val PROTECTED_CONSTRUCTOR_PROPERTY = "Cannot create builder for class, that has protected property in primary constructor"
    const val CONSTRUCTOR_ALL_DEFAULTS_REQUIRED = "Builder will have all nullable properties with null values by default, because not all properties have a default value"
}

internal class DslBuilderValidator(
    private val logger: KSPLogger
) : Validator<Unit>(logger) {
    override fun defaultHandler(node: KSNode, data: Unit): ValidationResult {
        logger.error(Errors.CLASS_REQUIRED, node)

        return super.defaultHandler(node, data)
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration, data: Unit
    ): ValidationResult = validate(classDeclaration) {
        assert { classKind == ClassKind.CLASS } orError Errors.COMMON_CLASS_REQUIRED

        assertNot { isAbstract() } orError Errors.CLASS_IS_ABSTRACT

        assertNot { isCompanionObject } orError Errors.CLASS_IS_COMPANION_OBJECT

        assertNot { hasModifier(Modifier.PRIVATE) } orError Errors.PRIVATE_CLASS

        assertNot { hasModifier(Modifier.INTERNAL) } orError Errors.INTERNAL_CLASS //TODO apply internal in future

        assertNot { hasModifier(Modifier.SEALED) } orError Errors.SEALED_CLASS //TODO apply sealed classes in future

        assertNotNull { primaryConstructor } orError Errors.PRIMARY_CONSTRUCTOR_REQUIRED

        assertNot { primaryConstructor?.hasModifier(Modifier.PRIVATE) ?: false } orError Errors.PRIVATE_PRIMARY_CONSTRUCTOR

        //assert { primaryConstructor?.parameters?.all { it. } }

        assertNot { isPrimaryConstructorHavePrivateProperty(element) } orError Errors.PRIVATE_CONSTRUCTOR_PROPERTY

        assertNot { isPrimaryConstructorHaveProtectedProperty(element) } orError Errors.PROTECTED_CONSTRUCTOR_PROPERTY

        check { primaryConstructor?.hasAllDefaults ?: false } orWarn Errors.CONSTRUCTOR_ALL_DEFAULTS_REQUIRED
    }

    private fun isPrimaryConstructorHavePrivateProperty(classDeclaration: KSClassDeclaration): Boolean {
        val properties = classDeclaration.getDeclaredProperties()
        return classDeclaration.primaryConstructor!!.parameters.any { parameter ->
            properties.find { it.simpleName.asString() == parameter.name?.asString() }?.hasModifier(Modifier.PRIVATE) == true
        }
    }

    private fun isPrimaryConstructorHaveProtectedProperty(classDeclaration: KSClassDeclaration): Boolean {
        val properties = classDeclaration.getDeclaredProperties()
        return classDeclaration.primaryConstructor!!.parameters.any { parameter ->
            properties.find { it.simpleName.asString() == parameter.name?.asString() }?.hasModifier(Modifier.PROTECTED) == true
        }
    }
}


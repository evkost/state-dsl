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

package com.evkost.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import kotlin.reflect.KClass

internal fun Resolver.getSymbols(annotationClass: KClass<*>) =
    getSymbolsWithAnnotation(annotationClass.qualifiedName.orEmpty())
        .filter(KSNode::validate)

internal fun KSAnnotated.hasAnnotation(annotationClass: KClass<*>) =
    annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationClass.qualifiedName }

internal val KSClassDeclaration.hasAllDefaults
    get() = primaryConstructor?.parameters?.any { !it.hasDefault }?.not()

internal fun KSModifierListOwner.hasModifier(modifier: Modifier) =
    modifiers.any { it == modifier }

internal val KSDeclaration.lowercaseName
    get() = simpleName.asString().replaceFirstChar { it.lowercase() }

internal val KSDeclaration.uppercaseName
    get() = simpleName.asString().replaceFirstChar { it.uppercase() }

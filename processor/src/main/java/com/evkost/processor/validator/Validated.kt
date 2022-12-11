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

package com.evkost.processor.validator

import com.google.devtools.ksp.symbol.KSNode

internal sealed interface Validated<out T: ValidateVisitor<*>>

@JvmInline internal value class Valid<out T: ValidateVisitor<*>>(val value: KSNode) : Validated<T>
@JvmInline internal value class Invalid<out T: ValidateVisitor<*>>(val value: KSNode) : Validated<T>

internal inline fun <T: KSNode, V: ValidateVisitor<*>> validated(value: T, assertion: (T) -> Boolean): Validated<V> =
    if (assertion(value)) Valid(value) else Invalid(value)
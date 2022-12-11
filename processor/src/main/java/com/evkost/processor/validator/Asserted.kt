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

@JvmInline
internal value class Asserted(
    val value: Boolean
) {
    inline fun onNonAssert(lambda: () -> Unit) {
        if (!value) lambda()
    }
}

internal inline fun assertOf(value: () -> Boolean) = Asserted(value())

internal inline fun assertNotOf(value: () -> Boolean) = Asserted(!value())

internal inline fun assertNotNullOf(value: () -> Any?) = Asserted(value() != null)

internal inline fun assertNullOf(value: () -> Any?) = Asserted(value() == null)


/*
 * Copyright (C) 2023 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rickbusarow.ktlint.internal

import kotlin.contracts.contract

/** from Kotlin's addToStdlib.kt */
internal inline fun <T> T.letIf(predicate: Boolean, body: T.() -> T): T {
  return if (predicate) body() else this
}

/**
 * shorthand for `requireNotNull(this, lazyMessage)`
 *
 * @throws IllegalArgumentException if receiver is null
 */
internal inline fun <T : Any> T?.requireNotNull(lazyMessage: () -> Any): T {
  contract {
    returns() implies (this@requireNotNull != null)
  }
  return requireNotNull(this, lazyMessage)
}

/**
 * shorthand for `requireNotNull(this)`
 *
 * @throws IllegalArgumentException if receiver is null
 */
@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> T?.requireNotNull(): T {
  contract {
    returns() implies (this@requireNotNull != null)
  }
  return requireNotNull(this)
}

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

import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal

/**
 * Determines whether the receiver project is the "real" root of this
 * composite build, as opposed to the root projects of included builds.
 *
 * @see isRootProject to check if the project is the root of any build in a composite build
 */
internal fun Project.isRealRootProject(): Boolean {
  return (gradle as GradleInternal).isRootBuild && this == rootProject
}

/**
 * shorthand for `this == rootProject`
 *
 * For composite builds, this will return true for the root of each included build.
 *
 * @see isRealRootProject to check if the project is the ultimate root of a composite build
 */
internal fun Project.isRootProject() = this == rootProject

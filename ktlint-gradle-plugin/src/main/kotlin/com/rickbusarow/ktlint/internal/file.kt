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

import java.io.File

/**
 * `File("a/b/c/d.txt").segments() == ["a", "b", "c", "d.txt"]`
 *
 * @since 0.10.0
 */
fun File.segments(): List<String> = path.split(File.separatorChar)

/**
 * Makes parent directories, then creates the receiver file. If a
 * [content] argument was provided, it will be written to the newly-created
 * file. If the file already existed, its content will be overwritten.
 */
fun File.createSafely(content: String? = null): File = apply {
  if (content != null) {
    makeParentDir().writeText(content)
  } else {
    makeParentDir().createNewFile()
  }
}

/**
 * Creates the directories if they don't already exist.
 *
 * @see File.mkdirs
 * @see File.makeParentDir
 */
fun File.mkdirsInline(): File = apply(File::mkdirs)

/**
 * Creates the parent directory if it doesn't already exist.
 *
 * @see File.mkdirsInline
 * @see File.mkdirs
 */
fun File.makeParentDir(): File = apply {
  val fileParent = requireNotNull(parentFile) { "File's `parentFile` must not be null." }
  fileParent.mkdirs()
}

/**
 * Walks upward in the file tree, looking for a directory which will resolve [relativePath].
 *
 * For example, given a receiver File path of './a/b/c/' and a `relativePath` of
 * 'foo/bar.txt', this function will attempt to resolve the following paths in order:
 *
 * ```text
 * ./a/b/c/foo/bar.txt
 * ./a/b/foo/bar.txt
 * ./a/foo/bar.txt
 * ./foo/bar.txt
 * ```
 *
 * @returns the first path to contain an [existent][File.exists]
 *   File for [relativePath], or `null` if it could not be resolved
 * @see resolveInParent for a version which throws if nothing is resolved
 * @since 0.10.5
 */
fun File.resolveInParentOrNull(relativePath: String): File? {
  return resolve(relativePath).existsOrNull()?.normalize()
    ?: parentFile?.resolveInParentOrNull(relativePath)
}

/**
 * Non-nullable version of [resolveInParentOrNull]
 *
 * @see resolveInParentOrNull for a nullable, non-throwing variant
 * @since 0.10.5
 * @throws IllegalArgumentException if a file cannot be resolved
 */
fun File.resolveInParent(relativePath: String): File {
  return requireNotNull(resolveInParentOrNull(relativePath)) {
    "Could not resolve a file with relative path in any parent paths.\n" +
      "\t       relative path: $relativePath\n" +
      "\tstarting parent path: $absolutePath"
  }
}

/**
 * @return the receiver [File] if it exists in the file system, otherwise null
 * @since 0.10.5
 */
fun File.existsOrNull(): File? = takeIf { it.exists() }

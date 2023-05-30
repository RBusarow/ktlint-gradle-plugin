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

// TODO: https://youtrack.jetbrains.com/issue/KTIJ-23114/
@file:Suppress("invisible_reference", "invisible_member")

package com.rickbusarow.ktlint

import com.rickbusarow.ktlint.internal.createSafely
import com.rickbusarow.ktlint.internal.letIf
import com.rickbusarow.ktlint.internal.suffixIfNot
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldNotBe
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD
import java.io.File

@Execution(SAME_THREAD)
internal interface BaseGradleTest {

  open class GradleTestEnvironment {

    val workingDir: File by lazy { kotlin.io.path.createTempDirectory().toFile() }

    val buildFile by lazy {
      workingDir.resolve("build.gradle.kts").createSafely(
        """
        plugins {
          id("com.rickbusarow.ktlint") version "${BuildConfig.version}"
        }
        """.trimIndent()
      )
    }

    val settingsFile by lazy {
      workingDir.resolve("settings.gradle.kts")
        .createSafely(
          """
          rootProject.name = "root"

          pluginManagement {
            repositories {
              mavenLocal()
              mavenCentral()
              gradlePluginPortal()
              google()
              maven("https://plugins.gradle.org/m2/")
            }
            plugins {
              id("org.jetbrains.kotlin.jvm") version "${BuildConfig.kotlinVersion}"
              id("${BuildConfig.pluginId}") version "${BuildConfig.version}"
            }
          }
          dependencyResolutionManagement {
            repositories {
              mavenLocal()
              mavenCentral()
              google()
              maven("https://plugins.gradle.org/m2/")
            }
          }
          """.trimIndent()
        )
    }

    private val gradleRunner: GradleRunner by lazy {

      GradleRunner.create()
        .forwardOutput()
        .withGradleVersion("8.1.1")
        .withDebug(true)
        .withProjectDir(workingDir)
    }

    private fun build(
      tasks: List<String>,
      withPluginClasspath: Boolean,
      stacktrace: Boolean,
      shouldFail: Boolean = false
    ): BuildResult {
      ensureFilesAreWritten()
      return gradleRunner
        .letIf(withPluginClasspath) { withPluginClasspath() }
        .withArguments(tasks.letIf(stacktrace) { plus("--stacktrace") })
        .let { runner ->
          if (shouldFail) {
            runner.buildAndFail()
          } else {
            runner.build()
              .also { result ->
                result.tasks
                  .forAll { buildTask ->
                    buildTask.outcome shouldNotBe TaskOutcome.FAILED
                  }
              }
          }
        }
    }

    fun shouldSucceed(
      vararg tasks: String,
      withPluginClasspath: Boolean = false,
      stacktrace: Boolean = true,
      assertions: BuildResult.() -> Unit = {}
    ): BuildResult {

      return build(
        tasks.toList(),
        withPluginClasspath = withPluginClasspath,
        stacktrace = stacktrace,
        shouldFail = false
      ).also { result ->
        result.assertions()
      }
    }

    private fun ensureFilesAreWritten() {
      buildFile
      settingsFile
      workingDir.walkTopDown()
        .filter { it.isFile }
        .forEach { println("file://$it") }
    }

    fun shouldFail(
      vararg tasks: String,
      withPluginClasspath: Boolean = false,
      stacktrace: Boolean = true,
      assertions: BuildResult.() -> Unit = {}
    ): BuildResult {
      return build(
        tasks.toList(),
        withPluginClasspath = withPluginClasspath,
        stacktrace = stacktrace,
        shouldFail = true
      ).also { result ->
        result.assertions()
      }
    }

    fun markdown(
      path: String,
      @Language("markdown") content: String
    ): File = File(path).createSafely(content.trimIndent())

    @JvmName("writeMarkdownContent")
    fun File.markdown(
      @Language("markdown") content: String
    ): File = createSafely(content.trimIndent())

    fun kotlin(
      path: String,
      @Language("kotlin") content: String
    ): File = File(path).createSafely(content.trimIndent().suffixIfNot("\n\n"))

    @JvmName("writeKotlinContent")
    fun File.kotlin(
      @Language("kotlin") content: String
    ): File = createSafely(content.trimIndent())

    operator fun File.invoke(contentBuilder: () -> String) {
      createSafely(contentBuilder().trimIndent())
    }
  }

  fun test(action: GradleTestEnvironment.() -> Unit) {
    GradleTestEnvironment().action()
  }
}
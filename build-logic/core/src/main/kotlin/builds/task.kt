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

package builds

import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

fun TaskContainer.maybeNamed(taskName: String, configuration: Task.() -> Unit) {

  if (names.contains(taskName)) {
    named(taskName, configuration)
    return
  }

  matchingName(taskName)
    .configureEach(configuration)
}

/**
 * code golf for `matching { it.name == taskName }`
 *
 * @since 0.1.1
 */
fun TaskContainer.matchingName(taskName: String): TaskCollection<Task> =
  matching { it.name == taskName }

/**
 * code golf for `withType<T>().matching { it.name == taskName }`
 *
 * @since 0.1.1
 */
inline fun <reified T : Task> TaskContainer.matchingNameWithType(
  taskName: String
): TaskCollection<T> {
  val collection = matching { it.name == taskName && it is T }
  @Suppress("UNCHECKED_CAST")
  return collection as TaskCollection<T>
}

/**
 * Finds all tasks named [taskName] in all projects.
 * Does not throw if there are no tasks with that name.
 *
 * @since 0.1.1
 * @throws IllegalStateException if the project is not the root project
 */
fun Project.allProjectsTasksMatchingName(taskName: String): List<TaskCollection<Task>> {
  checkProjectIsRoot { "only call `allProjectsTasksMatchingName(...)` from the root project." }
  return allprojects.map { proj -> proj.tasks.matchingName(taskName) }
}

/**
 * Finds all tasks named [taskName] in all projects.
 * Does not throw if there are no tasks with that name.
 *
 * @since 0.1.1
 * @throws IllegalStateException if the project is not the root project
 */
inline fun <reified T : Task> Project.allProjectsTasksMatchingNameWithType(
  taskName: String
): List<TaskCollection<T>> {
  checkProjectIsRoot { "only call `allProjectsTasksMatchingName(...)` from the root project." }
  return allprojects
    .map { proj -> proj.tasks.matchingNameWithType(taskName) }
}

/**
 * Finds all tasks named [taskName] in this project's subprojects.
 * Does not throw if there are no tasks with that name.
 *
 * @since 0.1.1
 */
fun Project.subProjectsTasksMatchingName(taskName: String): List<TaskCollection<Task>> {
  return subprojects.map { proj -> proj.tasks.matchingName(taskName) }
}

/**
 * Finds all tasks named [taskName] in this project's subprojects.
 * Does not throw if there are no tasks with that name.
 *
 * @since 0.1.1
 */
inline fun <reified T : Task> Project.subProjectsTasksMatchingNameWithType(
  taskName: String
): List<TaskCollection<T>> {
  return subprojects
    .map { proj -> proj.tasks.matchingNameWithType(taskName) }
}

/**
 * adds all [objects] as dependencies to every task in the collection, inside a `configureEach { }`
 *
 * @since 0.1.1
 */
fun <T : Task> TaskCollection<T>.dependOn(vararg objects: Any): TaskCollection<T> {
  return also { taskCollection ->
    taskCollection.configureEach { task -> task.dependsOn(*objects) }
  }
}

/**
 * adds all [objects] as dependencies inside a configuration block, inside a `configure { }`
 *
 * @since 0.1.1
 */
fun <T : Task> TaskProvider<T>.dependsOn(vararg objects: Any): TaskProvider<T> {
  return also { provider ->
    provider.configure { task ->
      task.dependsOn(*objects)
    }
  }
}

/**
 * adds all [objects] as `mustRunAfter` to every task
 * in the collection, inside a `configureEach { }`
 *
 * @since 0.1.1
 */
fun <T : Task> TaskCollection<T>.mustRunAfter(vararg objects: Any): TaskCollection<T> {
  return also { taskCollection ->
    taskCollection.configureEach { task -> task.mustRunAfter(*objects) }
  }
}

/**
 * adds all [objects] as `mustRunAfter` inside a configuration block, inside a `configure { }`
 *
 * @since 0.1.1
 */
fun <T : Task> TaskProvider<T>.mustRunAfter(vararg objects: Any): TaskProvider<T> {
  return also { provider ->
    provider.configure { task ->
      task.mustRunAfter(*objects)
    }
  }
}

/**
 * Returns a collection containing the objects in this collection of the
 * given type. Equivalent to calling `withType(type).all(configureAction)`.
 *
 * @param S The type of objects to find.
 * @param configuration The action to execute for each object in the resulting collection.
 * @return The matching objects. Returns an empty collection
 *   if there are no such objects in this collection.
 * @see [DomainObjectCollection.withType]
 * @since 0.1.1
 */
inline fun <reified S : Any> DomainObjectCollection<in S>.withType(
  noinline configuration: (S) -> Unit
): DomainObjectCollection<S>? = withType(S::class.java, configuration)

/**
 * Returns a collection containing the objects in this collection of the given
 * type. The returned collection is live, so that when matching objects are later
 * added to this collection, they are also visible in the filtered collection.
 *
 * @param S The type of objects to find.
 * @return The matching objects. Returns an empty collection
 *   if there are no such objects in this collection.
 * @see [DomainObjectCollection.withType]
 * @since 0.1.1
 */
inline fun <reified S : Any> DomainObjectCollection<in S>.withType(): DomainObjectCollection<S> =
  withType(S::class.java)

/**
 * Returns a collection containing the objects in this collection of the given
 * type. The returned collection is live, so that when matching objects are later
 * added to this collection, they are also visible in the filtered collection.
 *
 * @param S The type of objects to find.
 * @return The matching objects. Returns an empty collection
 *   if there are no such objects in this collection.
 * @see [TaskCollection.withType]
 * @since 0.1.1
 */
inline fun <reified S : Task> TaskCollection<in S>.withType(): TaskCollection<S> =
  withType(S::class.java)

inline fun <reified T : Task> TaskContainer.register(
  name: String,
  vararg constructorArguments: Any,
  noinline configuration: (T) -> Unit
): TaskProvider<T> = register(name, T::class.java, *constructorArguments)
  .apply { configure { configuration(it) } }

fun <T : Task> TaskContainer.registerOnce(
  name: String,
  type: Class<T>,
  configurationAction: Action<in T>
): TaskProvider<T> = if (names.contains(name)) {
  named(name, type, configurationAction)
} else {
  register(name, type, configurationAction)
}

/**
 * @return the fully qualified name of this task's
 *   type, without any '_Decorated' suffix if one exists
 * @since 0.1.1
 */
fun Task.undecoratedTypeName(): String {
  return javaClass.canonicalName.removeSuffix("_Decorated")
}

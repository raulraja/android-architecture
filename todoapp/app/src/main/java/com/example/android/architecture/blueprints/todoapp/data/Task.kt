/*
 * Copyright 2016, The Android Open Source Project
 *
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

package com.example.android.architecture.blueprints.todoapp.data

import com.google.common.base.Objects
import java.util.*

/**
 * Immutable model class for a Task.
 */
data class Task
/**
 * Use this constructor to specify a completed Task if the Task already has an id (copy of
 * another Task).

 * @param title       title of the task
 * *
 * @param description description of the task
 * *
 * @param id          id of the task
 * *
 * @param isCompleted   true if the task is completed, false if it's active
 */
@JvmOverloads constructor(val title: String?, val description: String?,
                          val id: String = UUID.randomUUID().toString(), val isCompleted: Boolean = false) {

    /**
     * Use this constructor to create a new completed Task.

     * @param title       title of the task
     * *
     * @param description description of the task
     * *
     * @param completed   true if the task is completed, false if it's active
     */
    constructor(title: String?, description: String?, completed: Boolean) : this(title, description,
            UUID.randomUUID().toString(), completed) {
    }

    val titleForList: String? get() = if (title?.isNotEmpty() ?: false) title else description

    val isActive: Boolean get() = !isCompleted

    val isEmpty: Boolean get() = title.isNullOrEmpty() && description.isNullOrEmpty()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other is Task -> Objects.equal(id, other.id) &&
                Objects.equal(title, other.title) &&
                Objects.equal(description, other.description)
        else -> false
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id, title, description)
    }

    override fun toString(): String {
        return "Task with title " + title!!
    }
}

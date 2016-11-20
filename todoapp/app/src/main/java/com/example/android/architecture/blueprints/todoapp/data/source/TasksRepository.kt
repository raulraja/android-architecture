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

package com.example.android.architecture.blueprints.todoapp.data.source


import com.example.android.architecture.blueprints.todoapp.data.Task
import java.util.*

/**
 * Concrete implementation to load tasks from the data sources into a cache.
 *
 *
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
class TasksRepository // Prevent direct instantiation.
private constructor(val mTasksRemoteDataSource: TasksDataSource,
                    val mTasksLocalDataSource: TasksDataSource) : TasksDataSource {

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    internal val mCachedTasks: MutableMap<String, Task> = LinkedHashMap()

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    internal var mCacheIsDirty = false

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     *
     *
     * Note: [LoadTasksCallback.onDataNotAvailable] is fired if all data sources fail to
     * get the data.
     */
    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        // Respond immediately with cache if available and not dirty
        if (mCachedTasks.isNotEmpty() && !mCacheIsDirty) {
            callback.onTasksLoaded(ArrayList(mCachedTasks.values))
            return
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getTasksFromRemoteDataSource(callback)
        } else {
            // Query the local storage if available. If not, query the network.
            mTasksLocalDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {
                override fun onTasksLoaded(tasks: List<Task>) {
                    refreshCache(tasks)
                    callback.onTasksLoaded(ArrayList(mCachedTasks.values))
                }

                override fun onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback)
                }
            })
        }
    }

    override fun saveTask(task: Task) {
        mTasksRemoteDataSource.saveTask(task)
        mTasksLocalDataSource.saveTask(task)

        mCachedTasks.put(task.id, task)
    }

    override fun completeTask(task: Task) {
        mTasksRemoteDataSource.completeTask(task)
        mTasksLocalDataSource.completeTask(task)

        val completedTask = Task(task.title, task.description, task.id, true)

        mCachedTasks.put(task.id, completedTask)
    }

    override fun completeTask(taskId: String) {
        completeTask(getTaskWithId(taskId)!!)
    }

    override fun activateTask(task: Task) {
        mTasksRemoteDataSource.activateTask(task)
        mTasksLocalDataSource.activateTask(task)

        val activeTask = Task(task.title, task.description, task.id)

        mCachedTasks.put(task.id, activeTask)
    }

    override fun activateTask(taskId: String) {
        activateTask(getTaskWithId(taskId)!!)
    }

    override fun clearCompletedTasks() {
        mTasksRemoteDataSource.clearCompletedTasks()
        mTasksLocalDataSource.clearCompletedTasks()

        val it = mCachedTasks.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.isCompleted) {
                it.remove()
            }
        }
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     *
     *
     * Note: [GetTaskCallback.onDataNotAvailable] is fired if both data sources fail to
     * get the data.
     */
    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val cachedTask = getTaskWithId(taskId)

        // Respond immediately with cache if available
        if (cachedTask != null) {
            callback.onTaskLoaded(cachedTask)
            return
        }

        // Load from server/persisted if needed.

        // Is the task in the local data source? If not, query the network.
        mTasksLocalDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
            override fun onTaskLoaded(task: Task) {
                mCachedTasks.put(task.id, task)
                callback.onTaskLoaded(task)
            }

            override fun onDataNotAvailable() {
                mTasksRemoteDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
                    override fun onTaskLoaded(task: Task) {
                        mCachedTasks.put(task.id, task)
                        callback.onTaskLoaded(task)
                    }

                    override fun onDataNotAvailable() {
                        callback.onDataNotAvailable()
                    }
                })
            }
        })
    }

    override fun refreshTasks() {
        mCacheIsDirty = true
    }

    override fun deleteAllTasks() {
        mTasksRemoteDataSource.deleteAllTasks()
        mTasksLocalDataSource.deleteAllTasks()

        mCachedTasks.clear()
    }

    override fun deleteTask(taskId: String) {
        mTasksRemoteDataSource.deleteTask(taskId)
        mTasksLocalDataSource.deleteTask(taskId)

        mCachedTasks.remove(taskId)
    }

    private fun getTasksFromRemoteDataSource(callback: TasksDataSource.LoadTasksCallback) {
        mTasksRemoteDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {
            override fun onTasksLoaded(tasks: List<Task>) {
                refreshCache(tasks)
                refreshLocalDataSource(tasks)
                callback.onTasksLoaded(ArrayList(mCachedTasks.values))
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }

    private fun refreshCache(tasks: List<Task>) {
        mCachedTasks.clear()
        tasks.forEach { mCachedTasks.put(it.id, it) }
        mCacheIsDirty = false
    }

    private fun refreshLocalDataSource(tasks: List<Task>) {
        mTasksLocalDataSource.deleteAllTasks()
        tasks.forEach { mTasksLocalDataSource.saveTask(it) }
    }

    private fun getTaskWithId(id: String) = when {
        mCachedTasks.isEmpty() -> null
        else -> mCachedTasks[id]
    }

    companion object {
        private var INSTANCE: TasksRepository? = null

        /**
         * Returns the single instance of this class, creating it if necessary.

         * @param tasksRemoteDataSource the backend data source
         * *
         * @param tasksLocalDataSource  the device storage data source
         * *
         * @return the [TasksRepository] instance
         */
        @JvmStatic fun getInstance(tasksRemoteDataSource: TasksDataSource, tasksLocalDataSource: TasksDataSource):
        TasksRepository {
            if (INSTANCE == null) {
                INSTANCE = TasksRepository(tasksRemoteDataSource, tasksLocalDataSource)
            }
            return INSTANCE as TasksRepository
        }

        /**
         * Used to force [.getInstance] to create a new instance
         * next time it's called.
         */
        @JvmStatic fun destroyInstance() {
            INSTANCE = null
        }
    }
}

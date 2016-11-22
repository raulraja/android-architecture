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

package com.example.android.architecture.blueprints.todoapp.taskdetail

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository

/**
 * Listens to user actions from the UI ([TaskDetailFragment]), retrieves the data and updates
 * the UI as required.
 */
class TaskDetailPresenter(private val mTaskId: String?,
                          private val mTasksRepository: TasksRepository,
                          private val mTaskDetailView: TaskDetailContract.View) : TaskDetailContract.Presenter {

    init {
        mTaskDetailView.setPresenter(this)
    }

    override fun start() {
        openTask()
    }

    private fun openTask() {
        if (mTaskId.isNullOrEmpty()) {
            mTaskDetailView.showMissingTask()
            return
        }

        mTaskDetailView.setLoadingIndicator(true)
        mTasksRepository.getTask(mTaskId!!, object : TasksDataSource.GetTaskCallback {
            override fun onTaskLoaded(task: Task) {
                // The view may not be able to handle UI updates anymore
                if (!mTaskDetailView.isActive()) return
                mTaskDetailView.setLoadingIndicator(false)
                showTask(task)
            }

            override fun onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mTaskDetailView.isActive()) return
                mTaskDetailView.showMissingTask()
            }
        })
    }

    override fun editTask() {
        if (mTaskId.isNullOrEmpty()) {
            mTaskDetailView.showMissingTask()
            return
        }
        mTaskDetailView.showEditTask(mTaskId!!)
    }

    override fun deleteTask() {
        if (mTaskId.isNullOrEmpty()) {
            mTaskDetailView.showMissingTask()
            return
        }
        mTasksRepository.deleteTask(mTaskId!!)
        mTaskDetailView.showTaskDeleted()
    }

    override fun completeTask() {
        if (mTaskId.isNullOrEmpty()) {
            mTaskDetailView.showMissingTask()
            return
        }
        mTasksRepository.completeTask(mTaskId!!)
        mTaskDetailView.showTaskMarkedComplete()
    }

    override fun activateTask() {
        if (mTaskId.isNullOrEmpty()) {
            mTaskDetailView.showMissingTask()
            return
        }
        mTasksRepository.activateTask(mTaskId!!)
        mTaskDetailView.showTaskMarkedActive()
    }

    private fun showTask(task: Task) {
        val title = task.title
        val description = task.description

        if (title.isNullOrEmpty()) mTaskDetailView.hideTitle()
        else mTaskDetailView.showTitle(title!!)

        if (description.isNullOrEmpty()) mTaskDetailView.hideDescription()
        else mTaskDetailView.showDescription(description!!)

        mTaskDetailView.showCompletionStatus(task.isCompleted)
    }
}

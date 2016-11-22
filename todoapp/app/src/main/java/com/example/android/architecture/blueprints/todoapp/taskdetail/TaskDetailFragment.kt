/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.*
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import kotlinx.android.synthetic.main.taskdetail_frag.task_detail_complete as mDetailCompleteStatus
import kotlinx.android.synthetic.main.taskdetail_frag.task_detail_description as mDetailDescription
import kotlinx.android.synthetic.main.taskdetail_frag.task_detail_title as mDetailTitle

private const val ARGUMENT_TASK_ID = "TASK_ID"
private const val REQUEST_EDIT_TASK = 1

/**
 * Main UI for the task detail screen.
 */
class TaskDetailFragment : Fragment(), TaskDetailContract.View {

    private lateinit var mPresenter: TaskDetailContract.Presenter

    override fun setPresenter(presenter: TaskDetailContract.Presenter) {
        mPresenter = presenter
    }

    override fun onResume() {
        super.onResume()
        mPresenter.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.taskdetail_frag, container, false)
        setHasOptionsMenu(true)

        // Set up floating action button
        val fab = activity.findViewById(R.id.fab_edit_task) as FloatingActionButton
        fab.setOnClickListener { mPresenter.editTask() }

        return root
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.menu_delete -> {
            mPresenter.deleteTask()
            true
        }
        else -> false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu)
    }

    override fun setLoadingIndicator(active: Boolean) {
        if (active) {
            mDetailTitle.text = ""
            mDetailDescription.text = getString(R.string.loading)
        }
    }

    override fun hideDescription() {
        mDetailDescription.visibility = View.GONE
    }

    override fun hideTitle() {
        mDetailTitle.visibility = View.GONE
    }

    override fun showDescription(description: String) {
        mDetailDescription.visibility = View.VISIBLE
        mDetailDescription.text = description
    }

    override fun showCompletionStatus(complete: Boolean) {
        mDetailCompleteStatus.isChecked = complete
        mDetailCompleteStatus.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) mPresenter.completeTask()
            else mPresenter.activateTask()
        }
    }

    override fun showEditTask(taskId: String) {
        val intent = Intent(context, AddEditTaskActivity::class.java)
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId)
        startActivityForResult(intent, REQUEST_EDIT_TASK)
    }

    override fun showTaskDeleted() {
        activity.finish()
    }

    override fun showTaskMarkedComplete() {
        Snackbar.make(view!!, getString(R.string.task_marked_complete), Snackbar.LENGTH_LONG).show()
    }

    override fun showTaskMarkedActive() {
        Snackbar.make(view!!, getString(R.string.task_marked_active), Snackbar.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_EDIT_TASK) {
            // If the task was edited successfully, go back to the list.
            if (resultCode == Activity.RESULT_OK) {
                activity.finish()
            }
        }
    }

    override fun showTitle(title: String) {
        mDetailTitle.visibility = View.VISIBLE
        mDetailTitle.text = title
    }

    override fun showMissingTask() {
        mDetailTitle.text = ""
        mDetailDescription.text = getString(R.string.no_data)
    }

    override fun isActive() = isAdded

    companion object {
        fun newInstance(taskId: String?) = TaskDetailFragment().apply {
            arguments = Bundle().apply { putString(ARGUMENT_TASK_ID, taskId) }
        }
    }
}

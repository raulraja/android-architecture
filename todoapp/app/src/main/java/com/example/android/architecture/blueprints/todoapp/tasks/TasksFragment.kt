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

package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.PopupMenu
import android.view.*
import android.widget.BaseAdapter
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskActivity
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailActivity
import java.util.*
import kotlinx.android.synthetic.main.task_item.view.complete as completeCB
import kotlinx.android.synthetic.main.task_item.view.title as titleTV
import kotlinx.android.synthetic.main.tasks_frag.filteringLabel as mFilteringLabelView
import kotlinx.android.synthetic.main.tasks_frag.noTasks as mNoTasksView
import kotlinx.android.synthetic.main.tasks_frag.noTasksAdd as mNoTaskAddView
import kotlinx.android.synthetic.main.tasks_frag.noTasksIcon as mNoTaskIcon
import kotlinx.android.synthetic.main.tasks_frag.noTasksMain as mNoTaskMainView
import kotlinx.android.synthetic.main.tasks_frag.refresh_layout as swipeRefreshLayout
import kotlinx.android.synthetic.main.tasks_frag.tasksLL as mTasksView
import kotlinx.android.synthetic.main.tasks_frag.tasks_list as listView

/**
 * Display a grid of [Task]s. User can choose to view all, active or completed tasks.
 */
class TasksFragment : Fragment(), TasksContract.View {

    private lateinit var mPresenter: TasksContract.Presenter

    /**
     * Listener for clicks on tasks in the ListView.
     */
    internal var mItemListener: TaskItemListener = object : TaskItemListener {
        override fun onTaskClick(clickedTask: Task) {
            mPresenter.openTaskDetails(clickedTask)
        }

        override fun onCompleteTaskClick(completedTask: Task) {
            mPresenter.completeTask(completedTask)
        }

        override fun onActivateTaskClick(activatedTask: Task) {
            mPresenter.activateTask(activatedTask)
        }
    }

    private var mListAdapter: TasksAdapter = TasksAdapter(ArrayList<Task>(0), mItemListener)

    override fun setPresenter(presenter: TasksContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.tasks_frag, container, false)

        // Set up tasks view
        listView.adapter = mListAdapter

        // Set up  no tasks view
        mNoTaskAddView.setOnClickListener { showAddTask() }

        // Set up floating action button
        val fab = activity.findViewById(R.id.fab_add_task) as FloatingActionButton
        fab.setImageResource(R.drawable.ic_add)
        fab.setOnClickListener { mPresenter.addNewTask() }

        // Set up progress indicator
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(activity, R.color.colorPrimary),
                ContextCompat.getColor(activity, R.color.colorAccent),
                ContextCompat.getColor(activity, R.color.colorPrimaryDark)
        )
        // Set the scrolling view in the custom SwipeRefreshLayout.
        swipeRefreshLayout.setScrollUpChild(listView)
        swipeRefreshLayout.setOnRefreshListener { mPresenter.loadTasks(false) }

        setHasOptionsMenu(true)

        return root
    }

    override fun onResume() {
        super.onResume()
        mPresenter.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mPresenter.result(requestCode, resultCode)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_clear -> mPresenter.clearCompletedTasks()
            R.id.menu_filter -> showFilteringPopUpMenu()
            R.id.menu_refresh -> mPresenter.loadTasks(true)
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu)
    }

    override fun showFilteringPopUpMenu() {
        val popup = PopupMenu(context, activity.findViewById(R.id.menu_filter))
        popup.menuInflater.inflate(R.menu.filter_tasks, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.active -> mPresenter.filtering = TasksFilterType.ACTIVE_TASKS
                R.id.completed -> mPresenter.filtering = TasksFilterType.COMPLETED_TASKS
                else -> mPresenter.filtering = TasksFilterType.ALL_TASKS
            }
            mPresenter.loadTasks(false)
            true
        }

        popup.show()
    }

    override fun setLoadingIndicator(active: Boolean) {
        if (view == null) return

        // Make sure setRefreshing() is called after the layout is done with everything else.
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = active }
    }

    override fun showTasks(tasks: List<Task>) {
        mListAdapter.replaceData(tasks)

        mTasksView.visibility = View.VISIBLE
        mNoTasksView.visibility = View.GONE
    }

    override fun showNoActiveTasks() {
        showNoTasksViews(resources.getString(R.string.no_tasks_active),
                R.drawable.ic_check_circle_24dp,
                false
        )
    }

    override fun showNoTasks() {
        showNoTasksViews(resources.getString(R.string.no_tasks_all),
                R.drawable.ic_assignment_turned_in_24dp,
                false
        )
    }

    override fun showNoCompletedTasks() {
        showNoTasksViews(resources.getString(R.string.no_tasks_completed),
                R.drawable.ic_verified_user_24dp,
                false
        )
    }

    override fun showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message))
    }

    private fun showNoTasksViews(mainText: String, iconRes: Int, showAddView: Boolean) {
        mTasksView.visibility = View.GONE
        mNoTasksView.visibility = View.VISIBLE

        mNoTaskMainView.text = mainText
        mNoTaskIcon.setImageDrawable(resources.getDrawable(iconRes))
        mNoTaskAddView.visibility = if (showAddView) View.VISIBLE else View.GONE
    }

    override fun showActiveFilterLabel() {
        mFilteringLabelView.text = resources.getString(R.string.label_active)
    }

    override fun showCompletedFilterLabel() {
        mFilteringLabelView.text = resources.getString(R.string.label_completed)
    }

    override fun showAllFilterLabel() {
        mFilteringLabelView.text = resources.getString(R.string.label_all)
    }

    override fun showAddTask() {
        val intent = Intent(context, AddEditTaskActivity::class.java)
        startActivityForResult(intent, AddEditTaskActivity.REQUEST_ADD_TASK)
    }

    override fun showTaskDetailsUi(taskId: String) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
        val intent = Intent(context, TaskDetailActivity::class.java)
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        startActivity(intent)
    }

    override fun showTaskMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete))
    }

    override fun showTaskMarkedActive() {
        showMessage(getString(R.string.task_marked_active))
    }

    override fun showCompletedTasksCleared() {
        showMessage(getString(R.string.completed_tasks_cleared))
    }

    override fun showLoadingTasksError() {
        showMessage(getString(R.string.loading_tasks_error))
    }

    private fun showMessage(message: String) {
        Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
    }

    override fun isActive() = isAdded

    private class TasksAdapter(var mTasks: List<Task>, private val mItemListener: TaskItemListener) : BaseAdapter() {

        fun replaceData(tasks: List<Task>) {
            mTasks = tasks
            notifyDataSetChanged()
        }

        override fun getCount() = mTasks.size

        override fun getItem(i: Int) = mTasks[i]

        override fun getItemId(i: Int) = i.toLong()

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            val rowView: View = view ?: LayoutInflater.from(viewGroup.context).inflate(R.layout.task_item, viewGroup, false)

            val task = getItem(i)

            rowView.titleTV.text = task.titleForList

            // Active/completed task UI
            rowView.completeCB.isChecked = task.isCompleted
            rowView.setBackgroundDrawable(viewGroup.context.resources.getDrawable(
                    if (task.isCompleted) R.drawable.list_completed_touch_feedback else R.drawable.touch_feedback))

            rowView.completeCB.setOnClickListener {
                if (task.isCompleted) mItemListener.onActivateTaskClick(task)
                else mItemListener.onCompleteTaskClick(task)
            }

            rowView.setOnClickListener { mItemListener.onTaskClick(task) }

            return rowView
        }
    }

    interface TaskItemListener {
        fun onTaskClick(clickedTask: Task)
        fun onCompleteTaskClick(completedTask: Task)
        fun onActivateTaskClick(activatedTask: Task)
    }

    companion object {
        fun newInstance() = TasksFragment()
    }
}

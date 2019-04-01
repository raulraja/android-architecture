package com.example.android.architecture.blueprints.todoapp.data

sealed class TasksError {
    object TasksNotAvailableError : TasksError()
}
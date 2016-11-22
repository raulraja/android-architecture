package com.example.android.architecture.blueprints.todoapp

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

inline fun <reified T : Fragment?> FragmentActivity.findFragment(idRes: Int): T {
    return supportFragmentManager.findFragmentById(idRes) as T
}

inline fun <reified T : Fragment> FragmentActivity.initFragment(idRes: Int, initFun: () -> T): T {
    val fragment = findFragment<T?>(idRes)
    return fragment ?: initFun()
}

fun FragmentActivity.addFragmentToActivity(fragment: Fragment, frameId: Int) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.add(frameId, fragment)
    transaction.commit()
}
package com.example.android.architecture.blueprints.todoapp.data

import arrow.core.Either
import arrow.effects.IO
import arrow.effects.fix

typealias ZIO<E, A> = IO<Either<E, A>>

fun <E, A> ZIO<E, A>.doOnSuccess(f: (A) -> Unit): ZIO<E, A> =
      fix().map {
          it.fold(ifLeft = { }, ifRight = { a ->
              f(a)
          })
          it
      }

fun <E, A> ZIO<E, A>.handleErrorWith(f: (E) -> ZIO<E, A>): ZIO<E, A> {
    return flatMap { either ->
        either.fold(ifLeft = {
            f(it)
        }, ifRight = { this })
    }
}

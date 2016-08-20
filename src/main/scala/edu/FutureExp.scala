package edu

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FutureExp {
  def futureOnComplete() = {
    Future {
      Thread.sleep(2000)
      println("stop")
    } onSuccess {
      case _ => println("success")
    }
  }

  def futureWithoutAwait() = {
    Future {
      Thread.sleep(2000)
      println("stop")
    }
  }
}
package edu

import akka.actor.{Actor, ActorSystem, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ActorWithFutures {
  def props(): Props = Props(new ActorWithFutures())

  case object Foo
}

class ActorWithFutures extends Actor {
  import ActorWithFutures._

  def receive: Receive = {
    case Foo =>
      Thread.sleep(1000)
      Future {
        Thread.sleep(1000)
        println("future stop")
      }
      println("receive stop")
  }
}

object FuturesInActor {
  def run() = {
    val actorSystem = ActorSystem("exp")
    val actor = actorSystem.actorOf(ActorWithFutures.props())
    actor ! ActorWithFutures.Foo
    println("after send")
  }

  //TODO stop everyting somehow
}


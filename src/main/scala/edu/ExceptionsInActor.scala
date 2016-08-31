package edu

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory


object ActorWithExceptions {
  def props(): Props = Props(new ActorWithExceptions())

  case object Foo
}

class ActorWithExceptions extends Actor with ActorLogging {
  import ActorWithExceptions._

  val a = 1 / 0

  def receive: Receive = {
    case Foo =>
      val i = 1 / 0
  }
}


object ExceptionsInActor {
  lazy val actorSystem = {
    val config = ConfigFactory.parseString("""
                                             |  akka.loglevel = "ERROR"
                                             |  akka.actor.debug {
                                             |    receive = on
                                             |    lifecycle = on
                                             |  }
                                             |
                                             |  akka.remote.netty.tcp.port=0
                                             |
                                             |  actor {
                                             |    provider = "akka.cluster.ClusterActorRefProvider"
                                             |  }
                                             |
                                             |  cluster {
                                             |    seed-nodes = ["akka.tcp://exp@127.0.0.1:2552"]
                                             |
                                             |    auto-down-unreachable-after = 10s
                                             |  }
                                           """.stripMargin)
    ActorSystem("exp", config)
  }

  def run() = {
    println("before actorOf")
    val actor = actorSystem.actorOf(ActorWithExceptions.props())
    println("before send")
    actor ! ActorWithExceptions.Foo
    println("after send")
  }

  def main(args: Array[String]): Unit = run()

  def stop() = actorSystem.terminate()
}


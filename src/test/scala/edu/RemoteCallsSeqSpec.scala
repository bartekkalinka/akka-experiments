package edu

import akka.actor.{Actor, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{FlatSpecLike, Matchers}
import akka.pattern.ask
import akka.util.Timeout
import edu.RemoteCallsSeq.Input

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class Call(input: Input)

case object GetCalls

class CheckCallsActor extends Actor {
  var calls: Seq[Input] = Seq.empty

  def receive: Receive = {
    case Call(input) =>
      calls = calls :+ input
      sender ! (input.value < 4)
    case GetCalls =>
      sender ! calls
  }
}

class RemoteCallsSeqSpec extends TestKit(ActorSystem("FindInSeqOfFuturesSpec"))
  with ImplicitSender
  with FlatSpecLike
  with Matchers {

  import RemoteCallsSeq._

  implicit val timeout = Timeout(50.seconds)

  "RemoteCallsSeq" should "call until first false and not more" in {
    val checkCallsActor = system.actorOf(Props[CheckCallsActor], "checkCallsActor")
    val seq = Seq(1, 2, 3, 4, 5, 6, 7).map(Input)
    val func: Input => Future[Boolean] = { input: Input => (checkCallsActor ? Call(input)).mapTo[Boolean] }
    val seqPrefixF = RemoteCallsSeq(seq, func).takeWhileTrue
    Await.result(seqPrefixF.map { seqPrefix => seqPrefix.map(_.value).toList should be (Seq(1, 2, 3))}, Duration.Inf)
    Await.result((checkCallsActor ? GetCalls).map {
      case calls: Seq[Input] => calls.map(_.value) should be (Seq(1, 2, 3, 4))
      case _ => fail("wrong type returned from GetCalls ask")
    }, Duration.Inf)
  }

}
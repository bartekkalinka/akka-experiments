package edu

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.Future

class MultipleCallsActorSpec extends TestKit(ActorSystem("MultipleCallsActorSpec"))
  with ImplicitSender
  with FlatSpecLike
  with Matchers {

  import MultipleCallsActor._

  case object MyCallRequestStatus extends CallRequestStatus

  case class MyCallRequest(id: Int, payload: String) extends CallRequest

  case class MyCallResponse(payload: String) extends CallResponse

  val callProducer = new CallProducer {
    override def send(request: CallRequest): Future[CallRequestStatus] = Future.successful(MyCallRequestStatus)
  }

  "MultipleCallsActor" should "handle multiple asynchronous calls" in {
    val calls: Seq[MyCallRequest] = Seq(
      MyCallRequest(1, "a"), MyCallRequest(2, "b"), MyCallRequest(3, "c")
    )
    val multipleCallsActor = system.actorOf(MultipleCallsActor.props(callProducer))
    multipleCallsActor ! HandleCalls(calls)
    multipleCallsActor ! HandleResponse(3, MyCallResponse("r"))
    multipleCallsActor ! HandleResponse(1, MyCallResponse("q"))
    multipleCallsActor ! HandleResponse(2, MyCallResponse("p"))
    expectMsg(Map[Int, CallResponse](
      1 -> MyCallResponse("q"), 2 -> MyCallResponse("p"), 3 -> MyCallResponse("r")
    ))
  }

  //TODO timeout for getting all responses
  //TODO busy state, so just one handle calls is processed at a time
  //TODO correct handling of repeated use
  //TODO test that CallProducer.send is called
}


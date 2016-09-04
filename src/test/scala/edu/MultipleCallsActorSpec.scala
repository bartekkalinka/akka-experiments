package edu

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{FlatSpecLike, Matchers}
import scala.concurrent.Future
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

class MultipleCallsActorSpec extends TestKit(ActorSystem("MultipleCallsActorSpec"))
  with ImplicitSender
  with FlatSpecLike
  with Matchers
  with MockitoSugar {

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

  it should "handle correctly repeated use" in {
    val calls: Seq[MyCallRequest] = Seq(MyCallRequest(1, "a"))
    val multipleCallsActor = system.actorOf(MultipleCallsActor.props(callProducer))
    multipleCallsActor ! HandleCalls(calls)
    multipleCallsActor ! HandleResponse(1, MyCallResponse("q"))
    expectMsg(Map[Int, CallResponse](1 -> MyCallResponse("q")))
    multipleCallsActor ! HandleCalls(calls)
    multipleCallsActor ! HandleResponse(1, MyCallResponse("p"))
    expectMsg(Map[Int, CallResponse](1 -> MyCallResponse("p")))
  }

  it should "not accept second HandleCalls request before serving the first one" in {
    val calls: Seq[MyCallRequest] = Seq(MyCallRequest(1, "a"))
    val multipleCallsActor = system.actorOf(MultipleCallsActor.props(callProducer))
    multipleCallsActor ! HandleCalls(calls)
    val calls2: Seq[MyCallRequest] = Seq(MyCallRequest(3, "b"))
    multipleCallsActor ! HandleCalls(calls2)
    expectMsg(ImBusy)
    multipleCallsActor ! HandleResponse(1, MyCallResponse("q"))
    expectMsg(Map[Int, CallResponse](1 -> MyCallResponse("q")))
  }

  it should "actually call producer send method" in {
    val calls: Seq[MyCallRequest] = Seq(MyCallRequest(1, "a"), MyCallRequest(2, "b"))
    val mockProducer = mock[CallProducer]
    val multipleCallsActor = system.actorOf(MultipleCallsActor.props(mockProducer))
    multipleCallsActor ! HandleCalls(calls)
    verify(mockProducer).send(MyCallRequest(1, "a"))
    verify(mockProducer).send(MyCallRequest(2, "b"))
  }

  //TODO timeout for getting all responses
  //TODO test incorrect ids in handle response
}


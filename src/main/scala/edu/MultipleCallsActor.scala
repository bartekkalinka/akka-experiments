package edu

import akka.actor.{ActorLogging, ActorRef, FSM, Props}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait CallRequest {
  val id: Int
}

trait CallRequestStatus

trait CallResponse

trait CallProducer {
  def send(request: CallRequest): Future[CallRequestStatus]
}

object MultipleCallsActor {
  def props(callProducer: CallProducer): Props =
    Props(classOf[MultipleCallsActor], callProducer)

  //received commands
  case class HandleCalls(calls: Seq[CallRequest])

  //sent responses
  case class HandleResponse(id: Int, response: CallResponse)
  case object ImBusy

  // states
  sealed trait State
  case object Idle extends State
  case object Busy extends State

  // data
  sealed trait Data
  case object Uninitialized extends Data
  final case class CallResponses(target: ActorRef, targetIds: Set[Int], responses: Map[Int, CallResponse]) extends Data
}

class MultipleCallsActor(callProducer: CallProducer) extends FSM[MultipleCallsActor.State, MultipleCallsActor.Data] with ActorLogging {
  import MultipleCallsActor._

  //TODO timeout for getting all responses

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(HandleCalls(calls), Uninitialized) =>
      val intCallResponses = CallResponses(sender, calls.map(_.id).toSet, Map[Int, CallResponse]())
      Future.traverse(calls) { call =>
        callProducer.send(call)
      }
      goto(Busy) using intCallResponses
  }

  when(Busy) {
    case Event(HandleResponse(id, response), CallResponses(target, targetIds, responses)) =>
      val newResponses = responses + (id -> response)
      if(newResponses.keys.toSet == targetIds) {
        target ! newResponses
        goto(Idle) using Uninitialized
      }
      else
        stay using CallResponses(target, targetIds, newResponses)
    case Event(HandleCalls(calls), callResponses) =>
      sender ! ImBusy
      stay using callResponses
  }
}


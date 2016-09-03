package edu

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

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

  case class HandleCalls(calls: Seq[CallRequest])

  case class HandleResponse(id: Int, response: CallResponse)
}

class MultipleCallsActor(callProducer: CallProducer) extends Actor with ActorLogging {
  import MultipleCallsActor._

  val callResponses = scala.collection.mutable.Map[Int, CallResponse]()
  var handleCallsSender: ActorRef = self
  var callIds: Set[Int] = Set()

  //TODO timeout for getting all responses
  //TODO busy state, so just one handle calls is processed at a time

  def receive: Receive = {
    case HandleCalls(calls) =>
      handleCallsSender = sender
      callIds = calls.map(_.id).toSet
      log.info(s"callIds $callIds")
      Future.traverse(calls) { call =>
        callProducer.send(call)
      }
    case HandleResponse(id, response) =>
      callResponses.put(id, response)
      log.info(s"callResponses.keys.toSet ${callResponses.keys.toSet}")
      if(callResponses.keys.toSet == callIds) {
        handleCallsSender ! callResponses.toMap
      }
  }
}


package edu

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object RemoteCallsSeq {
  case class Input(value: Int)
}

case class RemoteCallsSeq(seq: Seq[RemoteCallsSeq.Input], call: RemoteCallsSeq.Input => Future[Boolean]) {
  import RemoteCallsSeq._

  def takeWhileTrue: Future[Seq[Input]] = seq.foldLeft(Future.successful((true, Seq[Input]()))) {
    case (accF, input) => accF.flatMap {
      case (continue, acc) => if(continue) call(input).map((_, acc :+ input)) else Future.successful((continue, acc))
    }
  }.map {
    case (_, acc) => acc.init
  }
}


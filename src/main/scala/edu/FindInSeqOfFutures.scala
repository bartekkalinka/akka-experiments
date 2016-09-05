package edu

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FindInSeqOfFutures {
  case class Input(value: Int)
}

case class FindInSeqOfFutures(seq: Seq[FindInSeqOfFutures.Input], call: FindInSeqOfFutures.Input => Future[Boolean]) {
  import FindInSeqOfFutures._

  def takeUntilTrue: Future[Seq[Input]] = seq.foldLeft(Future.successful((true, Seq[Input]()))) {
    case (accF, input) => accF.flatMap {
      case (continue, acc) => if(continue) call(input).map((_, acc :+ input)) else Future.successful((continue, acc))
    }
  }.map {
    case (_, acc) => acc.init
  }
}


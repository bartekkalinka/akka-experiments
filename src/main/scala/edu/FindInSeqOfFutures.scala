package edu

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FindInSeqOfFutures {
  case class Input(value: Int)
}

case class FindInSeqOfFutures(seq: Seq[FindInSeqOfFutures.Input], call: FindInSeqOfFutures.Input => Future[Boolean]) {
  def takeUntilTrue: Future[Seq[FindInSeqOfFutures.Input]] = {
    val stream = seq.toStream
    Future.sequence(stream.map(call)).map { results =>
      results.zip(stream).takeWhile(_._1).map(_._2)
    }
  }
}


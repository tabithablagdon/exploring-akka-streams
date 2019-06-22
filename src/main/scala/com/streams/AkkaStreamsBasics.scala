package com.streams

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import scala.concurrent.Future

/**
  * Akka Streams Basics Prompts
  *
  * Implement the prompts below to satisfy the tests. Purpose of these prompts is to expose
  * you to the Akka Streams syntax and ease you into thinking about streaming systems.
  *
  * Note: In order to implement all those operations, no need to reference an `ActorSystem` or `ActorMaterializer`.
  * Flows / Sinks / Sources are "descriptions" of the graph and actual execution happens only later.
  * The `ActorMaterializer` transforms the description of a stream into a running executionable.
  *
  * Helpful dox to review before embarking on these exercises:
  * https://doc.akka.io/docs/akka/current/stream/stream-flows-and-basics.html
  *
  */
object AkkaStreamsBasics {

  /**
    * Prompt 1: mapToStrings
    *
    * A Source[O, _] is an operator with exactly one output, emitting data elements whenever
    * downstream operators are ready to receive them. Maps directly to Publisher` Reactive Streams protocol
    *
    * Change each of the streamed elements to their String values
    *
    * Many of Akka Streams' operators are designed to mimic Scala collection operators.
    *
    * Good reference on Akka Streams operators: https://doc.akka.io/docs/akka/current/stream/operators/index.html
    *
    **/
  def mapToStrings(ints: Source[Int, NotUsed]): Source[String, NotUsed] = ???


  /**
    * Prompt 2: filterOddValues
    *
    * Implement a flow that expects a stream of Integers, filters for even values, and outputs the result
    *
    */
  def filterOddValues: Flow[Int, Int, NotUsed] = ???


  /**
    * Prompt 3: sumOddValues
    *
    * Implement a flow that expects a stream of Integers, filters for odd values, sums up the values, and
    * outputs the result.
    *
    */
  def sumOddValues: Flow[Int, Int, NotUsed] = ???


  /**
    * Prompt 4: filterUsingPreviousFilterFlowAndMapToStrings
    *
    * Similar to function composition, Flows can be composed using the `via` operator.
    * try to implement this method by composing the previous methods. Rather than re-using operations
    * as `operation(source): Source`, re-use the previously built Flow[Int, Int].
    *
    */
  def filterAndMapToStrings(ints: Source[Int, NotUsed]): Source[String, NotUsed] = ???


  /**
    * Prompt 5: filterAndMapToStringsUsingTwoVias
    *
    * You likely noticed that the `via` composition style reads more nicely since it is possible to read it
    * from left to right the same way the functions will be applied to the elements.
    *
    * Let's now re-use the passed in toStringFlow to re-implement the previous source semantics,
    * however by chaining multiple Flows with each-other.
    */
  def filterAndMapToStringsUsingTwoVias(ints: Source[Int, NotUsed], toString: Flow[Int, String, _]): Source[String, NotUsed] = ???


  /**
    * Prompt 6: firstElementSource
    *
    * You can also "trim" a stream, by taking a number of elements (or by predicate).
    * In this method, take the first element only -- the stream should be then completed once the first element has arrived.
    */
  def firstElementSource(ints: Source[Int, NotUsed]): Source[Int, NotUsed] = ???


  /**
    * Prompt 7: firstElementOddValuesFuture
    *
    * Let's finally actually *run* a stream and realize a `Materialized Value`.
    * Implement and run a graph that materializes the first element from a stream of odd integers
    *
    * Take a look at the `runWith` method and review different types of Sinks
    */
  def firstElementOddValuesFuture(ints: Source[Int, NotUsed])(implicit mat: Materializer): Future[Int] = ???


  /**
    * Prompt 8: recoverToASingleElement
    *
    * Recover [[IllegalStateException]] values to a 0 value
    *
    * Let's dig into failure handling. When an operator in a stream fails, this will normally
    * lead to the entire stream not operating correctly.
    *
    * Helpful dox: https://doc.akka.io/docs/akka/current/stream/stream-error.html
    *
    */
  def recoverToASingleElement(ints: Source[Int, NotUsed]): Source[Int, NotUsed] = ???


  /**
    * Prompt 9: recoverToAlternateSource
    *
    * Recover [[IllegalStateException]] values to the provided fallback Source
    *
    */
  def recoverToAlternateSource(ints: Source[Int, NotUsed], fallback: Source[Int, NotUsed]): Source[Int, NotUsed] = ???


  /**
    * Prompt 10: sumUntilBackpressureGoesAway
    *
    * One of the main benefits of Akka Streams is built in backpressure and backpressure aware operators
    *
    * Implement a Flow that will be able to continue receiving elements from its upstream Source
    * and "sum up" values if backpressure is received from downstream (the Sink to which this Flow will be attached)
    *
    * By doing this, we are able to keep the upstream running at its nominal rate, while accumulating
    * all information. The downstream will then consume the accumulated values also at its nominal rate,
    * which in this case we know / expect to be slower than the upstream.
    *
    * If the downstream would happen to be faster than the upstream, no such aggregation is needed,
    * and the elements can be passed through directly.
    *
    * Helpful Dox: https://doc.akka.io/docs/akka/current/stream/stream-rate.html
    *
    */
  def sumUntilBackpressureGoesAway: Flow[Int, Int, _] = ???


  /**
    * Prompt 11: keepRepeatingLastObservedValue
    *
    * Let's flip the puzzle around. What if there is a faster Sink
    * who wants to consume elements, yet the upstream is slower at providing them?
    *
    * Implement a Flow that is able to "invent" values by repeating the previously emitted value.
    *
    * This could be seen as a "keepLast", where the stage keeps the last observed value from upstream.
    **
    * Hint: See also [[Iterator.continually]]
    */
  def keepRepeatingLastObservedValue: Flow[Int, Int, _] = ???

}

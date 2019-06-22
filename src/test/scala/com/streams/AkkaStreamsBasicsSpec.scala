package com.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.testkit.TestKit
import scala.concurrent.duration._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, Matchers}

class AkkaStreamsBasicsSpec extends TestKit(ActorSystem("StreamingBasics"))
  with FunSuiteLike
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures
{
  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = scaled(Span(500, Millis)), interval = scaled(Span(1000, Millis)))

  implicit val materializer: ActorMaterializer = ActorMaterializer.create(system)

  override def afterAll(): Unit = {
    concurrent.Await.ready(system.terminate(), 10 seconds)
  }

  val ints: Source[Int, NotUsed] = Source.fromIterator(() => Iterator.from(1)).take(100) // safety limit, to avoid infinite ones
  def numbers(to: Int): List[Int] = Iterator.from(1).take(to).toList

  test("map integers to strings") {
    val n = 10

    val tenStrings = AkkaStreamsBasics.mapToStrings(ints).take(n)
    val eventualStrings = tenStrings.runWith(Sink.seq)
    val s = eventualStrings.futureValue
    s shouldEqual numbers(n).map(_.toString)
  }

  test("filter for odd values") {
    val n = 10

    val tenStrings = ints.take(n).via(AkkaStreamsBasics.filterOddValues)
    val eventualStrings = tenStrings.runWith(Sink.seq)
    val s = eventualStrings.futureValue
    s shouldEqual numbers(n).filter(_ % 2 == 1)
  }

  test("sum odd values") {
    val n = 10

    val tenStrings = ints.take(n).via(AkkaStreamsBasics.sumOddValues)
    val eventualStrings = tenStrings.runWith(Sink.head)
    val s = eventualStrings.futureValue
    s shouldEqual numbers(n).filter(_ % 2 == 1).sum
  }

  test("provide a Flow that can filter out odd numbers") {
    val n = 100

    val it = AkkaStreamsBasics.filterAndMapToStrings(ints).runWith(Sink.seq)
    val s = it.futureValue
    s shouldEqual numbers(n).filter(_ % 2 == 1).map(_.toString)
  }

  test("provide a Flow that can filter out even numbers by reusing previous flows") {
    val n = 100

    val toString = Flow[Int].map(_.toString)
    val it = AkkaStreamsBasics.filterAndMapToStringsUsingTwoVias(ints, toString).runWith(Sink.seq)
    val s = it.futureValue
    s shouldEqual numbers(n).filter(_ % 2 == 1).map(_.toString)
  }

  test("firstElementSource should only emit one element") {
    val p = AkkaStreamsBasics.firstElementSource(ints.drop(12)).runWith(TestSink.probe)
    p.ensureSubscription()
    p.request(100)
    p.expectNext(13)
    p.expectComplete()
    p.expectNoMessage(300.millis)
  }

  test("firstElementOddValuesFuture should only emit one element") {
    val p = AkkaStreamsBasics.firstElementOddValuesFuture(ints.drop(3))(materializer).futureValue
    val q = AkkaStreamsBasics.firstElementOddValuesFuture(ints.drop(1))(materializer).futureValue
    p shouldEqual 5
    q shouldEqual 3
  }

  test("recover from a failure into a backup element") {
    val (p, source) = TestSource.probe[Int](system).preMaterialize()
    val r = AkkaStreamsBasics.recoverToASingleElement(source)
    val s = r.runWith(TestSink.probe)

    s.request(10)
    p.ensureSubscription()
    p.expectRequest()

    p.sendNext(1)
    p.sendNext(2)

    s.expectNext(1)
    s.expectNext(2)

    val ex = new IllegalStateException("KABOOM!")

    p.sendError(ex)
    s.expectNext(0)
    s.expectComplete()
  }

  test("recover from a failure into a backup stream") {
    val (p, source) = TestSource.probe[Int](system).preMaterialize()
    val fallback = Source(List(10, 11))
    val r = AkkaStreamsBasics
      .recoverToAlternateSource(source, fallback)
    val s = r.runWith(TestSink.probe)

    s.request(10)
    p.ensureSubscription()
    p.expectRequest()

    p.sendNext(1)
    p.sendNext(2)

    s.expectNext(1)
    s.expectNext(2)

    val ex = new IllegalStateException("KABOOM!")

    p.sendError(ex)
    s.expectNext(10)
      .expectNext(11)
  }

  test("keep repeating last observed value") {
    val (sourceProbe, sinkProbe) =
      TestSource.probe[Int]
        .via(AkkaStreamsBasics.keepRepeatingLastObservedValue)
        .toMat(TestSink.probe)(Keep.both)
        .run()

    sourceProbe.ensureSubscription()
    val r = sourceProbe.expectRequest()

    sourceProbe.sendNext(1)
    sinkProbe.request(1)
    sinkProbe.expectNext(1)
    sinkProbe.request(1)
    sinkProbe.expectNext(1)

    sourceProbe.sendNext(22)
    sinkProbe.request(3)
    sinkProbe.expectNext(22)
    sinkProbe.expectNext(22)
    sinkProbe.expectNext(22)

    sourceProbe.sendComplete()
  }
}

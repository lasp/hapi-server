package latis.service.hapi

import cats.effect.unsafe.implicits.global
import org.scalatest.EitherValues._
import org.scalatest.flatspec._

import latis.data.DomainData
import latis.data.RangeData
import latis.data.Sample
import latis.data.SeqFunction
import latis.dataset.MemoizedDataset
import latis.metadata.Metadata
import latis.model._
import latis.ops.ToHapiTime
import latis.time.Time

class ToHapiTimeSpec extends AnyFlatSpec{

  private lazy val toHapiTime = new ToHapiTime

  "The ToHapiTime operation" should "expand yyyy-MM-dd" in {
    val time = Time.fromMetadata(Metadata("id"->"time", "type"->"string", "units"->"yyyy-MM-dd", "coverage"->"2000-01-01/2000-01-02")).value
    val disp = Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters")).value
    val model = Function.from(time, disp).value
    val data = new SeqFunction(Seq(
      Sample(DomainData("2000-01-01"), RangeData(1)),
    ))
    val dataset = new MemoizedDataset(Metadata("id"->"d1"), model, data).withOperation(toHapiTime)
    val metadata = dataset.model.getScalars.head.metadata
    val size = metadata.getProperty("size").get
    val units = metadata.getProperty("units").get
    val timeVal = dataset.samples.map(t => t.domain.head.value).compile.toList.unsafeRunSync()

    assert(size == "24")
    assert(units == "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    assert(timeVal.head.toString == "2000-01-01T00:00:00.000Z")
  }

  it should "handle full length yyyy-MM-ddTHH:mm:ss.SSSZ" in {
    val time = Time.fromMetadata(Metadata("id"->"time", "type"->"string", "units"->"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "coverage"->"2000-01-01T00:00:00.000Z/2000-01-02T00:00:00.000Z")).value
    val disp = Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters")).value
    val model = Function.from(time, disp).value
    val data = new SeqFunction(Seq(
      Sample(DomainData("2000-01-01T01:01:01.001Z"), RangeData(1)),
    ))
    val dataset = new MemoizedDataset(Metadata("id"->"d1"), model, data).withOperation(toHapiTime)
    val metadata = dataset.model.getScalars.head.metadata
    val size = metadata.getProperty("size").get
    val units = metadata.getProperty("units").get
    val timeVal = dataset.samples.map(t => t.domain.head.value).compile.toList.unsafeRunSync()

    assert(size == "24")
    assert(units == "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    assert(timeVal.head.toString == "2000-01-01T01:01:01.001Z")
  }

  it should "expand yyyy-DDD" in {
    val time = Time.fromMetadata(Metadata("id"->"time", "type"->"string", "units"->"yyyy-DDD", "coverage"->"2000-001/2000-002")).value
    val disp = Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters")).value
    val model = Function.from(time, disp).value
    val data = new SeqFunction(Seq(
      Sample(DomainData("2000-001"), RangeData(1)),
    ))
    val dataset = new MemoizedDataset(Metadata("id"->"d1"), model, data).withOperation(toHapiTime)
    val metadata = dataset.model.getScalars.head.metadata
    val size = metadata.getProperty("size").get
    val units = metadata.getProperty("units").get
    val timeVal = dataset.samples.map(t => t.domain.head.value).compile.toList.unsafeRunSync()

    assert(size == "24")
    assert(units == "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    assert(timeVal.head.toString == "2000-01-01T00:00:00.000Z")
  }

  it should "handle full length yyyy-DDDTHH:mm:ss.SSSZ" in {
    val time = Time.fromMetadata(Metadata("id"->"time", "type"->"string", "units"->"yyyy-DDD'T'HH:mm:ss.SSS'Z'", "coverage"->"2000-001T00:00:00.000Z/2000-002T00:00:00.000Z")).value
    val disp = Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters")).value
    val model = Function.from(time, disp).value
    val data = new SeqFunction(Seq(
      Sample(DomainData("2000-001T01:01:01.001Z"), RangeData(1)),
    ))
    val dataset = new MemoizedDataset(Metadata("id"->"d1"), model, data).withOperation(toHapiTime)
    val metadata = dataset.model.getScalars.head.metadata
    val size = metadata.getProperty("size").get
    val units = metadata.getProperty("units").get
    val timeVal = dataset.samples.map(t => t.domain.head.value).compile.toList.unsafeRunSync()

    assert(size == "24")
    assert(units == "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    assert(timeVal.head.toString == "2000-01-01T01:01:01.001Z")
  }

  it should "handle time in seconds since 2000" in {
    val time = Time.fromMetadata(Metadata("id"->"time", "type"->"double", "units"->"seconds since 2000-01-01", "coverage"->"0/10000")).value
    val disp = Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters")).value
    val model = Function.from(time, disp).value
    val data = new SeqFunction(Seq(
      Sample(DomainData(3661.001), RangeData(1)),
    ))
    val dataset = new MemoizedDataset(Metadata("id"->"d1"), model, data).withOperation(toHapiTime)
    val metadata = dataset.model.getScalars.head.metadata
    val size = metadata.getProperty("size").get
    val units = metadata.getProperty("units").get
    val timeVal = dataset.samples.map(t => t.domain.head.value).compile.toList.unsafeRunSync()

    assert(size == "24")
    assert(units == "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    assert(timeVal.head.toString == "2000-01-01T01:01:01.001Z")
  }

  it should "handle time in milliseconds since 2000" in {
    val time = Time.fromMetadata(Metadata("id"->"time", "type"->"double", "units"->"milliseconds since 2000-01-01", "coverage"->"0/10000000")).value
    val disp = Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters")).value
    val model = Function.from(time, disp).value
    val data = new SeqFunction(Seq(
      Sample(DomainData(3661001.0), RangeData(1)),
    ))
    val dataset = new MemoizedDataset(Metadata("id"->"d1"), model, data).withOperation(toHapiTime)
    val metadata = dataset.model.getScalars.head.metadata
    val size = metadata.getProperty("size").get
    val units = metadata.getProperty("units").get
    val timeVal = dataset.samples.map(t => t.domain.head.value).compile.toList.unsafeRunSync()

    assert(size == "24")
    assert(units == "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    assert(timeVal.head.toString == "2000-01-01T01:01:01.001Z")
  }

  it should "handle time in seconds since 1970" in {
    val time = Time.fromMetadata(Metadata("id"->"time", "type"->"double", "units"->"seconds since 1970-01-01", "coverage"->"0/1000000000")).value
    val disp = Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters")).value
    val model = Function.from(time, disp).value
    val data = new SeqFunction(Seq(
      Sample(DomainData(946688461.001), RangeData(1)),
    ))
    val dataset = new MemoizedDataset(Metadata("id"->"d1"), model, data).withOperation(toHapiTime)
    val metadata = dataset.model.getScalars.head.metadata
    val size = metadata.getProperty("size").get
    val units = metadata.getProperty("units").get
    val timeVal = dataset.samples.map(t => t.domain.head.value).compile.toList.unsafeRunSync()

    assert(size == "24")
    assert(units == "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    assert(timeVal.head.toString == "2000-01-01T01:01:01.001Z")
  }

  it should "handle time in milliseconds since 1970" in {
    val time = Time.fromMetadata(Metadata("id"->"time", "type"->"double", "units"->"milliseconds since 1970-01-01", "coverage"->"0/1000000000000")).value
    val disp = Scalar.fromMetadata(Metadata("id"->"displacement", "type"->"int", "units"->"meters")).value
    val model = Function.from(time, disp).value
    val data = new SeqFunction(Seq(
      Sample(DomainData(946688461001.0), RangeData(1)),
    ))
    val dataset = new MemoizedDataset(Metadata("id"->"d1"), model, data).withOperation(toHapiTime)
    val metadata = dataset.model.getScalars.head.metadata
    val size = metadata.getProperty("size").get
    val units = metadata.getProperty("units").get
    val timeVal = dataset.samples.map(t => t.domain.head.value).compile.toList.unsafeRunSync()

    assert(size == "24")
    assert(units == "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    assert(timeVal.head.toString == "2000-01-01T01:01:01.001Z")
  }
}

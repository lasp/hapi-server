package latis.service.hapi

import munit.CatsEffectSuite
import org.scalatest.EitherValues._

import latis.data._
import latis.dataset.MemoizedDataset
import latis.metadata.Metadata
import latis.model._
import latis.ops.ToHapiTime
import latis.time.Time

class ToHapiTimeSpec extends CatsEffectSuite {

  private lazy val toHapiTime = new ToHapiTime

  test("expand yyyy-MM-dd") {
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

    dataset.samples.map(t => t.domain.head.value).compile.toList.map { timeVal =>
      assertEquals(size, "24")
      assertEquals(units, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      assertEquals(timeVal.head.toString, "2000-01-01T00:00:00.000Z")
    }
  }

  test("handle full length yyyy-MM-ddTHH:mm:ss.SSSZ") {
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

    dataset.samples.map(t => t.domain.head.value).compile.toList.map { timeVal =>
      assertEquals(size, "24")
      assertEquals(units, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      assertEquals(timeVal.head.toString, "2000-01-01T01:01:01.001Z")
    }
  }

  test("expand yyyy-DDD") {
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

    dataset.samples.map(t => t.domain.head.value).compile.toList.map { timeVal =>
      assertEquals(size, "24")
      assertEquals(units, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      assertEquals(timeVal.head.toString, "2000-01-01T00:00:00.000Z")
    }
  }

  test("handle full length yyyy-DDDTHH:mm:ss.SSSZ") {
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

    dataset.samples.map(t => t.domain.head.value).compile.toList.map { timeVal =>
      assertEquals(size, "24")
      assertEquals(units, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      assertEquals(timeVal.head.toString, "2000-01-01T01:01:01.001Z")
    }
  }

  test("handle time in seconds since 2000") {
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

    dataset.samples.map(t => t.domain.head.value).compile.toList.map { timeVal =>
      assertEquals(size, "24")
      assertEquals(units, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      assertEquals(timeVal.head.toString, "2000-01-01T01:01:01.001Z")
    }
  }

  test("handle time in milliseconds since 2000") {
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

    dataset.samples.map(t => t.domain.head.value).compile.toList.map { timeVal =>
      assertEquals(size, "24")
      assertEquals(units, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      assertEquals(timeVal.head.toString, "2000-01-01T01:01:01.001Z")
    }
  }

  test("handle time in seconds since 1970") {
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

    dataset.samples.map(t => t.domain.head.value).compile.toList.map { timeVal =>
      assertEquals(size, "24")
      assertEquals(units, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      assertEquals(timeVal.head.toString, "2000-01-01T01:01:01.001Z")
    }
  }

  test("handle time in milliseconds since 1970") {
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

    dataset.samples.map(t => t.domain.head.value).compile.toList.map { timeVal =>
      assertEquals(size, "24")
      assertEquals(units, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      assertEquals(timeVal.head.toString, "2000-01-01T01:01:01.001Z")
    }
  }
}

package com.themillhousegroup.reactivemongo.mocks.facets

import reactivemongo.play.json.collection.{ JSONCollection, JSONQueryBuilder }
import org.specs2.mock.Mockito
import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json._
import org.mockito.stubbing.Answer
import reactivemongo.api.Cursor
import org.mockito.invocation.InvocationOnMock
import reactivemongo.core.commands.{ GetLastError, LastError }

trait CollectionRemove extends MongoMockFacet {

  var uncheckedRemoves = Seq[Any]()

  private def setupMongoRemoves(targetCollection: JSONCollection,
    removeMatcher: => JsObject,
    ok: Boolean,
    maybeN: Option[Int],
    maybeCode: Option[Int]) = {

    // Nothing to mock an answer for - it's unchecked - but we record the remove to be useful
    targetCollection.uncheckedRemove(
      removeMatcher, anyBoolean)(
        anyPackWrites, anyEC) answers { args =>
          val o: JsObject = firstArg(args)
          uncheckedRemoves = uncheckedRemoves :+ o
          logger.debug(s"unchecked remove of $o, recorded for verification (${uncheckedRemoves.size})")
        }

    targetCollection.remove(
      removeMatcher, anyWriteConcern, anyBoolean)(
        anyPackWrites, anyEC) answers { args =>
          val o: JsObject = firstArg(args)
          logger.debug(s"Remove of object $o will be considered a ${bool2Success(ok)}")
          Future.successful(mockResult(ok, maybeN, maybeCode))
        }
  }

  def givenAnyMongoRemoveIsOK(targetCollection: JSONCollection, ok: Boolean = true) = {
    setupMongoRemoves(targetCollection, anyJs, ok, None, None)
  }

  def givenMongoRemoveIsOK(targetCollection: JSONCollection, removeQuery: JsObject, ok: Boolean = true) = {
    setupMongoRemoves(targetCollection, org.mockito.Matchers.eq(removeQuery), ok, None, None)
  }

  def givenAnyMongoRemoveIsOKAndAffectsNDocuments(targetCollection: JSONCollection, ok: Boolean = true, n: Int = 1) = {
    setupMongoRemoves(targetCollection, anyJs, ok, Some(n), None)
  }

  def givenMongoRemoveIsOKAndAffectsNDocuments(targetCollection: JSONCollection, removeQuery: JsObject, ok: Boolean = true, n: Int = 1) = {
    setupMongoRemoves(targetCollection, org.mockito.Matchers.eq(removeQuery), ok, Some(n), None)
  }
}

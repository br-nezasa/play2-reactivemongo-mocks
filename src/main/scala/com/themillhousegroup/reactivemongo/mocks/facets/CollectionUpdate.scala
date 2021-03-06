package com.themillhousegroup.reactivemongo.mocks.facets

import reactivemongo.play.json.collection.{ JSONCollection, JSONQueryBuilder }
import org.specs2.mock.Mockito
import reactivemongo.core.commands.LastError
import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json._

trait CollectionUpdate extends MongoMockFacet {

  var uncheckedUpdates = Seq[Any]()

  private def setupMongoUpdates[T](targetCollection: JSONCollection,
    selectorMatcher: => JsObject,
    updateMatcher: => JsObject,
    ok: Boolean) = {

    // Nothing to mock an answer for - it's unchecked - but we record the update to be useful
    targetCollection.uncheckedUpdate(
      selectorMatcher, updateMatcher, anyBoolean, anyBoolean)(
        anyPackWrites, anyPackWrites) answers { args =>
          val o: T = firstArg(args)
          uncheckedUpdates = uncheckedUpdates :+ o
          logger.debug(s"unchecked update of $o, recorded for verification (${uncheckedUpdates.size})")
        }

    targetCollection.update(
      selectorMatcher, updateMatcher, anyWriteConcern, anyBoolean, anyBoolean)(
        anyPackWrites, anyPackWrites, anyEC) answers { args =>
          val o: T = firstArg(args)
          logger.debug(s"Update of object $o will be considered a ${bool2Success(ok)}")
          Future.successful(mockUpdateResult(ok))
        }
  }

  def givenAnyMongoUpdateIsOK[T](targetCollection: JSONCollection, ok: Boolean = true) = {
    setupMongoUpdates[T](
      targetCollection,
      anyJs,
      anyJs,
      ok)
  }

  def givenMongoUpdateIsOK[T](targetCollection: JSONCollection, selector: JsObject, ok: Boolean = true) = {
    setupMongoUpdates[T](
      targetCollection,
      org.mockito.Matchers.eq(selector),
      anyJs,
      ok)
  }

}

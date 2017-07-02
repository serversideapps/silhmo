package utils.misc

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

import play.modules.reactivemongo.ReactiveMongoApi

import reactivemongo.api._
import reactivemongo.api.collections.bson._
import reactivemongo.bson._

import models.AuthToken

import org.joda.time.DateTime

/*object MongoTokenHash {
  val h = scala.collection.mutable.HashMap[java.util.UUID, AuthToken]()
}*/

class MongoTokenHash @Inject() (reactiveMongoApi: ReactiveMongoApi) extends CleverHashMap[java.util.UUID, AuthToken]("tokens") {
  //import MongoTokenHash._
  def tokensCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("tokens"))

  //def getfunc(key: java.util.UUID): Future[Option[AuthToken]] = Future.successful(if (h.contains(key)) Some(h(key)) else None)

  implicit object AuthTokenReader extends BSONDocumentReader[AuthToken] {
    def read(bson: BSONDocument): AuthToken = {
      val result = new AuthToken(
        id = java.util.UUID.fromString(bson.getAs[String]("id").getOrElse("")),
        userID = java.util.UUID.fromString(bson.getAs[String]("uid").getOrElse("")),
        expiry = DateTime.parse(bson.getAs[String]("expiry").getOrElse(""))
      )
      result
    }
  }

  def getfunc(tokenID: java.util.UUID): Future[Option[AuthToken]] = for {
    tokens <- tokensCollection
    token <- tokens.find(BSONDocument("id" -> tokenID.toString())).one[AuthToken]
  } yield token

  //def getlifunc(li: LoginInfo): Future[Option[AuthToken]] = Future.successful(h.find { case (id, token) => token.getli == li }.map(_._2))

  def getlifunc(loginInfo: LoginInfo): Future[Option[AuthToken]] = for {
    tokens <- tokensCollection
    token <- {
      tokens.find(BSONDocument()).one[AuthToken]
    }
  } yield token

  /*def savefunc(key: java.util.UUID, value: AuthToken): Future[Option[AuthToken]] = {
    h += (key -> value)
    Future.successful(Some(value))
  }*/

  implicit object AuthTokenWriter extends BSONDocumentWriter[AuthToken] {
    def write(token: AuthToken): BSONDocument =
      BSONDocument(
        "id" -> token.id.toString(),
        "uid" -> token.userID.toString(),
        "expiry" -> token.expiry.toString()
      )
  }

  def savefunc(key: java.util.UUID, token: AuthToken): Future[Option[AuthToken]] = for {
    tokens <- tokensCollection
    token <- tokens.insert(token)
  } yield None

  /*def deletefunc(key: java.util.UUID): Future[Option[AuthToken]] = {
    val value = h(key)
    h -= key
    Future.successful(Some(value))
  }*/

  def deletefunc(key: java.util.UUID): Future[Option[AuthToken]] = for {
    tokens <- tokensCollection
    result <- tokens.remove(BSONDocument("id" -> key.toString()))
  } yield None

  //def findallfunc(): Future[List[AuthToken]] = Future.successful((for ((k, v) <- h) yield v).toList)

  def findallfunc(): Future[List[AuthToken]] = tokensCollection.flatMap(
    tokens => tokens.find(BSONDocument()).cursor[AuthToken]().collect[List](100, Cursor.FailOnError[List[AuthToken]]())
  )

}
package utils.misc

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

import play.modules.reactivemongo.ReactiveMongoApi

import reactivemongo.api._
import reactivemongo.api.collections.bson._
import reactivemongo.bson._

import models.User
import settings.GlobalSettings._

/*object MongoUserHash {
  val h = scala.collection.mutable.HashMap[java.util.UUID, User]()
}*/

class MongoUserHash @Inject() (reactiveMongoApi: ReactiveMongoApi) extends CleverHashMap[java.util.UUID, User]("users") {
  //import MongoUserHash._
  def usersCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

  //def getfunc(key: java.util.UUID): Future[Option[User]] = Future.successful(if (h.contains(key)) Some(h(key)) else None)

  implicit object UserReader extends BSONDocumentReader[User] {
    def read(bson: BSONDocument): User = {
      val result = new User(
        userID = java.util.UUID.fromString(bson.getAs[String]("id").getOrElse("")),
        loginInfo = LoginInfo(
          bson.getAs[String]("liprid").getOrElse("credentials"),
          bson.getAs[String]("liprk").getOrElse("")
        ),
        handle = bson.getAs[String]("handle"),
        regpass = bson.getAs[String]("regpass"),
        firstName = bson.getAs[String]("firstname"),
        lastName = bson.getAs[String]("lastname"),
        fullName = bson.getAs[String]("fullname"),
        email = bson.getAs[String]("email"),
        avatarURL = bson.getAs[String]("avatarurl"),
        activated = bson.getAs[String]("activated").getOrElse("false") == "true"
      )
      result
    }
  }

  def getfunc(userID: java.util.UUID): Future[Option[User]] = for {
    users <- usersCollection
    user <- users.find(BSONDocument("id" -> userID.toString())).one[User]
  } yield user

  //def getlifunc(li: LoginInfo): Future[Option[User]] = Future.successful(h.find { case (id, user) => user.getli == li }.map(_._2))

  def getlifunc(loginInfo: LoginInfo): Future[Option[User]] = for {
    users <- usersCollection
    user <- {
      users.find(BSONDocument(
        "liprid" -> loginInfo.providerID,
        "liprk" -> loginInfo.providerKey
      )).one[User]
    }
  } yield user

  /*def savefunc(key: java.util.UUID, value: User): Future[Option[User]] = {
    h += (key -> value)
    Future.successful(Some(value))
  }*/

  implicit object UserWriter extends BSONDocumentWriter[User] {
    def write(user: User): BSONDocument =
      BSONDocument(
        "id" -> user.userID.toString(),
        "liprid" -> user.loginInfo.providerID,
        "liprk" -> user.loginInfo.providerKey,
        "handle" -> user.handle,
        "regpass" -> user.regpass,
        "firstname" -> user.firstName,
        "lastname" -> user.lastName,
        "fullname" -> user.fullName,
        "email" -> user.email,
        "avatarurl" -> user.avatarURL,
        "activated" -> user.activated.toString
      )
  }

  def savefunc(key: java.util.UUID, user: User): Future[Option[User]] = for {
    users <- usersCollection
    user <- users.insert(user)
  } yield None

  /*def deletefunc(key: java.util.UUID): Future[Option[User]] = {
    val value = h(key)
    h -= key
    Future.successful(Some(value))
  }*/

  def deletefunc(key: java.util.UUID): Future[Option[User]] = for {
    users <- usersCollection
    result <- users.remove(BSONDocument("id" -> key.toString()))
  } yield None

  //def findallfunc(): Future[List[User]] = Future.successful((for ((k, v) <- h) yield v).toList)

  def findallfunc(): Future[List[User]] = usersCollection.flatMap(
    users => users.find(BSONDocument()).cursor[User]().collect[List](MONGO_COLLECT_MAX, Cursor.FailOnError[List[User]]())
  )

}
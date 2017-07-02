package models.daos

import javax.inject.Inject

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import models.daos.UserDAOImpl._

import scala.collection.mutable
import scala.concurrent.Future

import play.modules.reactivemongo.ReactiveMongoApi

/**
 * Give access to the user object.
 */
class UserDAOMongoImpl @Inject() (reactiveMongoApi: ReactiveMongoApi) extends UserDAO {

  val users = new utils.misc.MongoUserHash(reactiveMongoApi)

  def get(key: java.util.UUID): Future[Option[User]] = users.get(key)
  def set(key: java.util.UUID, value: User): Future[Option[User]] = users.set(key, value)
  def findall(): Future[List[User]] = users.findall()

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = /*Future.successful*/ (
    //users.find { case (id, user) => user.loginInfo == loginInfo }.map(_._2)
    users.getli(loginInfo)
  )

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  //def find(userID: UUID) = Future.successful(users.get(userID))
  def find(userID: UUID) = users.get(userID)

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    //users += (user.userID -> user)
    users.set(user.userID, user)
    Future.successful(user)
  }
}

package models.daos

import java.util.UUID

import models.AuthToken
import models.daos.AuthTokenDAOImpl._
import org.joda.time.DateTime

import scala.collection.mutable
import scala.concurrent.Future

import javax.inject.Inject

import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDAOMongoImpl @Inject() (reactiveMongoApi: ReactiveMongoApi) extends AuthTokenDAO {

  val tokens = new utils.misc.MongoTokenHash(reactiveMongoApi)

  //def findall() = Future.successful((for ((k, v) <- tokens) yield v).toList)

  def findall() = tokens.findall()

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID) = tokens.get(id)

  /**
   * Finds expired tokens.
   *
   * @param dateTime The current date time.
   */
  /*def findExpired(dateTime: DateTime) = Future.successful {
    tokens.filter {
      case (id, token) =>
        token.expiry.isBefore(dateTime)
    }.values.toSeq
  }*/
  def findExpired(dateTime: DateTime) = Future.successful { Seq[models.AuthToken]() }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  /*def save(token: AuthToken) = {
    tokens += (token.id -> token)
    Future.successful(token)
  }*/
  def save(token: AuthToken) = tokens.save(token.id, token).map(opttoken => token)

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  /*def remove(id: UUID) = {
    tokens -= id
    Future.successful(())
  }*/
  def remove(id: UUID) = tokens.delete(id).map(_ => Unit)
}

/**
 * The companion object.
 */
/*object AuthTokenDAOMongoImpl {

  /**
   * The list of tokens.
   */
  val tokens: mutable.HashMap[UUID, AuthToken] = mutable.HashMap()
}*/

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

trait GetLoginInfo {
  def getli: LoginInfo
}

abstract class CleverHashMap[K, T <: GetLoginInfo](name: String = "cleverhashmap") {
  println(s"[ $name ] constructed")

  def getfunc(key: K): Future[Option[T]]
  def getlifunc(li: LoginInfo): Future[Option[T]]
  def savefunc(key: K, value: T): Future[Option[T]]
  def deletefunc(key: K): Future[Option[T]]
  def findallfunc(): Future[List[T]]

  def get(key: K): Future[Option[T]] = {
    println(s"[ $name ] get $key")
    getfunc(key)
  }

  def getli(li: LoginInfo): Future[Option[T]] = {
    println(s"[ $name ] getli $li")
    getlifunc(li)
  }

  def save(key: K, value: T): Future[Option[T]] = {
    println(s"[ $name ] saving $key -> $value")
    savefunc(key, value)
  }

  def delete(key: K): Future[Option[T]] = {
    println(s"[ $name ] deleting $key")
    deletefunc(key)
  }

  def update(key: K, value: T): Future[Option[T]] = {
    println(s"[ $name ] updating $key -> $value")
    delete(key).flatMap { result =>
      save(key, value)
    }
  }

  def set(key: K, value: T): Future[Option[T]] = {
    get(key).flatMap { hk =>
      hk match {
        case Some(v) => update(key, value)
        case _ => save(key, value)
      }
    }
  }

  def findall(): Future[List[T]] = {
    println(s"[ $name ] findall")
    findallfunc()
  }
}

class StaticCleverHashMap[K, T <: GetLoginInfo](name: String) extends CleverHashMap[K, T](name) {
  val h = scala.collection.mutable.HashMap[K, T]()

  def getfunc(key: K): Future[Option[T]] = Future.successful(if (h.contains(key)) Some(h(key)) else None)

  def getlifunc(li: LoginInfo): Future[Option[T]] = Future.successful(h.find { case (id, user) => user.getli == li }.map(_._2))

  def savefunc(key: K, value: T): Future[Option[T]] = {
    h += (key -> value)
    Future.successful(Some(value))
  }

  def deletefunc(key: K): Future[Option[T]] = {
    val value = h(key)
    h -= key
    Future.successful(Some(value))
  }

  def findallfunc(): Future[List[T]] = Future.successful((for ((k, v) <- h) yield v).toList)
}

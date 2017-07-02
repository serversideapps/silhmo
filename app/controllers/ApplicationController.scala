package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc.Controller
import utils.auth.DefaultEnv

import scala.concurrent.Future

import java.util.UUID

import scala.concurrent.ExecutionContext.Implicits.global

//////////////////////////////////////////////////////////////////

/**
 * The basic application controller.
 *
 * @param messagesApi The Play messages API.
 * @param silhouette The Silhouette stack.
 * @param socialProviderRegistry The social provider registry.
 * @param webJarAssets The webjar assets implementation.
 */
class ApplicationController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  socialProviderRegistry: SocialProviderRegistry,
  userDAO: models.daos.UserDAO,
  implicit val webJarAssets: WebJarAssets
)
  extends Controller with I18nSupport {

  /**
   * Handles the index action.
   *
   * @return The result to display.
   */
  def index = silhouette.SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  def dev = silhouette.UnsecuredAction.async { implicit request =>
    /*muserService.findall().map(userlist =>
      {
        Ok(views.html.dev(userlist.sortWith((a, b) => a.userID.compareTo(b.userID) > 0)))
      }
    )*/
    userDAO.findall().flatMap { userlist =>
      Future.successful(Ok(views.html.dev(userlist.sortWith((a, b) => a.userID.compareTo(b.userID) > 0))))
    }
  }

  def devflip(userID: String) = silhouette.UnsecuredAction.async { implicit request =>
    println("userid " + userID)
    val uuid = java.util.UUID.fromString(userID)
    println("uuid " + uuid)
    userDAO.get(uuid).flatMap {
      user =>
        user match {
          case Some(u) => userDAO.set(uuid, u.setActivated(!u.activated)).flatMap { user =>
            userDAO.findall().flatMap { userlist =>
              Future.successful(Ok(views.html.dev(userlist.sortWith((a, b) => a.userID.compareTo(b.userID) > 0))))
            }
          }
          case None => userDAO.findall().flatMap { userlist =>
            Future.successful(Ok(views.html.dev(userlist.sortWith((a, b) => a.userID.compareTo(b.userID) > 0))))
          }
        }
    }
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}

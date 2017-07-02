package models.daos

import play.api._

import javax.inject.Inject

class EnvDAOImpl @Inject() (c: Configuration) extends EnvDAO {

  def getDosendmail: Boolean = c.getBoolean("env_DO_SEND_MAIL").getOrElse(true)

  def getDocaptcha: Boolean = c.getBoolean("env_DO_CAPTCHA").getOrElse(true)
}

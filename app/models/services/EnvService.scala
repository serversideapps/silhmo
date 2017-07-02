package models.services

trait EnvService {

  def getDosendmail: Boolean

  def getDocaptcha: Boolean

}

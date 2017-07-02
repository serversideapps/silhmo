package models.services

import javax.inject.Inject

import models.daos.EnvDAO

class EnvServiceImpl @Inject() (envDAO: EnvDAO) extends EnvService {

  def getDosendmail: Boolean = envDAO.getDosendmail

  def getDocaptcha: Boolean = envDAO.getDocaptcha

}

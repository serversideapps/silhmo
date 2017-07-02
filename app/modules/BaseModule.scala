package modules

import com.google.inject.AbstractModule
import models.daos.{ AuthTokenDAO, AuthTokenDAOImpl, EnvDAO, EnvDAOImpl, AuthTokenDAOMongoImpl }
import models.services.{ AuthTokenService, AuthTokenServiceImpl, EnvService, EnvServiceImpl }
import net.codingwell.scalaguice.ScalaModule

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    bind[AuthTokenDAO].to[AuthTokenDAOMongoImpl]
    bind[AuthTokenService].to[AuthTokenServiceImpl]
    bind[EnvDAO].to[EnvDAOImpl]
    bind[EnvService].to[EnvServiceImpl]
  }
}

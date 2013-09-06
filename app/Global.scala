import akka.actor.Props
import com.google.common.cache.{Cache, CacheBuilder}
import jobs.{TestJob, Tick}
import play.libs.Akka
import play.Logger
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

/**
 * Created with IntelliJ IDEA.
 * User: mpandey
 * Date: 8/31/13
 * Time: 8:32 PM
 * To change this template use File | Settings | File Templates.
 */
import play.api._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Test code")

    val monitorActor = Akka.system.actorOf(Props[TestJob])

    Akka.system.scheduler.schedule(0.seconds, 5.seconds, monitorActor, Tick)
    Logger.info("Application has started")

  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
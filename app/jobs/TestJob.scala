package jobs

/**
 * Created with IntelliJ IDEA.
 * User: mpandey
 * Date: 8/31/13
 * Time: 7:07 PM
 * To change this template use File | Settings | File Templates.
 */

import akka.actor.Actor
import play.api.libs.json.{Json, JsValue}
import play.api.libs.iteratee.{Enumerator, Iteratee, Concurrent}
import play.Logger
import scala.concurrent.{Future, Promise}


sealed trait TestMessage

case object Tick extends TestMessage

case object Connect extends TestMessage

case class ResponseEnumerator(enumerator: Enumerator[String]) extends TestMessage

class TestJob extends Actor {

  val (enumerator, channel) = Concurrent.broadcast[String]

  def receive = {
    case Tick => {
      Logger.info("Got tick")
      channel.push(System.currentTimeMillis().toString())
    }
    case Connect => {
      channel.push(System.currentTimeMillis().toString())
      sender ! ResponseEnumerator(enumerator)
    }
  }

}
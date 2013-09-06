package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee.{Enumerator, Concurrent}
import play.api.libs.concurrent.Promise
import play.libs.{Akka, Time}
import java.util.Date
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import play.api.libs.EventSource
import akka.util.Timeout
import akka.actor.Props
import jobs.{ResponseEnumerator, TestJob}
import play.api.libs.json.JsValue
import play.Logger
import akka.pattern.ask
import jobs.Connect

object Application extends Controller {



  // --------------------------------
  // VERY SIMPLE STREAM : curl http://localhost:9000/poll
  // --------------------------------
  val pollEnumerator: Enumerator[String] = {
    Enumerator.generateM[String] {
      Promise.timeout(repeatTask, 5 seconds)
    }
  }

  def repeatTask = {
    Some(System.currentTimeMillis().toString)
  }

  def poll = Action {
    Ok.stream(pollEnumerator &> EventSource()).as("text/event-stream")
  }

  // --------------------------------
  // Channel Based streaming
  // --------------------------------
  val (channelEnumerator, out) = Concurrent.broadcast[String]

  // push to channel using curl http://localhost:9000/push
  def push =  Action {
    out.push(System.currentTimeMillis().toString()); Ok
  }

  // listen on channel url : curl http://localhost:9000/channel
  def channel = Action {
    Ok.stream(channelEnumerator &> EventSource()).as("text/event-stream")
  }


  // --------------------------------
  //Actor Based streaming :
  // --------------------------------
  def actor = Action {
    Async {
      implicit val timeout = Timeout(50000 seconds)
      val actor = Akka.system.actorOf(Props[TestJob])
      (actor ? Connect).map {
        case ResponseEnumerator(enumerator) =>
          Logger.error("Pushed message back")
          Ok.stream(enumerator &> EventSource()).as("text/event-stream")
      }
    }
  }

}
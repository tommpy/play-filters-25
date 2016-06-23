/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.play.filters

import akka.actor.ActorSystem
import akka.stream.{Materializer, ActorMaterializer}
import play.api.mvc.{EssentialFilter, Filter, RequestHeader, Result}
import play.api.{Logger, Play}
import play.mvc.Http.{HeaderNames, Status}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class CacheControlFilter(implicit override val mat: Materializer) extends Filter {
  val cachableContentTypes: Seq[String]

  final def apply(next: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    next(rh).map(r =>
      (r.header.status, r.body.contentType) match {
        case (Status.NOT_MODIFIED, _) => r
        case (_, Some(contentType)) if cachableContentTypes.exists(contentType.startsWith) => r
        case _ => r.withHeaders(HeaderNames.CACHE_CONTROL -> "no-cache,no-store,max-age=0")
      }
    )

  }
}

object CacheControlFilter {
  // do this for now just to get it to compile
  // we'll come back to this later
  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  def fromConfig(configKey: String) = {
    new CacheControlFilter() {
      override lazy val cachableContentTypes = {
        val c = Play.current.configuration.getStringList(configKey).toList.flatMap(_.asScala)
        Logger.info(s"Will allow caching of content types matching: ${c.mkString(", ")}")
        c
      }
    }
  }
}

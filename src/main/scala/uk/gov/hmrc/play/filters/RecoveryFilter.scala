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

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee._
import uk.gov.hmrc.play.http.HttpException
import play.api.http.Status._


object RecoveryFilter extends EssentialFilter with Results {
  override def apply(next: EssentialAction): EssentialAction = new EssentialAction  {
      def apply(rh: RequestHeader): Iteratee[Array[Byte], Result] = {
        Iteratee.flatten(next(rh).unflatten.map(_.it).recover(recoverErrors))
      }
  }

  def recoverErrors: PartialFunction[Throwable, Iteratee[Array[Byte], Result]] = {
    case e: HttpException if e.responseCode == NOT_FOUND => respondWith(new Status(e.responseCode)(e.getMessage))
  }

  def respondWith(result: Result): Iteratee[Array[Byte], Result] = Done(result, Input.Empty)
}

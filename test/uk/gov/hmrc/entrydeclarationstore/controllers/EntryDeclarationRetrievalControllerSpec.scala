/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.entrydeclarationstore.controllers

import org.scalatest.matchers.should.Matchers.*
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.HeaderNames
import play.api.libs.json.{JsString, JsValue}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.entrydeclarationstore.config.MockAppConfig
import uk.gov.hmrc.entrydeclarationstore.models.{EisSubmissionState, SubmissionIdLookupResult}
import uk.gov.hmrc.entrydeclarationstore.services.MockEntryDeclarationRetrievalService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EntryDeclarationRetrievalControllerSpec
    extends AnyWordSpec
    with MockEntryDeclarationRetrievalService
    with MockAppConfig {

  private val controller: EntryDeclarationRetrievalController =
    new EntryDeclarationRetrievalController(
      Helpers.stubControllerComponents(),
      mockEntryDeclarationRetrievalService,
      mockAppConfig)

  val eori             = "eori"
  val submissionId     = "submissionId"
  val correlationId    = "correlationId"
  val receivedDateTime = "receivedDateTime"
  val submissionIdLookupResult: SubmissionIdLookupResult =
    SubmissionIdLookupResult("dateTime", "housekeepingAt", "SubID", Some("eisSentTime"), EisSubmissionState.Sent)

  val bearerToken: String = "bearerToken"

  "EntryDeclarationRetrievalController" when {

    "getting payload from submissionId" when {
      val requestWithAuth = FakeRequest()
        .withHeaders(HeaderNames.AUTHORIZATION -> s"Bearer $bearerToken")

      "id exists" must {
        "return OK with the xml body" in {
          val payload: JsValue = JsString("payload")
          MockEntryDeclarationRetrievalService
            .retrieveSubmission(submissionId)
            .returns(Future.successful(Some(payload)))
          MockAppConfig.eisInboundBearerToken returns bearerToken

          val result: Future[Result] = controller.getSubmission(submissionId)(requestWithAuth)

          status(result) shouldBe OK

          contentAsString(result) shouldBe payload.toString

          contentType(result) shouldBe Some("application/json")
        }
      }

      "id does not exist" must {
        "return NOT_FOUND" in {
          MockEntryDeclarationRetrievalService.retrieveSubmission(submissionId).returns(Future.successful(None))
          MockAppConfig.eisInboundBearerToken returns bearerToken

          val result: Future[Result] = controller.getSubmission(submissionId)(requestWithAuth)

          status(result) shouldBe NOT_FOUND
        }
      }

      "return 403" when {
        "no authentication fails" in {
          MockAppConfig.eisInboundBearerToken returns "differentBearerToken"

          val result: Future[Result] = controller.getSubmission(submissionId)(requestWithAuth)

          status(result) shouldBe FORBIDDEN
        }
      }
    }
  }
}

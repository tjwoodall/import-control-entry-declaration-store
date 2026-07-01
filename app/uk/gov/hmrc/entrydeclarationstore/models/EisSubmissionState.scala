/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.entrydeclarationstore.models

import cats.Show
import cats.implicits.*
import play.api.libs.json.Format
import uk.gov.hmrc.entrydeclarationstore.utils.Enums

sealed trait EisSubmissionState {
  val alternativeName: String
}

object EisSubmissionState {
  case object NotSent extends EisSubmissionState {
    override val alternativeName = "not-sent"
  }
  case object Sent extends EisSubmissionState {
    override val alternativeName: String = "sent"
  }
  case object Error extends EisSubmissionState {
    override val alternativeName: String = "error"
  }

  private given show: Show[EisSubmissionState] = Show.show[EisSubmissionState](_.alternativeName)

  def mongoFormatString(eisSubmissionState: EisSubmissionState): String = eisSubmissionState.show

  given jsonFormat: Format[EisSubmissionState] = Enums.format[EisSubmissionState]
}

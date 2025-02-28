/*
 * Copyright 2015 Heiko Seeberger
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

package de.heikoseeberger.sbtheader

import sbt.URL
import scala.collection.breakOut

object LicenseDetection {

  private val spdxMapping =
    License.spdxLicenses.map(l => (l.spdxIdentifier, l))(breakOut): Map[String, SpdxLicense]

  def apply(
      licenses: Seq[(String, URL)],
      organizationName: String,
      startYear: Option[String],
      licenseStyle: LicenseStyle = LicenseStyle.Detailed
  ): Option[License] =
    apply(licenses, organizationName, startYear.map(_.toInt), None, licenseStyle)

  private[sbtheader] def apply(
      licenses: Seq[(String, URL)],
      organizationName: String,
      startYear: Option[Int],
      endYear: Option[Int],
      licenseStyle: LicenseStyle
  ): Option[License] = {
    val licenseName =
      licenses match {
        case (name, _) :: Nil => Some(name)
        case _                => None
      }

    for {
      name    <- licenseName
      license <- spdxMapping.get(name)
      year    <- combineYears(startYear, endYear)
    } yield license(year, organizationName, licenseStyle)
  }

  private def combineYears(startYear: Option[Int], endYear: Option[Int]): Option[String] =
    (startYear, endYear) match {
      case (Some(start), Some(end)) if start < end => Some(s"$start-$end")
      case (Some(start), _)                        => Some(start.toString)
      case (None, _)                               => None
    }
}

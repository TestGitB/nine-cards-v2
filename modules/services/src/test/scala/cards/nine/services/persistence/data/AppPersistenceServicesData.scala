/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
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

package cards.nine.services.persistence.data

import cards.nine.commons.test.data.ApplicationValues._
import cards.nine.commons.test.data.CommonValues._
import cards.nine.models.{IterableAppCursor, IterableCursor}
import cards.nine.repository.model.{App, AppData, DataCounter}
import cards.nine.services.persistence.conversions.AppConversions

import scala.util.Random

trait AppPersistenceServicesData extends AppConversions {

  val termDataCounter: String = Random.nextString(1)
  val countDataCounter: Int   = Random.nextInt(2)

  def repoAppData(num: Int = 0) =
    AppData(
      name = applicationName + num,
      packageName = applicationPackageName + num,
      className = applicationClassName + num,
      category = categoryStr,
      dateInstalled = dateInstalled,
      dateUpdate = dateUpdated,
      version = version,
      installedFromGooglePlay = installedFromGooglePlay)

  val repoAppData: AppData         = repoAppData(0)
  val seqRepoAppData: Seq[AppData] = Seq(repoAppData(0), repoAppData(1), repoAppData(2))

  def repoApp(num: Int = 0) = App(id = applicationId + num, data = repoAppData(num))

  val repoApp: App         = repoApp(0)
  val seqRepoApp: Seq[App] = Seq(repoApp(0), repoApp(1), repoApp(2))

  val iterableCursorApp = new IterableCursor[App] {
    override def count(): Int = seqRepoApp.length

    override def moveToPosition(pos: Int): App = seqRepoApp(pos)

    override def close(): Unit = ()
  }

  val iterableApps = new IterableAppCursor(iterableCursorApp, toApp)

  def createDataCounter(i: Int): DataCounter =
    DataCounter(term = s"$i - $termDataCounter", count = countDataCounter)

  val dataCounters   = 1 to 10 map createDataCounter
  val appPackageName = applicationPackageName + 0

}

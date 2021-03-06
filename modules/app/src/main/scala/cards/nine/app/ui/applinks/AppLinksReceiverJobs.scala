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

package cards.nine.app.ui.applinks

import android.net.Uri
import cards.nine.app.commons.Conversions
import cards.nine.app.ui.collections.tasks.CollectionJobs
import cards.nine.app.ui.commons.action_filters.CollectionAddedActionFilter
import cards.nine.app.ui.commons.{BroadAction, ImplicitsUiExceptions, Jobs}
import cards.nine.commons.NineCardExtensions._
import cards.nine.commons.services.TaskService._
import cards.nine.models.SharedCollection
import macroid.extras.ResourcesExtras._
import com.fortysevendeg.ninecardslauncher.R
import macroid.ActivityContextWrapper

class AppLinksReceiverJobs(actions: AppLinksReceiverUiActions)(
    implicit contextWrapper: ActivityContextWrapper)
    extends Jobs
    with Conversions
    with CollectionJobs
    with ImplicitsUiExceptions {

  def uriReceived(uri: Uri): TaskService[Unit] = {

    def safeExtractPath: Option[String] =
      Option(uri) flatMap (u => Option(u.getPath))

    val CollectionsPathRegex = "\\/shared-collection\\/(.+)".r

    def openInBrowser(uri: Uri): TaskService[Unit] =
      for {
        _ <- di.trackEventProcess.appLinkReceived(false)
        _ <- actions.showLinkNotSupportedMessage()
        _ <- di.launcherExecutorProcess.launchUrl(uri.toString)
        _ <- actions.exit()
      } yield ()

    (safeExtractPath, Option(uri)) match {
      case (Some(CollectionsPathRegex(id)), _) =>
        for {
          _                <- di.trackEventProcess.appLinkReceived(true)
          theme            <- getThemeTask
          _                <- actions.initializeView(theme)
          sharedCollection <- di.sharedCollectionsProcess.getSharedCollection(id)
          _                <- actions.showCollection(this, sharedCollection, theme)
        } yield ()
      case (_, Some(link)) =>
        openInBrowser(link)
      case (_, None) => actions.exit()
    }

  }

  def addCollection(sharedCollection: SharedCollection): TaskService[Unit] =
    for {
      col <- addSharedCollection(sharedCollection)
      _ <- sendBroadCastTask(
        BroadAction(CollectionAddedActionFilter.action, Some(col.id.toString)))
        .resolveLeftTo((): Unit)
      _ <- actions.exit()
    } yield ()

  def showError(): TaskService[Unit] =
    for {
      _ <- actions.showUnexpectedErrorMessage()
      _ <- actions.exit()
    } yield ()

  def shareCollection(sharedCollection: SharedCollection): TaskService[Unit] =
    for {
      _ <- di.launcherExecutorProcess.launchShare(
        getString(R.string.shared_collection_url, sharedCollection.id))
      _ <- actions.exit()
    } yield ()

  protected def getString(res: Int, format: AnyRef*) =
    resGetString(res, format)

}

package cards.nine.app.ui.launcher.actions.publicollections

import android.os.Bundle
import android.view.View
import cards.nine.app.commons.AppNineCardsIntentConversions
import cards.nine.app.ui.commons.AppLog
import cards.nine.app.ui.commons.actions.BaseActionFragment
import cards.nine.app.ui.commons.ops.TaskServiceOps._
import cards.nine.app.ui.launcher.LauncherPresenter
import cards.nine.models.{Collection, SharedCollection}
import cards.nine.models.types.{TopSharedCollection, TypeSharedCollection, Communication, NineCardsCategory}
import cards.nine.process.sharedcollections.SharedCollectionsConfigurationException
import cards.nine.process.theme.models.CardLayoutBackgroundColor
import com.fortysevendeg.ninecardslauncher.R

class PublicCollectionsFragment(implicit launcherPresenter: LauncherPresenter)
  extends BaseActionFragment
  with PublicCollectionsUiActions
  with PublicCollectionsDOM
  with PublicCollectionsListener
  with AppNineCardsIntentConversions { self =>

  lazy val collectionJobs = new PublicCollectionsJobs(self)

  lazy val packages = getSeqString(Seq(getArguments), BaseActionFragment.packages, Seq.empty[String])

  override def getLayoutId: Int = R.layout.list_action_fragment

  override protected lazy val backgroundColor: Int = theme.get(CardLayoutBackgroundColor)

  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {
    super.onViewCreated(view, savedInstanceState)
    collectionJobs.initialize().resolveServiceOr(onError)
  }

  override def loadPublicCollectionsByTypeSharedCollection(typeSharedCollection: TypeSharedCollection): Unit =
    collectionJobs.loadPublicCollectionsByTypeSharedCollection(typeSharedCollection).resolveServiceOr(onError)

  override def loadPublicCollectionsByCategory(category: NineCardsCategory): Unit =
    collectionJobs.loadPublicCollectionsByCategory(category).resolveServiceOr(onError)

  override def loadPublicCollections(): Unit =
    collectionJobs.loadPublicCollections().resolveServiceOr(onError)

  override def addLauncherCollection(collection: Collection): Unit =
    launcherPresenter.addCollection(collection)

  override def onAddCollection(sharedCollection: SharedCollection): Unit =
    collectionJobs.saveSharedCollection(sharedCollection).resolveServiceOr(_ => showErrorSavingCollectionInScreen())

  override def onShareCollection(sharedCollection: SharedCollection): Unit =
    collectionJobs.shareCollection(sharedCollection).resolveServiceOr(_ => showContactUsError())

  private[this] def onError(e: Throwable) = e match {
    case e: SharedCollectionsConfigurationException =>
      AppLog.invalidConfigurationV2
      showErrorLoadingCollectionInScreen()
    case _ => showErrorLoadingCollectionInScreen()
  }
}

object PublicCollectionsFragment {
  var statuses = PublicCollectionStatuses()
}

case class PublicCollectionStatuses(
  category: NineCardsCategory = Communication,
  typeSharedCollection: TypeSharedCollection = TopSharedCollection)




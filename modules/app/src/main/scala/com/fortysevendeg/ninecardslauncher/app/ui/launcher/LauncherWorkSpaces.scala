package com.fortysevendeg.ninecardslauncher.app.ui.launcher

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.fortysevendeg.ninecardslauncher.app.ui.commons.Constants
import com.fortysevendeg.ninecardslauncher.app.ui.components.AnimatedWorkSpaces
import com.fortysevendeg.ninecardslauncher.app.ui.launcher.WorkSpaceType._
import com.fortysevendeg.ninecardslauncher.process.collection.models.Collection
import macroid.{ActivityContextWrapper, Tweak, Ui}
import com.fortysevendeg.ninecardslauncher.app.ui.commons.Constants._

import scala.annotation.tailrec

class LauncherWorkSpaces(context: Context, attr: AttributeSet, defStyleAttr: Int)(implicit activityContext: ActivityContextWrapper)
  extends AnimatedWorkSpaces[LauncherWorkSpaceHolder, LauncherData](context, attr, defStyleAttr) {

  def this(context: Context)(implicit activityContext: ActivityContextWrapper) = this(context, null, 0)

  def this(context: Context, attr: AttributeSet)(implicit activityContext: ActivityContextWrapper) = this(context, attr, 0)

  override def getItemViewTypeCount: Int = 2

  override def getItemViewType(data: LauncherData, position: Int): Int = if (data.widgets) widgets else collections

  def isWidgetScreen = data(currentItem).widgets

  def isWidgetScreen(page: Int) = data(page).widgets

  def isCollectionScreen = !isWidgetScreen

  def isCollectionScreen(page: Int) = !isWidgetScreen(page)

  def goToWizardScreen(toRight: Boolean): Boolean = data.lift(if (toRight) {
    currentItem - 1
  } else {
    currentItem + 1
  }) exists (_.widgets)

  override def createView(viewType: Int): LauncherWorkSpaceHolder = viewType match {
    case `widgets` => new LauncherWorkSpaceWidgetsHolder
    case `collections` => new LauncherWorkSpaceCollectionsHolder(dimen)
  }

  override def populateView(view: Option[LauncherWorkSpaceHolder], data: LauncherData, viewType: Int, position: Int): Ui[_] =
    view match {
      case Some(v: LauncherWorkSpaceCollectionsHolder) => v.populate(data.collections)
      case _ => Ui.nop
    }
}

object WorkSpaceType {
  val widgets = 0
  val collections = 1
}

class LauncherWorkSpaceHolder(implicit activityContext: ActivityContextWrapper)
  extends FrameLayout(activityContext.application)

case class LauncherData(widgets: Boolean, collections: Seq[Collection] = Seq.empty)

object LauncherWorkSpacesTweaks {
  type W = LauncherWorkSpaces

  val defaultPage = 1

  // We create a new page every 9 collections
  @tailrec
  private def getCollectionsItems(collections: Seq[Collection], acc: Seq[LauncherData], newLauncherData: LauncherData): Seq[LauncherData] = {
    collections match {
      case Nil if newLauncherData.collections.nonEmpty => acc :+ newLauncherData
      case Nil => acc
      case h :: t if newLauncherData.collections.length == Constants.numSpaces => getCollectionsItems(t, acc :+ newLauncherData, LauncherData(widgets = false, Seq(h)))
      case h :: t =>
        val g: Seq[Collection] = newLauncherData.collections :+ h
        val n = LauncherData(widgets = false, g)
        getCollectionsItems(t, acc, n)
    }
  }

  def lwsData(collections: Seq[Collection], pageSelected: Int) = Tweak[W] {
    workspaces =>
      workspaces.data = LauncherData(widgets = true) +: getCollectionsItems(collections, Seq.empty, LauncherData(widgets = false))
      workspaces.init(pageSelected)
  }

  def lwsAddCollection(collection: Collection) = Tweak[W] {
    workspaces =>
      workspaces.data.lastOption foreach { data =>
        val lastWorkspaceHasSpace = data.collections.size < numSpaces
        if (lastWorkspaceHasSpace) {
          workspaces.data = workspaces.data map { d =>
            if (d == data) {
              d.copy(collections = d.collections :+ collection)
            } else d
          }
        } else {
          workspaces.data = workspaces.data :+ LauncherData(widgets = false, Seq(collection))
        }
        workspaces.selectPosition(workspaces.data.size - 1)
        workspaces.reset()
      }
  }

  def lwsRemoveCollection(collection: Collection) = Tweak[W] {
    workspaces =>
      val collections = workspaces.data flatMap (_.collections.filterNot(_ == collection))
      val maybeWorkspaceCollection = workspaces.data find (_.collections contains collection)
      val maybePage = maybeWorkspaceCollection map { workspace =>
        workspaces.data.indexOf(workspace)
      }
      workspaces.data = LauncherData(widgets = true) +: getCollectionsItems(collections, Seq.empty, LauncherData(widgets = false))
      val page = maybePage map { page =>
        if (workspaces.data.isDefinedAt(page)) page else workspaces.data.length - 1
      } getOrElse defaultPage
      workspaces.selectPosition(page)
      workspaces.reset()

  }

  def lwsSelect(position: Int) = Tweak[W](_.selectPosition(position))

}
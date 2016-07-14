package com.fortysevendeg.ninecardslauncher.app.ui.launcher.actions.createoreditcollection

import android.app.{Activity, Dialog}
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.{LinearLayout, ScrollView}
import com.fortysevendeg.macroid.extras.ImageViewTweaks._
import com.fortysevendeg.macroid.extras.ResourcesExtras._
import com.fortysevendeg.macroid.extras.TextTweaks._
import com.fortysevendeg.macroid.extras.ViewGroupTweaks._
import com.fortysevendeg.ninecardslauncher.app.commons.NineCardIntentConversions
import com.fortysevendeg.ninecardslauncher.app.ui.commons.ColorOps._
import com.fortysevendeg.ninecardslauncher.app.ui.commons.ImageResourceNamed._
import com.fortysevendeg.ninecardslauncher.app.ui.components.drawables.{IconTypes, PathMorphDrawable}
import com.fortysevendeg.ninecardslauncher.process.commons.types.ContactsCategory
import com.fortysevendeg.ninecardslauncher.process.commons.types.NineCardCategory._
import com.fortysevendeg.ninecardslauncher.process.commons.types.NineCardsMoment._
import com.fortysevendeg.ninecardslauncher2.{R, TR, TypedFindView}
import macroid.FullDsl._
import macroid._

case class IconDialogFragment(iconSelected: String)(implicit contextWrapper: ContextWrapper)
  extends DialogFragment
  with NineCardIntentConversions {

  val categoryIcons = appsCategories map { cat =>
    val name = resGetString(cat.getStringResource).getOrElse(cat.getStringResource)
    ItemData(name, cat.getIconResource)
  }

  val momentIcons = moments map { mom =>
    val name = resGetString(mom.getStringResource).getOrElse(mom.getStringResource)
    ItemData(name, mom.getIconResource)
  }

  val contactIcon = Seq {
    val name = resGetString(ContactsCategory.getStringResource).getOrElse(ContactsCategory.getStringResource)
    ItemData(name, ContactsCategory.getIconResource)
  }

  val icons = categoryIcons ++ momentIcons ++ contactIcon

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val rootView = new ScrollView(getActivity)
    val contentView = new LinearLayout(getActivity)
    contentView.setOrientation(LinearLayout.VERTICAL)

    val views = icons map { ic => new ItemView(ic, ic.icon == iconSelected) }

    ((rootView <~ vgAddView(contentView)) ~
      (contentView <~ vgAddViews(views))).run

    new AlertDialog.Builder(getActivity).setView(rootView).create()
  }

  class ItemView(data: ItemData, select: Boolean)
    extends LinearLayout(contextWrapper.bestAvailable)
    with TypedFindView {

    LayoutInflater.from(getActivity).inflate(R.layout.icon_info_item_dialog, this)

    lazy val text = Option(findView(TR.icon_dialog_name))
    lazy val icon = Option(findView(TR.icon_dialog_select))

    val colorizeDrawable = resGetDrawable(iconCollectionDetail(data.icon)).colorize(Color.GRAY)

    val drawable = new PathMorphDrawable(
      defaultIcon = IconTypes.CHECK,
      defaultStroke = resGetDimensionPixelSize(R.dimen.stroke_default),
      defaultColor = resGetColor(R.color.text_selected_color_dialog))

    ((text <~
      (if (select) tvColorResource(R.color.text_selected_color_dialog) else Tweak.blank) <~
      tvText(data.name) <~
      tvCompoundDrawablesWithIntrinsicBounds(left = Some(colorizeDrawable))) ~
      (icon <~ (if (select) ivSrc(drawable) else Tweak.blank)) ~
      (this <~ On.click{
        Ui {
          val responseIntent = new Intent
          responseIntent.putExtra(CreateOrEditCollectionFragment.iconRequest, data.icon)
          getTargetFragment.onActivityResult(getTargetRequestCode, Activity.RESULT_OK, responseIntent)
          dismiss()
        }
      })
    ).run

  }

  case class ItemData(name: String, icon: String)

}

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

package cards.nine.app.commons

import android.content.Intent
import cards.nine.app.ui.commons.Constants._
import cards.nine.app.ui.commons.ops.WidgetsOps._
import cards.nine.models.types._
import cards.nine.models.{NotCategorizedPackage, SharedCollection, SharedCollectionPackage, _}
import macroid.ActivityContextWrapper

import scala.util.Random

trait Conversions extends AppNineCardsIntentConversions {

  def toSeqCollectionData(collections: Seq[CloudStorageCollection]): Seq[CollectionData] =
    collections.zipWithIndex.map(zipped => toCollectionData(zipped._1, zipped._2))

  def toCollectionData(userCollection: CloudStorageCollection, position: Int): CollectionData =
    CollectionData(
      position = position,
      name = userCollection.name,
      collectionType = userCollection.collectionType,
      icon = userCollection.icon,
      themedColorIndex = position % numSpaces,
      appsCategory = userCollection.category,
      cards = toCardData(userCollection.items),
      moment = userCollection.moment map toMoment,
      originalSharedCollectionId = userCollection.originalSharedCollectionId,
      sharedCollectionId = userCollection.sharedCollectionId,
      sharedCollectionSubscribed = userCollection.sharedCollectionSubscribed getOrElse false)

  def toCardData(items: Seq[CloudStorageCollectionItem]): Seq[CardData] =
    items.zipWithIndex.map(zipped => toCardData(zipped._1, zipped._2))

  def toCardData(item: CloudStorageCollectionItem, position: Int): CardData = {
    val nineCardIntent = jsonToNineCardIntent(item.intent)
    CardData(
      position = position,
      term = item.title,
      packageName = nineCardIntent.extractPackageName(),
      cardType = CardType(item.itemType),
      intent = nineCardIntent)
  }

  def toCollectionDataFromSharedCollection(
      collection: SharedCollection,
      cards: Seq[CardData]): CollectionData =
    CollectionData(
      name = collection.name,
      collectionType = AppsCollectionType,
      icon = collection.icon,
      themedColorIndex = Random.nextInt(numSpaces),
      appsCategory = Option(collection.category),
      cards = cards,
      originalSharedCollectionId =
        if (collection.publicCollectionStatus == PublishedByMe)
          None
        else Option(collection.sharedCollectionId),
      sharedCollectionId = Option(collection.sharedCollectionId),
      publicCollectionStatus = collection.publicCollectionStatus)

  def toCardData(app: SharedCollectionPackage): CardData =
    CardData(
      term = app.title,
      packageName = Option(app.packageName),
      cardType = NoInstalledAppCardType,
      intent = toNineCardIntent(app))

  def toCardData(app: ApplicationData): CardData =
    CardData(
      term = app.name,
      packageName = Option(app.packageName),
      cardType = AppCardType,
      intent = toNineCardIntent(app))

  def toCardData(contact: Contact): CardData =
    CardData(
      term = contact.name,
      packageName = None,
      cardType = ContactCardType,
      intent = contactToNineCardIntent(contact.lookupKey),
      imagePath = Option(contact.photoUri))

  def toCardData(app: NotCategorizedPackage): CardData =
    CardData(
      term = app.title,
      packageName = Option(app.packageName),
      cardType = AppCardType,
      intent = toNineCardIntent(app))

  def toCardData(dockAppData: DockAppData): Option[CardData] = {
    dockAppData.dockType match {
      case AppDockType =>
        Option(
          CardData(
            term = dockAppData.name,
            packageName = dockAppData.intent.extractPackageName(),
            cardType = AppCardType,
            intent = dockAppData.intent))
      case ContactDockType =>
        Option(
          CardData(
            term = dockAppData.name,
            packageName = None,
            cardType = ContactCardType,
            intent = dockAppData.intent))
      case _ => None
    }
  }

  def toDockAppData(cloudStorageDockApp: CloudStorageDockApp): DockAppData =
    DockAppData(
      name = cloudStorageDockApp.name,
      dockType = cloudStorageDockApp.dockType,
      intent = jsonToNineCardIntent(cloudStorageDockApp.intent),
      imagePath = cloudStorageDockApp.imagePath,
      position = cloudStorageDockApp.position)

}

trait AppNineCardsIntentConversions extends NineCardsIntentConversions {

  def toNineCardIntent(app: SharedCollectionPackage): NineCardsIntent = {
    val intent = NineCardsIntent(NineCardsIntentExtras(package_name = Option(app.packageName)))
    intent.setAction(NineCardsIntentExtras.openNoInstalledApp)
    intent
  }

  def toNineCardIntent(app: NotCategorizedPackage): NineCardsIntent = {
    val intent = NineCardsIntent(NineCardsIntentExtras(package_name = Option(app.packageName)))
    intent.setAction(NineCardsIntentExtras.openNoInstalledApp)
    intent
  }

  def phoneToNineCardIntent(lookupKey: Option[String], tel: String): NineCardsIntent = {
    val intent = NineCardsIntent(
      NineCardsIntentExtras(contact_lookup_key = lookupKey, tel = Option(tel)))
    intent.setAction(NineCardsIntentExtras.openPhone)
    intent
  }

  def smsToNineCardIntent(lookupKey: Option[String], tel: String): NineCardsIntent = {
    val intent = NineCardsIntent(
      NineCardsIntentExtras(contact_lookup_key = lookupKey, tel = Option(tel)))
    intent.setAction(NineCardsIntentExtras.openSms)
    intent
  }

  def emailToNineCardIntent(lookupKey: Option[String], email: String): NineCardsIntent = {
    val intent = NineCardsIntent(
      NineCardsIntentExtras(contact_lookup_key = lookupKey, email = Option(email)))
    intent.setAction(NineCardsIntentExtras.openEmail)
    intent
  }

  def contactToNineCardIntent(lookupKey: String): NineCardsIntent = {
    val intent = NineCardsIntent(NineCardsIntentExtras(contact_lookup_key = Option(lookupKey)))
    intent.setAction(NineCardsIntentExtras.openContact)
    intent
  }

  def toNineCardIntent(intent: Intent): NineCardsIntent = {
    val i = NineCardsIntent(NineCardsIntentExtras())
    i.fill(intent)
    i
  }

  def toNineCardIntent(packageName: String, className: String): NineCardsIntent = {
    val intent = NineCardsIntent(
      NineCardsIntentExtras(package_name = Option(packageName), class_name = Option(className)))
    intent.setAction(NineCardsIntentExtras.openApp)
    intent.setClassName(packageName, className)
    intent
  }

  def toMoment(cloudStorageMoment: CloudStorageMoment): MomentData =
    MomentData(
      collectionId = None,
      timeslot = cloudStorageMoment.timeslot map toTimeSlot,
      wifi = cloudStorageMoment.wifi,
      bluetooth = cloudStorageMoment.bluetooth getOrElse Seq.empty,
      headphone = cloudStorageMoment.headphones,
      momentType = cloudStorageMoment.momentType,
      widgets = cloudStorageMoment.widgets map toWidgetDataSeq)

  def toMomentData(cloudStorageMoment: CloudStorageMoment): MomentData =
    MomentData(
      collectionId = None,
      timeslot = cloudStorageMoment.timeslot map toTimeSlot,
      wifi = cloudStorageMoment.wifi,
      bluetooth = cloudStorageMoment.bluetooth getOrElse Seq.empty,
      headphone = cloudStorageMoment.headphones,
      momentType = cloudStorageMoment.momentType,
      widgets = cloudStorageMoment.widgets map toWidgetDataSeq)

  def toWidgetDataSeq(widgets: Seq[CloudStorageWidget]): Seq[WidgetData] =
    widgets map toWidgetData

  def toWidgetData(widget: CloudStorageWidget): WidgetData =
    WidgetData(
      packageName = widget.packageName,
      className = widget.className,
      appWidgetId = None,
      area = WidgetArea(
        startX = widget.area.startX,
        startY = widget.area.startY,
        spanX = widget.area.spanX,
        spanY = widget.area.spanY),
      widgetType = widget.widgetType,
      label = widget.label,
      imagePath = widget.imagePath,
      intent = widget.intent map jsonToNineCardIntent)

  def toWidgetData(widget: AppWidget, momentId: Int, maybeCell: Option[Cell] = None)(
      implicit activityContextWrapper: ActivityContextWrapper): WidgetData = {
    val cell = maybeCell getOrElse widget.getSimulateCell
    WidgetData(
      momentId = momentId,
      packageName = widget.packageName,
      className = widget.className,
      appWidgetId = None,
      area = WidgetArea(startX = 0, startY = 0, spanX = cell.spanX, spanY = cell.spanY),
      widgetType = AppWidgetType,
      label = None,
      imagePath = None,
      intent = None)
  }

  def toSeqWidgetData(moments: Seq[Moment], widgetsByMoment: Seq[WidgetsByMoment])(
      implicit activityContextWrapper: ActivityContextWrapper): Seq[WidgetData] = moments flatMap {
    moment =>
      widgetsByMoment find (_.moment == moment.momentType) match {
        case Some(widgetByMoment) =>
          widgetByMoment.widgets.headOption map (toWidgetData(_, moment.id))
        case _ => None
      }
  }

  def toTimeSlot(cloudStorageMomentTimeSlot: CloudStorageMomentTimeSlot): MomentTimeSlot =
    MomentTimeSlot(
      from = cloudStorageMomentTimeSlot.from,
      to = cloudStorageMomentTimeSlot.to,
      days = cloudStorageMomentTimeSlot.days)

}

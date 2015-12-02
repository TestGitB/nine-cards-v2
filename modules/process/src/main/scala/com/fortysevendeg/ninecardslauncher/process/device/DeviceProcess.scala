package com.fortysevendeg.ninecardslauncher.process.device

import android.graphics.Bitmap
import com.fortysevendeg.ninecardslauncher.commons.contexts.ContextSupport
import com.fortysevendeg.ninecardslauncher.commons.services.Service._
import com.fortysevendeg.ninecardslauncher.process.device.models._

trait DeviceProcess {

  /**
   * Get the saved apps from the database
   * @param orderBy indicates the order to fetch the apps
   * @return the Seq[com.fortysevendeg.ninecardslauncher.process.device.models.App]
   * @throws AppException if exist some problem to get the apps
   */
  def getSavedApps(orderBy: GetAppOrder)(implicit context: ContextSupport): ServiceDef2[Seq[App], AppException]

  /**
    * Get iterable of saved apps from the database
    * @param orderBy indicates the order to fetch the apps
    * @return the Seq[com.fortysevendeg.ninecardslauncher.process.device.models.App]
    * @throws AppException if exist some problem to get the apps
    */
  def getIterableApps(orderBy: GetAppOrder)(implicit context: ContextSupport): ServiceDef2[IterableApps, AppException]

  /**
   * Create the bitmaps from a sequence of packages
   * @throws CreateBitmapException if exist some problem creating the bitmaps
   */
  def createBitmapsFromPackages(packages: Seq[String])(implicit context: ContextSupport): ServiceDef2[Unit, CreateBitmapException]

  /**
   * Get the available applications that contain shortcuts creating Intents and Drawables necessaries for UI actions
   * @return the Seq[com.fortysevendeg.ninecardslauncher.process.device.models.Shortcut] contains
   *         information about shortcut with the Intents and Drawables for UI actions
   * @throws ShortcutException if exist some problem to get the shortcuts in the cell phone
   */
  def getAvailableShortcuts(implicit context: ContextSupport): ServiceDef2[Seq[Shortcut], ShortcutException]

  /**
   * Save shortcut icon from bitmap
   * @return the String contains the path where the icon was stored
   * @throws ShortcutException if exist some problem storing icon
   */
  def saveShortcutIcon(name: String, bitmap: Bitmap)(implicit context: ContextSupport): ServiceDef2[String, ShortcutException]

  /**
   * Get the favorite contacts if they exist and fill all their data
   * @return the Seq[com.fortysevendeg.ninecardslauncher.process.device.models.Contact] contains
   *         information about the contact including its ContactInfo (if it exists)
   * @throws ContactException if exist some problem to get the favorite contacts
   */
  def getFavoriteContacts(implicit context: ContextSupport): ServiceDef2[Seq[Contact], ContactException]

  /**
   * Get the contacts by filter selected sorted without data. The filters are: all contacts, favorite contacts
   * and contacts with phone number
   * @return the Seq[com.fortysevendeg.ninecardslauncher.process.device.models.Contact] contains
   *         information about the contact
   * @throws ContactException if exist some problem to get the contacts
   */
  def getContacts(filter: ContactsFilter = AllContacts)(implicit context: ContextSupport): ServiceDef2[Seq[Contact], ContactException]

  /**
    * Get the iterable contacts by filter selected sorted without data. The filters are: all contacts, favorite contacts
    * and contacts with phone number
    * @return the com.fortysevendeg.ninecardslauncher.process.device.models.IterableContacts contains
    *         information about the contact
    * @throws ContactException if exist some problem to get the contacts
    */
  def getIterableContacts(filter: ContactsFilter = AllContacts)(implicit context: ContextSupport): ServiceDef2[IterableContacts, ContactException]

  /**
   * Get the contact and fill all their data
   * @return the com.fortysevendeg.ninecardslauncher.process.device.models.Contact contains
   *         information about the contact
   * @throws ContactException if exist some problem to get the contacts
   */
  def getContact(lookupKey: String)(implicit context: ContextSupport): ServiceDef2[Contact, ContactException]

  /**
   * Get the installed apps and store them in the repository
   * @throws AppException if exist some problem to get the apps or storing them
   */
  def saveInstalledApps(implicit context: ContextSupport): ServiceDef2[Unit, AppException]

  /**
   * Get an installed app and store it in the repository
   * @param packageName the packageName of the app to save
   * @throws AppException if exist some problem to get the app or storing it
   */
  def saveApp(packageName: String)(implicit context: ContextSupport): ServiceDef2[Unit, AppException]

  /**
   * Delete an app from the repository
   * @param packageName the packageName of the app to delete
   * @throws AppException if exist some problem deleting the app
   */
  def deleteApp(packageName: String)(implicit context: ContextSupport): ServiceDef2[Unit, AppException]

  /**
   * Get the contact and fill all their data
   * @param packageName the packageName of the app to update
   * @throws AppException if exist some problem to get the app or updating it
   */
  def updateApp(packageName: String)(implicit context: ContextSupport): ServiceDef2[Unit, AppException]

  /**
   * Get the widgets available on the phone
   * @return the Seq[com.fortysevendeg.ninecardslauncher.process.device.models.Widget]
   * @throws WidgetException if exist some problem to get the widgets
   */
  def getWidgets(implicit context: ContextSupport): ServiceDef2[Seq[Widget], WidgetException]

  /**
    * Get the last calls available on the phone
    * @return the Seq[com.fortysevendeg.ninecardslauncher.process.device.models.Call]
    * @throws CallException if exist some problem to get the last calls
    */
  def getLastCalls(implicit context: ContextSupport): ServiceDef2[Seq[LastCallsContact], CallException]
}

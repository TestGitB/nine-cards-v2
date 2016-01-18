package com.fortysevendeg.ninecardslauncher.services.api.impl

import com.fortysevendeg.ninecardslauncher.api.model._

import scala.util.Random

trait ApiServicesImplData {

  def generateUser =
    User(
      _id = Some(Random.nextString(10)),
      sessionToken = Some(Random.nextString(10)),
      email = Some(Random.nextString(10)),
      username = Some(Random.nextString(10)),
      password = Some(Random.nextString(10)),
      authData = Some(generateAuthData))

  def generateAuthData =
    AuthData(
      google = Some(generateAuthGoogle),
      facebook = None,
      twitter = None,
      anonymous = None)

  def generateAuthGoogle =
    AuthGoogle(
      email = Random.nextString(10),
      devices = generateAuthGoogleDevices(2))

  def generateAuthGoogleDevices(num: Int = 10) =
    1 to num map { _ =>
      AuthGoogleDevice(
        name = Random.nextString(10),
        deviceId = Random.nextString(10),
        secretToken = Random.nextString(10),
        permissions = generatePermissions())
    }

  def generatePermissions(num: Int = 10): Seq[String] = 1 to num map { n => s"Permission $n" }

  def generateInstallation =
    Installation(
      _id = Some(Random.nextString(10)),
      deviceType = Some(Random.nextString(10)),
      deviceToken = Some(Random.nextString(10)),
      userId = Some(Random.nextString(10)))

  def generateGooglePlayPackages =
    GooglePlayPackages(
      Seq.empty,
      generateGooglePlayPackageSeq())

  def generateGooglePlayPackageSeq(num: Int = 10) =
    1 to num map { _ =>
      GooglePlayPackage(generateGooglePlayApp)
    }

  def generateGooglePlayApp =
    GooglePlayApp(
      docid = Random.nextString(10),
      title = Random.nextString(10),
      creator = Random.nextString(10),
      descriptionHtml = Some(Random.nextString(10)),
      image = Seq.empty,
      details = generateGooglePlayDetails,
      offer = Seq.empty,
      generateGooglePlayAggregateRating)

  def generateGooglePlayDetails =
    GooglePlayDetails(generateGooglePlayAppDetails)

  def generateGooglePlayAppDetails =
    GooglePlayAppDetails(
      appCategory = generateCategories(),
      numDownloads = Random.nextInt(10).toString,
      developerEmail = Some(Random.nextString(10)),
      developerName = Some(Random.nextString(10)),
      developerWebsite = Some(Random.nextString(10)),
      versionCode = Some(Random.nextInt(10)),
      versionString = Some(Random.nextString(10)),
      appType = Some(Random.nextString(10)),
      permission = generatePermissions())

  def generateCategories(num: Int = 10): Seq[String] = 1 to num map { n => s"Category $n" }

  def generateGooglePlayAggregateRating =
    GooglePlayAggregateRating(
      ratingsCount = Random.nextInt(10),
      commentCount = Some(Random.nextInt(10)),
      oneStarRatings = Random.nextInt(10),
      twoStarRatings = Random.nextInt(10),
      threeStarRatings = Random.nextInt(10),
      fourStarRatings = Random.nextInt(10),
      fiveStarRatings = Random.nextInt(10),
      starRating = Random.nextDouble())

  def generateGooglePlaySimplePackages =
    GooglePlaySimplePackages(
      Seq.empty,
      generateGooglePlaySimplePackageSeq())

  def generateGooglePlaySimplePackageSeq(num: Int = 10) =
    1 to num map { _ =>
      GooglePlaySimplePackage(
        packageName = Random.nextString(10),
        appType = Random.nextString(10),
        appCategory = Random.nextString(10),
        numDownloads = Random.nextInt(10).toString,
        starRating = Random.nextDouble(),
        ratingCount = Random.nextInt(10),
        commentCount = Random.nextInt(10))
    }

  def generateUserConfig =
    UserConfig(
      Random.nextString(10),
      Random.nextString(10),
      generateUserConfigPlusProfile,
      generateUserConfigDeviceSeq(),
      generateUserConfigGeoInfo,
      generateUserConfigStatusInfo)

  def generateUserConfigPlusProfile =
    UserConfigPlusProfile(
      Random.nextString(10),
      generateUserConfigProfileImage)

  def generateUserConfigProfileImage =
    UserConfigProfileImage(
      imageType = Random.nextInt(10),
      imageUrl = Random.nextString(10),
      secureUrl = Option(Random.nextString(10)))

  def generateUserConfigDeviceSeq(num: Int = 10) =
    1 to num map { _ =>
      UserConfigDevice(
        Random.nextString(10),
        Random.nextString(10),
        Seq.empty)
    }

  def generateUserConfigGeoInfo =
    UserConfigGeoInfo(
      homeMorning = None,
      homeNight = None,
      work = None,
      current = None)

  def generateUserConfigStatusInfo =
    UserConfigStatusInfo(
      products = Seq.empty,
      friendsReferred = Random.nextInt(10),
      themesShared = Random.nextInt(10),
      collectionsShared = Random.nextInt(10),
      customCollections = Random.nextInt(10),
      earlyAdopter = false,
      communityMember = false,
      joinedThrough = None,
      tester = false)

  def generateGooglePlayRecommendation(googlePlayApps: Seq[GooglePlayApp]) =
    GooglePlayRecommendation(
      googlePlayApps.size,
      googlePlayApps map (app => GooglePlayRecommendationItems(app.docid, app, None))
    )

  def generateSharedCollectionList(num: Int = 10) =
    SharedCollectionList(
      items = (1 to num map (_ => generateSharedCollection))
    )

  def generateSharedCollection: SharedCollection =
      SharedCollection(
        _id = Random.nextString(10),
        sharedCollectionId = Random.nextString(10),
        publishedOn = 0,
        description = Random.nextString(10),
        screenshots = Seq.empty,
        author = Random.nextString(10),
        tags = Seq.empty,
        name = Random.nextString(10),
        shareLink = Random.nextString(10),
        packages = Seq.empty,
        resolvedPackages = Seq.empty,
        occurrence = Seq.empty,
        lat = 0,
        lng = 0,
        alt = 0,
        views = 0,
        category = Random.nextString(10),
        icon = Random.nextString(10),
        community = true)

/*  def generateShareCollection: ShareCollection =
    ShareCollection(
      sharedCollectionId = if (Random.nextBoolean) Some(Random.nextString(10)) else None,
      description = Random.nextString(10),
      author = Random.nextString(10),
      name = Random.nextString(10),
      packages = List.fill(Random.nextInt(10) + 1)(Random.nextString(10)),
      category = Random.nextString(10),
      icon = Random.nextString(10),
      community = Random.nextBoolean
    )
 */
  val offset = 0

  val limit = 20

  val category = "COMMUNICATION"

  val name = "Name"

  val description = "Description"

  val author = "Author"

  val packages = List("Package1", "Package2")

  val icon = "Icon"

  val community = true

  val collectionType = "TOP"

  val user = generateUser

  val installation = generateInstallation

  val googlePlayPackages = generateGooglePlayPackages

  val googlePlaySimplePackages = generateGooglePlaySimplePackages

  val googlePlayApps = 1 to 10 map (_ => generateGooglePlayApp)

  val googlePlayRecommendation = generateGooglePlayRecommendation(googlePlayApps)

  val userConfig = generateUserConfig

  val sharedCollectionList = generateSharedCollectionList()

  val sharedCollection = generateSharedCollection
}

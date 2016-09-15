package com.fortysevendeg.ninecardslauncher.services.drive.impl

import java.io.{InputStream, OutputStreamWriter}

import com.fortysevendeg.ninecardslauncher.commons._
import com.fortysevendeg.ninecardslauncher.commons.services.TaskService
import com.fortysevendeg.ninecardslauncher.services.drive._
import com.fortysevendeg.ninecardslauncher.services.drive.impl.DriveServicesImpl._
import com.fortysevendeg.ninecardslauncher.services.drive.impl.Extensions._
import com.fortysevendeg.ninecardslauncher.services.drive.models.{DriveServiceFile, DriveServiceFileSummary}
import com.google.android.gms.common.api.{CommonStatusCodes, GoogleApiClient, PendingResult, Result}
import com.google.android.gms.drive._
import com.google.android.gms.drive.metadata.CustomPropertyKey
import com.google.android.gms.drive.query.{Filters, Query, SortOrder, SortableField}
import monix.eval.Task

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

class DriveServicesImpl(client: GoogleApiClient)
  extends DriveServices
    with Conversions {

  private[this] val fileNotFoundError = (driveId: String) => s"File with id $driveId doesn't exists"

  private[this] val queryUUID = (driveId: String) => new Query.Builder()
    .addFilter(Filters.eq(propertyUUID, driveId))
    .build()

  override def listFiles(maybeFileType: Option[String]) = {
    val sortOrder = new SortOrder.Builder()
      .addSortAscending(SortableField.MODIFIED_DATE)
      .build()
    val maybeQuery = maybeFileType map { fileType =>
      new Query.Builder()
        .addFilter(Filters.eq(propertyFileType, fileType))
        .setSortOrder(sortOrder)
        .build()
    }
    searchFiles(maybeQuery)(seq => seq)
  }

  override def fileExists(driveId: String) =
    searchFileByUUID(driveId)(_.nonEmpty)

  override def readFile(driveId: String) =
    fetchDriveFile(driveId) { metadata =>
      val summary = toGoogleDriveFileSummary(metadata)
      metadata.getDriveId.asDriveFile
        .open(client, DriveFile.MODE_READ_ONLY, javaNull)
        .withResult { driveContentsResult =>
          val contents = driveContentsResult.getDriveContents
          val stringContent = scala.io.Source.fromInputStream(contents.getInputStream).mkString
          contents.discard(client)
          Right(DriveServiceFile(summary, stringContent))
        }
    }

  override def createFile(title: String, content: String, deviceId: String, fileType: String, mimeType: String) =
    createNewFile(newUUID, title, deviceId, fileType, mimeType, _.write(content))

  override def createFile(title: String, content: InputStream, deviceId: String, fileType: String, mimeType: String) =
    createNewFile(newUUID, title, deviceId, fileType, mimeType,
      writer => Iterator
        .continually(content.read)
        .takeWhile(_ != -1)
        .foreach(writer.write))

  override def updateFile(driveId: String, title: String, content: String) =
    updateFile(driveId, title, _.write(content))

  override def updateFile(driveId: String, title: String, content: InputStream) =
    updateFile(
      driveId,
      title,
      writer => Iterator
        .continually(content.read)
        .takeWhile(_ != -1)
        .foreach(writer.write))

  override def deleteFile(driveId: String) =
    fetchDriveFile(driveId)(_.getDriveId.asDriveFile.delete(client).withResult(_ => Right(Unit)))

  private[this] def newUUID = com.gilt.timeuuid.TimeUuid().toString

  private[this] def searchFileByUUID[R](driveId: String)(f: (Option[DriveServiceFileSummary]) => R) = {
    val query = new Query.Builder()
      .addFilter(Filters.eq(propertyUUID, driveId))
      .build()
    searchFiles(Option(query))(seq => f(seq.headOption))
  }

  private[this] def searchFiles[R](query: Option[Query])(f: (Seq[DriveServiceFileSummary]) => R) = TaskService {
    Task {
      val request = query match {
        case Some(q) => appFolder.queryChildren(client, q)
        case _ => appFolder.listChildren(client)
      }
      request.withResult { r =>
        val buffer = r.getMetadataBuffer

        /*
         * TODO - Remove this block as part of ticket 525 (https://github.com/47deg/nine-cards-v2/issues/525)
         * This code fixes actual devices using Google Drive
         */
        val (validFiles, filesToFix) = buffer.iterator().toIterable.toList.partition { metadata =>
          Option(metadata.getCustomProperties.get(propertyUUID)).nonEmpty
        }
        val fixedFiles = filesToFix map { metadata =>
          val uuid = newUUID
          val changeSet = new MetadataChangeSet.Builder()
            .setCustomProperty(propertyUUID, uuid)
            .build()
          metadata.getDriveId
            .asDriveResource()
            .updateMetadata(client, changeSet)
            .await()
          toGoogleDriveFileSummary(uuid, metadata).copy(uuid = uuid)
        }
        // End fix

        val response = f((validFiles map toGoogleDriveFileSummary) ++ fixedFiles)
        buffer.release()
        Right(response)
      }
    }
  }

  private[this] def appFolder = Drive.DriveApi.getAppFolder(client)

  private[this] def createNewFile(
    uuid: String,
    title: String,
    deviceId: String,
    fileType: String,
    mimeType: String,
    f: (OutputStreamWriter) => Unit) = TaskService {
    Task {
      Drive.DriveApi
        .newDriveContents(client)
        .withResult { r =>
          val changeSet = new MetadataChangeSet.Builder()
            .setTitle(title)
            .setMimeType(mimeType)
            .setCustomProperty(propertyUUID, uuid)
            .setCustomProperty(propertyDeviceId, deviceId)
            .setCustomProperty(propertyFileType, fileType)
            .build()

          val driveContents = r.getDriveContents
          val writer = new OutputStreamWriter(driveContents.getOutputStream)
          f(writer)
          writer.close()

          appFolder
            .createFile(client, changeSet, driveContents)
            .withResult { nr =>
              val now = new java.util.Date
              Right(DriveServiceFileSummary(
                uuid = uuid,
                deviceId = Some(deviceId),
                title = title,
                createdDate = now,
                modifiedDate = now))
            }
        }

    }
  }

  private[this] def updateFile(driveId: String, title: String, f: (OutputStreamWriter) => Unit) =
    fetchDriveFile(driveId) { metadata =>
      val changeSet = new MetadataChangeSet.Builder()
        .setTitle(title)
        .build()
      metadata.getDriveId.asDriveResource()
        .updateMetadata(client, changeSet)
        .withResult { metadataResult =>
          val newMetadata = metadataResult.getMetadata
          val summary = toGoogleDriveFileSummary(newMetadata)
          newMetadata.getDriveId.asDriveFile
            .open(client, DriveFile.MODE_WRITE_ONLY, javaNull)
            .withResult { driveContentsResult =>
              val contents = driveContentsResult.getDriveContents
              val writer = new OutputStreamWriter(contents.getOutputStream)
              f(writer)
              writer.close()
              contents.commit(client, javaNull).withResult(_ => Right(summary))
            }
        }
    }

  private[this] def fetchDriveFile[R](driveId: String)(f: (Metadata) => Either[DriveServicesException, R]) =
    TaskService {
      Task {
        appFolder
          .queryChildren(client, queryUUID(driveId))
          .withResult { r =>
            val buffer = r.getMetadataBuffer
            val response = buffer.iterator().toIterable.headOption match {
              case Some(metaData) => f(metaData)
              case None => Left(DriveServicesException(fileNotFoundError(driveId)))
            }
            buffer.release()
            response
          }
      }
    }

}

object DriveServicesImpl {

  private[this] val uuid = "FILE_UUID"

  private[this] val customFileType = "FILE_TYPE"

  private[this] val customDeviceId = "FILE_ID"

  def propertyUUID = new CustomPropertyKey(uuid, CustomPropertyKey.PRIVATE)

  def propertyFileType = new CustomPropertyKey(customFileType, CustomPropertyKey.PRIVATE)

  def propertyDeviceId = new CustomPropertyKey(customDeviceId, CustomPropertyKey.PRIVATE)

}

object Extensions {

  implicit class PendingResultOps[T <: Result](pendingResult: PendingResult[T]) {

    def withResult[R](f: (T) => Either[DriveServicesException, R]): Either[DriveServicesException, R] =
      withResult(f, None)

    def withResult[R](
      f: (T) => Either[DriveServicesException, R],
      validCodesAndDefault: Option[(Seq[Int], R)]): Either[DriveServicesException, R] =
      (fetchResult, validCodesAndDefault) match {
        case (Some(result), _) if result.getStatus.isSuccess =>
          Try(f(result)) match {
            case Success(r) => r
            case Failure(e) => Left(DriveServicesException(e.getMessage, cause = Some(e)))
          }
        case (Some(result), Some((validCodes, defaultValue))) if validCodes contains result.getStatus.getStatusCode =>
          Right(defaultValue)
        case (Some(result), _) =>
          Left(DriveServicesException(
            googleDriveError = statusCodeToError(result.getStatus.getStatusCode),
            message = result.getStatus.getStatusMessage))
        case _ =>
          Left(DriveServicesException(
            message = "Received a null reference in pending result",
            cause = Option(new NullPointerException())))
      }

    private[this] def fetchResult: Option[T] = Option(pendingResult) map (_.await())

    private[this] def statusCodeToError(statusCode: Int) = statusCode match {
      case CommonStatusCodes.SIGN_IN_REQUIRED => Option(DriveSigInRequired)
      case DriveStatusCodes.DRIVE_RATE_LIMIT_EXCEEDED => Option(DriveRateLimitExceeded)
      case DriveStatusCodes.DRIVE_RESOURCE_NOT_AVAILABLE => Option(DriveResourceNotAvailable)
      case _ => None
    }

  }

}

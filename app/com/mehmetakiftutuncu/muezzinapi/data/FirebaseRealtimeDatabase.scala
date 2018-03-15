package com.mehmetakiftutuncu.muezzinapi.data

import java.io.FileInputStream

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.database.{DataSnapshot, DatabaseReference, FirebaseDatabase}
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.google.inject.{ImplementedBy, Singleton}
import com.mehmetakiftutuncu.muezzinapi.utilities.{AbstractConf, Log, Logging}
import javax.inject.Inject
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

@ImplementedBy(classOf[FirebaseRealtimeDatabase])
trait AbstractFirebaseRealtimeDatabase {
  val root: DatabaseReference
}

@Singleton
class FirebaseRealtimeDatabase @Inject()(ApplicationLifecycle: ApplicationLifecycle,
                                         Conf: AbstractConf) extends AbstractFirebaseRealtimeDatabase with Logging {
  private val credentialsFile: String = Conf.getString("muezzinApi.firebaseRealtimeDatabase.credentialsFile", "")
  private val databaseUrl: String     = Conf.getString("muezzinApi.firebaseRealtimeDatabase.databaseUrl", "")

  private val firebaseOptions: FirebaseOptions = new FirebaseOptions.Builder()
    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsFile)))
    .setDatabaseUrl(databaseUrl)
    .build()

  Log.warn(s"""Connecting to Firebase Realtime Database at "$databaseUrl" with credentials file "$credentialsFile"...""")

  private val firebaseApp: FirebaseApp           = FirebaseApp.initializeApp(firebaseOptions)
  private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance(firebaseApp)

  override val root: DatabaseReference = firebaseDatabase.getReference

  ApplicationLifecycle.addStopHook {
    () =>
      Log.warn("Shutting down Firebase Realtime Database connection...")

      Future.successful(firebaseDatabase.goOffline())
  }
}

object FirebaseRealtimeDatabase {
  implicit class DatabaseReferenceExtensions(databaseReference: DatabaseReference) {
    def / (path: Int): DatabaseReference    = databaseReference.child(path.toString)
    def / (path: String): DatabaseReference = databaseReference.child(path)

    def cacheKey: String = databaseReference.getPath.toString
  }

  implicit class DataSnapshotExtensions(dataSnapshot: DataSnapshot) {
    def / (path: String): DataSnapshot = dataSnapshot.child(path)
  }
}

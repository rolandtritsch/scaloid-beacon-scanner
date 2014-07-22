package org.tritsch.scaloid.beacon.build

import sbt._
import sbt.Keys._
import android.Keys._

object Build extends android.AutoBuild {
  lazy val mySettings = super.settings ++ android.Plugin.androidBuild ++ Seq(
    name := "ScaloidBeaconScannerApp",
    version := "0.1",
    scalaVersion := "2.10.4",

    platformTarget in Android := "android-19",
    proguardCache in Android ++= Seq(
      ProguardCache("org.scaloid") % "org.scaloid"
    ),
    proguardOptions in Android ++= Seq(
      "-dontobfuscate",
      "-dontoptimize"
    ),
    libraryDependencies ++= Seq(
      "org.scaloid" %% "scaloid" % "3.3-8"
    ),
    scalacOptions in Compile ++= Seq(
      "-feature"
    ),

    run <<= run in Android,
    install <<= install in Android
  )

  lazy val root = Project(
    id = "ScaloidBeaconScannerId",
    base = file(".")
  ).settings(
    mySettings:_*
  )
}

import android.Keys._

android.Plugin.androidBuild

name := "scaloid-beacon-scanner"

scalaVersion := "2.10.3"

// see project.properties
//platformTarget in Android := "android-19"

proguardCache in Android ++= Seq(
  ProguardCache("org.scaloid") % "org.scaloid"
)

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize")

libraryDependencies += "org.scaloid" %% "scaloid" % "3.2-8"

scalacOptions in Compile ++= Seq("-feature")

run <<= run in Android

install <<= install in Android

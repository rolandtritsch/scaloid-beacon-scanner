import org.ensime.sbt.Plugin.Settings._

import org.ensime.sbt.util.SExp._

ensimeConfig := sexp(
  key(":compile-deps"), sexp(
    "/Users/roland/.ivy2/local/org.scaloid/scaloid_2.10/3.3-8-SNAPSHOT/jars/scaloid_2.10.jar",
    "/opt/android-sdk-macosx-r22.3/platforms/android-19/android.jar",
    "/opt/android-sdk-macosx-r22.3/extras/android/support/v4/android-support-v4.jar"
  ),
  key(":test-deps"), sexp(
  ),
  key(":source-roots"), sexp(
  )
)

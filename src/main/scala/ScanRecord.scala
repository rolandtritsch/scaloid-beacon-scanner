package org.tritsch.scaloid.beacon

/*
ScanRecord >(62):
type code length(1) - 02(02)
type code(s)(2 - determined by type code length) - 01:06
manufacturer specific data length(1) - 1A(26)
delimiter(1) - FF
manufacturer id(2) - 4C:00(apple)
02:15:23:5D:62:82:82:0C:43:CE:97:63:8E:42:70:64:1C:EC:00:65:01:F5:C6:11:07:77:63:C4:DC:FA:00:00:A0:00:40:F6:24:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00< ...
*/

case class PduRecord(r: Array[Byte]) {
  require(r.length == 38)

  val pduType: Int = r(0)
  val pduLength: Int = r(1)
  val beaconMacAddress: String = f"${r(2)}%02X:${r(3)}%02X:${r(4)}%02X:${r(5)}%02X:${r(6)}%02X:${r(7)}%02X"
  val length1: Int = r(8)
  val flagsFieldIdentifier: Int = r(9)
  val flagsField: Int = r(10)
  val length2: Int = r(11)
  val manufacturer: String = f"${r(12)}%02X"
  val appleId: String =  f"${r(13)}%02X:${r(14)}%02X"
  val twoFixedByte: String =  f"${r(15)}%02X:${r(16)}%02X"
  val beaconUUID: String = f"${r(17)}%02X${r(18)}%02X${r(19)}%02X${r(20)}%02X-${r(21)}%02X${r(22)}%02X-${r(23)}%02X${r(24)}%02X-${r(25)}%02X${r(26)}%02X-${r(27)}%02X${r(28)}%02X-${r(29)}%02X${r(30)}%02X${r(31)}%02X${r(32)}%02X"
  val majorId: Int = r(33) << 8 + r(34)
  val minorId: Int = r(35) << 8 + r(36)
  val remoteRssi: Int = r(37)

  assume(pduLength == 36, "Wrong pduLength")
  assume(length1 == 2, "Wrong length1")
  assume(length2 == 26, "Wrong length2")
  assume(appleId == "4C:00", "Wrong appleId")
}

object ScanRecord {
  def dump(sr: Array[Byte]): String = {
    s"(${sr.length}): " + sr.foldLeft(""){(r,c) => r + f":${c}%02X"}.drop(1)
  }
}

case class ScanRecord(sr: Array[Byte]) {
  require(sr.length == 46)

  val preample: String = f"${sr(0)}%02X"
  val accessAddress: String = f"${sr(1)}%02X:${sr(2)}%02X:${sr(3)}%02X:${sr(4)}%02X"
  val crc: String = f"${sr(5)}%02X:${sr(6)}%02X:${sr(7)}%02X"
  val pdu = PduRecord(sr.drop(8))

  assume(preample == "AA")
}

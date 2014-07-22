package org.tritsch.scaloid.beacon

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView

import org.scaloid.common._

import scala.collection.mutable

class BeaconScanner extends SActivity {
  override implicit val loggerTag = LoggerTag("BeaconScanner")

  private var bluetoothAdapter: BluetoothAdapter = null
  private val beaconMap = new mutable.HashMap[String, Int]

  // views ...
  private def startView: View = new SVerticalLayout {
    debug(s"switching to startview ...")
    this += new SButton(R.string.start_scan).onClick(
      contentView = listView
    )
  } padding 20.dip

  private def listView: View = new SVerticalLayout {
    debug(s"switching to listview ...")
    this += new SButton(R.string.stop_scan).onClick(
      contentView = startView
    )

    info(s"scanning for BLE devices ...")
    bluetoothAdapter.startLeScan(listenForDevices)
    Thread.sleep(5000)
    bluetoothAdapter.stopLeScan(listenForDevices)

    val beacons = SArrayAdapter(
      beaconMap.keys.map(k => {
        s"${k} -> [${beaconMap(k)}]"
      }).toArray
    )

    val lv = new SListView()
    lv.setAdapter(beacons)
    lv.onItemClick((parent: AdapterView[_], v: View, position: Int, row: Long) => {
      toast(s">${position}/${row}<")
    })

    this += lv
  } padding 20.dip

  // overrides ...
  override def onCreate(b: Bundle): Unit = {
    super.onCreate(b)
    debug("Enter - onCreate")

    val manager = getSystemService(Context.BLUETOOTH_SERVICE).asInstanceOf[BluetoothManager]
    bluetoothAdapter = manager.getAdapter

    contentView = startView

    debug("Leave - onCreate")
  }

  // the BLE stuff ...
  val listenForDevices = new BluetoothAdapter.LeScanCallback {
    override def onLeScan(device: BluetoothDevice, rssi: Int, sr: Array[Byte]): Unit = {
      debug("Enter - onLeScan")
      info(s"Found device >${device.getAddress}/${device.getName}< @ >${rssi}< ...")
      verbose(s"With ScanRecord >${ScanRecord.dump(sr)}< ...")

      beaconMap += (device.getAddress -> rssi)

      debug("Leave - onLeScan")
    }
  }
}

package org.tritsch.scaloid.beacon

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.AdapterView
import android.widget.Toast

import org.scaloid.common._

import scala.collection.mutable

object BeaconScanner {
  implicit val loggerTag = LoggerTag(classOf[BeaconScanner].getName)
  private val MSG_CONNECT_AND_DISPLAY = 100
}

class BeaconScanner extends SActivity {
  private var bluetoothAdapter: BluetoothAdapter = null
  private var beacons: SArrayAdapter[String] = null

  // views ...
  private def startView: View = new SVerticalLayout {
    this += new SButton(R.string.start_scan).onClick(
      contentView = listView
    )
  } padding 20.dip

  private def listView: View = new SVerticalLayout {
    this += new SButton(R.string.stop_scan).onClick(
      contentView = startView
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

    beacons = SArrayAdapter(Array("Searching ..."))

    contentView = startView

    debug("Leave - onCreate")
  }

  override def onResume: Unit = {
    super.onResume
    debug("Enter - onResume")
    info("Make sure BLE is available and enabled ...")

    if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
      startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
      finish
    } else {
      if(!getPackageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
        toast("!!! No BLE Support !!!")
        finish
      } else {
        resumeScan
      }
    }
    debug("Leave - onResume")
  }

  override def onPause: Unit = {
    super.onPause
    debug("Enter - onPause")
    stopScan
    debug("Leave - onPause")
  }

  // helpers and handlers ...
  private val handler = new Handler

  private val resumeRunnable = new Runnable {
    override def run: Unit = {
      resumeScan
    }
  }

  private val suspendRunnable = new Runnable {
    override def run: Unit = {
      suspendScan
    }
  }

  private def stopScan: Unit = {
    debug("Enter - stopScan")
    handler.removeCallbacks(resumeRunnable)
    handler.removeCallbacks(suspendRunnable)
    bluetoothAdapter.stopLeScan(listenForDevices)
    debug("Leave - stopScan")
  }

  private def resumeScan: Unit = {
    debug("Enter - resumeScan")
    bluetoothAdapter.startLeScan(listenForDevices)
    setProgressBarIndeterminateVisibility(true)
    handler.postDelayed(suspendRunnable, 1000)
    debug("Leave - resumeScan")
  }

  private def suspendScan: Unit = {
    debug("Enter - suspendScan")
    bluetoothAdapter.stopLeScan(listenForDevices)
    setProgressBarIndeterminateVisibility(false)
    handler.sendEmptyMessage(BeaconScanner.MSG_CONNECT_AND_DISPLAY)
    handler.postDelayed(resumeRunnable, 1000)
    debug("Leave - suspendScan")
  }

  // the BLE stuff ...
  val listenForDevices = new BluetoothAdapter.LeScanCallback {
    override def onLeScan(device: BluetoothDevice, rssi: Int, sr: Array[Byte]): Unit = {
      debug("Enter - onLeScan")
      info(s"Found device >${device.getAddress}/${device.getName}< @ >${rssi}< ...")
      verbose(s"With ScanRecord >${ScanRecord.dump(sr)}< ...")

      beacons.add(s"${device.getAddress} -> ${rssi}")
      debug("Leave - onLeScan")
    }
  }
}

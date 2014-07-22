package org.tritsch.scaloid.beacon

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AdapterView

import org.scaloid.common._

class BeaconScanner extends SActivity {
  override implicit val loggerTag = LoggerTag("BeaconScanner")
  private val MSG_CONNECT_AND_DISPLAY = 100

  private var bluetoothAdapter: BluetoothAdapter = null
  private var lv: SListView = null

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

    val beacons = SArrayAdapter(Array("Searching ..."))
    lv = new SListView()
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

  override def onResume: Unit = {
    super.onResume
    debug("Enter - onResume")
    resumeScan
    debug("Leave - onResume")
  }

  override def onPause: Unit = {
    super.onPause
    debug("Enter - onPause")
    stopScan
    debug("Leave - onPause")
  }

  // helpers and handlers ...
  private val handler = new Handler {
    override def handleMessage(msg: Message): Unit = {
      msg.what match {
        case MSG_CONNECT_AND_DISPLAY => {
          debug(s"Enter - MSG_CONNECT_AND_DISPLAY - ${lv}")

          if(lv != null) {
            val beacons = lv.adapter.asInstanceOf[ArrayAdapter[String]]
            beacons.notifyDataSetChanged
          }

          debug(s"Leave - MSG_CONNECT_AND_DISPLAY")
        }
      }
    }
  }

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

    handler.sendEmptyMessage(MSG_CONNECT_AND_DISPLAY)
    handler.postDelayed(resumeRunnable, 1000)

    debug("Leave - suspendScan")
  }

  // the BLE stuff ...
  val listenForDevices = new BluetoothAdapter.LeScanCallback {
    override def onLeScan(device: BluetoothDevice, rssi: Int, sr: Array[Byte]): Unit = {
      debug("Enter - onLeScan")
      info(s"Found device >${device.getAddress}/${device.getName}< @ >${rssi}< ...")
      verbose(s"With ScanRecord >${ScanRecord.dump(sr)}< ...")

      val beacons = lv.adapter.asInstanceOf[ArrayAdapter[String]]
      beacons.add(s"${device.getAddress} -> ${rssi}")

      debug("Leave - onLeScan")
    }
  }
}

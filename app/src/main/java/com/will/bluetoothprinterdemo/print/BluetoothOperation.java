package com.will.bluetoothprinterdemo.print;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import androidx.core.view.MotionEventCompat;
import android.util.Log;
import java.io.OutputStream;

public class BluetoothOperation implements IPrinterOpertion {
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_NONE = 0;
    private static final String TAG = "BluetoothOpertion";
    private static boolean isConnected = false;
    private static OutputStream outputStream = null;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver boundDeviceReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(intent.getAction())) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (BluetoothOperation.this.mDevice.equals(device)) {
                    switch (device.getBondState()) {
                        case 10:
                            if (BluetoothOperation.this.rePair) {
                                BluetoothOperation.this.rePair = false;
                                Log.i(BluetoothOperation.TAG, "removeBond success, wait create bound.");
                                BluetoothOperation.this.PairOrRePairDevice(false, device);
                                return;
                            } else if (BluetoothOperation.this.hasRegBoundReceiver) {
                                BluetoothOperation.this.mContext.unregisterReceiver(BluetoothOperation.this.boundDeviceReceiver);
                                BluetoothOperation.this.hasRegBoundReceiver = false;
                                BluetoothOperation.this.mHandler.obtainMessage(PrinterConstants.Connect.FAILED).sendToTarget();
                                Log.i(BluetoothOperation.TAG, "bound cancel");
                                return;
                            } else {
                                return;
                            }
                        case 11:
                            Log.i(BluetoothOperation.TAG, "bounding......");
                            return;
                        case MotionEventCompat.AXIS_RX /*12*/:
                            Log.i(BluetoothOperation.TAG, "bound success");
                            if (BluetoothOperation.this.hasRegBoundReceiver) {
                                BluetoothOperation.this.mContext.unregisterReceiver(BluetoothOperation.this.boundDeviceReceiver);
                                BluetoothOperation.this.hasRegBoundReceiver = false;
                            }
                            BluetoothOperation.this.openPrinter();
                            return;
                        default:
                            return;
                    }
                }
            }
        }
    };
    private String deviceAddress;
    private IntentFilter filter;
    private boolean hasRegBoundReceiver;
    private boolean hasRegDisconnectReceiver;
    private Context mContext;
    private BluetoothDevice mDevice;
    private Handler mHandler;
    private PrinterInstance mPrinter;
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
            Log.i(BluetoothOperation.TAG, "receiver is: " + action);
            if (action.equals("android.bluetooth.device.action.ACL_DISCONNECTED") && device != null && BluetoothOperation.this.mPrinter != null && BluetoothOperation.this.mPrinter.isConnected() && device.equals(BluetoothOperation.this.mDevice)) {
                BluetoothOperation.this.close();
            }
        }
    };
    private boolean rePair;

    public BluetoothOperation(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.hasRegDisconnectReceiver = false;
        this.filter = new IntentFilter();
        this.filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
    }

    public void open(Intent data) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        this.deviceAddress = data.getExtras().getString(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
        this.mDevice = adapter.getRemoteDevice(this.deviceAddress);
        if (this.mDevice.getBondState() == 10) {
            Log.i(TAG, "device.getBondState() is BluetoothDevice.BOND_NONE");
            PairOrRePairDevice(false, this.mDevice);
        } else if (this.mDevice.getBondState() == 12) {
            this.rePair = data.getExtras().getBoolean(BluetoothDeviceList.EXTRA_RE_PAIR);
            if (this.rePair) {
                PairOrRePairDevice(true, this.mDevice);
            } else {
                openPrinter();
            }
        }
    }

    private void openPrinter() {
        this.mPrinter = new PrinterInstance(this.mContext, this.mDevice, this.mHandler);
        this.mPrinter.openConnection();
    }

    private boolean PairOrRePairDevice(boolean re_pair, BluetoothDevice device) {
        try {
            if (!this.hasRegBoundReceiver) {
                this.mDevice = device;
                this.mContext.registerReceiver(this.boundDeviceReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
                this.hasRegBoundReceiver = true;
            }
            boolean success;
            if (re_pair) {
                success = ((Boolean) BluetoothDevice.class.getMethod("removeBond", new Class[0]).invoke(device, new Object[0])).booleanValue();
                Log.i(TAG, "removeBond is success? : " + success);
                return success;
            }
            success = ((Boolean) BluetoothDevice.class.getMethod("createBond", new Class[0]).invoke(device, new Object[0])).booleanValue();
            Log.i(TAG, "createBond is success? : " + success);
            return success;
        } catch (Exception e) {
            Log.i(TAG, "removeBond or createBond failed.");
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        if (this.mPrinter != null) {
            this.mPrinter.closeConnection();
            this.mPrinter = null;
        }
        if (this.hasRegDisconnectReceiver) {
            this.mContext.unregisterReceiver(this.myReceiver);
            this.hasRegDisconnectReceiver = false;
        }
    }

    public PrinterInstance getPrinter() {
        if (!(this.mPrinter == null || !this.mPrinter.isConnected() || this.hasRegDisconnectReceiver)) {
            this.mContext.registerReceiver(this.myReceiver, this.filter);
            this.hasRegDisconnectReceiver = true;
        }
        return this.mPrinter;
    }

    public void chooseDevice() {
        if (this.adapter.isEnabled()) {
            ((Activity) this.mContext).startActivityForResult(new Intent(this.mContext, BluetoothDeviceList.class), 1);
            return;
        }
        ((Activity) this.mContext).startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 2);
    }
}
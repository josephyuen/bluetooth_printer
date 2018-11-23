package com.will.bluetoothprinterdemo.print;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.Handler;
import androidx.core.view.MotionEventCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class BluetoothPort implements IPrinterPort {
    private static final String TAG = "BluetoothPort";
    private final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BroadcastReceiver boundDeviceReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.device.action.BOND_STATE_CHANGED".equals(intent.getAction())) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (BluetoothPort.this.mDevice.equals(device)) {
                    switch (device.getBondState()) {
                        case 10:
                            BluetoothPort.this.mContext.unregisterReceiver(BluetoothPort.this.boundDeviceReceiver);
                            BluetoothPort.this.setState(PrinterConstants.Connect.FAILED);
                            Utils.Log(BluetoothPort.TAG, "bound cancel");
                            return;
                        case 11:
                            Utils.Log(BluetoothPort.TAG, "bounding......");
                            return;
                        case MotionEventCompat.AXIS_RX /*12*/:
                            Utils.Log(BluetoothPort.TAG, "bound success");
                            BluetoothPort.this.mContext.unregisterReceiver(BluetoothPort.this.boundDeviceReceiver);
                            BluetoothPort.this.PairOrConnect(false);
                            return;
                        default:
                            return;
                    }
                }
            }
        }
    };
    private InputStream inputStream;
    private BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private Context mContext;
    private BluetoothDevice mDevice;
    private Handler mHandler;
    private BluetoothSocket mSocket;
    private int mState;
    private OutputStream outputStream;
    private int readLen;

    private class ConnectThread extends Thread {
        private ConnectThread() {
        }

        /* synthetic */ ConnectThread(BluetoothPort bluetoothPort, ConnectThread connectThread) {
            this();
        }

        public void run() {
            boolean hasError = false;
            BluetoothPort.this.mAdapter.cancelDiscovery();
            try {
                BluetoothPort.this.mSocket = BluetoothPort.this.mDevice.createRfcommSocketToServiceRecord(BluetoothPort.this.PRINTER_UUID);
                BluetoothPort.this.mSocket.connect();
            } catch (IOException e) {
                Utils.Log(BluetoothPort.TAG, "ConnectThread failed. retry.");
                e.printStackTrace();
                hasError = BluetoothPort.this.ReTryConnect();
            }
            synchronized (this) {
                BluetoothPort.this.mConnectThread = null;
            }
            if (!hasError) {
                try {
                    BluetoothPort.this.inputStream = BluetoothPort.this.mSocket.getInputStream();
                    BluetoothPort.this.outputStream = BluetoothPort.this.mSocket.getOutputStream();
                } catch (IOException e2) {
                    hasError = true;
                    Utils.Log(BluetoothPort.TAG, "Get Stream failed");
                    e2.printStackTrace();
                }
            }
            if (hasError) {
                BluetoothPort.this.setState(PrinterConstants.Connect.FAILED);
                BluetoothPort.this.close();
                return;
            }
            BluetoothPort.this.setState(PrinterConstants.Connect.SUCCESS);
        }
    }

    public BluetoothPort(BluetoothDevice device, Handler handler) {
        this.mHandler = handler;
        this.mDevice = device;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = PrinterConstants.Connect.CLOSED;
    }

    public BluetoothPort(String address, Handler handler) {
        this.mHandler = handler;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mDevice = this.mAdapter.getRemoteDevice(address);
        this.mState = PrinterConstants.Connect.CLOSED;
    }

    public void open() {
        Utils.Log(TAG, "connect to: " + this.mDevice.getName());
        if (this.mState != PrinterConstants.Connect.CLOSED) {
            close();
        }
        if (this.mDevice.getBondState() == 10) {
            Log.i(TAG, "device.getBondState() is BluetoothDevice.BOND_NONE");
            PairOrConnect(true);
        } else if (this.mDevice.getBondState() == 12) {
            PairOrConnect(false);
        }
    }

    private void PairOrConnect(boolean pair) {
        if (pair) {
            this.mContext.registerReceiver(this.boundDeviceReceiver, new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED"));
            boolean success = false;
            try {
                success = ((Boolean) BluetoothPort.class.getMethod("createBond", new Class[0]).invoke(this.mDevice, new Object[0])).booleanValue();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            } catch (InvocationTargetException e3) {
                e3.printStackTrace();
            } catch (NoSuchMethodException e4) {
                e4.printStackTrace();
            }
            Log.i(TAG, "createBond is success? : " + success);
            return;
        }
        this.mConnectThread = new ConnectThread();
        this.mConnectThread.start();
    }

    @TargetApi(10)
    private boolean ReTryConnect() {
        Utils.Log(TAG, "android SDK version is:" + VERSION.SDK_INT);
        try {
            if (VERSION.SDK_INT >= 10) {
                this.mSocket = this.mDevice.createInsecureRfcommSocketToServiceRecord(this.PRINTER_UUID);
            } else {
                this.mSocket = (BluetoothSocket) this.mDevice.getClass().getMethod("createRfcommSocket", new Class[]{Integer.TYPE}).invoke(this.mDevice, new Object[]{Integer.valueOf(1)});
            }
            this.mSocket.connect();
            return false;
        } catch (Exception e) {
            Utils.Log(TAG, "connect failed:");
            e.printStackTrace();
            return true;
        }
    }

    public void close() {
        Utils.Log(TAG, "close()");
        try {
            if (this.mSocket != null) {
                this.mSocket.close();
            }
        } catch (IOException e) {
            Utils.Log(TAG, "close socket failed");
            e.printStackTrace();
        }
        this.mConnectThread = null;
        this.mDevice = null;
        this.mSocket = null;
        if (this.mState != PrinterConstants.Connect.FAILED) {
            setState(PrinterConstants.Connect.CLOSED);
        }
    }

    public int write(byte[] data) {
        try {
            if (this.outputStream == null) {
                return -1;
            }
            this.outputStream.write(data);
            this.outputStream.flush();
            return 0;
        } catch (IOException e) {
            Utils.Log(TAG, "write error.");
            e.printStackTrace();
            return -1;
        }
    }

    public byte[] read() {
        byte[] readBuff = null;
        try {
            if (this.inputStream != null) {
                int available = this.inputStream.available();
                this.readLen = available;
                if (available > 0) {
                    readBuff = new byte[this.readLen];
                    this.inputStream.read(readBuff);
                }
            }
        } catch (IOException e) {
            Utils.Log(TAG, "read error");
            e.printStackTrace();
        }
        Log.w(TAG, "read length:" + this.readLen);
        return readBuff;
    }

    public synchronized byte[] read(int timeout) throws IOException {
        byte[] receiveBytes;
        receiveBytes = null;
        while (true) {
            try {
                int available = this.inputStream.available();
                this.readLen = available;
                if (available > 0) {
                    break;
                }
                timeout -= 50;
                if (timeout <= 0) {
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                Utils.Log(TAG, "read error1");
                e.printStackTrace();
            }
        }
        if (this.readLen > 0) {
            receiveBytes = new byte[this.readLen];
            this.inputStream.read(receiveBytes);
        }
        return receiveBytes;
    }

    private synchronized void setState(int state) {
        Utils.Log(TAG, "setState() " + this.mState + " -> " + state);
        if (this.mState != state) {
            this.mState = state;
            if (this.mHandler != null) {
                this.mHandler.obtainMessage(this.mState).sendToTarget();
            }
        }
    }

    public int getState() {
        return this.mState;
    }
}
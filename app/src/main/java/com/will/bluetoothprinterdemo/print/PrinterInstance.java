package com.will.bluetoothprinterdemo.print;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

public class PrinterInstance implements Serializable {
    public static boolean DEBUG = true;
    private static String TAG = "PrinterInstance";
    private static final long serialVersionUID = 1;
    private String charsetName = "gbk";
    private IPrinterPort myPrinter;

    public PrinterInstance(Context context, BluetoothDevice bluetoothDevice, Handler handler) {
        this.myPrinter = new BluetoothPort(bluetoothDevice, handler);
    }

    public String getEncoding() {
        return this.charsetName;
    }

    public void setEncoding(String charsetName) {
        this.charsetName = charsetName;
    }


    public boolean isConnected() {
        return this.myPrinter.getState() == PrinterConstants.Connect.SUCCESS;
    }

    public void openConnection() {
        this.myPrinter.open();
    }

    public void closeConnection() {
        this.myPrinter.close();
    }

    public int printText(String content) {
        byte[] data = null;
        try {
            if (this.charsetName != "") {
                data = content.getBytes(this.charsetName);
            } else {
                data = content.getBytes();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sendByteData(data);
    }

    public int sendByteData(byte[] data) {
        if (data == null) {
            return -1;
        }
        Utils.Log(TAG, "sendByteData length is: " + data.length);
        return this.myPrinter.write(data);
    }

}
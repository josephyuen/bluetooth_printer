package com.will.bluetoothprinterdemo.print;

import android.content.Intent;

public interface IPrinterOpertion {
    void chooseDevice();

    void close();

    PrinterInstance getPrinter();

    void open(Intent intent);
}
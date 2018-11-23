package com.will.bluetoothprinterdemo.print;

public interface IPrinterPort {
    void close();

    int getState();

    void open();

    byte[] read();

    int write(byte[] bArr);
}
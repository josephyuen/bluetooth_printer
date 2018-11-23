package com.will.bluetoothprinterdemo.utils;

import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Toast;

import com.github.promeg.pinyinhelper.Pinyin;
import com.will.bluetoothprinterdemo.R;
import com.will.bluetoothprinterdemo.print.IPrinterOpertion;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

/**
 * 蓝牙打印工具类
 */
public class PrintUtil {

    private static final String TAG = "PrintUtil";

    private OutputStream mOutputStream = null;


    /**
     * 初始化Pos实例
     *
     * @throws IOException
     */
    public PrintUtil(OutputStream outputStream) throws IOException {
        mOutputStream = outputStream;
    }

    public void printRawBytes(byte[] bytes) throws IOException {
        mOutputStream.write(bytes);
        mOutputStream.flush();
        Log.i(TAG,"打印输出完成！！！！！！！！!!!");
    }


    public byte[] getGbk(String stText) throws IOException {
        return stText.getBytes("GBK");
    }


    public static void printTest(BluetoothSocket bluetoothSocket) {


        String send_data =  "! 0 200 200 1000 1\n" +
                "PAGE-WIDTH 576\n" +
                "BG 0 0 0\n" +
                "TEXT 4 3 96 96 中方02-03铜湾\n" +
                "TEXT 4 0 80 200 湖南-怀化1812\n" +
                "ML 47\n" +
                "TEXT 4 0 96 256\n" +
                "萨尔单   134651210212湖南省长沙市\n" +
                "ENDML\n" +
                "ML 32\n" +
                "TEXT 2 0 96 360\n" +
                "萨尔单   134651210212湖南省长沙市\n" +
                "ENDML\n" +
                "BARCODE 128 2 1 70 80 456 1234567890123\n" +
                "TEXT 2 0 180 530 1234567890123\n" +
                "BARCODE 128 2 1 70 80 712 1234567890123\n" +
                "TEXT 2 0 180 788 1234567890123\n" +
                "BARCODE QR 474 586 M 2 U 4\n" +
                "MA,1234567890123\n" +
                "ENDQR\n" +
                "BARCODE QR 474 824 M 2 U 4\n" +
                "MA,1234567890123\n" +
                "ENDQR\n" +
                "ML 24\n" +
                "TEXT 55 0 150 576\n" +
                "备注内容备注内容备注内容备注内\n" +
                "备注内容\n" +
                "备注内容备注内容备注内\n" +
                "备注内容备注内容\n" +
                "容容备注内容\n" +
                "ENDML\n" +
                "TEXT 55 0 20 576 2018/08/13\n" +
                "TEXT 2 0 20 600 18:54:00\n" +
                "TEXT 2 0 20 632 1/1\n" +
                "TEXT 55 0 20 672 打印时间\n" +
                "ML 32\n" +
                "TEXT 2 0 96 824\n" +
                "萨尔单   134651210212湖南省长沙市xxxxxxxxxxxxxx\n" +
                "ENDML\n" +
                "ML 32\n" +
                "TEXT 2 0 96 890\n" +
                "萨尔单   134651210212湖南省长沙市xxxxxxxxxxxxxx\n" +
                "ENDML\n" +
                "FORM\n" +
                "PRINT\n";

             try {

                PrintUtil pUtil = new PrintUtil(bluetoothSocket.getOutputStream());
                pUtil.printRawBytes(pUtil.getGbk(send_data));



            } catch (IOException | NullPointerException e) {
                 Log.i(TAG,"打印失败了！！！！！！！！!!!");

            }

        }

    }

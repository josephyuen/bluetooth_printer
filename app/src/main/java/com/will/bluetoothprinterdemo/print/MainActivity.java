package com.will.bluetoothprinterdemo.print;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.will.bluetoothprinterdemo.R;

public class MainActivity extends Activity implements OnClickListener {


    /*
     *   CPCL  指令集
     *
     */

    private String s = "! 0 200 200 1000 1\r\n" +
            "PAGE-WIDTH 576\r\n" +
            "BG 0 0 0\r\n" +
            "TEXT 4 3 96 96 湖南-怀化1812\r\n" +
            "TEXT 4 0 80 200 鹤城01-null\r\n" +
            "ML 47\r\n" +
            "TEXT 4 0 96 256\r\n" +
            "肖五平   17788888888\r\n" +
            "鹤城区\r\n" +
            "ENDML\r\n" +
            "ML 32\r\n" +
            "TEXT 2 0 96 360\r\n" +
            "王国梁   18874493961\r\n" +
            "鹤城区\r\n" +
            "ENDML\r\n" +
            "BARCODE 128 2 1 70 80 456 9893296116152\r\n" +
            "TEXT 2 0 180 530 9893296116152\r\n" +
            "BARCODE 128 2 1 70 80 712 9893296116152\r\n" +
            "TEXT 2 0 180 788 9893296116152\r\n" +
            "BARCODE QR 474 586 M 2 U 4\r\n" +
            "MA,9893296116152\r\n" +
            "ENDQR\r\n" +
            "BARCODE QR 474 824 M 2 U 4\r\n" +
            "MA,9893296116152\r\n" +
            "ENDQR\r\n" +
            "ML 24\r\n" +
            "TEXT 55 0 150 576\r\n" +
            "快件送到收件人地址，经收件人或寄件人允许签字，视为送达\r\n" +
            "ENDML\r\n" +
            "TEXT 55 0 20 576 2018-08-17\r\n" +
            "TEXT 2 0 20 600 21:33:25\r\n" +
            "TEXT 2 0 20 632 1/1\r\n" +
            "TEXT 55 0 20 672 打印时间\r\n" +
            "ML 32\r\n" +
            "TEXT 2 0 96 824\r\n" +
            "肖五平   17788888888\r\n" +
            "鹤城区\r\n" +
            "ENDML\r\n" +
            "ML 32\r\n" +
            "TEXT 2 0 96 890\r\n" +
            "王国梁   18874493961\r\n" +
            "鹤城区\r\n" +
            "ENDML\r\n" +
            "FORM\r\n" +
            "PRINT";

    private static boolean isConnected;
    private TextView btnBluetooth;
    private Button connectButton;
    private ProgressDialog dialog;
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PrinterConstants.Connect.SUCCESS /*101*/:
                    MainActivity.isConnected = true;
                    MainActivity.this.mPrinter = MainActivity.this.myOpertion.getPrinter();
                    break;
                case PrinterConstants.Connect.FAILED /*102*/:
                    MainActivity.isConnected = false;
                    Toast.makeText(MainActivity.this.mContext, "蓝牙连接失败...", Toast.LENGTH_SHORT).show();
                    break;
                case PrinterConstants.Connect.CLOSED /*103*/:
                    MainActivity.isConnected = false;
                    Toast.makeText(MainActivity.this.mContext, "蓝牙连接已关闭...", Toast.LENGTH_SHORT).show();
                    break;
            }
            MainActivity.this.updateButtonState();
            if (MainActivity.this.dialog != null && MainActivity.this.dialog.isShowing()) {
                MainActivity.this.dialog.dismiss();
            }
        }
    };
    private PrinterInstance mPrinter;
    private BluetoothOperation mService = null;
    private IPrinterOpertion myOpertion;


    private EditText printData = null;

    private Button send = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("怀化邮政打印机");
        this.mContext = this;
        InitView();
    }

    private void InitView() {
        this.connectButton = (Button) findViewById(R.id.connect);
        this.connectButton.setOnClickListener(this);

        this.printData = (EditText) findViewById(R.id.print_data);
        this.send = (Button) findViewById(R.id.send);
        this.send.setOnClickListener(this);

        this.btnBluetooth = (TextView) findViewById(R.id.btnBluetooth);
        this.btnBluetooth.setOnClickListener(this);

        this.dialog = new ProgressDialog(this.mContext);
        this.dialog.setProgressStyle(0);
        this.dialog.setTitle("连接中...");
        this.dialog.setMessage("请稍等...");
        this.dialog.setIndeterminate(true);
        this.dialog.setCancelable(false);
        this.mService = new BluetoothOperation(this, this.mHandler);
        setTitleTextColor(0);
    }

    private void updateButtonState() {
        boolean z;
        if (isConnected) {
            this.connectButton.setText(R.string.disconnect);
        } else {
            String connStr = getResources().getString(R.string.connect);
            this.connectButton.setText(connStr);
        }
        TextView title = this.btnBluetooth;
        if (isConnected) {
            z = false;
        } else {
            z = true;
        }
        title.setEnabled(z);


        this.send.setEnabled(isConnected);
        if(isConnected){
            this.send.setText(R.string.btnSend);
        }else{
            this.send.setText("请先连接打印机");
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == -1) {
                    this.dialog.show();
                    new Thread(new Runnable() {
                        public void run() {
                            MainActivity.this.myOpertion.open(data);
                        }
                    }).start();
                    return;
                }
                return;
            case 2:
                if (resultCode == -1) {
                    this.myOpertion.chooseDevice();
                    return;
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
                    return;
                }
            default:
                return;
        }
    }

    public void onPageSelected(View view) {
        int index = 0;
        setTitleTextColor(index);
    }

    private void setTitleTextColor(int index) {
        this.btnBluetooth.setTextColor(-16776961);
        updateButtonState();
    }

    private void openConn() {
        if (isConnected) {
            this.myOpertion.close();
            this.myOpertion = null;
            this.mPrinter = null;
            return;
        }

        this.myOpertion = new BluetoothOperation(this, this.mHandler);
        this.myOpertion.chooseDevice();
    }

    public void onClick(View view) {
        String sendData;
        if (view == this.connectButton) {
            openConn();
        } else if (view == this.btnBluetooth) {
            onPageSelected(view);
        } else if (view == this.send) {
            if(mPrinter == null){
                openConn();
                return;
            }

            sendData = this.printData.getText().toString();

            // ---  防止频繁点击，卡死
            this.send.setEnabled(false);
            this.send.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.send.setEnabled(true);
                }
            },1500);


            if (sendData.length() > 0) {
                this.mPrinter.printText(sendData);
            } else {
                Toast.makeText(this, getText(R.string.empty), Toast.LENGTH_SHORT).show();
            }
        }
    }
}




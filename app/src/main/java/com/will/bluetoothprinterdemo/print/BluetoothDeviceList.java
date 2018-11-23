package com.will.bluetoothprinterdemo.print;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.will.bluetoothprinterdemo.R;

import java.util.Set;

public class BluetoothDeviceList extends Activity {
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_RE_PAIR = "re_pair";
    private static final String TAG = "DeviceListActivity";
    private BluetoothAdapter mBtAdapter;
    private OnCreateContextMenuListener mCreateContextMenuListener = new OnCreateContextMenuListener() {
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo arg2) {
            menu.setHeaderTitle(R.string.select_options);
            if (((TextView) ((AdapterContextMenuInfo) arg2).targetView).getText().toString().contains(" ( " + BluetoothDeviceList.this.getResources().getText(R.string.has_paired) + " )")) {
                menu.add(0, 0, 0, R.string.rePaire_connect).setOnMenuItemClickListener(BluetoothDeviceList.this.mOnMenuItemClickListener);
                menu.add(0, 1, 1, R.string.connect_paired).setOnMenuItemClickListener(BluetoothDeviceList.this.mOnMenuItemClickListener);
                return;
            }
            menu.add(0, 2, 2, R.string.paire_connect).setOnMenuItemClickListener(BluetoothDeviceList.this.mOnMenuItemClickListener);
        }
    };
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();
            BluetoothDeviceList.this.returnToPreviousActivity(info.substring(info.length() - 17), false);
        }
    };
    private OnItemLongClickListener mDeviceLongClickListener = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
            return false;
        }
    };
    private final OnMenuItemClickListener mOnMenuItemClickListener = new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            String info = ((TextView) ((AdapterContextMenuInfo) item.getMenuInfo()).targetView).getText().toString();
            String address = info.substring(info.length() - 17);
            switch (item.getItemId()) {
                case 0:
                    BluetoothDeviceList.this.returnToPreviousActivity(address, true);
                    break;
                case 1:
                case 2:
                    BluetoothDeviceList.this.returnToPreviousActivity(address, false);
                    break;
            }
            return false;
        }
    };
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                String itemName = device.getName() + " ( " + BluetoothDeviceList.this.getResources().getText(device.getBondState() == 12 ? R.string.has_paired : R.string.not_paired) + " )\n" + device.getAddress();
                BluetoothDeviceList.this.mPairedDevicesArrayAdapter.remove(itemName);
                BluetoothDeviceList.this.mPairedDevicesArrayAdapter.add(itemName);
                BluetoothDeviceList.this.pairedListView.setEnabled(true);
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                BluetoothDeviceList.this.setProgressBarIndeterminateVisibility(false);
                BluetoothDeviceList.this.setTitle(R.string.select_device);
                if (BluetoothDeviceList.this.mPairedDevicesArrayAdapter.getCount() == 0) {
                    BluetoothDeviceList.this.mPairedDevicesArrayAdapter.add(BluetoothDeviceList.this.getResources().getText(R.string.none_found).toString());
                    BluetoothDeviceList.this.pairedListView.setEnabled(false);
                }
                BluetoothDeviceList.this.scanButton.setText(R.string.button_scan);
                BluetoothDeviceList.this.scanButton.setEnabled(true);
            }
        }
    };
    private ListView pairedListView;
    private Button scanButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(5);
        setContentView(R.layout.device_list);
        setTitle(R.string.select_device);
        setResult(0);
        initView();
    }

    private void initView() {
        this.scanButton = (Button) findViewById(R.id.button_scan);
        this.scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BluetoothDeviceList.this.doDiscovery();
                v.setEnabled(false);
                BluetoothDeviceList.this.scanButton.setText("搜索中...");
            }
        });
        this.mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_item);
        this.pairedListView = (ListView) findViewById(R.id.paired_devices);
        this.pairedListView.setAdapter(this.mPairedDevicesArrayAdapter);
        this.pairedListView.setOnItemClickListener(this.mDeviceClickListener);
        this.pairedListView.setOnItemLongClickListener(this.mDeviceLongClickListener);
        this.pairedListView.setOnCreateContextMenuListener(this.mCreateContextMenuListener);
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = this.mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                this.mPairedDevicesArrayAdapter.add(device.getName() + " ( " + getResources().getText(R.string.has_paired) + " )\n" + device.getAddress());
            }
        }
    }

    protected void onStop() {
        if (this.mBtAdapter != null && this.mBtAdapter.isDiscovering()) {
            this.mBtAdapter.cancelDiscovery();
        }
        unregisterReceiver(this.mReceiver);
        super.onStop();
    }

    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.FOUND");
        filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        registerReceiver(this.mReceiver, filter);
        super.onResume();
    }

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        if (this.mBtAdapter.isDiscovering()) {
            this.mBtAdapter.cancelDiscovery();
        }
        this.mPairedDevicesArrayAdapter.clear();
        this.mBtAdapter.startDiscovery();
    }

    private void returnToPreviousActivity(String address, boolean re_pair) {
        if (this.mBtAdapter.isDiscovering()) {
            this.mBtAdapter.cancelDiscovery();
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        intent.putExtra(EXTRA_RE_PAIR, re_pair);
        setResult(-1, intent);
        finish();
    }
}
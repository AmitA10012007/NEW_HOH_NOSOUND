package com.example.new_hoh_nosound;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

public class BLEManager {
    public interface HeartRateCallback {
        void onHeartRateReceived(int bpm);
    }
    private HeartRateCallback callback;
    private BluetoothAdapter adapter;

    public BLEManager(Context ctx, HeartRateCallback cb) {
        callback = cb;
        BluetoothManager manager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
    }

    public void startScan() {
        // TODO: Start BLE scan, connect to ESP, receive heart rate, call callback.onHeartRateReceived(bpm);
    }

    public void stopScan() {
        // TODO: Stop BLE scan/connection
    }
}
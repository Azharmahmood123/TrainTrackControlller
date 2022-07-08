package com.train.track.controller.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.train.track.controller.R;
import com.train.track.controller.adapter.AvailableDevicesAdapter;
import com.train.track.controller.adapter.PairedDevicesAdapter;
import com.train.track.controller.dialogs.Dialogs;
import com.train.track.controller.model.BluetoothObject;
import com.train.track.controller.util.SpacesItemDecoration;
import com.train.track.controller.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothDevicesActivity extends AppCompatActivity implements View.OnClickListener, PairedDevicesAdapter.OnItemClickListener, AvailableDevicesAdapter.OnItemClickListener {
    private final String LOG_TAG = "TAG";

    private Toolbar toolbar;
    private AppCompatTextView tvBack;
    private AppCompatTextView tvScan;
    private LinearLayoutCompat llPairedDevices;
    private RecyclerView rvPairedDevices, rvAvailableDevices;
    private AppCompatTextView tvNoDeviceFound;
    private ProgressBar progressBar;

    private PairedDevicesAdapter pairedDevicesAdapter;
    private AvailableDevicesAdapter availableDevicesAdapter;
    private BluetoothAdapter mBluetoothAdapter;

    private final int REQUEST_ENABLE_BT = 99; // Any positive integer should work.
    private final List<BluetoothObject> pairedDevicesList = new ArrayList<>();
    private final List<BluetoothObject> availableDevicesList = new ArrayList<>();
    private boolean isDetecting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);
        findView();
        initializeControls();
        attachListeners();
    }

    private void findView() {
        toolbar = findViewById(R.id.toolbar);
        tvBack = findViewById(R.id.tvBack);
        tvScan = findViewById(R.id.tvScan);
        llPairedDevices = findViewById(R.id.llPairedDevices);
        rvPairedDevices = findViewById(R.id.rvPairedDevices);
        rvAvailableDevices = findViewById(R.id.rvAvailableDevices);
        progressBar = findViewById(R.id.progressBar);
        tvNoDeviceFound = findViewById(R.id.tvNoDeviceFound);
    }

    private void initializeControls() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        }

        rvPairedDevices.setLayoutManager(new LinearLayoutManager(BluetoothDevicesActivity.this));
        pairedDevicesAdapter = new PairedDevicesAdapter(pairedDevicesList, BluetoothDevicesActivity.this);
        rvPairedDevices.setAdapter(pairedDevicesAdapter);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.margin_ten);
        rvPairedDevices.addItemDecoration(new SpacesItemDecoration(1, spacingInPixels, true));
        ViewCompat.setNestedScrollingEnabled(rvPairedDevices, false);

        rvAvailableDevices.setLayoutManager(new LinearLayoutManager(BluetoothDevicesActivity.this));
        availableDevicesAdapter = new AvailableDevicesAdapter(availableDevicesList, BluetoothDevicesActivity.this);
        rvAvailableDevices.setAdapter(availableDevicesAdapter);
        rvAvailableDevices.addItemDecoration(new SpacesItemDecoration(1, spacingInPixels, true));
        ViewCompat.setNestedScrollingEnabled(rvAvailableDevices, false);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBluetoothOnDevice();
    }

    private void attachListeners() {
        tvBack.setOnClickListener(this);
        tvScan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == tvBack) {
            onBackPressed();
        } else if (v == tvScan) {
            if (isDetecting) {
                cancelScanForBluetoothDevices();
            } else {
                startScanForBluetoothDevices();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private void enableBluetoothOnDevice() {
        if (mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "This device does not have a bluetooth adapter");
            finish();
            // If the android device does not have bluetooth, just return and get out.
            // There's nothing the app can do in this case. Closing app.
        }

        // Check to see if bluetooth is enabled. Prompt to enable it
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            new Handler().postDelayed(() -> {
                loadPairedDevices();
                startScanForBluetoothDevices();
            }, 500);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == 0) {
                String message = "Since bluetooth permissions are not allowed, you will not be able to scan bluetooth devices.";
                Dialogs.showAlertWithOneButton(BluetoothDevicesActivity.this, message, "Error!",
                        "OK", false, null);
            } else {
                Log.i(LOG_TAG, "User allowed bluetooth access!");
                Utils.showToast("Bluetooth Permissions Allowed");
                new Handler().postDelayed(() -> {
                    loadPairedDevices();
                    startScanForBluetoothDevices();
                }, 500);
            }
        }
    }

    @Override
    public void onPairedItemClick(int position) {
        BluetoothObject bluetoothDevice = pairedDevicesList.get(position);
        sendBackOkResult(bluetoothDevice);
    }

    @Override
    public void onAvailableItemClick(int position) {
        BluetoothObject bluetoothDevice = availableDevicesList.get(position);
        sendBackOkResult(bluetoothDevice);
    }

    private void sendBackOkResult(BluetoothObject bluetoothObject) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Utils.BLUETOOTH_OBJECT, bluetoothObject);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void loadPairedDevices() {
        // Query paired devices
        if (mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are any paired devices
            if (pairedDevices.size() > 0) {
                llPairedDevices.setVisibility(View.VISIBLE);
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    // Create the device object and add it to the arrayList of devices
                    BluetoothObject bluetoothObject = new BluetoothObject();
                    bluetoothObject.setBluetoothName(device.getName());
                    bluetoothObject.setBluetoothAddress(device.getAddress());
                    bluetoothObject.setBluetoothBondState(device.getBondState());
                    bluetoothObject.setBluetoothType(device.getType());    // requires API 18 or higher
                    bluetoothObject.setBluetoothUUIDS(device.getUuids());
                    pairedDevicesList.add(bluetoothObject);
                }
                pairedDevicesAdapter.updateList(pairedDevicesList);
            } else {
                llPairedDevices.setVisibility(View.GONE);
            }
        }
    }

    private void startScanForBluetoothDevices() {
        if (mBluetoothAdapter != null) {
            // start looking for bluetooth devices
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
            progressBar.setVisibility(View.VISIBLE);
            mBluetoothAdapter.startDiscovery();
            isDetecting = true;
            tvScan.setText(getString(R.string.lbl_cancel));
        }
    }

    private void cancelScanForBluetoothDevices() {
        progressBar.setVisibility(View.GONE);
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
            isDetecting = false;
            tvScan.setText(getString(R.string.lbl_scan));
            if (pairedDevicesList.size() > 0) {
                tvNoDeviceFound.setVisibility(View.GONE);
            } else {
                tvNoDeviceFound.setVisibility(View.VISIBLE);
            }
            unregisterReceiver(mReceiver);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceHardwareAddress = device.getAddress(); // MAC address
                if (!checkBluetoothDeviceContains(deviceHardwareAddress)) {
                    BluetoothObject bluetoothObject = new BluetoothObject();
                    bluetoothObject.setBluetoothName(device.getName());
                    bluetoothObject.setBluetoothAddress(device.getAddress());
                    bluetoothObject.setBluetoothBondState(device.getBondState());
                    bluetoothObject.setBluetoothType(device.getType());    // requires API 18 or higher
                    bluetoothObject.setBluetoothUUIDS(device.getUuids());
                    availableDevicesList.add(bluetoothObject);
                    availableDevicesAdapter.updateList(availableDevicesList);
                }
            }
        }
    };

    private boolean checkBluetoothDeviceContains(String deviceHardwareAddress) {
        boolean contains = false;
        for (int i = 0; i < availableDevicesList.size(); i++) {
            BluetoothObject bluetoothObject = availableDevicesList.get(i);
            if (bluetoothObject.getBluetoothAddress().equalsIgnoreCase(deviceHardwareAddress)) {
                contains = true;
                break;
            }
        }
        return contains;
    }
}

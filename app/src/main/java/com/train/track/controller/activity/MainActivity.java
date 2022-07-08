package com.train.track.controller.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.train.track.controller.R;
import com.train.track.controller.dialogs.Dialogs;
import com.train.track.controller.model.BluetoothObject;
import com.train.track.controller.util.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private TextView tvBluetooth;
    private Button btnLogout;
    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnTrackOne;
    private Button btnTrackTwo;

    private final String TAG = MainActivity.class.getSimpleName();
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status
    private BluetoothAdapter mBTAdapter;
    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private final int BLUETOOTH_START_ACTIVITY_RESULT = 253;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        configure();

        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_READ) {
                    try {
                        String readMessage = new String((byte[]) msg.obj, "UTF-8");
                        Intent resultIntent = new Intent(MainActivity.this, MainActivity.class);
                        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        showNotificationMessage(getResources().getString(R.string.app_name), readMessage, resultIntent);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.what == CONNECTING_STATUS) {
                    tvBluetooth.setText(msg.obj.toString());
                    if (msg.arg1 == 1) {
                        btnConnect.setVisibility(View.GONE);
                        btnDisconnect.setVisibility(View.VISIBLE);
                    } else {
                        btnConnect.setVisibility(View.VISIBLE);
                        btnDisconnect.setVisibility(View.GONE);
                    }
                }
            }
        };
    }

    private void findViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvBluetooth = findViewById(R.id.tvBluetooth);
        btnLogout = findViewById(R.id.btnLogout);
        btnConnect = findViewById(R.id.btnConnect);
        btnDisconnect = findViewById(R.id.btnDisconnect);
        btnTrackOne = findViewById(R.id.btnTrackOne);
        btnTrackTwo = findViewById(R.id.btnTrackTwo);
    }

    private void configure() {
        String welcomeText = "Welcome " + Utils.getStringPrefs(Utils.PrefConstants.PREF_USER_EMAIL);
        tvWelcome.setText(welcomeText);

        btnConnect.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BluetoothDevicesActivity.class);
            startActivityForResult(intent, BLUETOOTH_START_ACTIVITY_RESULT);
        });
        btnDisconnect.setOnClickListener(v -> {
            if (mBTSocket != null && mBTSocket.isConnected() && mConnectedThread != null) {
                try {
                    mConnectedThread.interrupt();
                    mBTSocket.close();
                    mHandler.obtainMessage(CONNECTING_STATUS, -1, -1, "Disconnected").sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btnTrackOne.setOnClickListener(v -> {
            if (mBTSocket != null && mBTSocket.isConnected() && mConnectedThread != null) {
                String sendStr = "A";
                mConnectedThread.write(sendStr);
            } else {
                String message = "No bluetooth device is connected, Please connect device first.";
                Dialogs.showAlertWithOneButton(MainActivity.this, message, "Device !", "OK",
                        false, null);
            }
        });
        btnTrackTwo.setOnClickListener(v -> {
            if (mBTSocket != null && mBTSocket.isConnected() && mConnectedThread != null) {
                String sendStr = "B";
                mConnectedThread.write(sendStr);
            } else {
                String message = "No bluetooth device is connected, Please connect device first.";
                Dialogs.showAlertWithOneButton(MainActivity.this, message, "Device !", "OK",
                        false, null);
            }
        });

        btnLogout.setOnClickListener(v -> {
            Utils.setPrefs(Utils.PrefConstants.PREF_USER_EMAIL, "");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == BLUETOOTH_START_ACTIVITY_RESULT) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        BluetoothObject bluetoothObject = bundle.getParcelable(Utils.BLUETOOTH_OBJECT);
                        tryDeviceConnection(bluetoothObject);
                    }
                }
            }
        }
    }

    private void tryDeviceConnection(BluetoothObject bluetoothObject) {
        if (!mBTAdapter.isEnabled()) {
            Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
            return;
        }

        tvBluetooth.setText(getString(R.string.lbl_connecting));
        // Get the device MAC address, which is the last 17 chars in the View

        // Spawn a new thread to avoid blocking the GUI one
        new Thread() {
            @Override
            public void run() {
                boolean fail = false;
                BluetoothDevice device = mBTAdapter.getRemoteDevice(bluetoothObject.getBluetoothAddress());
                try {
                    mBTSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    fail = true;
                    Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                }
                // Establish the Bluetooth socket connection.
                try {
                    mBTSocket.connect();
                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                        mHandler.obtainMessage(CONNECTING_STATUS, -1, -1, "Connection Failed").sendToTarget();
                    } catch (IOException e2) {
                        //insert code to deal with this
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                }
                if (!fail) {
                    mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                    mConnectedThread.start();
                    String message = "Connected to Device: " + bluetoothObject.getBluetoothName();
                    mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, message).sendToTarget();
                }
            }
        }.start();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection", e);
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }

    private void showNotificationMessage(String title, String message, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
        String channelID = "12345";
        String description = "Test Notification";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelID, description, importance);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
            Notification.Builder builder = new Notification.Builder(MainActivity.this, channelID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent);
            notificationManager.notify(12345, builder.build());
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, channelID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent);// set pending intent;
            notificationManager.notify(12345, builder.build());
        }
        try {
            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/raw/" + R.raw.notification);
            Ringtone r = RingtoneManager.getRingtone(MainActivity.this, alarmSound);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
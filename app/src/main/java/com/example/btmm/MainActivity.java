package com.example.btmm;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.tabs.TabLayout;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Page1 graphFragment = new Page1();
    private Page3 bleFragment = new Page3();

    private boolean generateDataWithoutBleConnection = true;
    BluetoothDevice bleDevice;
    BluetoothGatt bleGatt;
    BluetoothGattDescriptor bleDescriptor;
    private BluetoothAdapter mBluetoothAdapter;
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //if (Build.VERSION.SDK_INT >= 31) {
            //    ma.checkPermission(Manifest.permission.BLUETOOTH_SCAN, 2);
            //}

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                Log.i("Main", "Connected to GATT server.");
                ContextCompat.getMainExecutor(getApplicationContext()).execute(() -> {Toast.makeText(MainActivity.this,"Connected to device", Toast.LENGTH_LONG).show();});
                bleGatt = gatt;
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                Log.i("GattCallback", "Discovery");
                //android.os.SystemClock.sleep(60000);
                Log.i("GattCallback", "Finished Waiting");
                gatt.discoverServices();
                Log.i("GattCallback", "DiscoveryCalled");


                // Attempts to discover services after successful connection.
                //Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("Main", "Disconnected from GATT server.");
                ContextCompat.getMainExecutor(getApplicationContext()).execute(() -> {Toast.makeText(MainActivity.this,"Disconnected", Toast.LENGTH_LONG).show();});
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                gatt.close();
            }
            else {
                Log.i("Main", "Gatt connection error");
                ContextCompat.getMainExecutor(getApplicationContext()).execute(() -> {Toast.makeText(MainActivity.this,"Connection error", Toast.LENGTH_LONG).show();});
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BluetoothGattCallback", "Discovered some services");

                List<BluetoothGattService> gattServices = gatt.getServices();
                Log.i("onServicesDiscovered", "Services count: " + gattServices.size());

                if (gattServices.size() == 0) {
                    checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                    Log.i("OnService", "Discovery");
                    //android.os.SystemClock.sleep(10000);
                    Log.i("OnService", "Finished Waiting");
                    gatt.discoverServices();
                    //gattServices = gatt.getServices();
                    //Log.i("onService", "Services count: "+gattServices.size());
                    //Log.i("OnService", "DiscoveryCalled");
                }
                //for (BluetoothGattService gattService : gattServices) {
                //    String serviceUUID = gattService.getUuid().toString();
                //    Log.i("onServicesDiscovered", "Service uuid "+serviceUUID);
                //}
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.d("Serv", "Found Service " + service.getUuid().toString());
                    for (BluetoothGattCharacteristic mcharacteristic : service.getCharacteristics()) {
                        //Log.i("Char", "Found Characteristic " + mcharacteristic.getUuid().toString());
                        if (containsNotifyProperty(mcharacteristic)) {
                            Log.i("OnService", "Has notify:" + mcharacteristic.getUuid().toString());
                            checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                            boolean isCharacteristicNotifyEnabled = bleGatt.setCharacteristicNotification(mcharacteristic, true);
                            Log.i("EnableNotification", "isEnabled: "+isCharacteristicNotifyEnabled);
                            //bleDescriptor = mcharacteristic.getDescriptor(mcharacteristic.getUuid());
                            //bleDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            //gatt.writeDescriptor(bleDescriptor);
                            Log.i("HasNotify", "Value:" +gatt.readCharacteristic(mcharacteristic));
                            //for (int i =0; i< 1000; i++) {
                            //    addNewData((float)(Math.random() * 40) + 30f);
                            //    android.os.SystemClock.sleep(200);
                            //}
                        }
                    }
                }

            } else {
                Log.i("BluetoothGattCallback", "onServicesDiscovered received: " + status);
            }
        }


        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                Log.i("CharRead","Read: "+gatt.readCharacteristic(characteristic));
                decodeData(characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("GattChar", "Value changed");
            graphFragment.newData(0);
        }
/*
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

        }
*/
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(3);

        //setting up the adapter

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        //setTitle("ECS193 bt_mm");

        // add the fragments
        viewPagerAdapter.add(bleFragment,"Bluetooth");
        //viewPagerAdapter.add(new Page2("No Data Yet"),"Raw");
        viewPagerAdapter.add(graphFragment,"Graph");

        //Set the adapter
        viewPager.setAdapter(viewPagerAdapter);

        // The Page (fragment) titles will be displayed in the
        // tabLayout hence we need to  set the page viewer
        // we use the setupWithViewPager().

        tabLayout =  findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

    }
    public void fakeData() {
        if(generateDataWithoutBleConnection ==true) {
            Log.i("Main","FakeData");
            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    graphFragment.newData((float)(Math.floor((Math.random() * 40) + 0)));
                    handler.postDelayed(this, 200);
                }
            };
            handler.postDelayed(r, 100);
        }
    }

    public void connectToDevice(BluetoothDevice bleDevice) {
        Log.i("Main","Attempting to connect");
        checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
        bleGatt = bleDevice.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
        Log.i("Main", "Conn");
    }
    public boolean containsNotifyProperty(BluetoothGattCharacteristic characteristic) {
        int flag = characteristic.getProperties();
        if ((flag & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            //Log.i("ContainsNotify", "Notify Char: "+characteristic.getUuid().toString());
            return true;
        }
        else {
            //Log.i("ContainsNotify", "No notify Char: "+characteristic.getUuid().toString());
            return false;
        }
    }
    public void updateMode(float val) {

    }
    public void decodeData(byte[] data) {
        Log.i("BeforeDecodeValue: ", String.valueOf(data[0]));
        Log.i("x",String.valueOf(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat()));
        graphFragment.newData(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getFloat());
    }
    public void checkPermission(String permission, int reqCode) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
            ActivityCompat.requestPermissions(this, new String[] {permission}, reqCode);
        }
    }
}


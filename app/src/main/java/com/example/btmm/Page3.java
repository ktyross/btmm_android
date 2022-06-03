package com.example.btmm;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Page3 extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int BT_PERMISSION_CODE = 0;
    private static final int LOC_PERMISSION_CODE = 100;
    private final static String TAG = MainActivity.class.getSimpleName();
    public static final int REQUEST_ENABLE_BT = 1;
    public static final int BTLE_SERVICES = 2;
    private HashMap<String, BTLE_Device> mBTDevicesHashMap;
    private ArrayList<BTLE_Device> mBTDevicesArrayList;
    private ListAdapter_BTLE_Devices adapter;
    private ListView listView;
    private Button btn_Scan;
    private BroadcastReceiver_BTState mBTStateUpdateReceiver;
    private Scanner_BTLE mBTLeScanner;
    private TextView connectionStatus;
    private Handler handler;

    private boolean generateDataWithoutBleConnection = false;
    private BluetoothDevice bleDevice;
    private BluetoothGatt bleGatt;
    private BluetoothGattDescriptor bleDescriptor;
    private BluetoothAdapter mBluetoothAdapter;
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //if (Build.VERSION.SDK_INT >= 31) {
            //    ma.checkPermission(Manifest.permission.BLUETOOTH_SCAN, 2);
            //}

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //bleFragment.setConnectionMessage("Connected to: ", gatt.getDevice().getName());
                ContextCompat.getMainExecutor(getContext()).execute(() -> {Toast.makeText(getContext(),"Connected to device", Toast.LENGTH_LONG).show();});
                bleGatt = gatt;
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                //android.os.SystemClock.sleep(60000);
                // Attempts to discover services after successful connection.
                gatt.discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                setConnectionMessage("No connection ", "");
                ContextCompat.getMainExecutor(getContext()).execute(() -> {Toast.makeText(getContext(),"Disconnected", Toast.LENGTH_LONG).show();});
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                gatt.close();
            }
            else {
                setConnectionMessage("No connection ", "");
                ContextCompat.getMainExecutor(getContext()).execute(() -> {Toast.makeText(getContext(),"Connection error", Toast.LENGTH_LONG).show();});
                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> gattServices = gatt.getServices();
                if (gattServices.size() == 0) {
                    checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                    //android.os.SystemClock.sleep(10000);
                    gatt.discoverServices();
                    //gattServices = gatt.getServices();
                }
                //for (BluetoothGattService gattService : gattServices) {
                //    String serviceUUID = gattService.getUuid().toString();
                //    Log.i("onServicesDiscovered", "Service uuid "+serviceUUID);
                //}
                for (BluetoothGattService service : gatt.getServices()) {
                    for (BluetoothGattCharacteristic mcharacteristic : service.getCharacteristics()) {
                        if (containsNotifyProperty(mcharacteristic)) {
                            checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                            for (BluetoothGattDescriptor descriptor:mcharacteristic.getDescriptors()){
                                gatt.setCharacteristicNotification(descriptor.getCharacteristic(), true);
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(descriptor);
                                break;
                            }

                           // gatt.readCharacteristic(mcharacteristic);
                            //Log.i("HasNotify", "Value:" +gatt.readCharacteristic(mcharacteristic));
                            //for (int i =0; i< 1000; i++) {
                            //    addNewData((float)(Math.random() * 40) + 30f);
                            //    android.os.SystemClock.sleep(200);
                            //}
                        }
                    }
                }

            }
        }

/*
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update UI
                                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                                //Log.i("CharRead","Read: "+gatt.readCharacteristic(characteristic));
                                ((MainActivity)getActivity()).decodeData(characteristic.getValue());
                            }
                        });
                    }
                    gatt.readCharacteristic(characteristic);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
*/
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.i("GattChar", "Value changed");
            checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // update UI
                    ((MainActivity)getActivity()).decodeData(characteristic.getValue());
                }
            });
        }
/*
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {

        }
*/
    };

    public Page3(){
        //required empty public constructor.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        handler = new Handler();
        View rootView = inflater.inflate(R.layout.fragment_page3,container,false);
        if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(getContext(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        connectionStatus = (TextView) rootView.findViewById(R.id.connStatus);
        rootView.findViewById(R.id.dc).setOnClickListener(this);

        mBTStateUpdateReceiver = new BroadcastReceiver_BTState(getContext());
        mBTLeScanner = new Scanner_BTLE(this, 5000, -75);

        mBTDevicesHashMap = new HashMap<>();
        mBTDevicesArrayList = new ArrayList<>();

        adapter = new ListAdapter_BTLE_Devices(getActivity(), R.layout.btle_device_list_item, mBTDevicesArrayList);

        listView = new ListView(getContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        btn_Scan = (Button) rootView.findViewById(R.id.btn_scan);
        ((ScrollView) rootView.findViewById(R.id.scrollView)).addView(listView);
        rootView.findViewById(R.id.btn_scan).setOnClickListener(this);


        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        getContext().registerReceiver(mBTStateUpdateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
//                Utils.toast(getApplicationContext(), "Thank you for turning on Bluetooth");
            } else if (resultCode == RESULT_CANCELED) {
                Utils.toast(getContext(), "Please turn on Bluetooth");
            }
        } else if (requestCode == BTLE_SERVICES) {
            // Do something
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dcFromDevice();
        Context context = view.getContext();

//        Utils.toast(context, "List Item clicked");

        // do something with the text views and start the next activity.

        stopScan();

        String name = mBTDevicesArrayList.get(position).getName();
        String address = mBTDevicesArrayList.get(position).getAddress();


        //Intent intent = new Intent(getActivity(), Activity_BTLE_Services.class);
        //intent.putExtra(Activity_BTLE_Services.EXTRA_NAME, name);
        //intent.putExtra(Activity_BTLE_Services.EXTRA_ADDRESS, address);
        //startActivityForResult(intent, BTLE_SERVICES);


        setConnectionMessage("Attempting to connect to: ", mBTDevicesArrayList.get(position).getName());

        connectToDevice(mBTDevicesArrayList.get(position).getBluetoothDevice());
        //((MainActivity)getActivity()).addNewData((float)(Math.random() * 40) + 30f);
        //mBTDevicesArrayList.clear();
        //mBTDevicesHashMap.clear();

        adapter.notifyDataSetChanged();
        setConnectionMessage("Connected to: ", mBTDevicesArrayList.get(position).getName());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_scan:
                checkPermission(Manifest.permission.BLUETOOTH, BT_PERMISSION_CODE);
                checkPermission(Manifest.permission.BLUETOOTH_ADMIN, BT_PERMISSION_CODE+1);
                //if (Build.VERSION.SDK_INT >= 31) {
                //    checkPermission(Manifest.permission.BLUETOOTH_SCAN, BT_PERMISSION_CODE+2);
                //    checkPermission(Manifest.permission.BLUETOOTH_CONNECT, BT_PERMISSION_CODE+3);
                //}
                //else {
                checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, LOC_PERMISSION_CODE);
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, LOC_PERMISSION_CODE + 1);
                //}
                if (!mBTLeScanner.isScanning()) {
                    startScan();
                }
                else {
                    stopScan();
                }

                break;
            case R.id.dc:
                dcFromDevice();
                break;
            default:
                break;
        }
        fakeData();
    }

    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();
        if (!mBTDevicesHashMap.containsKey(address)) {
            BTLE_Device btleDevice = new BTLE_Device(device);
            btleDevice.setRSSI(rssi);

            //checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 0);
            if (device.getName() != null) {
                mBTDevicesHashMap.put(address, btleDevice);
                mBTDevicesArrayList.add(btleDevice);
            }
        } else {
            mBTDevicesHashMap.get(address).setRSSI(rssi);
        }

        adapter.notifyDataSetChanged();
    }

    public void startScan(){
        btn_Scan.setText("Scanning...");

        mBTDevicesArrayList.clear();
        mBTDevicesHashMap.clear();

        mBTLeScanner.start();
    }

    public void stopScan() {
        btn_Scan.setText("Scan Again");

        mBTLeScanner.stop();
    }

    public void checkPermission(String permission, int reqCode) {
        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission);
            ActivityCompat.requestPermissions(getActivity(), new String[] {permission}, reqCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);

        if (requestCode == BT_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Bluetooth Permission Granted", Toast.LENGTH_SHORT) .show();
            }
            else {
                Toast.makeText(getContext(), "Bluetooth Permission Denied", Toast.LENGTH_SHORT) .show();
            }
        }
        else if (requestCode == LOC_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Location Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setConnectionMessage(String message,String name) {
        connectionStatus.setText(message + name);
    }

    public void fakeData() {
        if(generateDataWithoutBleConnection ==true) {
            Log.i("Main","FakeData");

            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    ((MainActivity)getActivity()).newData((float)(Math.floor((Math.random() * 40) + 0)));
                    handler.postDelayed(this, 100);
                }
            };
            handler.postDelayed(r, 100);
        }
    }

    public void connectToDevice(BluetoothDevice bleDevice) {
        checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
        bleGatt = bleDevice.connectGatt(getContext(), false, bluetoothGattCallback);
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

    public void dcFromDevice() {
        //checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 0);
        if (bleGatt != null) {
            bleGatt.disconnect();
        }
    }
}
/*new Thread(new Runnable() {
                @Override
                public void run() {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update UI
                                checkPermission(Manifest.permission.BLUETOOTH_CONNECT, 1);
                                //Log.i("CharRead","Read: "+gatt.readCharacteristic(characteristic));
                                ((MainActivity)getActivity()).decodeData(characteristic.getValue());
                            }
                        });
                    }
                    gatt.readCharacteristic(characteristic);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

 */
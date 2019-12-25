package tw.org.iii.android201922;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.HashMap;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        123);
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private BluetoothAdapter bluetoothAdapter;
    private void init(){
        mClient = new BluetoothClient(this);
        initListView();
        myBTReceiver = new MyBTReceiver();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }else{
            discoverBTDevice();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            Log.v("brad", "BT OK");
            discoverBTDevice();
        }
    }

    private void discoverBTDevice(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(myBTReceiver, filter);
        //scanBTDevice(null);


    }

    public void scanBTDevice(View view){
        devices.clear();
        //bluetoothAdapter.startDiscovery();

        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();
        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {

            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                //Beacon beacon = new Beacon(device.scanRecord);
                //BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));
                HashMap<String,String> data = new HashMap<>();
                data.put(from[0], device.getName());
                data.put(from[1], device.getAddress());
                if (!devices.contains(data)) {
                    devices.add(data);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onSearchStopped() {

            }

            @Override
            public void onSearchCanceled() {

            }
        });
    }

    public void stopScanBTDevice(View view) {
        bluetoothAdapter.cancelDiscovery();
    }

    private MyBTReceiver myBTReceiver;


    private class MyBTReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceName = device.getName();
            String deviceMAC = device.getAddress(); // MAC address

            Log.v("brad", deviceName + ":" + deviceMAC);

            HashMap<String,String> data = new HashMap<>();
            data.put(from[0], deviceName);
            data.put(from[1], deviceMAC);
            if (!devices.contains(data)) {
                devices.add(data);
                adapter.notifyDataSetChanged();
            }

        }
    }

    private ListView listDevice;
    private LinkedList<HashMap<String,String>> devices = new LinkedList<>();
    private SimpleAdapter adapter;
    private String[] from = {"name", "mac"};
    private int[] to = {R.id.deviceName, R.id.deviceMAC};


    private void initListView(){
        listDevice = findViewById(R.id.listDevices);
        adapter = new SimpleAdapter(this, devices, R.layout.item_device, from, to);
        listDevice.setAdapter(adapter);
    }

    BluetoothClient mClient;





}

package out.example.beaconattend;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataActivity extends AppCompatActivity {

    private MinewBeaconManager mMinewBeaconManager;

    private static final int REQUEST_ENABLE_BT = 2;

    private boolean isStarting;

    private String minUUID = null;

    private Button send;
    private Button get;
    private Button init;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private String id;
    private String pw;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        handler = new Handler();

        mMinewBeaconManager = MinewBeaconManager.getInstance(this);

        checkBluetooth();

        if (isBluetoothOn()) {
            isStarting = true;
            mMinewBeaconManager.startScan();
        }

        initListener();

        send = findViewById(R.id.data_request);
        get = findViewById(R.id.data_get);
        init = findViewById(R.id.data_init);
        listView = findViewById(R.id.data_listView);
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.support_simple_spinner_dropdown_item);
        listView.setAdapter(adapter);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        pw = intent.getStringExtra("pw");

        init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SocketUtility.connect("init", (in, out) -> {
                    boolean result = in.readBoolean();
                    handler.post(() -> Toast.makeText(getApplicationContext(), "초기화 요청: " + result, Toast.LENGTH_SHORT).show());
                });
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (minUUID != null) {
                    SocketUtility.connect("stateUpdate", (in, out) -> {
                        out.writeUTF(id);
                        out.writeUTF(pw);
                        out.writeUTF(minUUID);
                        out.flush();

                        handler.post(() -> {
                            Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_SHORT).show();
                        });
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "아직 비콘을 찾는 중입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SocketUtility.connect("userState", (in, out) -> {
                    List<Pair<String, String>> data = new ArrayList<>();
                    int count = in.readInt();

                    for (int i = 0; i < count; i++) {
                        data.add(new Pair<>(in.readUTF(), in.readUTF())); // 닉네임, 룸
                    }

                    handler.post(() -> {
                        adapter.clear();
                        for (int i = 0; i < count; i++) {
                            Pair<String, String> value = data.get(i);
                            adapter.add(value.first + ": " + value.second);
                        }
                        adapter.notifyDataSetChanged();
                        listView.deferNotifyDataSetChanged();
                    });
                });
            }
        });
    }

    private void showBLEDialog() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isStarting) {
            mMinewBeaconManager.stopScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "블루투스가 필요합니다", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    if (!isStarting) {
                        isStarting = true;
                        mMinewBeaconManager.startScan();
                    }
                }
                break;
        }
    }

    private void checkBluetooth() {
        BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
        switch (bluetoothState) {
            case BluetoothStateNotSupported:
                Toast.makeText(this, "Not Support BLE", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BluetoothStatePowerOff:
                showBLEDialog();
                break;
            case BluetoothStatePowerOn:
                break;
        }
    }

    public boolean isBluetoothOn() {
        BluetoothState bluetoothState = mMinewBeaconManager.checkBluetoothState();
        switch (bluetoothState) {
            case BluetoothStatePowerOn:
                return true;
            default:
                return false;
        }
    }

    private void initListener() {
        mMinewBeaconManager.setDeviceManagerDelegateListener(new MinewBeaconManagerListener() {
            /**
             *   if the manager find some new beacon, it will call back this method.
             *
             *  @param minewBeacons  new beacons the manager scanned
             */
            @Override
            public void onAppearBeacons(List<MinewBeacon> minewBeacons) {
            }

            /**
             *  if a beacon didn't update data in 10 seconds, we think this beacon is out of rang, the manager will call back this method.
             *
             *  @param minewBeacons beacons out of range
             */
            @Override
            public void onDisappearBeacons(List<MinewBeacon> minewBeacons) {
                /*for (MinewBeacon minewBeacon : minewBeacons) {
                    String deviceName = minewBeacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();
                    Toast.makeText(getApplicationContext(), deviceName + "  out range", Toast.LENGTH_SHORT).show();
                }*/
            }

            /**
             *  the manager calls back this method every 1 seconds, you can get all scanned beacons.
             *
             *  @param minewBeacons all scanned beacons
             */
            @Override
            public void onRangeBeacons(final List<MinewBeacon> minewBeacons) {
                for (int i = 0; i < minewBeacons.size(); i++) {
                    String value = minewBeacons.get(i).getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_Name).getStringValue();

                    if (!value.contains("MiniBeacon")) {
                        minewBeacons.remove(i);
                        i--;
                    }
                }
                int min = Integer.MAX_VALUE;
                String minBeacon = null;

                for (MinewBeacon beacon : minewBeacons) {
                    int distance = Math.abs(beacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_RSSI).getIntValue());
                    String uuid = beacon.getBeaconValue(BeaconValueIndex.MinewBeaconValueIndex_UUID).getStringValue();

                    Log.d("beacon", "distance: " + distance);

                    if (min > distance) {
                        min = distance;
                        minBeacon = uuid;
                    }
                }

                if(minUUID == null && minBeacon != null) {
                    Toast.makeText(getApplicationContext(), "비콘 찾음" + minBeacon, Toast.LENGTH_LONG).show();

                }
                Log.d("beacon", "uuid:" + minBeacon);
                minUUID = minBeacon;

//                Toast.makeText(getApplicationContext(), "매칭됨:" + minewBeacons.size(), Toast.LENGTH_SHORT).show();
            }

            /**
             *  the manager calls back this method when BluetoothStateChanged.
             *
             *  @param state BluetoothState
             */
            @Override
            public void onUpdateState(BluetoothState state) {
                switch (state) {
                    case BluetoothStatePowerOn:
                        Toast.makeText(getApplicationContext(), "BluetoothStatePowerOn", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothStatePowerOff:
                        Toast.makeText(getApplicationContext(), "BluetoothStatePowerOff", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }
}

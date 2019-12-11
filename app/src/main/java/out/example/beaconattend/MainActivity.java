package out.example.beaconattend;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.minew.beacon.BeaconValueIndex;
import com.minew.beacon.BluetoothState;
import com.minew.beacon.MinewBeacon;
import com.minew.beacon.MinewBeaconManager;
import com.minew.beacon.MinewBeaconManagerListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindAnim;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    EditText id;

    EditText pw;

    Button login;

    Button register;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        id = findViewById(R.id.main_id);
        pw = findViewById(R.id.main_pw);
        login = findViewById(R.id.main_login);
        register = findViewById(R.id.main_register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SocketUtility.connect("login", (in, out) -> {
                    String idStr = id.getText().toString();
                    String pwStr = pw.getText().toString();

                    out.writeUTF(idStr);
                    out.writeUTF(pwStr);
                    out.flush();

                    if (in.readBoolean()) {
                        Intent intent = new Intent(getApplicationContext(), DataActivity.class);
                        intent.putExtra("id", idStr);
                        intent.putExtra("pw", pwStr);

                        startActivity(intent);
                    } else {
                        handler.post(() -> Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show());
                    }

                });
            }
        });

        PermissionUtility.requestPermission(this, Arrays.asList(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 100:
                if (PermissionUtility.isAllPermissionGranted(grantResults)) {
                    Toast.makeText(getApplicationContext(), "역할 부여 완료", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "모든 역할 부여가 필요합니다", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

}

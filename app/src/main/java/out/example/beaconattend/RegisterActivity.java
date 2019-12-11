package out.example.beaconattend;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    EditText id;

    EditText pw;

    EditText name;

    Button register;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        id = findViewById(R.id.register_id);
        pw = findViewById(R.id.register_pw);
        name = findViewById(R.id.register_name);
        register = findViewById(R.id.register_register);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SocketUtility.connect("register", (in, out) -> {
                    out.writeUTF(id.getText().toString());
                    out.writeUTF(pw.getText().toString());
                    out.writeUTF(name.getText().toString());
                    out.flush();

                    if (in.readBoolean()) {

                        handler.post(() -> {
                            Toast.makeText(getApplicationContext(), "회원가입 성공", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        handler.post(() -> Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
    }
}

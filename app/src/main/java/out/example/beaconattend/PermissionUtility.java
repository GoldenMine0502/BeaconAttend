package out.example.beaconattend;

import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtility {
    public static boolean isAllPermissionGranted(Activity activity, List<String> permissions) { // 모든 퍼미션을 부여받았는지 검사하는 코드
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED && !ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isAllPermissionGranted(int[] grantResults) { // 모든 퍼미션을 부여받았는지 검사하는 코드
        for(int grantResult : grantResults) {
            if(grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }

        return true;
    }

    public static void requestPermission(Activity activity, List<String> permissions, final int REQUEST_CODE) { // 퍼미션을 요구하는 코드
        List<String> toRequest = new ArrayList<>();

        // 아직 권한을 부여받지 않은 것만 추려내서
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED && !ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permission)) {
                toRequest.add(permission);
            }
        }

        // 요청
        if (toRequest.size() > 0) {
            ActivityCompat.requestPermissions(activity, (String[]) permissions.toArray(),
                    REQUEST_CODE);
        }
    }


}

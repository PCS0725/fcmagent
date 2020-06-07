package someapp.autoupdate;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

/**
 * This class is the app Launcher activity. It displays a screen with FCM device id to user.
 * Device id is used to target notifications to a device.
 * @author Prabhat Sharma
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MAIN ACTIVITY";
    private TextView textView;
    private final String INTENT_NAME = "UPDATE_EVENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.tokenView);

        //To log and view the device id
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        String token = task.getResult().getToken();

                        String msg = "Token : " + token;
                        Log.e(TAG, msg);
                        textView.setText(msg);
                    }
                });

    }
}

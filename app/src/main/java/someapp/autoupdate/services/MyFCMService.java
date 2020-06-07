package someapp.autoupdate.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import someapp.autoupdate.util.NotificationHandler;


/*
 * This class receives notifications from FCM backend and handles them.
 * Extends FCMService, runs in background even when app is not open.
 * @author Prabhat Sharma
*/
public class MyFCMService extends FirebaseMessagingService {

    private final String TAG = "FCMService";
    private NotificationHandler notificationHandler = null;

    /**
     * This is a callback method which is called whenever device id changes.
     * We should send the new device id to our backend server to target notifications properly.
     * Observer function on device id
     * @param token the new device token
     */
    @Override
    public void onNewToken(String token) {
        //send the token to the server here
        Log.d(TAG, "New token: " + token);
    }

    /**
     * Callback method, called when a notification is received. Invoke the message handler here.
     * @param remoteMessage The data payload of notification received.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage == null)
            Log.d(TAG,"Null message received");
        else{
            Map<String, String> data = remoteMessage.getData();
            notificationHandler = new NotificationHandler(this.getApplicationContext());
            notificationHandler.handleNotification(data);
        }

    }

}

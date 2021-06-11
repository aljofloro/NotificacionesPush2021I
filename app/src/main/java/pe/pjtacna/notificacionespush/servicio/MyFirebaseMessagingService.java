package pe.pjtacna.notificacionespush.servicio;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import pe.pjtacna.notificacionespush.actividades.MainActivity;
import pe.pjtacna.notificacionespush.app.Config;
import pe.pjtacna.notificacionespush.util.NotificationUtils;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
  private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

  private NotificationUtils notificationUtils;

  @Override
  public void onNewToken(String s){
    super.onNewToken(s);
    Log.e("NEW TOKEN",s);
  }

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage){
    Log.e(TAG,"FROM:"+remoteMessage.getFrom());
    if(remoteMessage == null) return;
    if(remoteMessage.getNotification() != null){
      Log.e(TAG, "Notification Body: "+remoteMessage.getNotification().getBody());
      processNotification(remoteMessage.getNotification().getBody());
    }
    if(remoteMessage.getData().size() > 0){
      Log.e(TAG, "Data : " +remoteMessage.getData().toString());
      try {
        JSONObject jsonObject = new JSONObject(remoteMessage.getData().toString());
        translateMessage(jsonObject);
      } catch (JSONException e) {
        e.printStackTrace();
        Log.e(TAG,"Exception : "+e.getMessage());
      }
    }
  }

  private void processNotification(String message){
    if(!NotificationUtils.isAppInBackground(getApplicationContext())){
      Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
      pushNotification.putExtra("message",message);
      LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
      notificationUtils = new NotificationUtils(getApplicationContext());
      notificationUtils.playNotificationAlarm();
    }
  }

  private void translateMessage(JSONObject jsonObject){
    try {
        JSONObject data = jsonObject.getJSONObject("data");
        String title = data.getString("title");
        String message = data.getString("message");
        boolean isBackground = data.getBoolean("is_background");
        String urlImage = data.getString("image");
        String timestamp = data.getString("timestamp");
        JSONObject payload = data.getJSONObject("payload");
        if(!NotificationUtils.isAppInBackground(getApplicationContext())){
          processNotification(message);
        }else{
          Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
          resultIntent.putExtra("message",message);
          if(TextUtils.isEmpty(urlImage)){
            notificationUtils.showNotificationMessage(title,message,timestamp,resultIntent);
          }else{
            resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationUtils.showNotificationMessage(title,message,timestamp,resultIntent,urlImage);
          }
        }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }





}

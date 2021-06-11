package pe.pjtacna.notificacionespush.actividades;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

import pe.pjtacna.notificacionespush.R;
import pe.pjtacna.notificacionespush.app.Config;

public class MainActivity extends AppCompatActivity {

  TextView tv_id, tv_message;
  BroadcastReceiver broadcastReceiver;
  private static final String TAG = MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    FirebaseInstallations.getInstance().getToken(true)
        .addOnSuccessListener(new OnSuccessListener<InstallationTokenResult>() {
          @Override
          public void onSuccess(InstallationTokenResult installationTokenResult) {
            saveSharedPreference(installationTokenResult.getToken());
            showFirebaseId();
          }
        });

    tv_id = findViewById(R.id.tv_id);
    tv_message = findViewById(R.id.tv_message);

    broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Config.REGISTRATION_COMPLETE)){
          FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
          showFirebaseId();
        }else if(intent.getAction().equals(Config.PUSH_NOTIFICATION)){
          String message = intent.getStringExtra("message");
          Toast.makeText(getApplicationContext(),"Push Notification: "+ message,Toast.LENGTH_SHORT).show();
          tv_message.setText(message);
        }
      }
    };
    showFirebaseId();
  }

  private void saveSharedPreference(String token){
    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Config.SHARED_PREF,0);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString("REGID",token);
    editor.apply();
  }

  private void showFirebaseId(){
    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(Config.SHARED_PREF,0);
    String regId = sharedPreferences.getString("REGID",null);
    Log.e(TAG,"FIREBASE ID: "+regId);
    if(!TextUtils.isEmpty(regId)){
      tv_id.setText("FIREBASE ID: "+regId);
    }else{
      tv_id.setText(" WE DON'T HAVE ANY ID YET");
    }
  }

  @Override
  protected void onResume(){
    super.onResume();
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETE));
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,new IntentFilter(Config.PUSH_NOTIFICATION));
    clearNotifications();
  }

  @Override
  protected void onPause(){
    super.onPause();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
  }

  private void clearNotifications(){
    NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancelAll();
  }


}
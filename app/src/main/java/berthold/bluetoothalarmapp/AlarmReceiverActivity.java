package berthold.bluetoothalarmapp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmReceiverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);

        MediaPlayer mpPlayer= MediaPlayer.create(getApplicationContext(),R.raw.sound);
        mpPlayer.start();

        // @rem:This shows how to wake up the device, when it is in sleep mode (wake up device programaticaly)@@
        // @rem:See={link:https://stackoverflow.com/questions/40259780/wake-up-device-programmatically}@@
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.v("ALARM:","Destroyed");
    }
}

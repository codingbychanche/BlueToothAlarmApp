package berthold.bluetoothalarmapp;
/**
 * This code is executed in background as long as the app is not closed or destroyed.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import static java.security.AccessController.getContext;

public class JobServiceAlarmListenerForBTIncomming extends androidx.core.app.JobIntentService {

    static final int JOB_ID = 1000;

    private static boolean canceled;

    private ConnectionNotifier notify;
    // Debug
    private String tag;

    // BT
    private static BluetoothSocket mSocket = null;
    private static InputStream mIs;
    private static OutputStream mOs;

    // UI
    private static Handler handler;
    private static TextView console;

    // Control
    private int lastState;
    private static final int WAS_ALARM = 1;
    private static final int WAS_NO_ALARM = 2;

    /**
     * This listens for incoming connections and starts the alarm...
     *
     * @param intent
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        consoleOut("Input stream opened....\n");
        consoleOut("Waiting for incoming data.......\n");
        lastState = WAS_NO_ALARM;


        while (canceled) {
            Log.v("Logging", "-");

            byte[] packetReceieved = new byte[1024];

            try {
                int bytesAvailable = mIs.available();
                if (bytesAvailable > 0) {
                    packetReceieved = new byte[bytesAvailable];
                    mIs.read(packetReceieved);
                    lastState = WAS_ALARM;
                }
                if (lastState == WAS_ALARM) {

                    String received = new String(packetReceieved, 0, bytesAvailable);
                    Log.v("RECEIVED", received);


                    Date currentTime = Calendar.getInstance().getTime();
                    consoleOut("ALARM:" + currentTime + "\n");

                    MediaPlayer mpPlayer = MediaPlayer.create(getApplicationContext(), R.raw.sound);
                    mpPlayer.start();

                    // Optional,
                    startAlarmActivity("Alarm");

                    lastState = WAS_NO_ALARM;
                }
            } catch (IOException e) {
                consoleOut("Error: " + e.toString() + "\n");
                Log.v(tag,"ERROR RECONNECTING");
                closeInputStream();
                notify.currentConnectionWasinterrupted();
                cancelThisJob();
            }

            // Just a little bit in order to evaluate correctly if there was an alarm or not..
            // The time span given in ms specifies the time which has to be elapsed before
            // a new alarm can be triggered....
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * This starts the job service (the code inside this classes 'onHandleWork()' method.
     * This is invoked by the {@link BluetoothClientThread} when a connection could be
     * established.
     *
     * @param context
     * @param alarmListenerForBTIncomming
     */
    static void doWork(Context context,ConnectionNotifier notify,Intent alarmListenerForBTIncomming, BluetoothSocket socket, Handler h, TextView c) {
        canceled = true;

        handler = h;
        console = c;
        mSocket = socket;

        InputStream tmpIs = null;
        OutputStream tmpOs = null;

        try {
            tmpIs = mSocket.getInputStream();
            tmpOs = mSocket.getOutputStream();
        } catch (IOException e) {
            consoleOut("Error: Could not open input/ output stream cause:" + e.toString() + " \n");
            cancelThisJob();
            notify.currentConnectionWasinterrupted();
        }
        mOs = tmpOs;
        mIs = tmpIs;

        enqueueWork(context, JobServiceAlarmListenerForBTIncomming.class, JOB_ID, alarmListenerForBTIncomming);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        consoleOut("JobService Destroyed\n");
        closeInputStream();
    }

    /*
     * This starts the activity displaying that an alarm-
     * event was received.
     */
    private void startAlarmActivity(final String message) {

        consoleOut("Starting alarm.....\n");

        // I commented that out because it is difficult to predict if
        // this activity is already open or not. This way the activity
        // will be opened all over again each time an alarm was triggered
        // Besides, the activity needed some logic to be able to decide if
        // it was started for the first time or because an alarm was triggered
        // and thus being able to record he event....

        //Intent in = new Intent(this, MainActivity.class);
        //this.startActivity(in);
    }

    /*
     * Shows text inside console- textView
     */
    private static void consoleOut(String message) {
        final String m;
        m = message;
        handler.post(new Runnable() {
            @Override
            public void run() {
                console.append(m);
            }
        });
    }

    static void cancelThisJob() {
        canceled = false;

    }

    private static void closeInputStream() {
        try {
            mIs.close();
            consoleOut("Input Stream closed....\n");
        } catch (IOException e) {
            consoleOut("Could not close input stream:" + e.toString() + "\n");
        }
    }

    private static void closeOutputStream() {
        try {
            mOs.close();
        } catch (IOException e) {
            consoleOut("Could not close output stream:" + e.toString() + "\n");
        }
    }
}

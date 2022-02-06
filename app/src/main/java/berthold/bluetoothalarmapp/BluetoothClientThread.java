package berthold.bluetoothalarmapp;

/**
 * This try's to establish a connection to a BT- device.
 * If a connection could be established it starts the job
 * service listening to incoming messages.
 *
 *
 */

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.util.UUID;


public class BluetoothClientThread extends Thread {

    // Notifier
    ConnectionNotifier notify;

    // BT
    private BluetoothSocket mSocket = null;
    private BluetoothDevice blueToothDevice = null;

    // UI
    private Context context;
    private TextView console;
    private String dataToSend;
    private Handler handler;

    // Use this uuid for connection with other devs....
    //private UUID myUUID=UUID.fromString("00002415-0000-1000-8000-00805F9B34FB");
    // Use this uuid for connection with Hc05 Module....
    private UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String devName;

    /*
     * Constructor, get socket
     */
    public BluetoothClientThread(Context context,ConnectionNotifier main,BluetoothDevice blueToothDevice, TextView console,  Handler handler) {

        this.context=context;
        this.notify=main;
        this.blueToothDevice =blueToothDevice;
        this.console = console;
        this.dataToSend = dataToSend;
        this.handler = handler;
        mSocket = null;

        try {
            mSocket =blueToothDevice.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e) {
            Log.v("Client:Socket:", e.toString());
        }
    }

    /*
     * Try to establish a bluetooth connection.
     * When a connection could be established,
     * a job service listening to incoming data
     * is started.
     */
    public void run() {
        try {
            Log.v("Client:Connecting......", "");
            mSocket.connect();
            consoleOut("Client: Connection successful!\n");

            Intent jobIntent=new Intent (context, JobServiceAlarmListenerForBTIncomming.class);
            JobServiceAlarmListenerForBTIncomming.doWork(context,notify,jobIntent,mSocket,handler,console);

        } catch (IOException e) {
            Log.v("Client: Thread:", e.toString());
            consoleOut("Client: Connection failed! Cause:" + e.toString() + "\n");
            notify.currentConnectionWasinterrupted();
        }
        return;
    }

    /*
     * Cancel
     */
    public void cancel() {
        try {
            mSocket.close();
            consoleOut("Disconected!\n");
        } catch (IOException e) {
            Log.v("Client: thread:", e.toString());
        }
        return;
    }

    /*
     * Output to console
     */
    private void consoleOut(String message) {
        final String m;
        m = message;
        handler.post(new Runnable() {
            @Override
            public void run() {
                console.append(m);
            }
        });
    }
}

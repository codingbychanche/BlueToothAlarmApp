package berthold.bluetoothalarmapp;

/**
 * This listens to incoming data from a bluetooth connection
 * and starts an alarm, if matching data for this condition was send
 * from the bt- device.
 * <p>
 * This achieved by using Android's job service library.
 *
 * @rem: App that runs in the background and notifies the user about incoming bluetooth data@@
 * @rem: When data is received, an alarm is set and executed. This works as long as the app is not closed@@
 * @rem: AlarmService is used to demonstrate how to wake the up the device even when the screen is black@@
 * @rem: JobService example. Device WakeUp example@@
 */

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements ConnectionNotifier {

    // Debug
    String tag;

    // UI
    private static Handler handler;
    private ListView btBondedDevicesListView;
    private ArrayAdapter btBondedAdapter;
    private TextView console;
    private Button cancel;

    // Bluetooth
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    Set<BluetoothDevice> btBondedDevices;

    // Data
    String[] listOfBondedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       handler=new Handler();

        // Debug
        tag=this.getClass().getSimpleName();

        // UI
        btBondedDevicesListView = (ListView) findViewById(R.id.bt_bonded_dev_list);
        console = (TextView) findViewById(R.id.console);
        cancel=findViewById(R.id.cancel);

        // Bluetooth
        // Get bluetooth manager and adapter.
        // If adapter=null => Device does not support bluetooth!
        BluetoothManager bm = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        bluetoothAdapter = bm.getAdapter();
        listOfBondedDevices = null;
        if (bluetoothAdapter != null)
            listOfBondedDevices = fillBtBondedDeviceList();
        else
            listOfBondedDevices[0] = "Keine";

        btBondedAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfBondedDevices);
        btBondedDevicesListView.setAdapter(btBondedAdapter);

        // Check device selected from list of bonded devices and establish a connection.
        btBondedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Iterator bi = btBondedDevices.iterator();
                bluetoothDevice = null;
                for (int i = 0; i <= position; i++) bluetoothDevice = (BluetoothDevice) bi.next();
                if (bluetoothDevice != null) {
                    console.append("Selected device:" + bluetoothDevice.getName().toString() + " Adress:" + bluetoothDevice.getAddress() + "\n\n");

                   connect();
                }
            }
        });

        // Cancel
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JobServiceAlarmListenerForBTIncomming.cancelThisJob();
            }
        });
    }

    public void connect(){
        // Try to establish a bluetooth connection
        BluetoothClientThread bluetoothClientThread = new BluetoothClientThread(getApplicationContext(), this,bluetoothDevice, console, handler);
        bluetoothClientThread.start();
    }

    @Override
    public void currentConnectionWasinterrupted(){
        Log.v (tag,"Connection was interrupted, trying to reconnect");

        handler.post (new Runnable() {
            @Override
            public void run() {
                console.append("Reconnecting\n\n");
            }
        });
        connect();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v("RESUMED","-");
    }

    /*
     * Fill bluetooth device list
     */
    private String[] fillBtBondedDeviceList() {
        StringBuilder deviceList = new StringBuilder();

        btBondedDevices = bluetoothAdapter.getBondedDevices();
        if (btBondedDevices.size() > 0) {
            for (BluetoothDevice dev : btBondedDevices) {
                deviceList.append(dev.getName() + "\n");
                deviceList.append(dev.getAddress() + ",");

            }
        } else deviceList.append("Keine Geräte!\n,");

        String[] listOfBondedDevices = deviceList.toString().split(",");

        return listOfBondedDevices;
    }
}

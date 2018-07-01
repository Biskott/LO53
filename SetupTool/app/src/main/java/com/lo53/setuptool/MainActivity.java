package com.lo53.setuptool;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import static android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP;

/**
 * MainActivity Class :</br>
 * Activity which allows to display the main display et to set calibration request to the sevrer</br>
 * Attributes :</br>
 * public static final String serverAddressLabel : label which defined server address</br>
 * public static final String portLabel : label which defined current port to send HTTP request</br>
 * public static final int PREFERENCES_INTENT : integer used to communicate whith preferencesActivity activity</br>
 * private static String serverAddress : current server address</br>
 * private static String port : port to use for HTTP request</br>
 * private static String macAddress : device mac address
 * private static TestConnection checkConnectionThread : Thread which allows to check the server's disponibility regularly</br>
 * private TextView text : TextView displaying server's disponibility state</br>
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Attributes
     */
    public static final String serverAddressLabel = "SERVER_ADDRESS";
    public static final String portLabel = "PORT";
    public static final int PREFERENCES_INTENT = 3;
    private static String serverAddress = "192.168.1.130";
    private static String port = "80";
    private static String macAddress = "02:00:00:00:00:00";

    private static TestConnection checkConnectionThread;

    private TextView text;

    /**
     * onCreate function
     * Called when activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        text = (TextView) findViewById(R.id.testText);

        int imageRessource = getResources().getIdentifier("@drawable/connexion_button", null, getApplicationContext().getPackageName());
        Drawable connected = getResources().getDrawable(imageRessource);
        imageRessource = getResources().getIdentifier("@drawable/connexion_button_deconnected", null, getApplicationContext().getPackageName());
        Drawable deconnected = getResources().getDrawable(imageRessource);

        TextView connectionStatus = (TextView) findViewById(R.id.connectionStatus);

        // Display of the heading bar
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new GradientDrawable(BOTTOM_TOP, new int[]{0xFFFF8800, 0xFFFFB055}));
        ab.setTitle("Setup Tool");

        macAddress = getMacAddress();

        // Thread creation
        checkConnectionThread = new TestConnection(serverAddress, port, connected, deconnected, connectionStatus);
        checkConnectionThread.start();
    }

    /**
     * getMacAddress function
     * Used to get the device wi-fi adapter's mac address
     * @return mac address of the wi-fi adapter of the device
     */
    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    /**
     * onCreateOptionsMenu function
     * Called when the action bar is created
     * It allows to add items to the action bar (setting button in our case)
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * onOptionsItemSelected function
     * Called when a menu button is selected
     * It allowed to launch preferencesActivity when setting button is pressed
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, preferencesActivity.class);
            intent.putExtra(serverAddressLabel, serverAddress);
            intent.putExtra(portLabel, port);
            startActivityForResult(intent, PREFERENCES_INTENT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * onBackPressed function
     * It allows to manage the application closing when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Quit application")
                .setMessage("Are you sure you want to exit this application ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent();
                        setResult(2,intent);
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }

    /**
     * onActivityResult function
     * It allows to get new server and port values send by the preferencesActivity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PREFERENCES_INTENT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                if(bundle != null){
                    serverAddress = bundle.getString(serverAddressLabel);
                    port = bundle.getString(portLabel);
                    checkConnectionThread.changeParamater(serverAddress, port);
                }
            }
        }
    }

    /**
     * configurationButtonClick function
     * Called when configurationButton is pressed
     * It allows to send a new configuration request to the server
     * @param view
     */
    protected void configurationButtonClick(View view){
        String x = ((TextView) findViewById(R.id.x_value)).getText().toString();
        String y = ((TextView) findViewById(R.id.y_value)).getText().toString();
        Request r = new Request(x,y);
        r.execute();
    }

    /**
     * Request class
     * This class is extended of AsyncTask and allows to send requests to server
     * AsyncTask needed because HTTP requests and responses are not instantaneous
     */
    private class Request extends AsyncTask<Void, Void, Void> {

        private String urlContent = "";
        private String xValue;
        private String yValue;

        public Request(String x, String y){
            xValue = x;
            yValue = y;
        }

        protected void onPreExecute(){
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params){

            try {
                URL url = new URL("http://" + serverAddress + ":" + port + "/calibrate?x=" + xValue
                        + "&y=" + yValue + "&mac=" + macAddress);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    urlContent +=line;
                }
                in.close();

            }catch (Exception e){
                System.out.println("Error in loading response : " + e);
                Toast.makeText(getApplicationContext(), "Error in communication !", Toast.LENGTH_SHORT).show();
                this.cancel(true);
            }
            return null;
        }

        protected void onPostExecute(Void result){

            text.post(new Runnable() {
                @Override
                public void run() {
                    if(urlContent.trim().equals("{\"calibration\": \"ok\"}")){
                        Toast.makeText(MainActivity.this, "Uploading new configuration successfull", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Server problem, try again", Toast.LENGTH_SHORT).show();
                    }
                    text.setText(urlContent);
                }
            });
        }
    }
}

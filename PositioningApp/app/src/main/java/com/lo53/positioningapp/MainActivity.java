package com.lo53.positioningapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP;

public class MainActivity extends AppCompatActivity {

    public static final String serverAddressLabel = "SERVER_ADDRESS";
    public static final String portLabel = "PORT";
    public static final int PREFERENCES_INTENT = 3;
    private static String serverAddress = "192.168.1.130";
    private static String port = "80";
    private static String macAddress = "02:00:00:00:00:00";

    private static TestConnection checkConnectionThread;

    protected static View position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        int imageRessource = getResources().getIdentifier("@drawable/connexion_button", null, getApplicationContext().getPackageName());
        Drawable connected = getResources().getDrawable(imageRessource);
        imageRessource = getResources().getIdentifier("@drawable/connexion_button_deconnected", null, getApplicationContext().getPackageName());
        Drawable deconnected = getResources().getDrawable(imageRessource);

        TextView connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        position = (View)findViewById(R.id.position);

        // Display of the heading bar
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setBackgroundDrawable(new GradientDrawable(BOTTOM_TOP, new int[]{0xFFFF8800, 0xFFFFB055}));
        ab.setTitle("Positioning App");

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
            // Nothing
        }
        return "02:00:00:00:00:00";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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
     * Fonction onBackPressed
     * Permet de gérer la fermeture de l'application lors de l'appuie sur la touche retour
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Fermer l'application")
                .setMessage("Etes-vous sur de vouloir fermer cette application ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent();
                        setResult(2,intent);
                        finish();
                    }
                })
                .setNegativeButton("Non", null)
                .create()
                .show();
    }

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

    protected void onPositionButtonClick(View view){
        Request r = new Request();
        r.execute();

        /*Random r = new Random();
        displayPosition(r.nextFloat()*100, r.nextFloat()*100);*/
    }

    public void displayPosition(float x, float y){

        float maxHeight = convertDpToPixel(400);
        float maxWidth = convertDpToPixel(360);

        int realX = (int) (x*( (maxWidth -3)/100));
        int realY = (int) (y*( (maxHeight -3)/100));

        Toast.makeText(this, "New position : " + Float.toString(x) + "," + Float.toString(y), Toast.LENGTH_SHORT).show();

        position.setX(realX);
        position.setY(realY);

        //TODO : Actualiser la position sur l'affichage et convertir en pos en x pixel
    }

    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    private class Request extends AsyncTask<Void, Void, Void> {

        private String urlContent = "";

        protected void onPreExecute(){
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params){

            try {
                URL url = new URL("http://" + serverAddress + ":" + port + "/locateme?mac=" + macAddress);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    urlContent +=line;
                }
                in.close();

            }catch (Exception e){
                System.out.println("Error in loading response : " + e);
                Toast.makeText(getApplicationContext(), "Error in communication", Toast.LENGTH_LONG).show();
                this.cancel(true);
            }
            return null;
        }

        protected void onPostExecute(Void result){

            try{
                urlContent = urlContent.replace("{\"x\": ", "");
                urlContent = urlContent.replace(" \"y\": ", "");
                urlContent = urlContent.replace("}", "");
                String[] values = urlContent.split(";");
                float x = Float.parseFloat(values[0]);
                float y = Float.parseFloat(values[1]);
                displayPosition(x,y);
                //Toast.makeText(getApplicationContext(), "Position actualised", Toast.LENGTH_SHORT).show();

            }catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

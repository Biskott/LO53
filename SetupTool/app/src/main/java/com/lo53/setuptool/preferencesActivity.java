package com.lo53.setuptool;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP;

/**
 * preferencesActivity activity</br>
 * This activity  allows to change settings (server address and port value)</br>
 * Attributes :</br>
 * private static String serverAddress : actual server address used
 * private static String port : actual port used
 */
public class preferencesActivity extends AppCompatActivity {

    private static String serverAddress = "192.168.0.1";
    private static String port = "5000";

    /**
     * onCreate function
     * Called when activity is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            try{
                serverAddress = bundle.getString(MainActivity.serverAddressLabel);
                port = bundle.getString(MainActivity.portLabel);
                ((EditText)findViewById(R.id.server_address)).setText(serverAddress);
                ((EditText)findViewById(R.id.port)).setText(port);
            }catch (Exception e){
                Toast.makeText(this, "Error in preferences loading", Toast.LENGTH_SHORT).show();
                System.out.println("Error in preferences loading");
            }
        }

        // Display of the heading bar
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setBackgroundDrawable(new GradientDrawable(BOTTOM_TOP, new int[]{0xFFFF8800, 0xFFFFB055}));
        ab.setTitle("Preferences");
    }

    /**
     * onSetButtonClick function
     * Called when SetButton is pressed
     * It allows to save new parameter
     * @param view
     */
    protected void onSetButtonClick(View view){
        EditText newServerAdress = (EditText)findViewById(R.id.server_address);
        EditText newPort = (EditText)findViewById(R.id.port);

        serverAddress = newServerAdress.getText().toString();
        port = newPort.getText().toString();

        Toast.makeText(this, "Parameters saved", Toast.LENGTH_LONG).show();
    }

    /**
     * onBackPressed function
     * Called when back button ispressed
     */
    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra(MainActivity.serverAddressLabel, serverAddress);
        intent.putExtra(MainActivity.portLabel, port);
        setResult(MainActivity.PREFERENCES_INTENT,intent);
        finish();
    }
}

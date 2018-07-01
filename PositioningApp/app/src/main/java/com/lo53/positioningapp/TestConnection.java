package com.lo53.positioningapp;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * TestConnection class :</br>
 * This class is extended of Thread</br>
 * It allows to have a thread checking the server disponibility</br>
 * Attributes :</br>
 * private String serverAddress : actual value of the server address
 * private String port : actual value of the actual port
 * private Drawable connected : Drawable corresponding to the background of connectionStatus when the server is available
 * private Drawable connected : Drawable corresponding to the background of connectionStatus when the server is not available
 * private TextView connectionStatus : TextView which indicates if the server is available
 */
public class TestConnection extends Thread {

    /**
     * Attributes
     */
    private String serverAddress;
    private String port;

    private Drawable connected;
    private Drawable deconnected;
    private TextView connectionStatus;

    /**
     * Class constructor
     * @param serverAddress : actual server address
     * @param port : actual port
     * @param connected : connected background drawable
     * @param deconnected : deconnected backgroung drawable
     * @param connectionStatus : TextView which indicates server availability
     */
    public TestConnection(String serverAddress, String port, Drawable connected, Drawable deconnected, TextView connectionStatus){
        this.serverAddress = serverAddress;
        this.port = port;
        this.connected = connected;
        this.deconnected = deconnected;
        this.connectionStatus = connectionStatus;
    }

    /**
     * run function
     * Used to launch the thread
     * This thread send ping requests to the server every 5 seconds to check the availability
     */
    public void run(){
        while(true){
            ConnectionRequest r = new ConnectionRequest();
            r.execute();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * changeParameter function
     * Called when server and port address change
     * This function actualise this parameters
     * @param serverAddress : actual server address
     * @param port : actual port
     */
    public void changeParamater(String serverAddress, String port){
        this.serverAddress = serverAddress;
        this.port = port;
    }

    /**
     * ConnectionRequest class
     * It is extended of AsyncTask due to the HTTP request (delay of transmission and response)
     * This class allows to send ping request to the server and to actualise the display corresponding to the server availability
     */
    private class ConnectionRequest extends AsyncTask<Void, Void, Void> {

        private String urlContent = "";

        protected void onPreExecute(){
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params){

            try {
                URL url = new URL("http://" + serverAddress + ":" + port + "/ping");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    urlContent +=line;
                }
                in.close();

            }catch (Exception e){
                System.out.println("Error for ping server : " + e);

                connectionStatus.post(new Runnable() {
                    @Override
                    public void run() {
                        connectionStatus.setBackground(deconnected);
                        connectionStatus.setText("Déconnecté");
                    }
                });

                this.cancel(true);
            }
            return null;
        }

        protected void onPostExecute(Void result){

            connectionStatus.post(new Runnable() {
                @Override
                public void run() {
                    if(urlContent.trim().equals("pong")){
                        connectionStatus.setBackground(connected);
                        connectionStatus.setText("Connecté");
                    }
                    else{
                        connectionStatus.setBackground(deconnected);
                        connectionStatus.setText("Déconnecté");
                    }
                }
            });
        }
    }
}

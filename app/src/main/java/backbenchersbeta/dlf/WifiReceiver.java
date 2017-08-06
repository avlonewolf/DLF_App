package backbenchersbeta.dlf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



/*
c8:3a:35:f9:34:38
c8:3a:35:f9:34:30
c8:3a:35:f9:3c:71
*/


/**
 * Created by anirudh on 7/11/15.
 */
public class WifiReceiver extends BroadcastReceiver implements SensorEventListener{

    private WifiManager wifiManager;
    private HashMap<String,Integer> wifiMap;
    private static HashMap<Integer ,Entity> entityMap;
    MainActivity mainActivity;
    private ImageView iv;
    private Bitmap im;
    public String url;
    private String imeiNo;
    private Sensor accelerometer,magneticR;
    private SensorManager sm;
    private String accel = "";
    private String mag = "";


    public WifiReceiver(WifiManager wifiManager, MainActivity mainActivity) {
        this.wifiManager = wifiManager;
        wifiMap = new HashMap<>();
        entityMap = new HashMap<Integer,Entity>();
        this.mainActivity = mainActivity;
        sm = (SensorManager)mainActivity.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener((SensorEventListener) this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        magneticR = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sm.registerListener((SensorEventListener) this,magneticR,SensorManager.SENSOR_DELAY_NORMAL);
        iv = (ImageView)this.mainActivity.findViewById(R.id.mapImageView);
        im = BitmapFactory.decodeResource(this.mainActivity.getResources(), R.drawable.icfinal);
    }

    public static HashMap<Integer, Entity> getEntityMap() {
        return entityMap;
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for(int i=0;i<scanResults.size();i++){
            Log.d("receive", "onReceive");
            ScanResult sc = scanResults.get(i);
            String bssid = sc.BSSID;
            if(!bssid.startsWith("c8:3a:35:f9:"))
                continue;
            wifiMap.put(sc.BSSID,sc.level);
        }

        //Querying server to retrieve position data, ***NEED TO HANDLE EMPTY QUEUE
        Log.d("wifiList", wifiMap.entrySet()+"");
        String query="";
        String mac1="",mac2="",strength1="",strength2="";
        String id = "";
        Pair<String,Integer> maxPair = popMax(wifiMap);
        mac1 = maxPair.first;
        strength1 = maxPair.second+"";
        maxPair = popMax(wifiMap);
        mac2 = maxPair.first;
        strength2 = maxPair.second+"";
        String charset="UTF-8";
        TelephonyManager TM = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        imeiNo = TM.getDeviceId();
        imeiNo = imeiNo.substring(7);
        id = imeiNo+"";

        try {
            query = String.format("id=%s&mac1=%s&mac2=%s&strength1=%s&strength2=%s&accel=%s&mag=%s",
                    URLEncoder.encode(id, charset),
                    URLEncoder.encode(mac1, charset),
                    URLEncoder.encode(mac2, charset),
                    URLEncoder.encode(strength1, charset),
                    URLEncoder.encode(strength2, charset),
                    URLEncoder.encode(accel, charset),
                    URLEncoder.encode(mag, charset));
        }
        catch (UnsupportedEncodingException uce){
            uce.printStackTrace();
        }

        Log.d("query", "onReceive: " + query);
        //EditText urlText = (EditText)mainActivity.findViewById(R.id.urlText);
        url = "http://192.168.0.128:8000/test?" + query;
        new AsyncCaller().execute();

    }


    protected void fillEntityMap(String jsonStr){

        if (jsonStr != null) {
            try {
                JSONArray entries = (new JSONObject(jsonStr)).getJSONArray("entries");
                for (int i = 0; i < entries.length(); i++) {

                    JSONObject content = entries.getJSONObject(i);
                    JSONObject position = content.getJSONObject("position");

                        entityMap.put(content.getInt("id"), new Entity(
                                content.getInt("id"),
                                content.getInt("type"),
                                position.getInt("x"),
                                position.getInt("y"),
                                position.getInt("z")
                        ));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
        }
    }

    protected void fillCanvas(){
        Bitmap tempBitmap = Bitmap.createBitmap(im.getWidth(), im.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(im, 0, 0, null);
        Paint myPaint = new Paint();

        Iterator it = entityMap.entrySet().iterator();
        Entity tempEntity;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            tempEntity = (Entity)pair.getValue();

            switch (tempEntity.getType()){
                case 0:myPaint.setColor(Color.BLUE);break;
                case 1:break;
                case 2:break;
                case 3:myPaint.setColor(Color.RED);break;
            }
            tempCanvas.drawCircle(tempEntity.getX(), tempEntity.getY(), 15, myPaint);
            myPaint.setStyle(Paint.Style.STROKE);
            tempCanvas.drawCircle(tempEntity.getX(), tempEntity.getY(), 20, myPaint);
            myPaint.setStyle(Paint.Style.FILL);
            tempCanvas.drawLine (0,0, 15, 28,myPaint);

        }

        iv.setImageDrawable(new BitmapDrawable(mainActivity.getResources(), tempBitmap));

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType())
        {
            case Sensor.TYPE_ACCELEROMETER: accel = event.values[0] + ":" + event.values[1] + ":" + event.values[2] + "";
                                            break;

            case Sensor.TYPE_MAGNETIC_FIELD: mag = event.values[0] + ":" + event.values[1] + ":" + event.values[2] + "";
                                             break;

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class AsyncCaller extends AsyncTask<Void, Void, Void>
    {
        private String jsonStr;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI from here
            //do your long running http tasks here,you don't want to pass argument and u can access the parent class' variable url over here
            try {
                URL website = new URL(url);
                Log.d("After before", "getJsonString ");
                URLConnection connection = website.openConnection();
                Log.d("After open", "getJsonString ");
                connection.setRequestProperty("Accept-Charset", "UTF-8");
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null)
                    response.append(inputLine);

                in.close();
                jsonStr = response.toString();
                Log.d("response", "doInBackground "+jsonStr);
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d("Response: ", "> " + jsonStr);
            fillEntityMap(jsonStr);
            fillCanvas();            //this method will be running on UI thread

        }

    }

    private Pair<String,Integer> popMax(HashMap<String,Integer> wifiLevel){
        int maxLevel=-1000;
        String maxKey="";
        for (String key: wifiLevel.keySet()) {
            if(maxLevel < wifiLevel.get(key)){
                maxKey = key;
                maxLevel = wifiLevel.get(key);
            }
            Log.d("level", "popMax "+maxKey+"");
        }
        if(maxKey!="")
            wifiLevel.put(maxKey,-1000);
        return new Pair<>(maxKey,maxLevel);
    }

}

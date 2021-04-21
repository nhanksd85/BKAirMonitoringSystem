package hello.world.fpt;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.PendingIntent;
import android.app.UiAutomation;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.scottyab.aescrypt.AESCrypt;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.http.RealResponseBody;
import com.squareup.okhttp.internal.spdy.Header;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import hello.world.fpt.Models.CalibModel;
import okio.GzipSource;
import okio.Okio;

public class MainActivity extends Activity implements SerialInputOutputManager.Listener  {

    final String TAG = "MAIN_TAG";
    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    UsbSerialPort port;


    TextView txtPM_1_0, txtPM_2_5, txtPM_10, txtCO2, txtCO, txtHCHO;
    TextView txtTemp, txtHumi, txtLocation;

    Button btnSetting, btnExit;

    private void initUI(){
        txtPM_1_0   = findViewById(R.id.txtPM_1_0);
        txtPM_2_5   = findViewById(R.id.txtPM_2_5);
        txtPM_10    = findViewById(R.id.txtPM_10);
        txtCO2      = findViewById(R.id.txtCO2);
        txtCO       = findViewById(R.id.txtCO);
        txtHCHO     = findViewById(R.id.txtHCHO);

        txtTemp     = findViewById(R.id.txtTemp);
        txtHumi     = findViewById(R.id.txtHumi);
        txtLocation = findViewById(R.id.txtLocation);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initUiFlags();
        goFullscreen();
        openUART();

        btnSetting = findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAppFromPackageName("com.android.settings");
            }
        });

        btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAppFromPackageName("com.android.rockchip");
            }
        });

        //Log.d(TAG, AES256("30,41,15,23,23,1043,0,0"));
        //postHuRuKuServer("30,41,15,23,23,1043,0,0");
        //Log.d(TAG, bytesToHex(AES128("ABCDEFGHIJKLMNOPABCDEFGHIJKLMNOP")));

        //txtLocation.setText(bytesToHex(DecodeAES128(AES128("ABCDEFGHIJKLMNOPABCDEFGHIJKLMNOP"))));

        //Log.d(TAG,bytesToHex(DecodeAES128(test_64_byte)));

        txtLocation.setText(bytesToHex(DecodeAES128(test_64_byte)));
        setup_reset_timer();
        setup_calib_timer();
        //testConvert();
    }

    CalibModel[] mCalibSensors;

    public void launchAppFromPackageName(String packageName)
    {
        //Launch an application from package name
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            startActivity(launchIntent);
        }
    }


    private void openUART(){
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);

        if (availableDrivers.isEmpty()) {
            Log.d("UART", "UART is not available");

        }else {
            Log.d("UART", "UART is available");

            UsbSerialDriver driver = availableDrivers.get(0);
            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {

                PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
                manager.requestPermission(driver.getDevice(), usbPermissionIntent);

                manager.requestPermission(driver.getDevice(), PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));


                return;
            } else {

                port = driver.getPorts().get(0);
                try {
                    port.open(connection);
                    //port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                    port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    //port.write("ABC#".getBytes(), 1000);

                    SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, this);
                    Executors.newSingleThreadExecutor().submit(usbIoManager);
                    Log.d("UART", "UART is openned");

                } catch (Exception e) {
                    Log.d("UART", "There is error");
                }
            }
        }

    }




    private void sendDataToThingSpeak(int temp, int humi){
        String url ="https://api.thingspeak.com/update?api_key=5GWU7UTD50GU8T0O&field1="+ temp + "&field2=" + humi;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("ABC", "Request is fail");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d("ABC", "Request is successful");
                response.body().string();
            }
        });
    }

    private void getDataFromThingSpeak(int number_of_points){
        String url = "https://api.thingspeak.com/channels/1352683/feeds.json?results=" + number_of_points;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("ABC", "Request is fail");
            }

            @Override
            public void onResponse(Response response) throws IOException {

                String data = response.body().string();
                //TODO

                Log.d("ABC", data);
            }
        });
    }


    Timer aTimer;
    private int timer_counter = 30;
    private void setup_reset_timer(){
        aTimer = new Timer();
        TimerTask aTask = new TimerTask() {
            @Override
            public void run() {
                if(timer_counter > 0){
                    timer_counter--;
                }else{
                    //reset_app();
                }
            }
        };
        aTimer.schedule(aTask, 1000,1000);
    }


    Timer aCalibTimer;
    private int calib_counter = 60;
    private void setup_calib_timer(){
        aCalibTimer = new Timer();
        TimerTask aTask = new TimerTask() {
            @Override
            public void run() {
                if(calib_counter > 0){
                    calib_counter--;
                }else{
                    calib_counter = 60;
                    request_calib_data();
                }
            }
        };
        aTimer.schedule(aTask, 1000,1000);
    }
    private void request_calib_data(){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        String url = "https://ubc.sgp1.cdn.digitaloceanspaces.com/BK_AIR/calib_air_sensor.txt";


        Request request = builder.url(url).build();



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d(TAG,"Testing fail");
            }

            @Override
            public void onResponse(Response response) throws IOException {

                String data = response.body().string();
                mCalibSensors = new Gson().fromJson(data, CalibModel[].class);
                Log.d(TAG, "Number of sensors: " + mCalibSensors.length);
            }
        });
    }


    byte[] test_64_byte = new byte[]{(byte)0xC3, (byte)0xA4, (byte)0x95,(byte)0x85
            ,(byte)0x8F, (byte)0xB7,(byte)0x0B,(byte)0xDE ,(byte)0x87 ,(byte)0x70 ,(byte)0xB0 ,(byte)0x72 ,(byte)0x4E
            ,(byte)0x00 ,(byte)0x7D ,(byte)0xF6 ,(byte)0x5B ,(byte)0x9D ,(byte)0x0C ,(byte)0xA7 ,(byte)0x36 ,(byte)0x0F ,(byte)0xA2 ,(byte)0x44 ,(byte)0x2F ,(byte)0xF4 ,(byte)0x74
            ,(byte)0x71 ,(byte)0xF4 ,(byte)0x26 ,(byte)0x77 ,(byte)0x30 ,(byte)0xE8 ,(byte)0xC1 ,(byte)0x35 ,(byte)0x15 ,(byte)0x37 ,(byte)0xA2 ,(byte)0x2C ,(byte)0xFD ,(byte)0xE2
            ,(byte)0x8B ,(byte)0xF2 ,(byte)0x97 ,(byte)0xF4 ,(byte)0xE1 ,(byte)0x24 ,(byte)0x2D ,(byte)0xE8 ,(byte)0xC1 ,(byte)0x35 ,(byte)0x15 ,(byte)0x37 ,(byte)0xA2 ,(byte)0x2C
            ,(byte)0xFD ,(byte)0xE2 ,(byte)0x8B ,(byte)0xF2 ,(byte)0x97 ,(byte)0xF4 ,(byte)0xE1 ,(byte)0x24 ,(byte)0x2D};

    private void reset_app(){
        android.os.Process.killProcess(android.os.Process.myPid());
    }





    private void sendDataToRapido(final String[] data){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtTemp.setText(data[0] + "");
                txtHumi.setText(data[1] + "");
                txtPM_1_0.setText(data[2] + "");
                txtPM_2_5.setText(data[3] + "");
                txtPM_10.setText(data[4] + "");
                txtCO2.setText(data[5] + "");
                txtCO.setText(data[6] + "");
                txtHCHO.setText(data[7] + "");
            }
        });


        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();

        String url = "https://rapido.npnlab.com/api/rapido/push?station_id=" + "9" +

                "&sensors[0].id=1&sensors[0].value=SEN_TDS" +
                "&sensors[1].id=1005&sensors[1].value=SEN_DHT11_TEMP" +
                "&sensors[2].id=1006&sensors[2].value=SEN_DHT11_HUMI" +


                "&sensors[3].id=1010&sensors[3].value=SEN_PM1_0" +
                "&sensors[4].id=1011&sensors[4].value=SEN_PM2_5" +
                "&sensors[5].id=1012&sensors[5].value=SEN_PM_10" +

                "&sensors[6].id=1014&sensors[6].value=SEN_CO2_PPM" +

                "&sensors[7].id=1016&sensors[7].value=SEN_CO_PPM" +

                "&sensors[8].id=1017&sensors[8].value=SEN_HCHO"+
                "&sensors[9].id=1019&sensors[9].value=SEN_HDS";



        url = url.replaceAll("SEN_TDS", data[0]);
        url = url.replaceAll("SEN_HDS", data[1]);
        url = url.replaceAll("SEN_DHT11_TEMP", data[0]);
        url = url.replaceAll("SEN_DHT11_HUMI", data[1]);


        url = url.replaceAll("SEN_PM1_0", data[2]);
        url = url.replaceAll("SEN_PM2_5", data[3]);
        url = url.replaceAll("SEN_PM_10", data[4]);

        url = url.replaceAll("SEN_CO2_PPM", data[5]);

        url = url.replaceAll("SEN_CO_PPM", data[6]);
        url = url.replaceAll("SEN_HCHO", data[7]);
        Log.d("ABC", url);

        Request request = builder.url(url).build();



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("ABC","Testing fail");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d("ABC","Server response " + response.body().string());

            }
        });
    }

    private void sendHuRuKuServer(String data){
        String url = "http://hazard-monitoring-system.herokuapp.com/api/1.0/getData?gatewayID=2&sensorID=2&sensorValue=["
        + data + "]&latlon=[10.23,106.76]";
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();

        Request request = builder.url(url).build();



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("ABC","Testing fail");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                Log.d("ABC","Server response " + response.body().string());

            }
        });

    }

    public void postHuRuKuServer(final String ID_Sensor, final String dataSensor) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String encodeData = AES256(dataSensor).replace("\n","");

                    URL url = new URL("https://hazard-monitoring-system.herokuapp.com/api/1.0/msg");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    //conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());

                    String data = "{\n" +
                            "    \"gatewayID\": 4,\n" +
                            "    \"sensorID\" : " + ID_Sensor + ",\n" +
                            "    \"lat\" : 0.0,\n" +
                            "    \"long\": 0.0,\n" +
                            "    \"sensorValue\": \"" + encodeData + "\"\n" +
                            "}";


                    os.writeBytes(data);

                    os.flush();
                    os.close();

                    Log.i(TAG, "SERVER RESPONSE: " + String.valueOf(conn.getResponseCode()));
                    Log.i(TAG , "SERVER RESPONSE: " + conn.getResponseMessage());

                    conn.disconnect();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    String buffer  = "";

    byte[] test_byte = {0x01, 0x02, 0x03, 0x04};
    private void testConvert(){
        String test_str = new String(test_byte);
        Log.d(TAG, test_str);
        byte[] hello = test_str.getBytes();
        Log.d(TAG, hello[0] + " " + hello[1] + hello[2] + hello[3]);
    }


    void processBuffer3(){
        int begin_index = -1;
        int end_index = 0;
        for(end_index = index_byte - 1; end_index>=2; end_index--){
            if(arr_byte[end_index] == (byte)0x0a && arr_byte[end_index-1] == (byte)0x0d && arr_byte[end_index - 2] == 0x21){
                Log.d(TAG, "Find an end point " + end_index);
                break;
            }
        }
        if(end_index >= 10){
            for(begin_index = end_index - 3; begin_index>=2; begin_index--){
                if(arr_byte[begin_index] == 0x23 && arr_byte[begin_index - 1] == 0x23 && arr_byte[begin_index -2] == 0x23  ){
                    Log.d(TAG, "Find an start point " + begin_index);
                    break;
                }
            }
        }
        if(end_index > begin_index && begin_index >=0){
            index_byte -= end_index;
            byte[] encryptData = new byte[64];
            for(int i = 0; i < 64; i++){
                encryptData[i] = arr_byte[i + begin_index + 1];
            }
            String decodeData = new String(DecodeAES128(encryptData)).replaceAll("A","");


            String[] splitData = decodeData.split(Pattern.quote(","));
            final String ID = splitData[0];
            Log.d(TAG, "ID: " + ID);
            decodeData = decodeData.substring(ID.length() + 1);
            Log.d(TAG, "Sensory data: "  + decodeData);
            postHuRuKuServer(ID , decodeData);
            sendDataToRapido(decodeData.split(Pattern.quote(",")));
            final String abc = decodeData;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtLocation.setText(ID + "**" + abc);
                }
            });
        }
    }


    void processBuffer2(){
        if(buffer.contains("#") && buffer.contains("!")){
            int beginIndex = buffer.indexOf("#");
            int endIndex = buffer.indexOf("!");
            if(beginIndex >=0 && endIndex > beginIndex){
                String data = buffer.substring(beginIndex + 1, endIndex);
                Log.d("ABC", "Extract: "  + data);
                Log.d("ABC", "Len: "  + data.getBytes().length);
                if(data.length() == 64) {
                    String decodeData = new String(DecodeAES128(data.getBytes()));

                    Log.d("ABC", "Decode: " + decodeData);
                }
            }
            buffer = "";
        }
    }

    void processBuffer(){
        //TODO
        String test = "#27;58;3;593$";
        Pattern sensory_data = Pattern.compile("\\#(.+)\\!");
        Matcher m = sensory_data.matcher(new String(buffer));



        while (m.find() == true){
            String data = m.group(1);
            Log.d("ABC", "Process" + data);

//            final String decodeData = new String(DecodeAES128(data.getBytes())).replaceAll("41","");
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    txtLocation.setText(decodeData);
//                }
//            });
//            decodeData = decodeData.replaceAll("A","");
//            Log.d(TAG, "Decode: " + decodeData);
//
//            String[] splitData = decodeData.split(Pattern.quote(","));
//            String ID = splitData[0];
//            Log.d(TAG, "ID: " + ID);
//            decodeData = decodeData.substring(ID.length());
//            Log.d(TAG, "Sensory data: "  + decodeData);

            //sendDataToRapido(data.split(Pattern.quote(",")));
            //sendHuRuKuServer(data);
            //postHuRuKuServer(data);
            buffer = "";
        }
    }

    byte[] arr_byte = new byte[1024];
    int index_byte = 0;
    @Override
    public void onNewData(final byte[] data) {
//        buffer += new String(data);
//        Log.d("ABC", new String(data));
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                //txtLocation.setText(new String(data));
//            }
//        });



        timer_counter = 30;

        //processBuffer();

        for(int i = 0; i < data.length; i++){
            arr_byte[index_byte++] = data[i];
            if(index_byte == 1024) index_byte = 0;
        }

        //Log.d("ABC","Receive: " + data.length);
        //Log.d("ABC", "Buffer lengh" + index_byte);
        processBuffer3();
    }

    @Override
    public void onRunError(Exception e) {

    }

    public void goFullscreen() {
        //setUiFlags(true);
        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiOptions |= View.STATUS_BAR_HIDDEN;

        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public void enableWifi() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled() == false) //  getWifiState() == WifiManager.WIFI_STATE_DISABLED)
                wifiManager.setWifiEnabled(true);
        } catch (Exception e) {
        }
    }

    public void exitFullscreen() {

        setUiFlags(false);

    }

    /**
     * Correctly sets up the fullscreen flags to avoid popping when we switch
     * between fullscreen and not
     */
    private void initUiFlags() {
        int flags = View.SYSTEM_UI_FLAG_VISIBLE;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        View decorView = getWindow().getDecorView();
        if (decorView != null) {
            decorView.setSystemUiVisibility(flags);
            //decorView.setOnSystemUiVisibilityChangeListener(fullScreenListener);
        }
    }

    /**
     * Applies the correct flags to the windows decor view to enter
     * or exit fullscreen mode
     *
     * @param fullscreen True if entering fullscreen mode
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setUiFlags(boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            View decorView = getWindow().getDecorView();
            if (decorView != null) {
                decorView.setSystemUiVisibility(fullscreen ? getFullscreenUiFlags() : View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    /**
     * Determines the appropriate fullscreen flags based on the
     * systems API version.
     *
     * @return The appropriate decor view flags to enter fullscreen mode when supported
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int getFullscreenUiFlags() {
        int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        return flags;
    }




    private  String AES256(String data){
        try {

            byte[] key = {-23,-18,-59,-75,-34,123,-109,-8,29,-62,-60,4,-22,-74,-34,7,46,-52,29,-109,-3,70,69,113,98,80,-34,98,120,6,-47,-112};
            byte[] input = data.getBytes();
            byte[] output = null;

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            output = cipher.doFinal(input);

            String encoded = Base64.encodeToString(output, Base64.DEFAULT);
            return encoded;

        } catch (Exception ex) {
            return "";
        }
    }

    private  byte[] AES128(String data){
        try {

            byte[] key = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53};
            byte[] input = data.getBytes();
            byte[] output = null;

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            //Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            //Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            output = cipher.doFinal(input);
            return output;
            //return bytesToHex(output);

        } catch (Exception ex) {
            return null;
        }
    }
    private  byte[] DecodeAES128(byte[] data){
        try {

            byte[] key = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53};
            byte[] input = data;
            byte[] output = null;

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            //Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            output = cipher.doFinal(input);

            return  output;

        } catch (Exception ex) {
            Log.d(TAG, "Error: " + ex.toString());
            return null;
        }
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}

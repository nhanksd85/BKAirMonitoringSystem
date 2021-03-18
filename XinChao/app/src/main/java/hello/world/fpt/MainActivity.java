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

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import okio.GzipSource;
import okio.Okio;

public class MainActivity extends Activity implements SerialInputOutputManager.Listener  {

    final String TAG = "MAIN_TAG";
    private static final String ACTION_USB_PERMISSION = "com.android.recipes.USB_PERMISSION";
    private static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    UsbSerialPort port;


    TextView txtPM_1_0, txtPM_2_5, txtPM_10, txtCO2, txtCO, txtHCHO;
    TextView txtTemp, txtHumi, txtLocation;



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
    }




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
                    port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                    port.write("ABC#".getBytes(), 1000);

                    SerialInputOutputManager usbIoManager = new SerialInputOutputManager(port, this);
                    Executors.newSingleThreadExecutor().submit(usbIoManager);
                    Log.d("UART", "UART is openned");

                } catch (Exception e) {
                    Log.d("UART", "There is error");
                }
            }
        }

    }


    private void sendDataToRapido(String[] data){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();

        String url = "https://rapido.npnlab.com/api/rapido/push?station_id=" + "9" +
                "&sensors[0].id=1010&sensors[0].value=SEN_PM1_0" +
                "&sensors[1].id=1011&sensors[1].value=SEN_PM2_5" +
                "&sensors[2].id=1012&sensors[2].value=SEN_PM_10" +
                "&sensors[3].id=1013&sensors[3].value=SEN_CO2_PWM" +
                "&sensors[4].id=1014&sensors[4].value=SEN_CO2_PPM" +
                "&sensors[5].id=1015&sensors[5].value=SEN_CO_PWM" +
                "&sensors[6].id=1016&sensors[6].value=SEN_CO_PPM";

        url = url.replaceAll("SEN_PM1_0", data[0]);
        url = url.replaceAll("SEN_PM2_5", data[1]);
        url = url.replaceAll("SEN_PM_10", data[2]);
        url = url.replaceAll("SEN_CO2_PWM", data[3]);
        url = url.replaceAll("SEN_CO2_PPM", data[4]);
        url = url.replaceAll("SEN_CO_PWM", data[5]);
        url = url.replaceAll("SEN_CO_PPM", data[6]);
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



    String buffer  = "";
    void processBuffer(){
        //TODO
        String test = "#27;58;3;593$";
        Pattern sensory_data = Pattern.compile("\\#(.+)\\!");
        Matcher m = sensory_data.matcher(new String(buffer));
        while (m.find() == true){
//            String data = m.group(1);
//            Log.d("ABC", data);
//            if(counter >= 15 * 60) {
//                sendDataToRapido(data.split(Pattern.quote(",")));
//                counter = 0;
//            }
//            buffer = "";
        }
    }

    @Override
    public void onNewData(byte[] data) {
        buffer += new String(data);
        processBuffer();
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



}

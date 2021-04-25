package hello.world.fpt;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends Activity {

    EditText txtPM1_0_Min, txtPM1_0_Max;
    EditText txtPM2_5_Min, txtPM2_5_Max;
    EditText txtPM10_Min, txtPM10_Max;
    EditText txtCO2_Min, txtCO2_Max;
    EditText txtCO_Min, txtCO_Max;
    EditText txtHCHO_Min, txtHCHO_Max;
    EditText txtID_Rapido;
    EditText txtID_Sensor;

    Button btnBack, btnSetting;

    public void launchAppFromPackageName(String packageName)
    {
        //Launch an application from package name
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(launchIntent);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        txtPM1_0_Min = findViewById(R.id.txtPM1_0_Min);
        txtPM1_0_Max = findViewById(R.id.txtPM1_0_Max);

        txtPM2_5_Min = findViewById(R.id.txtPM2_5_Min);
        txtPM2_5_Max = findViewById(R.id.txtPM2_5_Max);

        txtPM10_Min = findViewById(R.id.txtPM10_Min);
        txtPM10_Max = findViewById(R.id.txtPM10_Max);

        txtCO2_Min = findViewById(R.id.txtCO2_Min);
        txtCO2_Max = findViewById(R.id.txtCO2_Max);

        txtCO_Min = findViewById(R.id.txtCO_Min);
        txtCO_Max = findViewById(R.id.txtCO_Max);

        txtHCHO_Min = findViewById(R.id.txtHCHO_Min);
        txtHCHO_Max = findViewById(R.id.txtHCHO_Max);

        txtID_Rapido = findViewById(R.id.txtIDStation);
        txtID_Sensor = findViewById(R.id.txtIDSensor);

        txtPM1_0_Min.setText(NPNConstants.SETTING_PM1_0_MIN + "");
        txtPM1_0_Max.setText(NPNConstants.SETTING_PM1_0_MAX + "");
        txtPM2_5_Min.setText(NPNConstants.SETTING_PM2_5_MIN + "");
        txtPM2_5_Max.setText(NPNConstants.SETTING_PM2_5_MAX + "");
        txtPM10_Min.setText(NPNConstants.SETTING_PM10_MIN + "");
        txtPM10_Max.setText(NPNConstants.SETTING_PM10_MAX + "");


        txtCO2_Min.setText(NPNConstants.SETTING_CO2_MIN + "");
        txtCO2_Max.setText(NPNConstants.SETTING_CO2_MAX + "");
        txtCO_Min.setText(NPNConstants.SETTING_CO_MIN + "");
        txtCO_Max.setText(NPNConstants.SETTING_CO_MAX + "");
        txtHCHO_Min.setText(NPNConstants.SETTING_HCHO_MIN + "");
        txtHCHO_Max.setText(NPNConstants.SETTING_HCHO_MAX + "");

        txtID_Rapido.setText(NPNConstants.SETTING_ID_STATION + "");
        txtID_Sensor.setText(NPNConstants.SETTING_ID_SENSOR + "");


        btnSetting = findViewById(R.id.btnNetworkSetting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAppFromPackageName("com.npn.wifi.setting");
            }
        });

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_PM1_0_MIN, txtPM1_0_Min.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_PM1_0_MAX, txtPM1_0_Max.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_PM2_5_MIN, txtPM2_5_Min.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_PM2_5_MAX, txtPM2_5_Max.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_PM10_MIN, txtPM10_Min.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_PM10_MAX, txtPM10_Max.getText().toString());

                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_CO2_MIN, txtCO2_Min.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_CO2_MAX, txtCO2_Max.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_CO_MIN, txtCO_Min.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_CO_MAX, txtCO_Max.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_HCHO_MIN, txtHCHO_Min.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_HCHO_MAX, txtHCHO_Max.getText().toString());


                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_ID_STATION, txtID_Rapido.getText().toString());
                Ultis.saveKey(SettingActivity.this,NPNConstants.SETTING_KEY_ID_SENSOR, txtID_Sensor.getText().toString());


                NPNConstants.SETTING_PM1_0_MIN = Integer.parseInt(txtPM1_0_Min.getText().toString());
                NPNConstants.SETTING_PM1_0_MAX = Integer.parseInt(txtPM1_0_Max.getText().toString());
                NPNConstants.SETTING_PM2_5_MIN = Integer.parseInt(txtPM2_5_Min.getText().toString());
                NPNConstants.SETTING_PM2_5_MAX = Integer.parseInt(txtPM2_5_Max.getText().toString());
                NPNConstants.SETTING_PM10_MIN = Integer.parseInt(txtPM10_Min.getText().toString());
                NPNConstants.SETTING_PM10_MAX = Integer.parseInt(txtPM10_Max.getText().toString());

                NPNConstants.SETTING_CO2_MIN = Integer.parseInt(txtCO2_Min.getText().toString());
                NPNConstants.SETTING_CO2_MAX = Integer.parseInt(txtCO2_Max.getText().toString());
                NPNConstants.SETTING_CO_MIN = Integer.parseInt(txtCO_Min.getText().toString());
                NPNConstants.SETTING_CO_MAX = Integer.parseInt(txtCO_Max.getText().toString());
                NPNConstants.SETTING_HCHO_MIN = Integer.parseInt(txtHCHO_Min.getText().toString());
                NPNConstants.SETTING_HCHO_MAX = Integer.parseInt(txtHCHO_Max.getText().toString());


                NPNConstants.SETTING_ID_STATION = Integer.parseInt(txtID_Rapido.getText().toString());
                NPNConstants.SETTING_ID_SENSOR = Integer.parseInt(txtID_Sensor.getText().toString());


                finish();
            }
        });

    }
}

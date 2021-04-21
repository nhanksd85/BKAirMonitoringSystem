package hello.world.fpt.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by USER on 4/20/2021.
 */

public class CalibModel {
    @SerializedName("ID")
    private int ID;

    @SerializedName("TEMP")
    private float TEMP;

    @SerializedName("HUMI")
    private float HUMI;

    @SerializedName("PM1_0")
    private float PM1_0;

    @SerializedName("PM2_5")
    private float PM2_5;

    @SerializedName("PM10")
    private float PM10;

    @SerializedName("CO2")
    private float CO2;

    @SerializedName("CO")
    private float CO;

    @SerializedName("HCHO")
    private float HCHO;


    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public float getTEMP() {
        return TEMP;
    }

    public void setTEMP(float TEMP) {
        this.TEMP = TEMP;
    }

    public float getHUMI() {
        return HUMI;
    }

    public void setHUMI(float HUMI) {
        this.HUMI = HUMI;
    }

    public float getPM1_0() {
        return PM1_0;
    }

    public void setPM1_0(float PM1_0) {
        this.PM1_0 = PM1_0;
    }

    public float getPM2_5() {
        return PM2_5;
    }

    public void setPM2_5(float PM2_5) {
        this.PM2_5 = PM2_5;
    }

    public float getPM10() {
        return PM10;
    }

    public void setPM10(float PM10) {
        this.PM10 = PM10;
    }

    public float getCO2() {
        return CO2;
    }

    public void setCO2(float CO2) {
        this.CO2 = CO2;
    }

    public float getCO() {
        return CO;
    }

    public void setCO(float CO) {
        this.CO = CO;
    }

    public float getHCHO() {
        return HCHO;
    }

    public void setHCHO(float HCHO) {
        this.HCHO = HCHO;
    }
}

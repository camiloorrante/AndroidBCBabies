package com.neoris.bcbabies;

public class Registrant {
    /*Datos generales*/
    private String step1_hospital;
    private String step1_doctor;
    private String step1_date;
    private String step1_hour;
    private String step1_country;
    private String step1_address;
    /*Datos del bebe*/
    private String step2_newBornName;
    private Integer step2_gender;
    private String step2_newbornFinger;
    /*Datos de la madre*/
    private String step3_motherName;
    private String step3_motherFinger;
    private String step3_motherIneFrontB64;
    private String step3_motherIneBackB64;
    /*Datos del padre*/
    private String step4_fatherName;
    private String step4_fatherFinger;
    private String step4_fatherIneFrontB64;
    private String step4_fatherIneBackB64;


    //region getters and setters datos grales
    public String getStep1_hospital() {
        return step1_hospital;
    }

    public void setStep1_hospital(String step1_hospital) {
        this.step1_hospital = step1_hospital;
    }

    public String getStep1_doctor() {
        return step1_doctor;
    }

    public void setStep1_doctor(String step1_doctor) {
        this.step1_doctor = step1_doctor;
    }

    public String getStep1_date() {
        return step1_date;
    }

    public void setStep1_date(String step1_date) {
        this.step1_date = step1_date;
    }

    public String getStep1_hour() {
        return step1_hour;
    }

    public void setStep1_hour(String step1_hour) {
        this.step1_hour = step1_hour;
    }

    public String getStep1_country() {
        return step1_country;
    }

    public void setStep1_country(String step1_country) {
        this.step1_country = step1_country;
    }

    public String getStep1_address() {
        return step1_address;
    }

    public void setStep1_address(String step1_address) {
        this.step1_address = step1_address;
    }

    //endregion

    //region getters and setters datos bebe
    public String getStep2_newBornName() {
        return step2_newBornName;
    }

    public void setStep2_newBornName(String step2_newBornName) {
        this.step2_newBornName = step2_newBornName;
    }
    public Integer getStep2_gender() {
        return step2_gender;
    }

    public void setStep2_gender(Integer step2_gender) {
        this.step2_gender = step2_gender;
    }

    public String getStep2_newbornFinger() {
        return step2_newbornFinger;
    }

    public void setStep2_newbornFinger(String step2_newbornFinger) {
        this.step2_newbornFinger = step2_newbornFinger;
    }
    //endregion

    //region getters and setters datos madre
    public String getStep3_motherName() {
        return step3_motherName;
    }

    public void setStep3_motherName(String step3_motherName) {
        this.step3_motherName = step3_motherName;
    }

    public void setStep3_motherFinger(String step3_motherFinger) {
        this.step3_motherFinger = step3_motherFinger;
    }

    public String getStep3_motherFinger() {
        return step3_motherFinger;
    }

    public String getStep3_motherIneFrontB64() {
        return step3_motherIneFrontB64;
    }

    public void setStep3_motherIneFrontB64(String step3_motherIneFrontB64) {
        this.step3_motherIneFrontB64 = step3_motherIneFrontB64;
    }

    public String getStep3_motherIneBackB64() {
        return step3_motherIneBackB64;
    }

    public void setStep3_motherIneBackB64(String step3_motherIneBackB64) {
        this.step3_motherIneBackB64 = step3_motherIneBackB64;
    }

    //endregion

    //region getter and setters datos padre
    public String getStep4_fatherName() {
        return step4_fatherName;
    }

    public void setStep4_fatherName(String step4_fatherName) {
        this.step4_fatherName = step4_fatherName;
    }
    public String getStep4_fatherFinger() {
        return step4_fatherFinger;
    }
    public void setStep4_fatherFinger(String step4_fatherFinger) {
        this.step4_fatherFinger = step4_fatherFinger;
    }

    public String getStep4_fatherIneFrontB64() {
        return step4_fatherIneFrontB64;
    }

    public void setStep4_fatherIneFrontB64(String step4_fatherIneFrontB64) {
        this.step4_fatherIneFrontB64 = step4_fatherIneFrontB64;
    }
    public String getStep4_fatherIneBackB64() {
        return step4_fatherIneBackB64;
    }

    public void setStep4_fatherIneBackB64(String step4_fatherIneBackB64) {
        this.step4_fatherIneBackB64 = step4_fatherIneBackB64;
    }
    //endregion
}

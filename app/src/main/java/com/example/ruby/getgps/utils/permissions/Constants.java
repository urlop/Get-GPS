package com.example.ruby.getgps.utils.permissions;

import android.Manifest;
import android.os.Environment;

import java.util.HashMap;

/**
 * Created by rubymobile on 8/1/16.
 */
public interface Constants {
    int REQUEST_CODE_ASK_PERMISSIONS = 101;
    int REQUEST_PERMISSION_SETTING = 102;
    String OK_BUTTON = "Ok";
    String CANCEL_BUTTON = "Cancel";

    HashMap<String, String> PERMISSIONS_MAP = new HashMap() {
        {
            put(Manifest.permission.READ_CALENDAR, "Read Calendar");
            put(Manifest.permission.WRITE_CALENDAR, "Write Calendar");
            put(Manifest.permission.CAMERA, "Camera");
            put(Manifest.permission.READ_CONTACTS, "Read Contacts");
            put(Manifest.permission.WRITE_CONTACTS, "Write Contacts");
            put(Manifest.permission.GET_ACCOUNTS, "Accounts");
            put(Manifest.permission.ACCESS_FINE_LOCATION, "GPS");
            put(Manifest.permission.ACCESS_COARSE_LOCATION, "GPS Mobile");
            put(Manifest.permission.RECORD_AUDIO, "Microphone");
            put(Manifest.permission.READ_PHONE_STATE, "Phone State");
            put(Manifest.permission.CALL_PHONE, "Call");
            put(Manifest.permission.READ_CALL_LOG, "Calling Log");
            put(Manifest.permission.WRITE_CALL_LOG, "Calling Log Write");
            put(Manifest.permission.BODY_SENSORS, "Sensors");
            put(Manifest.permission.READ_SMS, "Read SMS");
            put(Manifest.permission.SEND_SMS, "Send SMS");
            put(Manifest.permission.RECEIVE_SMS, "Receive SMS");
            put(Manifest.permission.READ_EXTERNAL_STORAGE, "Read External Storage");
            put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Write External Storage");

        }
    };

    HashMap<String, String> PERMISSIONS_SCOOPED = new HashMap() {
        {
            put(Environment.DIRECTORY_ALARMS, "Alarms");
            put(Environment.DIRECTORY_DCIM, "DCIM");
            put(Environment.DIRECTORY_DOCUMENTS, "Documents");
            put(Environment.DIRECTORY_DOWNLOADS, "Downloads");
            put(Environment.DIRECTORY_MOVIES, "Movies");
            put(Environment.DIRECTORY_MUSIC, "Music");
            put(Environment.DIRECTORY_NOTIFICATIONS, "Notifications");
            put(Environment.DIRECTORY_PICTURES, "Pictures");
            put(Environment.DIRECTORY_PODCASTS, "Podcasts");
        }

    };


}

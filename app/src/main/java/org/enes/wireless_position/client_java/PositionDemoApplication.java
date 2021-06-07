package org.enes.wireless_position.client_java;

import android.Manifest;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.enes.wireless_position.client_java.network.UDPDataSender;
import org.enes.wireless_position.client_java.pojo.BeaconListPOJO;
import org.enes.wireless_position.client_java.pojo.BeaconPOJO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PositionDemoApplication extends Application implements BeaconConsumer, RangeNotifier {

    private static PositionDemoApplication instance;

    public static PositionDemoApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;


        startBeaconService();
    }

    public boolean is_service_started = false;

    public boolean isGrantPermission() {
        String fine_location = Manifest.permission.ACCESS_FINE_LOCATION;
        String background_location = Manifest.permission.ACCESS_BACKGROUND_LOCATION;

        int fine_location_status = checkSelfPermission(fine_location);
        int background_location_status = checkSelfPermission(background_location);

        boolean b1 = false, b2 = false;

        if(fine_location_status == PackageManager.PERMISSION_GRANTED) {
            b1 = true;
        }

        if(background_location_status == PackageManager.PERMISSION_GRANTED) {
            b2 = true;
        }
        return b1 && b2;
    }

    private BeaconManager beaconManager;
    private static final long DEFAULT_FOREGROUND_SCAN_PERIOD = 500L;
    private static final long DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD = 500L;
    public static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    public static final String IBEACON_UUID = "2F9C6368-EBF8-4158-9E59-C371C9DF97A6";



    public void startBeaconService() {
        if(isGrantPermission()) {
            if(beaconManager == null)
                beaconManager = BeaconManager.getInstanceForApplication(this);
            if(!is_service_started) {
                beaconManager.removeAllMonitorNotifiers();
                beaconManager.setForegroundBetweenScanPeriod(DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD);
                beaconManager.setForegroundScanPeriod(DEFAULT_FOREGROUND_SCAN_PERIOD);
                beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));
                beaconManager.bind(this);
            }
        }
    }

    public void stopBeaconService() {
        if(is_service_started) {
            beaconManager.removeAllMonitorNotifiers();

            is_service_started = false;
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        Log.e("test","onBeaconServiceConnect");
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region(IBEACON_UUID, null, null, null));
            is_service_started = true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
        if(beacons != null && beacons.size() > 0) {
//            List<Beacon> beaconList = new ArrayList<>();
            List<BeaconPOJO> beaconPOJOS = new ArrayList<>();
            Iterator<Beacon> iterator = beacons.iterator();
            Log.e("tag", "-----------LOG--START-----------");
            while (iterator.hasNext()) {
                Beacon beacon = iterator.next();
                String uuid = beacon.getId1().toString();
                if(uuid.equalsIgnoreCase(IBEACON_UUID)) {
                    String id2 = beacon.getId2().toString();
                    String id3 = beacon.getId3().toString();
                    int rssi = beacon.getRssi();
                    String id1 = beacon.getId1().toString();
                    Log.e("tag", "uuid:" +  id1 + "id2:" + id2 +", id3:" + id3 + ", rssi:" + rssi);
//                    beaconList.add(beacon);
                    BeaconPOJO beaconPOJO = new BeaconPOJO();
                    beaconPOJO.uuid = id1;
                    beaconPOJO.id2 = id2;
                    beaconPOJO.id3 = id3;
                    beaconPOJO.rssi = rssi;
                    beaconPOJOS.add(beaconPOJO);
                }
            }
            Log.e("tag", "-----------LOG---END------------");
            Bundle bundle = new Bundle();
            bundle.putSerializable(GET_BEACON_LIST_KEY, (Serializable) beaconPOJOS);
            Intent intent = new Intent(INTENT_FILTER_BEACON_FOUND);
            intent.putExtras(bundle);
            sendBroadcast(intent);
            //
            sendListToServer(beaconPOJOS);
        }
    }

    public static final String GET_BEACON_LIST_KEY = "BEACON_LIST";

    public static final String INTENT_FILTER_BEACON_FOUND = "BEACON_FOUND";

    private void sendListToServer(List<BeaconPOJO> list) {
        if(list != null && list.size() > 0) {
            BeaconListPOJO beaconListPOJO = new BeaconListPOJO();
            beaconListPOJO.bs_id = Settings.Global.getString(getContentResolver(), "device_name");
            beaconListPOJO.data = list;

            Gson gson = new Gson();
            String str = gson.toJson(beaconListPOJO);
            UDPDataSender.sendDataToServer(str);
        }
    }


}

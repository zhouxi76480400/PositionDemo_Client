package org.enes.wireless_position.client_java;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.util.Collection;
import java.util.Iterator;

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
            Iterator<Beacon> iterator = beacons.iterator();
            while (iterator.hasNext()) {
                Beacon beacon = iterator.next();
                String uuid = beacon.getId1().toString();
                if(uuid.equalsIgnoreCase(IBEACON_UUID)) {
                    String id2 = beacon.getId2().toString();
                    String id3 = beacon.getId3().toString();
                    int rssi = beacon.getRssi();
                    Log.e("tag", "id2:" + id2 +", id3:" + id3 + ", rssi:" + rssi);
                }
            }
            Log.e("tag", "-----------------------");
        }
    }
}

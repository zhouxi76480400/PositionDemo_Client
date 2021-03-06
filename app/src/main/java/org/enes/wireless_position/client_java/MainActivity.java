package org.enes.wireless_position.client_java;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import org.altbeacon.beacon.Beacon;
import org.enes.wireless_position.client_java.adapter.BeaconAdapter;
import org.enes.wireless_position.client_java.pojo.BeaconPOJO;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private LinearLayout ll_main, ll_no_permission;
    private Button btn_request_permissions;
    private RecyclerView rv_main;

    private BeaconAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        ll_main = findViewById(R.id.ll_main);
        ll_no_permission = findViewById(R.id.ll_no_permission);
        btn_request_permissions = findViewById(R.id.btn_request_permissions);
        btn_request_permissions.setOnClickListener(this);
        rv_main = findViewById(R.id.rv_main);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv_main.setLayoutManager(linearLayoutManager);
        adapter = new BeaconAdapter(this, pojoList);
        rv_main.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionAndShowUI();
        registerBR();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterBR();
    }

    public static final int REQUEST_PERMISSION_CODE = 0x00061;

    private List<String> needs_permission_list;

    private void checkPermissionAndShowUI() {
        if(needs_permission_list == null) {
            needs_permission_list = new ArrayList<>();
        }
        needs_permission_list.clear();

        String fine_location = Manifest.permission.ACCESS_FINE_LOCATION;
        String background_location = Manifest.permission.ACCESS_BACKGROUND_LOCATION;

        int fine_location_status = checkSelfPermission(fine_location);
        int background_location_status = checkSelfPermission(background_location);

        if(fine_location_status != PackageManager.PERMISSION_GRANTED) {
            needs_permission_list.add(fine_location);
        }

        if(background_location_status != PackageManager.PERMISSION_GRANTED) {
            needs_permission_list.add(background_location);
        }

        if(needs_permission_list.size() > 0) {
            showPermissionRequestUI();
        } else{
            showWorkUI();
        }
    }

    private void showPermissionRequestUI() {
        if(ll_main.getVisibility() == View.VISIBLE)
            ll_main.setVisibility(View.GONE);
        if(ll_no_permission.getVisibility() != View.VISIBLE)
            ll_no_permission.setVisibility(View.VISIBLE);
    }

    private void showWorkUI() {
        if(ll_main.getVisibility() != View.VISIBLE)
            ll_main.setVisibility(View.VISIBLE);
        if(ll_no_permission.getVisibility()!= View.GONE)
            ll_no_permission.setVisibility(View.GONE);
        //
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(getString(R.string.do_not_lock_screen));


    }

    private void requestPermissions() {
        boolean is_user_denied = false;
        String[] str_array = new String[needs_permission_list.size()];
        for(int i = 0 ; i < needs_permission_list.size() ; i ++) {
            String needs_permission = needs_permission_list.get(i);
            str_array[i] = needs_permission;
            if(!shouldShowRequestPermissionRationale(needs_permission)) {
                is_user_denied = true;
            }
        }
        if(!is_user_denied)
            requestPermissions(str_array, REQUEST_PERMISSION_CODE);
        else {
            goToSettingsAndSetPermissions();
        }
    }

    private void goToSettingsAndSetPermissions() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        goToSettingForGrantPermissions();
                        break;
                    case DialogInterface.BUTTON_NEUTRAL:
                        System.exit(0);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.request_permissions))
                .setMessage(getString(R.string.user_denied_permissions_hint))
                .setNeutralButton(R.string.user_denied_alert_exit_app, listener)
                .setPositiveButton(R.string.user_denied_go_to_settings, listener)
                .setNegativeButton(R.string.user_denied_cancel,listener)
                .create();
        alertDialog.setCancelable(false);

        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != REQUEST_PERMISSION_CODE) {
            checkPermissionAndShowUI();
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {
        int res_id = v.getId();
        if(res_id == R.id.btn_request_permissions) {
            requestPermissions();
        }
    }

    private void goToSettingForGrantPermissions() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(),
                null);
        intent.setData(uri);
        startActivityIfNeeded(intent, REQUEST_PERMISSION_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_PERMISSION_CODE) {
            checkPermissionAndShowUI();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private List<BeaconPOJO> pojoList = new ArrayList<>();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase(PositionDemoApplication.INTENT_FILTER_BEACON_FOUND)) {
                ArrayList<BeaconPOJO> arrayList =
                        (ArrayList<BeaconPOJO>) intent.getExtras().getSerializable(PositionDemoApplication.GET_BEACON_LIST_KEY);
                if(arrayList != null&& arrayList.size() > 0) {
                    pojoList.clear();
                    pojoList.addAll(arrayList);
                    //
                    refreshRecyclerView();
                }
            }
        }
    };

    private void registerBR() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PositionDemoApplication.INTENT_FILTER_BEACON_FOUND);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void unregisterBR() {
        unregisterReceiver(broadcastReceiver);
    }

    private void refreshRecyclerView() {
        if(adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

}
package org.enes.wireless_position.client_java.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.enes.wireless_position.client_java.R;
import org.enes.wireless_position.client_java.pojo.BeaconPOJO;

import java.util.List;

public class BeaconAdapter extends RecyclerView.Adapter<BeaconAdapter.BeaconViewHolder> {

    private List<BeaconPOJO> beaconPOJOList;

    private Context context;

    public BeaconAdapter(Context context, List<BeaconPOJO> list) {
        super();
        this.context = context;
        this.beaconPOJOList = list;
    }

    @NonNull
    @Override
    public BeaconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.view_beacon_adapter, parent, false);
        BeaconViewHolder viewHolder = new BeaconViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconAdapter.BeaconViewHolder holder, int position) {
        BeaconPOJO beaconPOJO = beaconPOJOList.get(position);
        holder.tv_uuid.setText(holder.tv_uuid.getContext().getString(R.string.str_uuid) + beaconPOJO.uuid);
        holder.tv_id2.setText(holder.tv_id2.getContext().getString(R.string.str_id2) + beaconPOJO.id2);
        holder.tv_id3.setText(holder.tv_id3.getContext().getString(R.string.str_id3) + beaconPOJO.id3);
        holder.tv_rssi.setText(holder.tv_rssi.getContext().getString(R.string.str_rssi) + beaconPOJO.rssi);
    }

    @Override
    public int getItemCount() {
        if(beaconPOJOList != null) {
            return beaconPOJOList.size();
        }
        return 0;
    }

    static class BeaconViewHolder extends RecyclerView.ViewHolder {

        public TextView tv_uuid, tv_id2, tv_id3, tv_rssi;

        public BeaconViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_uuid = itemView.findViewById(R.id.tv_uuid);
            tv_id2 = itemView.findViewById(R.id.tv_id2);
            tv_id3 = itemView.findViewById(R.id.tv_id3);
            tv_rssi = itemView.findViewById(R.id.tv_rssi);
        }
    }

}

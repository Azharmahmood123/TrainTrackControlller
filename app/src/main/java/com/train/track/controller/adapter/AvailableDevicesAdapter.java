package com.train.track.controller.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.train.track.controller.R;
import com.train.track.controller.model.BluetoothObject;

import java.util.List;

public class AvailableDevicesAdapter extends RecyclerView.Adapter<AvailableDevicesAdapter.PairedDevicesViewHolder> {

    private List<BluetoothObject> bluetoothObjectList;
    private final OnItemClickListener onItemClickListener;

    public AvailableDevicesAdapter(List<BluetoothObject> bluetoothObjectList, OnItemClickListener onItemClickListener) {
        this.bluetoothObjectList = bluetoothObjectList;
        this.onItemClickListener = onItemClickListener;
    }

    public void updateList(List<BluetoothObject> bluetoothObjectList) {
        this.bluetoothObjectList = bluetoothObjectList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PairedDevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_available_bluetooth_devices, parent, false);
        return new PairedDevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PairedDevicesViewHolder holder, int position) {
        BluetoothObject bluetoothObject = bluetoothObjectList.get(position);
        holder.bind(bluetoothObject);
        holder.itemView.setOnClickListener(v -> onItemClickListener.onAvailableItemClick(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return bluetoothObjectList.size();
    }

    public static class PairedDevicesViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView tvName;
        private final AppCompatTextView tvAddress;

        public PairedDevicesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }

        void bind(BluetoothObject bluetoothObject) {
            tvName.setText(bluetoothObject.getBluetoothName());
            tvAddress.setText(bluetoothObject.getBluetoothAddress());
        }
    }

    public interface OnItemClickListener {
        void onAvailableItemClick(int position);
    }
}

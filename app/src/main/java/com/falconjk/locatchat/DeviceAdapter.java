package com.falconjk.locatchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<Device> devices = new ArrayList<>();
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
    }

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    public void addDevice(Device device) {
        // 檢查設備是否已存在
        int existingIndex = findDeviceIndex(device.getFingerprint());

        if (existingIndex != -1) {
            // 如果設備已存在，更新設備信息
            devices.set(existingIndex, device);
            notifyItemChanged(existingIndex);
        } else {
            // 如果是新設備，添加到列表
            devices.add(device);
            notifyItemInserted(devices.size() - 1);
        }
    }

    // 添加清理離線設備的方法
    public void removeOfflineDevices(long timeoutMillis) {
        long currentTime = System.currentTimeMillis();
        List<Device> devicesToRemove = new ArrayList<>();

        for (Device device : devices) {
            // 如果設備最後更新時間超過 timeoutMillis，認為設備離線
            if (currentTime - device.getLastUpdateTime() > timeoutMillis) {
                devicesToRemove.add(device);
            }
        }

        for (Device device : devicesToRemove) {
            int index = devices.indexOf(device);
            if (index != -1) {
                devices.remove(index);
                notifyItemRemoved(index);
            }
        }
    }
    // 根據 fingerprint 查找設備在列表中的位置
    private int findDeviceIndex(String fingerprint) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getFingerprint().equals(fingerprint)) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.bind(device);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAlias;
        private TextView tvModel;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlias = itemView.findViewById(R.id.tvAlias);
            tvModel = itemView.findViewById(R.id.tvModel);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeviceClick(devices.get(position));
                }
            });
        }

        public void bind(Device device) {
            tvAlias.setText(device.getAlias());
            tvModel.setText(device.getDeviceModel());
        }
    }
}

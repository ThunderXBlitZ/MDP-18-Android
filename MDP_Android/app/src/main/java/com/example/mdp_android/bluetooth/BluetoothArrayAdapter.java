package com.example.mdp_android.bluetooth;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mdp_android.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothArrayAdapter<T> extends ArrayAdapter {
    private Context mContext;
    private List<DeviceDetails> itemList = new ArrayList<DeviceDetails>();

    public BluetoothArrayAdapter(@NonNull Context context, ArrayList<DeviceDetails> list) {
        super(context, 0 , list);
        mContext = context;
        itemList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

            listItem = LayoutInflater.from(mContext).inflate(R.layout.bluetooth_list_item,parent,false);

        DeviceDetails currentDevice = itemList.get(position);
        String name = currentDevice.getDeviceName();
        String address = currentDevice.getAddress();

        TextView nameText = (TextView) listItem.findViewById(R.id.item_device_name);
        nameText.setText(name);

        TextView addressText = (TextView) listItem.findViewById(R.id.item_device_address);
        addressText.setText(address);

        TextView statusText = (TextView) listItem.findViewById(R.id.item_device_status);
        Boolean status = name.equals(BluetoothManager.getInstance().getDeviceName()) && address.equals(BluetoothManager.getInstance().getDeviceAddress());
        if(status && BluetoothManager.getInstance().isConnected()) {
            currentDevice.setConnected(true);
            statusText.setText("Connected");
            int color = mContext.getResources().getColor(R.color.colorPrimaryDark);
            statusText.setTextColor(color);
        } else {
            currentDevice.setConnected(false);
            statusText.setText("\u25CB");
            statusText.setTextColor(0xBB000000);
        }

        return listItem;
    }
}

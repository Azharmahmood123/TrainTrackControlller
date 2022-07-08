package com.train.track.controller.model;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

public class BluetoothObject implements Parcelable {

    private String bluetoothName;
    private String bluetoothAddress;
    private int bluetoothBondState;
    private int bluetoothType;
    private ParcelUuid[] bluetoothUUIDS;

    public BluetoothObject() {
        this.bluetoothName = "";
        this.bluetoothAddress = "";
        this.bluetoothBondState = 0;
        this.bluetoothType = 0;
        this.bluetoothUUIDS = new ParcelUuid[0];
    }

    protected BluetoothObject(Parcel in) {
        bluetoothName = in.readString();
        bluetoothAddress = in.readString();
        bluetoothBondState = in.readInt();
        bluetoothType = in.readInt();
        bluetoothUUIDS = in.createTypedArray(ParcelUuid.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bluetoothName);
        dest.writeString(bluetoothAddress);
        dest.writeInt(bluetoothBondState);
        dest.writeInt(bluetoothType);
        dest.writeTypedArray(bluetoothUUIDS, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BluetoothObject> CREATOR = new Creator<BluetoothObject>() {
        @Override
        public BluetoothObject createFromParcel(Parcel in) {
            return new BluetoothObject(in);
        }

        @Override
        public BluetoothObject[] newArray(int size) {
            return new BluetoothObject[size];
        }
    };

    public String getBluetoothName() {
        return bluetoothName;
    }

    public void setBluetoothName(String bluetoothName) {
        this.bluetoothName = bluetoothName;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    public int getBluetoothBondState() {
        return bluetoothBondState;
    }

    public void setBluetoothBondState(int bluetoothBondState) {
        this.bluetoothBondState = bluetoothBondState;
    }

    public int getBluetoothType() {
        return bluetoothType;
    }

    public void setBluetoothType(int bluetoothType) {
        this.bluetoothType = bluetoothType;
    }

    public ParcelUuid[] getBluetoothUUIDS() {
        return bluetoothUUIDS;
    }

    public void setBluetoothUUIDS(ParcelUuid[] bluetoothUUIDS) {
        this.bluetoothUUIDS = bluetoothUUIDS;
    }
}

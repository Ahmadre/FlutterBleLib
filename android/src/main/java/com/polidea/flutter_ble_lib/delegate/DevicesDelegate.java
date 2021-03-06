package com.polidea.flutter_ble_lib.delegate;

import com.polidea.flutter_ble_lib.SafeMainThreadResolver;
import com.polidea.flutter_ble_lib.constant.ArgumentKey;
import com.polidea.flutter_ble_lib.constant.MethodName;
import com.polidea.flutter_ble_lib.converter.BleErrorJsonConverter;
import com.polidea.flutter_ble_lib.converter.DevicesResultJsonConverter;
import com.polidea.multiplatformbleadapter.BleAdapter;
import com.polidea.multiplatformbleadapter.Device;
import com.polidea.multiplatformbleadapter.OnErrorCallback;
import com.polidea.multiplatformbleadapter.OnSuccessCallback;
import com.polidea.multiplatformbleadapter.errors.BleError;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import org.json.JSONException;


public class DevicesDelegate extends CallDelegate {

    private static final String TAG = DevicesDelegate.class.getName();
    private static List<String> supportedMethods = Arrays.asList(MethodName.GET_CONNECTED_DEVICES,
            MethodName.GET_KNOWN_DEVICES);

    private BluetoothAdapter mBluetoothAdapter;
    private BleAdapter bleAdapter;
    private BleErrorJsonConverter bleErrorJsonConverter = new BleErrorJsonConverter();
    private DevicesResultJsonConverter devicesResultJsonConverter = new DevicesResultJsonConverter();

    public DevicesDelegate(BleAdapter bleAdapter) {
        super(supportedMethods);
        this.bleAdapter = bleAdapter;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
        switch (methodCall.method) {
            case MethodName.GET_KNOWN_DEVICES:
                getKnownDevices(methodCall.<List<String>>argument(ArgumentKey.DEVICE_IDENTIFIERS), result);
                return;

            case MethodName.GET_CONNECTED_DEVICES:
                getConnectedDevices(methodCall.<List<String>>argument(ArgumentKey.UUIDS), result);
                return;

            case MethodName.GET_PAIRED_DEVICES:
                getPairedDevices(result);
                return;
            default:
                result.notImplemented();
        }
    }

    private void getConnectedDevices(@NonNull final List<String> serviceUUIDs,
            @NonNull final MethodChannel.Result result) {
        Log.d(TAG, "Get known devices");

        final SafeMainThreadResolver resolver = new SafeMainThreadResolver<>(new OnSuccessCallback<Device[]>() {
            @Override
            public void onSuccess(Device[] devices) {
                try {
                    result.success(devicesResultJsonConverter.toJson(devices));
                } catch (JSONException e) {
                    e.printStackTrace();
                    result.error(null, e.getMessage(), null);
                }
            }
        }, new OnErrorCallback() {
            @Override
            public void onError(BleError error) {
                Log.e(TAG, "Get known devices error " + error.reason + "  " + error.internalMessage);
                result.error(String.valueOf(error.errorCode.code), error.reason, bleErrorJsonConverter.toJson(error));
            }
        });

        bleAdapter.getConnectedDevices(serviceUUIDs.toArray(new String[serviceUUIDs.size()]),
                new OnSuccessCallback<Device[]>() {
                    @Override
                    public void onSuccess(Device[] devices) {
                        Log.d(TAG, "Found known devices: " + devices.length);
                        resolver.onSuccess(devices);
                    }
                }, new OnErrorCallback() {
                    @Override
                    public void onError(BleError error) {
                        resolver.onError(error);
                    }
                });
    }

    private void getKnownDevices(@NonNull final List<String> deviceIdentifiers,
            @NonNull final MethodChannel.Result result) {
        Log.d(TAG, "Get known devices");

        final SafeMainThreadResolver resolver = new SafeMainThreadResolver<>(new OnSuccessCallback<Device[]>() {
            @Override
            public void onSuccess(Device[] devices) {
                try {
                    result.success(devicesResultJsonConverter.toJson(devices));
                } catch (JSONException e) {
                    e.printStackTrace();
                    result.error(null, e.getMessage(), null);
                }
            }
        }, new OnErrorCallback() {
            @Override
            public void onError(BleError error) {
                Log.e(TAG, "Get known devices error " + error.reason + "  " + error.internalMessage);
                result.error(String.valueOf(error.errorCode.code), error.reason, bleErrorJsonConverter.toJson(error));
            }
        });

        bleAdapter.getKnownDevices(deviceIdentifiers.toArray(new String[deviceIdentifiers.size()]),
                new OnSuccessCallback<Device[]>() {
                    @Override
                    public void onSuccess(Device[] devices) {
                        Log.d(TAG, "Found known devices" + devices.length);
                        resolver.onSuccess(devices);
                    }
                }, new OnErrorCallback() {
                    @Override
                    public void onError(BleError error) {
                        resolver.onError(error);
                    }
                });
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH)
    private void getPairedDevices(@NonNull final MethodChannel.Result result) {
        Log.d(TAG, "Get paired devices");

        final SafeMainThreadResolver resolver = new SafeMainThreadResolver<>(new OnSuccessCallback<Device[]>() {
            @Override
            public void onSuccess(Device[] devices) {
                try {
                    result.success(devicesResultJsonConverter.toJson(devices));
                } catch (JSONException e) {
                    e.printStackTrace();
                    result.error(null, e.getMessage(), null);
                }
            }
        }, new OnErrorCallback() {
            @Override
            public void onError(BleError error) {
                Log.e(TAG, "Get paired devices error " + error.reason + "  " + error.internalMessage);
                result.error(String.valueOf(error.errorCode.code), error.reason, bleErrorJsonConverter.toJson(error));
            }
        });

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        List<Device> devices = new ArrayList<>();

        for (int i = 0; i < pairedDevices.size(); i++) {
            BluetoothDevice blDevice =  pairedDevices.iterator().next();
            Device device = new Device(blDevice.getAddress(), blDevice.getName());
            devices.add(device);
        }

        Log.d(TAG, "Found paired devices" + devices.size());
        resolver.onSuccess(devices);
    }
}

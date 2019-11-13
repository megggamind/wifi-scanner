package com.nauto.scanner;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private WifiManager wifi;
    private int networkId = -1;
    private static final int MAX_CONNECTION_STATUS_RETRIES = 15;

    private CountDownLatch wifiConnectedDownLatch;
    private int numberOfRetries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    }

    public void onScanClicked(View view) {
//        findExisting();


        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        List<ScanResult> results = wifi.getScanResults();
        Log.d(TAG, "onScanClicked: " + results.size());

        for (ScanResult result : results) {
            String resultString = "" + result.SSID;
            Log.d(TAG, "resultString: " + resultString);

            System.out.println("resultString: " + resultString);
//            if (ssid.equals(resultString)) {
//                connected = connectTo(result, password, ssid);
//            }
        }
//        if (connected) {
//            promise.resolve(true);
//        } else {
//            System.out.println("Could Not connect");
//            promise.reject(new Throwable("Could Not connect"));
//        }
    }

    private void findExisting() {
        Log.d(TAG, "findExisting: ");

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifi.setWifiEnabled(false);
        wifi.setWifiEnabled(true);

        try {
            Thread.sleep(5000);
        } catch (Exception e) {

        }

        List<WifiConfiguration> existingConfigs = wifi.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + "1phone" + "\"")) {
                networkId = existingConfig.networkId;
//                connected = wifi.enableNetwork(networkId, true);
            }
        }


        if (networkId != -1) {
            boolean ret = wifi.removeNetwork(networkId);
            System.out.println("Aniket2, ret: " + ret);

            wifi.saveConfiguration();

        }


        List<ScanResult> results = wifi.getScanResults();

        boolean connected = false;
        for (ScanResult result : results) {
            String resultString = "" + result.SSID;

            System.out.println("Aniket2, connected0: " + resultString);

//            if (ssid.equals(resultString)) {
//                connected = connectTo(result, password, ssid);
//            }
        }
    }


    public void onConnectClicked(View view) {
        connectToProtectedSSID("N3-N30HA02921F0D5W", "W5D0F12920AH03N-3N");
        getRouterIpAddress();
    }


    public void onGetRouterIpAddressClicked(View view) {
        getRouterIpAddress();
    }

    private void getRouterIpAddress() {

        DhcpInfo dhcpInfo = wifi.getDhcpInfo();
        String ip = longToIP(dhcpInfo.serverAddress);

        System.out.println("ip: " + ip);
    }

    private static String longToIP(int longIp) {
        StringBuilder sb = new StringBuilder();
        String[] strip = new String[4];
        strip[3] = String.valueOf((longIp >>> 24));
        strip[2] = String.valueOf((longIp & 0x00FFFFFF) >>> 16);
        strip[1] = String.valueOf((longIp & 0x0000FFFF) >>> 8);
        strip[0] = String.valueOf((longIp & 0x000000FF));
        sb.append(strip[0]);
        sb.append(".");
        sb.append(strip[1]);
        sb.append(".");
        sb.append(strip[2]);
        sb.append(".");
        sb.append(strip[3]);
        return sb.toString();
    }

    private WifiConfiguration IsExist(String SSID) {
        List<WifiConfiguration> existingConfigs = wifi.getConfiguredNetworks();
        if (existingConfigs == null) {
            return null;
        }

        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }


    public void connectToProtectedSSID(String ssid, String password) {
        List<ScanResult> results = wifi.getScanResults();
        boolean connected = false;

        System.out.println("results: " + results.size());

        for (ScanResult result : results) {
            String resultString = "" + result.SSID;
            System.out.println("resultString: " + resultString);
            if (ssid.equals(resultString)) {
                System.out.println("found SSID");
                connected = connectTo(result, password, ssid);
            }
        }
        if (!connected) {
            List<WifiConfiguration> existingConfigs = wifi.getConfiguredNetworks();
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\"" + ssid + "\"")) {
                    networkId = existingConfig.networkId;
                    connected = wifi.enableNetwork(networkId, true);
                }
            }
        }

        if (connected) {
            System.out.println("Connected");
        } else {
            System.out.println("Could not Connected");
        }
    }

    /**
     * If the SSID is not found in the avialable lists of wifi through scanning,
     * then check the already existing wifi networks and make the connection using the networkId
     */
    public Boolean connectTo(ScanResult result, String password, String ssid) {
        networkId = -1;
        WifiConfiguration conf = new WifiConfiguration();

        conf.allowedAuthAlgorithms.clear();
        conf.allowedGroupCiphers.clear();
        conf.allowedKeyManagement.clear();
        conf.allowedPairwiseCiphers.clear();
        conf.allowedProtocols.clear();

        conf.SSID = String.format("\"%s\"", ssid);

        WifiConfiguration tempConfig = this.IsExist(conf.SSID);
        if (tempConfig != null) {
            wifi.removeNetwork(tempConfig.networkId);
        }

        String capabilities = result.capabilities;

        if (capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains("WPA/WPA2 PSK")) {

            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);

            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.preSharedKey = String.format("\"%s\"", password);

        } else if (capabilities.contains("WEP")) {
            conf.wepKeys[0] = "\"" + password + "\"";
            conf.wepTxKeyIndex = 0;
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else {
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        List<WifiConfiguration> mWifiConfigList = wifi.getConfiguredNetworks();
        if (mWifiConfigList == null) {
            System.out.println("mWifiConfigList == null");
            return false;
        }

        // Use the existing network config if exists
        for (WifiConfiguration wifiConfig : mWifiConfigList) {
            if (wifiConfig.SSID.equals(conf.SSID)) {
                conf = wifiConfig;
                networkId = conf.networkId;
            }
        }

        if (networkId == -1) {
            networkId = wifi.addNetwork(conf);

            wifi.saveConfiguration();
        }

        // if network not added return false
        if (networkId == -1) {
            System.out.println("networkId == -1");
            return false;
        }

        // disconnect current network


        boolean disconnected = disconnect();
        if (!disconnected) {
            System.out.println("disconnected: " + disconnected);
            return false;
        }

        // enable new network

        boolean connected = wifi.enableNetwork(networkId, true);
        System.out.println("connected: " + connected);

        return connected;
    }

    public void onDisconnectClicked(View view) {
        disconnect();
    }

    private boolean disconnect() {
        if (wifi != null && wifi.isWifiEnabled()) {
            int netId = wifi.getConnectionInfo().getNetworkId();
            wifi.removeNetwork(netId);
            wifi.disableNetwork(netId);
            return wifi.disconnect();
        }

        return false;
    }


    private void restartWifi() {
        wifi.setWifiEnabled(false);
        wifi.setWifiEnabled(true);
        numberOfRetries = 0;
        checkIfComplete();

        System.out.println("restartWifi");
    }

    private void checkIfComplete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
                System.out.println("isWifiEnabled: " + wifi.isWifiEnabled());
                if (wifi.isWifiEnabled() && wifi.getScanResults().size() != 0) {
                    if (wifiConnectedDownLatch != null) {
                        wifiConnectedDownLatch.countDown();
                    }
                } else {
                    if (numberOfRetries < MAX_CONNECTION_STATUS_RETRIES) {
                        checkIfComplete();
                        numberOfRetries++;
                    }
                }
            }
        }).start();
    }

    public void onWifiEnabledClicked(View view) {
        restartWifi();
        wifiConnectedDownLatch = new CountDownLatch(1);
        try {
            wifiConnectedDownLatch.await(20, TimeUnit.SECONDS);
        } catch (Exception exception) {
        }

        List<ScanResult> results = wifi.getScanResults();

        for (ScanResult result : results) {
            String resultString = "" + result.SSID;
            Log.d(TAG, "resultString: " + resultString);

            System.out.println("resultString: " + resultString);
//            if (ssid.equals(resultString)) {
//                connected = connectTo(result, password, ssid);
//            }
        }
    }

    public void onWifiDisabledClicked(View view) {
        System.out.println("onWifiDisabledClicked");
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
    }
}

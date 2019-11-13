package com.nauto.scanner;

import android.net.wifi.WifiManager;

import java.util.concurrent.CountDownLatch;

class Worker extends Thread {
    private int delay;
    private WifiManager wifi;
    private CountDownLatch latch;
    private int numberOfRetries = 0;
    private static final int MAX_CONNECTION_STATUS_RETRIES = 5;

    public Worker(int delay, CountDownLatch latch,
                  String name, WifiManager wifi) {
        super(name);
        this.delay = delay;
        this.latch = latch;
        this.wifi = wifi;
    }

    private void checkIfComplete() {
        try {
            Thread.sleep(delay);
            System.out.println("isWifiEnabled: " + wifi.isWifiEnabled());
            if (wifi.isWifiEnabled()) {
                latch.countDown();
            } else {
                if (numberOfRetries < MAX_CONNECTION_STATUS_RETRIES) {
                    checkIfComplete();
                    numberOfRetries++;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        checkIfComplete();
    }
}

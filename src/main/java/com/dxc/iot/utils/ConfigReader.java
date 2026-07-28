package com.dxc.iot.utils;

import java.io.FileInputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties props = new Properties();

    static {
        try {
            props.load(new FileInputStream("config.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String get(String key) {
        String systemVal = System.getProperty(key);
        if (systemVal != null) {
            return systemVal;
        }
        return props.getProperty(key);
    }
}
package com.gi2t.face.detect.util;

import java.lang.reflect.Method;

public class PropertyUtil {
    public final static String className = "android.os.SystemProperties";

    public static String get(String key, String defaultValue){
        String value = defaultValue;
        try {
            Class<?> c = Class.forName(className);
            Method get = c.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(c, key, defaultValue));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static void set(String key, String value) {
        try {
            Class<?> c = Class.forName(className);
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

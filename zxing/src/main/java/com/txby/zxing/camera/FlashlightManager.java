/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.txby.zxing.camera;

import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * This class is used to activate the weak light on some camera phones (not flash)
 * in order to illuminate surfaces for scanning. There is no official way to do this,
 * but, classes which allow access to this function still exist on some devices.
 * This therefore proceeds through a great deal of reflection.
 * <p>
 * See <a href="http://almondmendoza.com/2009/01/05/changing-the-screen-brightness-programatically/">
 * http://almondmendoza.com/2009/01/05/changing-the-screen-brightness-programatically/</a> and
 * <a href="http://code.google.com/p/droidled/source/browse/trunk/src/com/droidled/demo/DroidLED.java">
 * http://code.google.com/p/droidled/source/browse/trunk/src/com/droidled/demo/DroidLED.java</a>.
 * Thanks to Ryan Alford for pointing out the availability of this class.
 */
final class FlashlightManager {
    private static final String TAG = FlashlightManager.class.getSimpleName();

    private static final Object CLASS_HARDWARE_SERVICE;
    private static final Method METHOD_SET_FLASH_ENABLED;

    static {
        CLASS_HARDWARE_SERVICE = getClassHardwareService();
        METHOD_SET_FLASH_ENABLED = getMethodSetFlashEnabled();

        if (CLASS_HARDWARE_SERVICE == null) {
            Log.v(TAG, "This device does supports control of a flashlight");
        } else {
            Log.v(TAG, "This device does not support control of a flashlight");
        }
    }

    private FlashlightManager() {

    }

    static void enableFlashlight() {
        setFlashlight(true);
    }

    static void disableFlashlight() {
        setFlashlight(false);
    }

    private static void setFlashlight(boolean active) {
        invoke(METHOD_SET_FLASH_ENABLED, CLASS_HARDWARE_SERVICE, active);
    }

    //////

    private static Object getClassHardwareService() {
        Class<?> serviceManagerClass = maybeForName("android.os.ServiceManager");
        if (serviceManagerClass == null) {
            return null;
        }
        Method getServiceMethod = maybeGetMethod(serviceManagerClass, "getService", String.class);
        if (getServiceMethod == null) {
            return null;
        }
        Object hardwareService = invoke(getServiceMethod, null, "hardware");
        if (hardwareService == null) {
            return null;
        }

        Class<?> iHardwareServiceStubClass = maybeForName("android.os.IHardwareService$Stub");
        if (iHardwareServiceStubClass == null) {
            return null;
        }
        Method asInterfaceMethod = maybeGetMethod(iHardwareServiceStubClass, "asInterface", IBinder.class);
        if (asInterfaceMethod == null) {
            return null;
        }
        return invoke(asInterfaceMethod, null, hardwareService);
    }

    private static Method getMethodSetFlashEnabled() {
        if (CLASS_HARDWARE_SERVICE == null) {
            return null;
        }
        Class<?> proxyClass = CLASS_HARDWARE_SERVICE.getClass();
        return maybeGetMethod(proxyClass, "setFlashlightEnabled", boolean.class);
    }

    //////

    private static Class<?> maybeForName(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            Log.w(TAG, "Unexpected error while finding class " + name, e);
            return null;
        }
    }

    private static Method maybeGetMethod(Class<?> clazz, String name, Class<?>... argClasses) {
        try {
            return clazz.getMethod(name, argClasses);
        } catch (Exception e) {
            Log.w(TAG, "Unexpected error while finding method " + name, e);
            return null;
        }
    }

    private static Object invoke(Method method, Object instance, Object... args) {
        try {
            return method.invoke(instance, args);
        } catch (Exception e) {
            Log.w(TAG, "Unexpected error while invoking " + method, e);
            return null;
        }
    }

}

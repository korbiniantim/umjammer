/*
 * Copyright (c) 1999 - 2001, International Business Machines Corporation.
 * All Rights Reserved.
 *
 * This software is provided and licensed under the terms and conditions
 * of the Common Public License:
 * http://oss.software.ibm.com/developerworks/opensource/license-cpl.html
 */

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbHub;
import javax.usb.util.UsbUtil;

import vavi.util.Debug;


/**
 * Find a specific UsbDevice.
 * <p>
 * This finds a specific UsbDevice based on some of the device's properties.
 * This class should not be used except by other example code.
 * @author Dan Streetman
 */
public class FindUsbDevice {
    private static short staticVendorId = (short) 0xffff; /* This probably will never match */
    private static short staticProductId = (short) 0xffff; /* This probably will never match */
    private static byte staticDeviceClass = UsbConst.HUB_CLASSCODE; /* This will match all hubs. :) */
    private static String staticManufacturerString = "This probably won't match anything";
    private static final String VENDOR_ID_KEY = "idVendor";
    private static final String PRODUCT_ID_KEY = "idProduct";
    private static final String DEVICE_CLASS_KEY = "bDeviceClass";
    private static final String MANUFACTURER_STRING_KEY = "manufacturer";
    private static final String KEYS = "\t" + VENDOR_ID_KEY + "\n" + "\t" + PRODUCT_ID_KEY + "\n" + "\t" + DEVICE_CLASS_KEY + "\n" + "\t" + MANUFACTURER_STRING_KEY;
    private static final String USAGE = "Usage : java FindUsbDevice <key=value>\n" + "\n" + "\tvalid keys are:\n" + KEYS;

    public static void main(String[] argv) {
        parseArgv(argv);

        UsbHub virtualRootUsbHub = ShowTopology.getVirtualRootUsbHub();
        List<UsbDevice> usbDevices = null;

        /* This gets all connected devices. */
        usbDevices = getAllUsbDevices(virtualRootUsbHub);
//System.err.println(StringUtil.paramString(usbDevices));
//for (UsbDevice device : usbDevices) {
//    Debug.println(StringUtil.paramString(device));
//}
        System.err.println("Found " + usbDevices.size() + " devices total.");

        /* This will recursively search for all devices with the specified vendor and product id. */
        usbDevices = getUsbDevicesWithId(virtualRootUsbHub, getVendorId(), getProductId());

        System.err.print("Found " + usbDevices.size() + " devices with");
        System.err.print(" vendor ID 0x" + UsbUtil.toHexString(getVendorId()));
        System.err.print(" product ID 0x" + UsbUtil.toHexString(getProductId()));
        System.err.println("");

        /* This will recursively search for all devices with the specified device class. */
        usbDevices = getUsbDevicesWithDeviceClass(virtualRootUsbHub, getDeviceClass());

        System.err.print("Found " + usbDevices.size() + " devices with");
        System.err.print(" device class 0x" + UsbUtil.toHexString(getDeviceClass()));
        System.err.println("");

        /* This will recursively search for all devices with the specified manufacturer string. */
        usbDevices = getUsbDevicesWithManufacturerString(virtualRootUsbHub, getManufacturerString());

        System.err.print("Found " + usbDevices.size() + " devices with");
        System.err.print(" manufacturer string \"" + getManufacturerString() + "\"");
        System.err.println("");
    }

    /**
     * This forms an inclusive list of all UsbDevices connected to this UsbDevice.
     * <p>
     * The list includes the provided device.  If the device is also a hub,
     * the list will include all devices connected to it, recursively.
     * @param usbDevice The UsbDevice to use.
     * @return An inclusive List of all connected UsbDevices.
     */
    public static List<UsbDevice> getAllUsbDevices(UsbDevice usbDevice) {
        List<UsbDevice> list = new ArrayList<UsbDevice>();

        list.add(usbDevice);

        /* this is just normal recursion.  Nothing special. */
        if (usbDevice.isUsbHub()) {
            @SuppressWarnings("unchecked")
            List<UsbDevice> devices = ((UsbHub) usbDevice).getAttachedUsbDevices();

            for (int i = 0; i < devices.size(); i++) {
                list.addAll(getAllUsbDevices(devices.get(i)));
            }
        }

        return list;
    }

    /**
     * Get a List of all devices that match the specified vendor and product id.
     * @param usbDevice The UsbDevice to check.
     * @param vendorId The vendor id to match.
     * @param productId The product id to match.
     * return A List of any matching UsbDevice(s).
     */
    public static List<UsbDevice> getUsbDevicesWithId(UsbDevice usbDevice, short vendorId, short productId) {
        List<UsbDevice> list = new ArrayList<UsbDevice>();

        /* A device's descriptor is always available.  All descriptor
         * field names and types match exactly what is in the USB specification.
         * Note that Java does not have unsigned numbers, so if you are
         * comparing 'magic' numbers to the fields, you need to handle it correctly.
         * For example if you were checking for Intel (vendor id 0x8086) devices,
         *   if (0x8086 == descriptor.idVendor())
         * will NOT work.  The 'magic' number 0x8086 is a positive integer, while
         * the _short_ vendor id 0x8086 is a negative number!  So you need to do either
         *   if ((short) 0x8086 == descriptor.idVendor())
         * or
         *   if (0x8086 == UsbUtil.unsignedInt(descriptor.idVendor()))
         * or
         *   short intelVendorId = (short) 0x8086;
         *   if (intelVendorId == descriptor.idVendor())
         * Note the last one, if you don't cast 0x8086 into a short,
         * the compiler will fail because there is a loss of precision;
         * you can't represent positive 0x8086 as a short; the max value
         * of a signed short is 0x7fff (see Short.MAX_VALUE).
         *
         * See javax.usb.util.UsbUtil.unsignedInt() for some more information.
         */
        if ((vendorId == usbDevice.getUsbDeviceDescriptor().idVendor()) && (productId == usbDevice.getUsbDeviceDescriptor().idProduct())) {
            list.add(usbDevice);
        }

        /* this is just normal recursion.  Nothing special. */
        if (usbDevice.isUsbHub()) {
            @SuppressWarnings("unchecked")
            List<UsbDevice> devices = ((UsbHub) usbDevice).getAttachedUsbDevices();

            for (int i = 0; i < devices.size(); i++) {
                list.addAll(getUsbDevicesWithId(devices.get(i), vendorId, productId));
            }
        }

        return list;
    }

    /**
     * Get a List of all devices that match the specified device class.
     * @param usbDevice The UsbDevice to check.
     * @param deviceClass The device class to match.
     * @return A List of any matching UsbDevice(s).
     */
    public static List<UsbDevice> getUsbDevicesWithDeviceClass(UsbDevice usbDevice, byte deviceClass) {
        List<UsbDevice> list = new ArrayList<UsbDevice>();

        /* See above about comparing unsigned numbers, note this is an unsigned byte. */
        if (deviceClass == usbDevice.getUsbDeviceDescriptor().bDeviceClass()) {
            list.add(usbDevice);
        }

        /* this is just normal recursion.  Nothing special. */
        if (usbDevice.isUsbHub()) {
            @SuppressWarnings("unchecked")
            List<UsbDevice> devices = ((UsbHub) usbDevice).getAttachedUsbDevices();

            for (int i = 0; i < devices.size(); i++) {
                list.addAll(getUsbDevicesWithDeviceClass(devices.get(i), deviceClass));
            }
        }

        return list;
    }

    /**
     * Get a List of all devices that match the specified manufacturer string.
     * @param usbDevice The UsbDevice to check.
     * @param manufacturerString The manufacturer string to match.
     * @return A List of any matching UsbDevice(s).
     */
    public static List<UsbDevice> getUsbDevicesWithManufacturerString(UsbDevice usbDevice, String manufacturerString) {
        List<UsbDevice> list = new ArrayList<UsbDevice>();

        /* Getting the product string may generate an UsbException,
         * as it may be necessary to actually communicate with the device
         * which could fail.
         */
try {
Debug.println(usbDevice.getUsbDeviceDescriptor());
} catch (Exception e) {
e.printStackTrace(System.err);
}
        try {
            if (manufacturerString.equals(usbDevice.getManufacturerString())) {
                list.add(usbDevice);
            }
        } catch (UsbException uE) {
            /* If there is an UsbException, we couldn't communicate
             * with the device for some reason.  The exact reason should be
             * indicated by the UsbException (we won't try to determine it here).
             * We could try to get the string again (possibly after trying to
             * figure out and/or fix the cause of the UsbException),
             * or we could ignore this device, or we could throw the UsbException,
             * or some other Exception, on up.  Since this is an example we'll
             * throw a RuntimeException on up (if we threw the UsbException,
             * we would have to declare that in this method definition).
             * This isn't a good thing to do in normal code.
             */
uE.printStackTrace(System.err);
            throw new RuntimeException("Couldn't get manufacturer string : " + uE.toString());
        } catch (UnsupportedEncodingException usE) {
            /* If there is an UnsupportedEncodingException, the
             * available Java libraries did not have an encoding that
             * could convert the 16-bit UNICODE byte[] to a String.
             * This is uncommon, and probably means that the string
             * is not in english _and_ the Java libraries are significantly
             * reduced, possibly for an embedded Java (J2ME?) implementation.
             * For this case, we'll ignore the device - the provided string
             * most likely does not match whatever the device's string is.
             * But, who knows, it might...remember this is just an example!
             */
        }

        /* this is just normal recursion.  Nothing special. */
        if (usbDevice.isUsbHub()) {
            @SuppressWarnings("unchecked")
            List<UsbDevice> devices = ((UsbHub) usbDevice).getAttachedUsbDevices();

            for (int i = 0; i < devices.size(); i++) {
                list.addAll(getUsbDevicesWithManufacturerString(devices.get(i), manufacturerString));
            }
        }

        return list;
    }

    /**
     * Get a vendor ID.
     * @return A vendor ID.
     */
    public static short getVendorId() {
        return staticVendorId;
    }

    /**
     * Get a product ID.
     * @return A product ID.
     */
    public static short getProductId() {
        return staticProductId;
    }

    /**
     * Get a device class.
     * @return A device class.
     */
    public static byte getDeviceClass() {
        return staticDeviceClass;
    }

    /**
     * Get a manufacturer string.
     * @return A manufacturer string.
     */
    public static String getManufacturerString() {
        return staticManufacturerString;
    }

    /**
     * Parse the parameters.
     * @param argv The command-line parameters.
     */
    public static void parseArgv(String[] argv) {
        for (int i = 0; i < argv.length; i++) {
            int equalsIndex = argv[i].indexOf('=');

            try {
                String key = argv[i].substring(0, equalsIndex);
                String value = argv[i].substring(equalsIndex + 1);

                if (key.equals(VENDOR_ID_KEY)) {
                    staticVendorId = (short) Integer.decode(value).intValue();
                } else if (key.equals(PRODUCT_ID_KEY)) {
                    staticProductId = (short) Integer.decode(value).intValue();
                } else if (key.equals(DEVICE_CLASS_KEY)) {
                    staticDeviceClass = (byte) Integer.decode(value).intValue();
                } else if (key.equals(MANUFACTURER_STRING_KEY)) {
                    staticManufacturerString = value;
                } else {
                    System.err.println("Unrecognized key \"" + key + "\"\n" + USAGE);
                    System.exit(1);
                }
            } catch (Exception e) {
                System.err.println("Invalid key-value pair \"" + argv[i] + "\"\n" + USAGE);
                System.exit(1);
            }
        }
    }
}

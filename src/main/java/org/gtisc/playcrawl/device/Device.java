/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.device;

import java.util.List;

/**
 *
 * @author meng
 */
public class Device {

    public class DeviceBuild {

        private String seq;
        private String product;
        private String carrier;
        private String radio;
        private String bootloader;
        private String client;
        private String device;
        private String model;
        private String manufacturer;
        private String buildProduct;
        private boolean otaInstalled;

        public String getSeq() {
            return seq;
        }

        public void setSeq(String seq) {
            this.seq = seq;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public String getCarrier() {
            return carrier;
        }

        public void setCarrier(String carrier) {
            this.carrier = carrier;
        }

        public String getRadio() {
            return radio;
        }

        public void setRadio(String radio) {
            this.radio = radio;
        }

        public String getBootloader() {
            return bootloader;
        }

        public void setBootloader(String bootloader) {
            this.bootloader = bootloader;
        }

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
            this.manufacturer = manufacturer;
        }

        public String getBuildProduct() {
            return buildProduct;
        }

        public void setBuildProduct(String buildProduct) {
            this.buildProduct = buildProduct;
        }

        public boolean isOtaInstalled() {
            return otaInstalled;
        }

        public void setOtaInstalled(boolean otaInstalled) {
            this.otaInstalled = otaInstalled;
        }
    }

    public class DeviceOperator {

        private String cellOperator;
        private String simOperator;
        private String roaming;
        private int userNumber;

        public String getCellOperator() {
            return cellOperator;
        }

        public void setCellOperator(String cellOperator) {
            this.cellOperator = cellOperator;
        }

        public String getSimOperator() {
            return simOperator;
        }

        public void setSimOperator(String simOperator) {
            this.simOperator = simOperator;
        }

        public String getRoaming() {
            return roaming;
        }

        public void setRoaming(String roaming) {
            this.roaming = roaming;
        }

        public int getUserNumber() {
            return userNumber;
        }

        public void setUserNumber(int userNumber) {
            this.userNumber = userNumber;
        }
    }

    public class DeviceConfig {

        private int touchScreen;
        private int keyboard;
        private boolean hasHardKeyboard;
        private int navigation;
        private boolean hasFiveWayNavigation;
        private int screenLayout;
        private int screenDensity;
        private int screenWidth;
        private int screenHeight;
        private List<String> systemSharedLibrary;
        private List<String> systemAvailableFeature;
        private List<String> nativePlatform;
        private List<String> systemSupportedLocale;
        private int glEsVersion;
        private List<String> glExtension;

        public int getTouchScreen() {
            return touchScreen;
        }

        public void setTouchScreen(int touchScreen) {
            this.touchScreen = touchScreen;
        }

        public int getKeyboard() {
            return keyboard;
        }

        public void setKeyboard(int keyboard) {
            this.keyboard = keyboard;
        }

        public boolean isHasHardKeyboard() {
            return hasHardKeyboard;
        }

        public void setHasHardKeyboard(boolean hasHardKeyboard) {
            this.hasHardKeyboard = hasHardKeyboard;
        }

        public int getNavigation() {
            return navigation;
        }

        public void setNavigation(int navigation) {
            this.navigation = navigation;
        }

        public boolean isHasFiveWayNavigation() {
            return hasFiveWayNavigation;
        }

        public void setHasFiveWayNavigation(boolean hasFiveWayNavigation) {
            this.hasFiveWayNavigation = hasFiveWayNavigation;
        }

        public int getScreenLayout() {
            return screenLayout;
        }

        public void setScreenLayout(int screenLayout) {
            this.screenLayout = screenLayout;
        }

        public int getScreenDensity() {
            return screenDensity;
        }

        public void setScreenDensity(int screenDensity) {
            this.screenDensity = screenDensity;
        }

        public int getScreenWidth() {
            return screenWidth;
        }

        public void setScreenWidth(int screenWidth) {
            this.screenWidth = screenWidth;
        }

        public int getScreenHeight() {
            return screenHeight;
        }

        public void setScreenHeight(int screenHeight) {
            this.screenHeight = screenHeight;
        }

        public int getGlEsVersion() {
            return glEsVersion;
        }

        public void setGlEsVersion(int glEsVersion) {
            this.glEsVersion = glEsVersion;
        }

        public List<String> getSystemSharedLibrary() {
            return systemSharedLibrary;
        }

        public void setSystemSharedLibrary(List<String> systemSharedLibrary) {
            this.systemSharedLibrary = systemSharedLibrary;
        }

        public List<String> getSystemAvailableFeature() {
            return systemAvailableFeature;
        }

        public void setSystemAvailableFeature(List<String> systemAvailableFeature) {
            this.systemAvailableFeature = systemAvailableFeature;
        }

        public List<String> getNativePlatform() {
            return nativePlatform;
        }

        public void setNativePlatform(List<String> nativePlatform) {
            this.nativePlatform = nativePlatform;
        }

        public List<String> getSystemSupportedLocale() {
            return systemSupportedLocale;
        }

        public void setSystemSupportedLocale(List<String> systemSupportedLocale) {
            this.systemSupportedLocale = systemSupportedLocale;
        }

        public List<String> getGlExtension() {
            return glExtension;
        }

        public void setGlExtension(List<String> glExtension) {
            this.glExtension = glExtension;
        }
    }

    private DeviceBuild build;
    private DeviceOperator operator;
    private DeviceConfig config;

    public DeviceBuild getBuild() {
        return build;
    }

    public void setBuild(DeviceBuild build) {
        this.build = build;
    }

    public DeviceOperator getOperator() {
        return operator;
    }

    public void setOperator(DeviceOperator operator) {
        this.operator = operator;
    }

    public DeviceConfig getConfig() {
        return config;
    }

    public void setConfig(DeviceConfig config) {
        this.config = config;
    }
}

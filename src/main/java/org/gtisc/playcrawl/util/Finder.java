/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gtisc.playcrawl.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author meng
 */
public class Finder {

    private static final Path ASSET = Paths.get("asset");

    public static Path getPlatformMeta() {
        return ASSET.resolve("platform").resolve("meta.json");
    }

    public static Path getDeviceMeta() {
        return ASSET.resolve("device").resolve("meta.json");
    }

    public static Path getDevice(String name) {
        return ASSET.resolve("device").resolve(name + ".json");
    }
}

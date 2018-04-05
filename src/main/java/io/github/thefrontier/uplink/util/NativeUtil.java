package io.github.thefrontier.uplink.util;

import io.github.thefrontier.uplink.Uplink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;

import static io.github.thefrontier.uplink.Uplink.LOGGER;

public class NativeUtil {

    public static final File LINUX_64 = new File("uplink/linux_64/libdiscord-rpc.so");
    public static final File WINDOWS_64 = new File("uplink/windows_64/discord-rpc.dll");
    public static final File WINDOWS_32 = new File("uplink/windows_32/discord-rpc.dll");
    public static final File OSX_64 = new File("uplink/osx_64/libdiscord-rpc.dylib");

    public static void loadNativeLibrary() {
        try {
            String arch = System.getProperty("os.arch").toLowerCase();
            int bits = arch.contains("64") ? 64 : 32;

            if (SystemUtils.IS_OS_LINUX) {
                if (bits != 64) {
                    throw new RuntimeException("Uplink does NOT support 32bit Linux.");
                }
                FileUtils.copyInputStreamToFile(
                        Uplink.class.getResourceAsStream("/linux-x86-64/libdiscord-rpc.so"),
                        NativeUtil.LINUX_64);
                System.load(NativeUtil.LINUX_64.getAbsolutePath());
                LOGGER.info("Loaded linux_64 from " + NativeUtil.LINUX_64.getAbsolutePath());
            } else if (SystemUtils.IS_OS_WINDOWS) {
                if (bits == 64) {
                    FileUtils.copyInputStreamToFile(
                            Uplink.class.getResourceAsStream("/win32-x86-64/discord-rpc.dll"),
                            NativeUtil.WINDOWS_64);
                    System.load(NativeUtil.WINDOWS_64.getAbsolutePath());
                    LOGGER.info("Loaded windows_64 from " + NativeUtil.WINDOWS_64.getAbsolutePath());
                } else {
                    FileUtils.copyInputStreamToFile(
                            Uplink.class.getResourceAsStream("/win32-x86/discord-rpc.dll"),
                            NativeUtil.WINDOWS_32);
                    System.load(NativeUtil.WINDOWS_32.getAbsolutePath());
                    LOGGER.info("Loaded windows_32 from " + NativeUtil.WINDOWS_32.getAbsolutePath());
                }
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                FileUtils.copyInputStreamToFile(
                        Uplink.class.getResourceAsStream("/darwin/libdiscord-rpc.dylib"),
                        NativeUtil.OSX_64);
                System.load(NativeUtil.OSX_64.getAbsolutePath());
                LOGGER.info("Loaded osx_64 from " + NativeUtil.OSX_64.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.error("Error while loading native lib", e);
        }
    }
}
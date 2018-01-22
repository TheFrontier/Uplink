package io.github.thefrontier.uplink.util;

import net.minecraft.client.Minecraft;

public class MiscUtil {

    public static String getIGN() {
        return Minecraft.getMinecraft().getSession().getProfile().getName();
    }
}

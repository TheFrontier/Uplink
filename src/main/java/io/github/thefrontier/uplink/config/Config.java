package io.github.thefrontier.uplink.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Config {

    @Setting
    public DisplayData displayData = new DisplayData();

    @ConfigSerializable
    public static class DisplayData {

        @Setting
        public String clientId;

        @Setting
        public String smallDataUid;

        @Setting
        public String baseUrl;

        @Setting
        public boolean hideUnknownIps;
    }
}
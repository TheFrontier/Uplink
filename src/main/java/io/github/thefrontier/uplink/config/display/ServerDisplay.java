package io.github.thefrontier.uplink.config.display;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ServerDisplay {

    @Setting
    public String uid;

    @Setting
    public String key;

    @Setting
    public String name;

    public String getUid() {
        return uid;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public ServerDisplay self() {
        return this;
    }
}
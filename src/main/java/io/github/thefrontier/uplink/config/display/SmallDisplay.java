package io.github.thefrontier.uplink.config.display;

public class SmallDisplay {

    private String uid;

    private String key;

    private String name;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SmallDisplay self() {
        return this;
    }

    @Override
    public String toString() {
        return "SmallDisplay{" +
                "uid='" + uid + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
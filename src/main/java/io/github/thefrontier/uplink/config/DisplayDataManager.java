package io.github.thefrontier.uplink.config;

import com.google.common.reflect.TypeToken;
import io.github.thefrontier.uplink.config.display.ServerDisplay;
import io.github.thefrontier.uplink.config.display.SmallDataDisplay;
import ninja.leaping.configurate.json.JSONConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

public class DisplayDataManager {

    private final Config config;

    private final JSONConfigurationLoader smallDataLoader;
    private final JSONConfigurationLoader serversLoader;

    private Map<String, SmallDataDisplay> smallDataDisplays;
    private Map<String, ServerDisplay> serverDisplays;

    public DisplayDataManager(Config config) throws IOException, ObjectMappingException {
        this.config = config;

        this.smallDataLoader = JSONConfigurationLoader.builder()
                .setURL(new URL(this.config.displayData.baseUrl + "smallData/" + this.config.displayData.clientId + ".json"))
                .build();
        this.serversLoader = JSONConfigurationLoader.builder()
                .setURL(new URL(this.config.displayData.baseUrl + "servers/" + this.config.displayData.clientId + ".json"))
                .build();

        this.smallDataDisplays = this.smallDataLoader.load().getList(TypeToken.of(SmallDataDisplay.class)).stream()
                .collect(Collectors.toMap(SmallDataDisplay::getUid, SmallDataDisplay::self));
        this.serverDisplays = this.serversLoader.load().getList(TypeToken.of(ServerDisplay.class)).stream()
                .collect(Collectors.toMap(ServerDisplay::getUid, ServerDisplay::self));

        System.out.println("Small Data Uids: " + this.smallDataDisplays.keySet());
        System.out.println("Server Uids: " + this.serverDisplays.keySet());
    }

    public Map<String, SmallDataDisplay> getSmallDataDisplays() {
        return smallDataDisplays;
    }

    public Map<String, ServerDisplay> getServerDisplays() {
        return serverDisplays;
    }
}

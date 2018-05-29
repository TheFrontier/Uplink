package io.github.thefrontier.uplink.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.thefrontier.uplink.config.display.GuiDisplay;
import io.github.thefrontier.uplink.config.display.ServerDisplay;
import io.github.thefrontier.uplink.config.display.SmallDisplay;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class DisplayDataManager {

    private Map<String, String> guiDisplays;
    private Map<String, SmallDisplay> smallDisplays;
    private Map<String, ServerDisplay> serverDisplays;

    public DisplayDataManager(Logger logger, Config config) throws IOException, FileNotFoundException {
        Gson gson = new GsonBuilder().create();

        URL guiUrl = new URL(config.displayUrls.gui + config.clientId + ".json");
        URL smallUrl = new URL(config.displayUrls.small + config.clientId + ".json");
        URL serverUrl = new URL(config.displayUrls.server + config.clientId + ".json");

        logger.trace("Using GUI Data Full URL: " + guiUrl);
        logger.trace("Using Small Data Full URL: " + smallUrl);
        logger.trace("Using Server Data Full URL: " + serverUrl);

        SmallDisplay[] smallArr = gson.fromJson(new InputStreamReader(smallUrl.openStream()), SmallDisplay[].class);
        ServerDisplay[] serverArr = gson.fromJson(new InputStreamReader(serverUrl.openStream()), ServerDisplay[].class);

        logger.trace("Received Small Data: " + Arrays.toString(smallArr));
        logger.trace("Received Server Data: " + Arrays.toString(serverArr));

        this.guiDisplays = gson.fromJson(new InputStreamReader(guiUrl.openStream()), GuiDisplay.class).classNameToInfo;
        this.smallDisplays = Arrays.stream(smallArr)
                .collect(Collectors.toMap(SmallDisplay::getUid, SmallDisplay::self));
        this.serverDisplays = Arrays.stream(serverArr)
                .collect(Collectors.toMap(ServerDisplay::getUid, ServerDisplay::self));

        logger.trace("Loaded Small Data: " + this.smallDisplays.keySet());
        logger.trace("Loaded Servers: " + this.serverDisplays.keySet());
    }

    public Map<String, String> getGuiDisplays() {
        return guiDisplays;
    }

    public Map<String, SmallDisplay> getSmallDisplays() {
        return smallDisplays;
    }

    public Map<String, ServerDisplay> getServerDisplays() {
        return serverDisplays;
    }
}
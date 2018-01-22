package io.github.thefrontier.uplink;

import club.minnced.discord.rpc.DiscordRichPresence;
import io.github.thefrontier.uplink.config.Config;
import io.github.thefrontier.uplink.config.DisplayDataManager;
import io.github.thefrontier.uplink.config.display.ServerDisplay;
import io.github.thefrontier.uplink.config.display.SmallDataDisplay;
import io.github.thefrontier.uplink.util.MiscUtil;

import static io.github.thefrontier.uplink.util.TimeUtil.epochSecond;

public class PresenceManager {

    private final DisplayDataManager dataManager;
    private final Config config;

    private final DiscordRichPresence loadingGame = new DiscordRichPresence();
    private final DiscordRichPresence mainMenu = new DiscordRichPresence();
    private final DiscordRichPresence inGame = new DiscordRichPresence();

    public boolean currentlyIngame = false;

    public PresenceManager(DisplayDataManager dataManager, Config config) {
        this.dataManager = dataManager;
        this.config = config;

        loadingGame.state = "Loading the Game";
        loadingGame.largeImageKey = "state-default";
        loadingGame.largeImageText = "Minecraft";

        mainMenu.state = "In the Main Menu";
        mainMenu.largeImageKey = "state-default";
        mainMenu.largeImageText = "Minecraft";

        SmallDataDisplay smallData = dataManager.getSmallDataDisplays().get(this.config.displayData.smallDataUid);

        if (smallData == null) {
            return;
        }

        loadingGame.smallImageKey = smallData.key;
        loadingGame.smallImageText = smallData.name;

        mainMenu.smallImageKey = smallData.key;
        mainMenu.smallImageText = smallData.name;

        inGame.smallImageKey = smallData.key;
        inGame.smallImageText = smallData.name;
    }

    public DiscordRichPresence loadingGame() {
        loadingGame.startTimestamp = epochSecond();
        return loadingGame;
    }

    public DiscordRichPresence mainMenu() {
        mainMenu.startTimestamp = epochSecond();
        return mainMenu;
    }

    public DiscordRichPresence ingameMP(String ip, int playerCount, int maxPlayers) {
        ServerDisplay server = dataManager.getServerDisplays().get(ip);

        if (server != null) {
            inGame.largeImageKey = server.key;
            inGame.largeImageText = "IP: " + server.name;
        } else if (this.config.displayData.hideUnknownIps) {
            inGame.largeImageKey = "state-unknown-server";
            inGame.largeImageText = "Unknown Server";
        } else {
            inGame.largeImageKey = "state-unknown-server";
            inGame.largeImageText = "IP: " + ip;
        }

        inGame.state = "In a Server";
        inGame.details = "IGN: " + MiscUtil.getIGN();
        inGame.startTimestamp = epochSecond();
        inGame.partyId = ip;
        inGame.partySize = playerCount;
        inGame.partyMax = maxPlayers;

        return inGame;
    }

    public DiscordRichPresence updatePlayerCount(int playerCount) {
        inGame.partySize = playerCount;

        return inGame;
    }

    public DiscordRichPresence ingameSP(String world) {
        inGame.state = "In Singleplayer";
        inGame.details = "IGN: " + MiscUtil.getIGN();
        inGame.startTimestamp = epochSecond();
        inGame.largeImageKey = "state-default";
        inGame.largeImageText = "World: " + world;
        inGame.partyId = "";
        inGame.partySize = 0;
        inGame.partyMax = 0;

        return inGame;
    }
}

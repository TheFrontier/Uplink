package io.github.thefrontier.uplink;

import club.minnced.discord.rpc.DiscordRichPresence;
import io.github.thefrontier.uplink.config.Config;
import io.github.thefrontier.uplink.config.DisplayDataManager;
import io.github.thefrontier.uplink.config.display.ServerDisplay;
import io.github.thefrontier.uplink.config.display.SmallDisplay;
import io.github.thefrontier.uplink.util.MiscUtil;

import static io.github.thefrontier.uplink.util.MiscUtil.epochSecond;

class PresenceManager {

    private final DisplayDataManager dataManager;
    private final Config config;

    private final DiscordRichPresence loadingGame = new DiscordRichPresence();
    private final DiscordRichPresence gui = new DiscordRichPresence();
    private final DiscordRichPresence inGame = new DiscordRichPresence();

    private PresenceState curState = PresenceState.INIT;

    PresenceManager(DisplayDataManager dataManager, Config config) {
        this.dataManager = dataManager;
        this.config = config;

        loadingGame.state = "Loading the Game";
        loadingGame.largeImageKey = "state-default";
        loadingGame.largeImageText = "Minecraft";

        gui.largeImageKey = "state-default";
        gui.largeImageText = "Minecraft";

        SmallDisplay smallData = dataManager.getSmallDisplays().get(this.config.smallDataUid);

        if (smallData == null) {
            return;
        }

        loadingGame.smallImageKey = smallData.key;
        loadingGame.smallImageText = smallData.name;

        gui.smallImageKey = smallData.key;
        gui.smallImageText = smallData.name;

        inGame.smallImageKey = smallData.key;
        inGame.smallImageText = smallData.name;
    }

    // ------------------- Getters -------------------- //

    public PresenceState getCurState() {
        return curState;
    }

    public void setCurState(PresenceState curState) {
        this.curState = curState;
    }

    // -------------------- Mutators -------------------- //

    DiscordRichPresence loadingGame() {
        loadingGame.startTimestamp = epochSecond();
        return loadingGame;
    }

    DiscordRichPresence gui(Class<?> clazz) {
        gui.state = dataManager.getGuiDisplays().get(clazz.getSimpleName());
        gui.startTimestamp = epochSecond();

        return gui;
    }

    DiscordRichPresence ingameMP(String ip, int playerCount, int maxPlayers) {
        ServerDisplay server = dataManager.getServerDisplays().get(ip);

        if (server != null) {
            inGame.largeImageKey = server.key;
            inGame.largeImageText = "IP: " + server.name;
        } else if (this.config.hideUnknownIPs) {
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

    DiscordRichPresence updatePlayerCount(int playerCount, int maxPlayers) {
        inGame.partySize = playerCount;
        inGame.partyMax = maxPlayers;

        return inGame;
    }

    DiscordRichPresence ingameSP(String world) {
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

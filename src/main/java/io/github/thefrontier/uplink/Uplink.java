package io.github.thefrontier.uplink;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import com.google.common.reflect.TypeToken;
import io.github.thefrontier.uplink.config.Config;
import io.github.thefrontier.uplink.config.DisplayDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import ninja.leaping.configurate.json.JSONConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod(modid = Uplink.MOD_ID, name = Uplink.MOD_NAME, version = Uplink.VERSION, clientSideOnly = true)
public class Uplink {

    public static final String MOD_ID = "uplink";
    public static final String MOD_NAME = "Uplink";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static Uplink INSTANCE;

    private final DiscordRPC rpc = DiscordRPC.INSTANCE;

    private PresenceManager presenceManager;
    private DisplayDataManager dataManager;
    private Config config;

    private int curTick = 0;

    @Mod.EventHandler
    public void onConstruction(FMLConstructionEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        Path configPath = event.getModConfigurationDirectory().toPath().resolve("Uplink.json");

        if (Files.notExists(configPath)) {
            try {
                Files.copy(getClass().getResourceAsStream("Uplink.json"), configPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JSONConfigurationLoader loader = JSONConfigurationLoader.builder()
                .setPath(configPath).build();

        try {
            config = loader.load().getValue(TypeToken.of(Config.class));
        } catch (Exception e) {
            System.out.println("Can't load config");
            e.printStackTrace();
        }

        try {
            dataManager = new DisplayDataManager(config);
        } catch (Exception e) {
            System.out.println("Can't load data manager");
            e.printStackTrace();
        }

        presenceManager = new PresenceManager(dataManager, config);

        // Initialize rpc and callback handling

        rpc.Discord_Initialize(config.displayData.clientId, new DiscordEventHandlers(), false, null);

        Thread callbackHandler = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                rpc.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    rpc.Discord_Shutdown();
                }
            }
        }, "RPC-Callback-Handler");
        callbackHandler.start();

        Runtime.getRuntime().addShutdownHook(new Thread(callbackHandler::interrupt));

        rpc.Discord_UpdatePresence(presenceManager.loadingGame());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (curTick >= 320) {
            curTick = 0;

            ServerData curServer = Minecraft.getMinecraft().getCurrentServerData();

            if (curServer != null) {
                int playerCount = Integer.valueOf(StringUtils.stripControlCodes(curServer.populationInfo).split("/")[0]);
                rpc.Discord_UpdatePresence(presenceManager.updatePlayerCount(playerCount));
            }
        } else {
            curTick++;
        }
    }

    @SubscribeEvent
    public void onMainMenu(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && presenceManager.currentlyIngame) {
            presenceManager.currentlyIngame = false;
            rpc.Discord_UpdatePresence(presenceManager.mainMenu());
        }
    }

    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayerMP || event.getEntity() instanceof EntityPlayerSP)) {
            return;
        }

        if (presenceManager.currentlyIngame) {
            return;
        }

        presenceManager.currentlyIngame = true;

        ServerData curServer = Minecraft.getMinecraft().getCurrentServerData();

        if (curServer != null) {
            String[] popInfo = StringUtils.stripControlCodes(curServer.populationInfo).split("/");
            int playerCount = Integer.valueOf(popInfo[0]) + 1;
            int maxPlayers = Integer.valueOf(popInfo[1]);

            rpc.Discord_UpdatePresence(presenceManager.ingameMP(curServer.serverIP, playerCount, maxPlayers));
        } else {
            rpc.Discord_UpdatePresence(presenceManager.ingameSP(event.getWorld().getWorldInfo().getWorldName()));
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        presenceManager.currentlyIngame = false;
        rpc.Discord_UpdatePresence(presenceManager.mainMenu());
    }
}

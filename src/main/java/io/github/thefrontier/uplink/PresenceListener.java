package io.github.thefrontier.uplink;

import club.minnced.discord.rpc.DiscordRPC;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.apache.logging.log4j.Logger;

public class PresenceListener {

    private final DiscordRPC rpc;
    private final Logger logger;
    private final PresenceManager presenceManager;

    private int curTick = 0;
    private int curPlayerCount = 0;

    PresenceListener(DiscordRPC rpc, Logger logger, PresenceManager presenceManager) {
        this.rpc = rpc;
        this.logger = logger;
        this.presenceManager = presenceManager;
    }

    @SuppressWarnings("ConstantConditions")
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (presenceManager.getCurState() != PresenceState.INGAME) {
            curTick = 0;
            return;
        }

        if (curTick >= 1000) {
            curTick = 0;

            try {
                int playerCount = Minecraft.getMinecraft().getNetHandler().playerInfoList.size();
                int maxPlayers = Minecraft.getMinecraft().getNetHandler().currentServerMaxPlayers;

                if (this.curPlayerCount != playerCount) {
                    rpc.Discord_UpdatePresence(presenceManager.updatePlayerCount(playerCount, maxPlayers));
                    this.curPlayerCount = playerCount;
                    logger.info("Server Player Count: " + playerCount + "/" + maxPlayers);
                }
            } catch (NullPointerException ignored) {
            }
        } else {
            curTick++;
        }
    }

    @SubscribeEvent
    public void onMainMenu(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu && presenceManager.getCurState() != PresenceState.MENU_MAIN) {
            presenceManager.setCurState(PresenceState.MENU_MAIN);
            rpc.Discord_UpdatePresence(presenceManager.gui(GuiMainMenu.class));
        }
    }

    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        if (!(event.entity instanceof EntityPlayerMP || event.entity instanceof EntityPlayerSP)) {
            // Ignore non-players.
            return;
        }

        ServerData curServer = Minecraft.getMinecraft().func_147104_D();

        if (curServer != null) {
            if (presenceManager.getCurState() == PresenceState.INGAME) {
                // Player is already in a server.
                return;
            }

            rpc.Discord_UpdatePresence(presenceManager.ingameMP(curServer.serverIP, 0, 0));
        } else {
            rpc.Discord_UpdatePresence(presenceManager.ingameSP(event.world.getWorldInfo().getWorldName()));
        }

        presenceManager.setCurState(PresenceState.INGAME);
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        rpc.Discord_UpdatePresence(presenceManager.gui(GuiMainMenu.class));
        presenceManager.setCurState(PresenceState.MENU_MAIN);
    }
}
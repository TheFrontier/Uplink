package io.github.thefrontier.uplink;

import club.minnced.discord.rpc.DiscordRPC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class PresenceListener {

    private final DiscordRPC rpc;
    private final PresenceManager presenceManager;

    private int curTick = 0;
    private int curPlayerCount = 0;

    PresenceListener(DiscordRPC rpc, PresenceManager presenceManager) {
        this.rpc = rpc;
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
                int playerCount = Minecraft.getMinecraft().getConnection().getPlayerInfoMap().size();
                int maxPlayers = Minecraft.getMinecraft().getConnection().currentServerMaxPlayers;

                if (this.curPlayerCount != playerCount) {
                    rpc.Discord_UpdatePresence(presenceManager.updatePlayerCount(playerCount, maxPlayers));
                    this.curPlayerCount = playerCount;
                }
            } catch (NullPointerException ignored) {
            }
        } else {
            curTick++;
        }
    }

    @SubscribeEvent
    public void onMainMenu(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiMainMenu && presenceManager.getCurState() != PresenceState.MENU_MAIN) {
            presenceManager.setCurState(PresenceState.MENU_MAIN);
            rpc.Discord_UpdatePresence(presenceManager.mainMenu());
        }
    }

    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayerMP || event.getEntity() instanceof EntityPlayerSP)) {
            // Ignore non-players.
            return;
        }

        ServerData curServer = Minecraft.getMinecraft().getCurrentServerData();

        if (curServer != null) {
            if (presenceManager.getCurState() == PresenceState.INGAME) {
                // Player is already in a server.
                return;
            }

            rpc.Discord_UpdatePresence(presenceManager.ingameMP(curServer.serverIP, 0, 0));
        } else {
            rpc.Discord_UpdatePresence(presenceManager.ingameSP(event.getWorld().getWorldInfo().getWorldName()));
        }

        presenceManager.setCurState(PresenceState.INGAME);
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        rpc.Discord_UpdatePresence(presenceManager.mainMenu());
        presenceManager.setCurState(PresenceState.MENU_MAIN);
    }
}
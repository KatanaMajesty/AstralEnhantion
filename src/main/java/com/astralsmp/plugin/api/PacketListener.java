package com.astralsmp.plugin.api;

import io.netty.channel.*;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Интерфейс, который наследует в себе дефолтный org.bukkit.event.Listener,
 * а так же добавляет возможность слушать отправленные и полученные пакеты
 */
public interface PacketListener extends Listener {

    @EventHandler default void onJoin(PlayerJoinEvent event){
        injectPlayer(event.getPlayer());
    }

    @EventHandler default void onLeave(PlayerQuitEvent event){
        removePlayer(event.getPlayer());
    }

    static void removePlayer(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().b.a.k;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getName());
            return null;
        });
    }

    default void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {

            @Override
            public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                receivingPacketManager(packet, player);
                if (!getCancelled()) super.channelRead(channelHandlerContext, packet);
                else setCancelled(false);
            }

            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                sendingPacketManager(packet, player);
                if (!getCancelled()) super.write(channelHandlerContext, packet, channelPromise);
                else setCancelled(false);
            }

            public Player getPlayer() {
                return player;
            }

        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().b.a.k.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);

    }

    void receivingPacketManager(Object packet, Player p);

    void sendingPacketManager(Object packet, Player p);

    void setCancelled(boolean canceled);

    boolean getCancelled();
}

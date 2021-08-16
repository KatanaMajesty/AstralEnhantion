package com.astralsmp.plugin.data;

import com.astralsmp.plugin.api.PacketAPI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockEventHandler implements Listener {

    private final PacketAPI api = new PacketAPI();
    public static final List<CustomBlock> customBlocks = new ArrayList<>();

    @EventHandler public void onAstralBlockBreak(@NotNull BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.NOTE_BLOCK) {
            Block b = e.getBlock();
            CustomBlock cb = getCustomBlock(b);
            if (cb == null) return;
            System.out.println(1);
            e.setDropItems(false);
            runOnNearbyPlayers(b, 16, 16, 16, player -> api.sendPacket(PacketAPI.blockBreakSoundPacket(cb.getBreakSound(), b), player));
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) cb.getCorrectDrop(e.getPlayer(), b);
        }
    }

    // TODO: 16.08.2021 переписать этот класс, по возможности избавиться при помощи проверки блока ниже. Идея описана ниже
    /*
    Метод ниже можно заменить, просто напросто проверяя при ломании блока на наличие нотного блока выше. Если блок выше - нотный, то
    ломаем его player.breakBlock и в таком случае ему не отправляется пакет обновления нотного блока
     */
    @EventHandler
    public static void onBlockPhysics(BlockPhysicsEvent event) {
        int offset = 1;
        Block aboveBlock = event.getBlock().getLocation().add(0, offset, 0).getBlock();
        if (aboveBlock.getType() == Material.NOTE_BLOCK) {
            while (aboveBlock.getType() == Material.NOTE_BLOCK) {
                event.setCancelled(true);
                aboveBlock.getState().update(true, true);
                offset++;
                aboveBlock = event.getBlock().getLocation().add(0, offset, 0).getBlock();
            }
        }
        Block b = event.getBlock();
        if (b.getType() == Material.NOTE_BLOCK)
            event.setCancelled(true);
        if (b.getType().toString().toLowerCase().contains("sign"))
            return;
        event.getBlock().getState().update(true, false);
    }

    private static void runOnNearbyPlayers(Block b, double v1, double v2, double v3, Consumer<Player> consumer) {
        for (Entity e : b.getWorld().getNearbyEntities(b.getLocation().add(0.5, 0.5, 0.5), v1, v2, v3,
                entity -> entity instanceof Player)) {
            consumer.accept((Player) e);
        }
    }

    private static boolean isCustomBlock(Block b) {
        for (CustomBlock cb : customBlocks) {
            if (cb.isCorrectBlock(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Метод для получения объекта кастомного блока
     *
     * @param b блок, который подразумевается кастомным
     * @return null, если такого кастомного блока нет
     */
    @Nullable
    private static CustomBlock getCustomBlock(Block b) {
        for (CustomBlock cb : customBlocks) {
            if (cb.isCorrectBlock(b)) {
                return cb;
            }
        }
        return null;
    }


}

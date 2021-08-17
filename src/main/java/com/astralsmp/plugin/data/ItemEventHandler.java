package com.astralsmp.plugin.data;

import com.astralsmp.plugin.api.PacketAPI;
import com.astralsmp.plugin.modules.BlockRelated;
import net.minecraft.world.EnumHand;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemEventHandler implements Listener {

    public ItemEventHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    private final Plugin plugin;
    private static final PacketAPI api = new PacketAPI();
    static List<CustomItem> customItems = new ArrayList<>();

    @EventHandler public void onAstralBlockPlace(@NotNull PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        ItemStack main = p.getInventory().getItemInMainHand();
        ItemStack off = p.getInventory().getItemInOffHand();
        CustomItem customMain = getCustomItem(main);
        CustomItem customOff = getCustomItem(off);
        EnumHand hand;
        ItemStack item;
        if (customMain != null && customMain.isPlaceable()) {
            hand = EnumHand.a;
            item = main;
        } else if (customOff != null
                && customOff.isPlaceable()
                && e.getClickedBlock().getType() != Material.NOTE_BLOCK) {
            hand = EnumHand.b;
            item = off;
        } else return;
        place(e, item, hand);
    }

    private void place(PlayerInteractEvent event, ItemStack item, EnumHand hand) {
        CustomItem custom = getCustomItem(item);
        if (custom == null) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Block relative = block.getRelative(event.getBlockFace());
        for (int i = 0; i < 2; i++) {
            if (BlockRelated.isReplaceable(block.getType())) continue;
            if (containsEntity(relative)) {
                event.setCancelled(true);
                return;
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                Block posToPlace;
                if (BlockRelated.isReplaceable(block.getType()))
                    posToPlace = block;
                else posToPlace = relative;
                if (relative.getType() != Material.AIR && relative.getType() != Material.WATER) return;
                if (event.getPlayer().getGameMode() == GameMode.SURVIVAL && getCustomItem(item) != null)
                    item.setAmount(item.getAmount() - 1);
                runOnNearbyPlayers(posToPlace, 8, 8, 8,
                        p -> api.sendPacket(PacketAPI.blockPlaceSoundPacket(custom.getPlaceSound(), posToPlace), p));
                api.sendPacket(PacketAPI.handSwingAnimationPacket(hand, player), player);
                posToPlace.setType(Material.NOTE_BLOCK);
                posToPlace.setMetadata(CustomItem.CLASS_ID, new FixedMetadataValue(plugin, custom.getNmsName()));
                NoteBlock noteBlock = (NoteBlock) posToPlace.getBlockData();
                noteBlock.setInstrument(custom.getInstrument());
                noteBlock.setNote(custom.getNote());
                posToPlace.setBlockData(noteBlock);
            }
        }.runTask(plugin);
    }

    private static void runOnNearbyPlayers(Block b, double v1, double v2, double v3, Consumer<Player> consumer) {
        for (Entity e : b.getWorld().getNearbyEntities(b.getLocation().add(0.5, 0.5, 0.5), v1, v2, v3,
                entity -> entity instanceof Player)) {
            consumer.accept((Player) e);
        }
    }

    private static boolean containsEntity(@NotNull Block block) {
        if (!isUnPlaceableLoc(block))
            return !block.getLocation().getWorld().getNearbyEntities(
                    block.getLocation().add(0.5, 0.5, 0.5), 0.5, 0, 0.5).isEmpty();
        return false;
    }

    private static boolean isUnPlaceableLoc(@NotNull Block block) {
        return block.getType() != Material.AIR && block.getType() != Material.WATER && block.getType() != Material.LAVA;
    }

    /**
     * @return null, если такого кастомного предмета нет
     */
    @Nullable
    private static CustomItem getCustomItem(ItemStack item) {
        for (CustomItem custom : customItems) {
            if (custom.getItem().isSimilar(item)) return custom;
        }
        return null;
    }

}

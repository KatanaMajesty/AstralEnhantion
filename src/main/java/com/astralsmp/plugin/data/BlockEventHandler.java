package com.astralsmp.plugin.data;

import com.astralsmp.plugin.api.PacketAPI;
import com.astralsmp.plugin.api.PacketListener;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayOutBlockAction;
import net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect;
import net.minecraft.sounds.SoundEffects;
import net.minecraft.world.EnumHand;
import net.minecraft.world.item.context.ItemActionContext;
import net.minecraft.world.level.block.SoundEffectType;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class BlockEventHandler implements PacketListener {

    public BlockEventHandler(Plugin plugin) {
        this.plugin = plugin;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            api.sendPacket(PacketAPI.fatigueApplyPacket(p), p);
            injectPlayer(p);
        }
    }
    private final Plugin plugin;
    private boolean canceled;
    private static final PacketAPI api = new PacketAPI();
    public static final List<CustomBlock> customBlocks = new ArrayList<>();
    private static final Map<UUID, BoundingBox> moveBox = new HashMap<>();
    private static final Map<UUID, Block> breakMap = new HashMap<>();

    /*
    ====================
    СТАТИЧЕСКИЕ МЕТОДЫ
    ====================
     */

    @EventHandler public static void onCustomBlockPistonExtract(@NotNull BlockPistonExtendEvent e) {
        for (Block b : e.getBlocks()) {
            if (b.getType() == Material.NOTE_BLOCK && getCustomBlock(b) != null) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler public static void onCustomBlockPistonRetract(@NotNull BlockPistonRetractEvent e) {
        for (Block b : e.getBlocks()) {
            if (b.getType() == Material.NOTE_BLOCK && getCustomBlock(b) != null) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler public static void onCustomBlockWalk(@NotNull PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        Location l = e.getFrom();
        Block b = l.add(0, -1, 0).getBlock();
        CustomBlock cb = getCustomBlock(b);
        if (b.getType() != Material.NOTE_BLOCK
                || getCustomBlock(b) == null
                || player.getPose() == Pose.SNEAKING)
            return;
        UUID uuid = player.getUniqueId();
        Location l2 = e.getTo();
        if ((int) l.getX() - (int) l2.getX() == 0
                && (int) l.getZ() - (int) l2.getZ() == 0)
            return;

        double x = l.getX();
        double z = l.getZ();
        double r = 4;

        if (moveBox.containsKey(uuid) && !moveBox.get(uuid).contains(l.getX(), 0, l.getZ())) {
            moveBox.remove(uuid);
            for (Entity p : b.getWorld().getNearbyEntities(l, 8, 8, 8,
                    entity -> entity instanceof Player)) {
                api.sendPacket(PacketAPI.blockWalkSoundPacket(cb.getWalkSound(), b), (Player) p);
            }
        } else if (!moveBox.containsKey(uuid)) {
            BoundingBox box = new BoundingBox(x + r, 0, z + r, x - r, 0, z - r);;
            moveBox.put(uuid, box);
        }
    }

    @EventHandler public static void onCustomBlockBreak(@NotNull BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.NOTE_BLOCK) {
            Block b = e.getBlock();
            CustomBlock cb = getCustomBlock(b);
            if (cb == null) return;
            System.out.println("Astral Custom Block был сломан: " + cb.getClass().getName());
            e.setDropItems(false);
            runOnNearbyPlayers(b, 16, 16, 16, player -> api.sendPacket(PacketAPI.blockBreakSoundPacket(cb.getBreakSound(), b), player));
            if (e.getPlayer().getGameMode() != GameMode.CREATIVE) cb.getCorrectDrop(e.getPlayer(), b);
        }
    }

    @EventHandler public static void onCustomBlockClick(@NotNull PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getType() == Material.NOTE_BLOCK) {
            Block b = e.getClickedBlock();
            if (getCustomBlock(b) == null) return;
            Player p = e.getPlayer();
            ItemStack m = p.getInventory().getItemInMainHand();
            e.setCancelled(true);
            if (m.getType().isEdible() && p.getPose() == Pose.SNEAKING) {
                e.setCancelled(false);
                return;
            }
            if (m.getType().isBlock() && !m.getType().isAir())
                noteBlockPlaceableAgain(p, e, m, EnumHand.a);
        }
    }

    @EventHandler public static void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        api.sendPacket(PacketAPI.fatigueApplyPacket(p), p);
    }

    // TODO: 16.08.2021 переписать этот класс, по возможности избавиться при помощи проверки блока ниже. Идея описана ниже
    /*
    Метод ниже можно заменить, просто напросто проверяя при ломании блока на наличие нотного блока выше. Если блок выше - нотный, то
    ломаем его player.breakBlock и в таком случае ему не отправляется пакет обновления нотного блока
     */
    @EventHandler public static void onCustomBlockPhysics(BlockPhysicsEvent e) {
        int offset = 1;
        Block aboveBlock = e.getBlock().getLocation().add(0, offset, 0).getBlock();
        if (aboveBlock.getType() == Material.NOTE_BLOCK) {
            while (aboveBlock.getType() == Material.NOTE_BLOCK) {
                e.setCancelled(true);
                aboveBlock.getState().update(true, true);
                offset++;
                aboveBlock = e.getBlock().getLocation().add(0, offset, 0).getBlock();
            }
        }
        Block b = e.getBlock();
        if (b.getType() == Material.NOTE_BLOCK)
            e.setCancelled(true);
        if (b.getType().toString().toLowerCase().contains("sign"))
            return;
        e.getBlock().getState().update(true, false);
    }

    private static void runOnNearbyPlayers(Block b, double v1, double v2, double v3, Consumer<Player> consumer) {
        for (Entity e : b.getWorld().getNearbyEntities(b.getLocation().add(0.5, 0.5, 0.5), v1, v2, v3,
                entity -> entity instanceof Player)) {
            consumer.accept((Player) e);
        }
    }

    /**
     * Метод для получения объекта кастомного блока
     *
     * @param b блок, который подразумевается кастомным
     * @return null, если такого кастомного блока нет
     */
    @Nullable private static CustomBlock getCustomBlock(Block b) {
        for (CustomBlock cb : customBlocks) {
            if (cb.isCorrectBlock(b)) {
                return cb;
            }
        }
        return null;
    }

    private static void noteBlockPlaceableAgain(@NotNull Player p, @NotNull PlayerInteractEvent e, @NotNull ItemStack item, @NotNull EnumHand hand) {
        Block clicked = e.getClickedBlock();
        BlockFace f = e.getBlockFace();
        EnumDirection d = null;
        switch (f) {
            case DOWN -> d = EnumDirection.a;
            case UP -> d = EnumDirection.b;
            case NORTH -> d = EnumDirection.c;
            case SOUTH -> d = EnumDirection.d;
            case WEST -> d = EnumDirection.e;
            case EAST -> d = EnumDirection.f;
        }
        assert d != null;
        CraftPlayer craftPlayer = (CraftPlayer) p;
        net.minecraft.world.item.ItemStack nmsMain = CraftItemStack.asNMSCopy(item);
        Block rel = clicked.getRelative(f);
        if (isUnPlaceableLoc(rel)) return;
        if (containsEntity(rel)) return;
        nmsMain.placeItem(new ItemActionContext(
                craftPlayer.getHandle(), hand,
                MovingObjectPositionBlock.a(
                        new Vec3D(clicked.getX(), clicked.getY(), clicked.getZ()),
                        d, PacketAPI.getBlockPosition(clicked))
        ), hand);
        if (!isUnPlaceableLoc(rel)) return;
        net.minecraft.world.level.block.Block b = ((CraftBlock) rel).getNMS().getBlock();
        SoundEffectType sType = b.getStepSound(null);
        api.sendPacket(PacketAPI.blockPlaceSoundPacket(sType.getPlaceSound(), clicked.getRelative(f)), p);
    }

    private static boolean isUnPlaceableLoc(@NotNull Block block) {
        return block.getType() != Material.AIR && block.getType() != Material.WATER && block.getType() != Material.LAVA;
    }

    private static boolean containsEntity(@NotNull Block block) {
        if (!isUnPlaceableLoc(block))
            return !block.getLocation().getWorld().getNearbyEntities(
                    block.getLocation().add(0.5, 0.5, 0.5), 0.5, 0, 0.5).isEmpty();
        return false;
    }

    private static Location toLocation(BlockPosition bp, Player p) {
        return new Location(p.getWorld(), bp.getX(), bp.getY(), bp.getZ());
    }

    private static BukkitRunnable breakSoundThread(Block posBlock, Player p, UUID uuid, CustomBlock cb) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                    while (breakMap.containsKey(uuid)) {
                        if (breakMap.get(uuid) != posBlock)
                            break;
                        api.sendPacket(PacketAPI.blockHitSoundPacket(cb.getHitSound(), posBlock), p);
                        Thread.sleep(240);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.cancel();
            }
        };
    }

    private static BukkitRunnable breakAnimationThread(Block posBlock, Player p, UUID uuid, CustomBlock cb) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (cb == null) return;
                    double millis = cb.realBreakTime(p) * 1000;
                    long b = (long) millis;
                    long cycle = b / 9;
                    byte dS = 0;
                    do {
                        Thread.sleep(cycle);
                        if (breakMap.get(uuid)!= posBlock)
                            break;
                        byte finalDS = dS;
                        Bukkit.getScheduler().callSyncMethod(cb.getPlugin(), () -> {
                            runOnNearbyPlayers(posBlock, 30, 30, 30,
                                    player -> api.sendPacket(PacketAPI.blockBreakAnimationPacket(posBlock, finalDS), player));
                            return null;
                        });
                        if (dS == 9)
                            Bukkit.getScheduler().callSyncMethod(cb.getPlugin(), () -> {
                                api.receiveDigPacket(PacketAPI.blockDigProcessPacket(PacketPlayInBlockDig.EnumPlayerDigType.c, posBlock), p);
                                p.spawnParticle(
                                        Particle.BLOCK_CRACK,
                                        posBlock.getLocation().add(0.5, 0.5, 0.5),
                                        60, 0.1, 0.1, 0.1, 0.5,
                                        posBlock.getBlockData());
                                p.breakBlock(posBlock);
                                return null;
                            });
                        dS++;
                    } while (breakMap.containsKey(uuid) && dS < 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.cancel();
            }
        };
    }

    /*
    ======================
    PACKET LISTENER
    ======================
     */

    @Override
    public void receivingPacketManager(Object packet, Player player) {
        if (packet instanceof PacketPlayInBlockDig p) {
            if (player.getGameMode() == GameMode.CREATIVE) return;
            BlockPosition bp = p.b();
            Block b = toLocation(bp, player).getBlock();
            CustomBlock cb = getCustomBlock(b);
            UUID uuid = player.getUniqueId();
            PacketPlayInBlockDig.EnumPlayerDigType digEnum = p.d();
            if (breakMap.get(uuid) != null && breakMap.get(uuid) != b && (digEnum == PacketPlayInBlockDig.EnumPlayerDigType.b
                || digEnum == PacketPlayInBlockDig.EnumPlayerDigType.c)) {
                breakProcStop(this.plugin, b, uuid, player);
                return;
            } else if (digEnum == PacketPlayInBlockDig.EnumPlayerDigType.a) {
                if (cb == null) api.sendPacket(PacketAPI.fatigueRemovePacket(player), player);
                else {
                    api.sendPacket(PacketAPI.fatigueApplyPacket(player), player);
                    setCancelled(true);
                }
                breakMap.put(uuid, b);
                if (cb == null) return;
            } else return;
            breakSoundThread(b, player, uuid, cb).runTaskAsynchronously(cb.getPlugin());
            breakAnimationThread(b, player, uuid, cb).runTaskAsynchronously(cb.getPlugin());
            api.sendPacket(PacketAPI.blockBreakAnimationPacket(b, (byte) -1), player);
        }
    }

    @Override
    public void sendingPacketManager(Object packet, Player player) {
        if (packet instanceof PacketPlayOutNamedSoundEffect p
                && (p.b() == SoundEffects.mk
                || p.b() == SoundEffects.ml
                || p.b() == SoundEffects.mm
                || p.b() == SoundEffects.mn
                || p.b() == SoundEffects.mo
                || p.b() == SoundEffects.mp
                || p.b() == SoundEffects.mq
                || p.b() == SoundEffects.mr
                || p.b() == SoundEffects.ms
                || p.b() == SoundEffects.mt
                || p.b() == SoundEffects.mu
                || p.b() == SoundEffects.mv
                || p.b() == SoundEffects.mw
                || p.b() == SoundEffects.mx
                || p.b() == SoundEffects.my
                || p.b() == SoundEffects.mz)) {
            setCancelled(true);
        }
        if (packet instanceof PacketPlayOutBlockAction p) {
            BlockPosition bp = p.b();
            Block b = toLocation(bp, player).getBlock();
            if (b.getType() == Material.NOTE_BLOCK) setCancelled(true);
        }
    }

    /**
     * Отменить отправку пакета
     */
    @Override
    public void setCancelled(boolean canceled) {
        this.canceled = canceled;
    }

    @Override
    public boolean getCancelled() {
        return canceled;
    }

    private static void breakProcStop(Plugin plugin, Block posBlock, UUID uuid, Player p) {
        api.sendPacket(PacketAPI.fatigueApplyPacket(p), p);
        breakMap.remove(uuid);
        Bukkit.getScheduler().callSyncMethod(plugin, () -> {
            for (Entity e : posBlock.getWorld().getNearbyEntities(posBlock.getLocation(), 30, 30, 30,
                    entity -> entity instanceof Player)) {
                api.sendPacket(PacketAPI.blockBreakAnimationPacket(posBlock, (byte) -1), (Player) e);
            }
            return null;
        });
    }
}

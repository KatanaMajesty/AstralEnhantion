package com.astralsmp.plugin.data;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Consumer;

public abstract class CustomBlock {

    /*
    ====================
    НЕ ОТНОСЯЩИЕСЯ ПОЛЯ
    ====================
     */
    private final Plugin plugin;

    /*
    ====================
    ПОЛЯ ДЛЯ СОЗДАНИЯ БЛОКА
    ====================
     */
    private Instrument instrument;
    private Note note;
    @Nullable
    private Material material;
    private double hardness;
    private CustomItem dropItem;
    private Integer dropCount;
    @Nullable
    private CustomItem defDropItem;
    @Nullable
    private Integer defDropCount;
    private boolean isFortunable = false;
    private boolean isSilkTouchable = false;
    @Nullable
    private CustomItem silkDropItem;
    @Nullable
    private Integer silkDropCount;
    private String breakSound;
    private String hitSound;
    private String walkSound;
    private String fallSound;

    protected CustomBlock(Plugin plugin) {
        this.plugin = plugin;
        init();
        if (instrument == null
                | (dropItem == null && defDropItem == null)
                | breakSound == null
                | hitSound == null
                | walkSound == null
                | fallSound == null
                | dropCount == null) closeBlockCreation();
        BlockEventHandler.customBlocks.add(this);
    }

    /**
     * setInstrument(Instrument.BANJO);
     *         setNote(new Note(1));
     *         setMaterial(Material.IRON_PICKAXE);
     *         setHardness(3);
     *         setFortunable(true);
     *         setSilkTouchable(true);
     *         setSilkDropItem(new com.astralsmp.custom.items.AventurineOre(plugin));
     *         setSilkDropCount(1);
     *         setDropItem(new Aventurine(plugin));
     *         setDropCount(5);
     *         setDefDropItem(null);
     *         setDefDropCount(0);
     *         setBreakSound("block.stone.break");
     *         setHitSound("block.stone.hit");
     *         setWalkSound("block.stone.step");
     *         setFallSound("block.stone.fall");
     */
    protected abstract void init();

    private void closeBlockCreation() {
        throw new NullPointerException(String.format("Field of BLOCK is Null at %s", getClass().getName()));
    }

    public boolean isCorrectBlock(Block b) {
        if (b.getType() != Material.NOTE_BLOCK) return false;
        NoteBlock nb = (NoteBlock) b.getBlockData();
        return nb.getNote().equals(this.note) && nb.getInstrument() == this.instrument;
    }

    void getCorrectDrop(Player p, Block b) {
        String[] arr = {"pickaxe", "_axe", "shovel", "hoe"};
        ItemStack item = p.getInventory().getItemInMainHand();
        String pItem = item.toString().toLowerCase();
        String requiredItem = this.material == null ? "null" : this.material.toString().toLowerCase();
        World w = b.getWorld();
        ItemStack drop;
        for (String s : arr) {
            if (pItem.contains(s) && requiredItem.contains(s)) {
                if (getToolHierarchy(item.getType()) >= getToolHierarchy(material)) {
                    if (isSilkTouchable && item.containsEnchantment(Enchantment.SILK_TOUCH) && silkDropItem != null) {
                        drop = silkDropItem.getItem();
                        drop.setAmount(silkDropCount == null ? 1 : silkDropCount);
                    } else {
                        drop = dropItem.getItem();
                        drop.setAmount(dropCount);
                        if (isFortunable && item.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS))
                            drop.setAmount(fortuneMultiplier(item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS), drop.getAmount()));
                    }
                } else continue;
                w.dropItemNaturally(b.getLocation(), drop);
                return;
            }
        }
        if (defDropItem != null) {
            drop = defDropItem.getItem();
            drop.setAmount(defDropCount == null ? 1 : defDropCount);
        } else return;
        w.dropItemNaturally(b.getLocation(), drop);
    }

    /**
     * Чем больше число иерархии, тем сильнее инструмент
     * @param m
     * @return
     */
    private static int getToolHierarchy(Material m) {
        String s = m.toString().toLowerCase();
        int iHierarchy = 0;
        if (s.contains("pickaxe")) {
            switch (m) {
                case WOODEN_PICKAXE, GOLDEN_PICKAXE -> iHierarchy = 1;
                case STONE_PICKAXE -> iHierarchy = 2;
                case IRON_PICKAXE -> iHierarchy = 3;
                case DIAMOND_PICKAXE -> iHierarchy = 4;
                case NETHERITE_PICKAXE -> iHierarchy = 5;
            }
        } else if (s.contains("axe")) {
            switch (m) {
                case WOODEN_AXE, GOLDEN_AXE -> iHierarchy = 1;
                case STONE_AXE -> iHierarchy = 2;
                case IRON_AXE -> iHierarchy = 3;
                case DIAMOND_AXE -> iHierarchy = 4;
                case NETHERITE_AXE -> iHierarchy = 5;
            }
        } else if (s.contains("shovel")) {
            switch (m) {
                case WOODEN_SHOVEL, GOLDEN_SHOVEL -> iHierarchy = 1;
                case STONE_SHOVEL -> iHierarchy = 2;
                case IRON_SHOVEL -> iHierarchy = 3;
                case DIAMOND_SHOVEL -> iHierarchy = 4;
                case NETHERITE_SHOVEL -> iHierarchy = 5;
            }
        } else if (s.contains("hoe")) {
            switch (m) {
                case WOODEN_HOE, GOLDEN_HOE -> iHierarchy = 1;
                case STONE_HOE  -> iHierarchy = 2;
                case IRON_HOE -> iHierarchy = 3;
                case DIAMOND_HOE -> iHierarchy = 4;
                case NETHERITE_HOE -> iHierarchy = 5;
            }
        }
        return iHierarchy;
    }

    private static int getToolSpeed(Material m) {
        String s = m.toString().toLowerCase();
        int toolSpeed;
        if (!s.contains("axe") && !s.contains("shovel") && !s.contains("hoe")) return 1;
        if (s.contains("wooden")) toolSpeed = 2;
        else if (s.contains("stone")) toolSpeed = 4;
        else if (s.contains("iron")) toolSpeed = 6;
        else if (s.contains("diamond")) toolSpeed = 8;
        else if (s.contains("netherite")) toolSpeed = 9;
        else if (s.contains("gold")) toolSpeed = 12;
        else return 1;
        return toolSpeed;
    }

    private static int fortuneMultiplier(int lvl, int d) {
        final int defWeight = 2;
        Random rd = new Random();
        for (int i = 2; i < defWeight + lvl; i++) {
            if (rd.nextBoolean()) {
                d += i - 1;
                break;
            }
        }
        return d;
    }

    /*
    ====================
    ГЕТТЕРЫ
    ====================
     */

    public Plugin getPlugin() {
        return plugin;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public Note getNote() {
        return note;
    }

    @Nullable
    public Material getMaterial() {
        return material;
    }

    public double getHardness() {
        return hardness;
    }

    public CustomItem getDropItem() {
        return dropItem;
    }

    public Integer getDropCount() {
        return dropCount;
    }

    @Nullable
    public CustomItem getDefDropItem() {
        return defDropItem;
    }

    @Nullable
    public Integer getDefDropCount() {
        return defDropCount;
    }

    public boolean isFortunable() {
        return isFortunable;
    }

    public boolean isSilkTouchable() {
        return isSilkTouchable;
    }

    @Nullable
    public CustomItem getSilkDropItem() {
        return silkDropItem;
    }

    @Nullable
    public Integer getSilkDropCount() {
        return silkDropCount;
    }

    public String getBreakSound() {
        return breakSound;
    }

    public String getHitSound() {
        return hitSound;
    }

    public String getWalkSound() {
        return walkSound;
    }

    public String getFallSound() {
        return fallSound;
    }

    /*
    ====================
    СЕТТЕРЫ
    ====================
     */

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public void setMaterial(@Nullable Material material) {
        this.material = material;
    }

    public void setHardness(double hardness) {
        this.hardness = hardness;
    }

    public void setDropItem(CustomItem dropItem) {
        this.dropItem = dropItem;
    }

    public void setDropCount(Integer dropCount) {
        this.dropCount = dropCount;
    }

    public void setDefDropItem(@Nullable CustomItem defDropItem) {
        this.defDropItem = defDropItem;
    }

    public void setDefDropCount(@Nullable Integer defDropCount) {
        this.defDropCount = defDropCount;
    }

    public void setFortunable(boolean fortunable) {
        isFortunable = fortunable;
    }

    public void setSilkTouchable(boolean silkTouchable) {
        isSilkTouchable = silkTouchable;
    }

    public void setSilkDropItem(@Nullable CustomItem silkDropItem) {
        this.silkDropItem = silkDropItem;
    }

    public void setSilkDropCount(@Nullable Integer silkDropCount) {
        this.silkDropCount = silkDropCount;
    }

    public void setBreakSound(String breakSound) {
        this.breakSound = breakSound;
    }

    public void setHitSound(String hitSound) {
        this.hitSound = hitSound;
    }

    public void setWalkSound(String walkSound) {
        this.walkSound = walkSound;
    }

    public void setFallSound(String fallSound) {
        this.fallSound = fallSound;
    }
}

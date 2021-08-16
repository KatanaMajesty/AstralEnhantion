package com.astralsmp.plugin.data.block;

import com.astralsmp.plugin.data.CustomBlock;
import com.astralsmp.plugin.data.item.ItAventurineOre;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.plugin.Plugin;

public class AventurineOre extends CustomBlock {

    public AventurineOre(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected void init() {
        setInstrument(Instrument.BANJO);
        setNote(new Note(1));
        setMaterial(Material.IRON_PICKAXE);
        setHardness(3);
        setFortunable(true);
        setSilkTouchable(true);
//        setSilkDropItem(new com.astralsmp.custom.items.AventurineOre(getPlugin()));
        setSilkDropItem(null);
        setSilkDropCount(1);
//        setDropItem(new Aventurine(getPlugin()));
        setDropItem(new ItAventurineOre(getPlugin()));
        setDropCount(5);
        setDefDropItem(null);
        setDefDropCount(0);
        setBreakSound("block.stone.break");
        setHitSound("block.stone.hit");
        setWalkSound("block.stone.step");
        setFallSound("block.stone.fall");
    }
}

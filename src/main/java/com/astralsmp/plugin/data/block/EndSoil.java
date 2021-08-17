package com.astralsmp.plugin.data.block;

import com.astralsmp.plugin.data.CustomBlock;
import com.astralsmp.plugin.data.item.ItEndSoil;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.plugin.Plugin;

public class EndSoil extends CustomBlock {

    public EndSoil(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void init() {
        setInstrument(Instrument.BELL);
        setNote(new Note(1));
        setFortunable(false);
        setHardness(1.5);
        setDefDropItem(null);
        setDropItem(new ItEndSoil(getPlugin()));
        setDropCount(1);
        setMaterial(Material.STONE_SHOVEL);
        setBreakSound("block.dripstone_block.break");
        setWalkSound("block.dripstone_block.step");
        setFallSound("block.dripstone_block.hit");
        setHitSound("block.dripstone_block.hit");
    }
}

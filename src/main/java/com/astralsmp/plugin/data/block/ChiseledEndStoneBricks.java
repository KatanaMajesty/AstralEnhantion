package com.astralsmp.plugin.data.block;

import com.astralsmp.plugin.data.CustomBlock;
import com.astralsmp.plugin.data.item.ItChiseledEndStoneBricks;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.plugin.Plugin;

public class ChiseledEndStoneBricks extends CustomBlock {

    public ChiseledEndStoneBricks(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected void init() {
        setInstrument(Instrument.BELL);
        setNote(new Note(3));
        setMaterial(Material.WOODEN_PICKAXE);
        setHardness(3);
        setDropItem(new ItChiseledEndStoneBricks(getPlugin()));
        setDropCount(1);
        setDefDropItem(null);
        setBreakSound("block.stone.break");
        setHitSound("block.stone.hit");
        setWalkSound("block.stone.step");
        setFallSound("block.stone.fall");
    }
}

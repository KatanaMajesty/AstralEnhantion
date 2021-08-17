package com.astralsmp.plugin.data.block;

import com.astralsmp.plugin.data.CustomBlock;
import com.astralsmp.plugin.data.item.ItEndStem;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.plugin.Plugin;

public class EndStem extends CustomBlock {

    public EndStem(Plugin plugin) {
        super(plugin);
    }

    @Override
    protected void init() {
        ItEndStem item = new ItEndStem(getPlugin());
        setDefDropItem(item);
        setDefDropCount(1);
        setDropItem(item);
        setDropCount(1);
        setMaterial(Material.WOODEN_AXE);
        setHardness(2);
        setInstrument(Instrument.BELL);
        setNote(new Note(2));
        setBreakSound("custom.block.wood.break");
        setFallSound("custom.block.wood.hit");
        setHitSound("custom.block.wood.hit");
        setWalkSound("custom.block.wood.step");
    }
}

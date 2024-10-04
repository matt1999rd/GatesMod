package fr.moonshade.gates.tscapability;

import net.minecraft.world.level.Level;

public interface IIdTSStorage {
    int getId();
    void changeId(Level world);
    void setId(int newId);
    void setId(int newId,Level world);
}

package fr.mattmouss.gates.tscapability;

import net.minecraft.world.World;

public interface IIdTSStorage {
    int getId();
    void changeId(World world);
    void setId(int newId);
    void setId(int newId,World world);
}

package fr.mattmouss.gates.tscapability;

import net.minecraft.world.World;

public interface ITSStorage {
    int getId();
    void changeId(World world);
    void setId(int newId);
    void setId(int newId,World world);
    boolean getAnimationInWork();
    void startAnimation();
    void endAnimation();
}

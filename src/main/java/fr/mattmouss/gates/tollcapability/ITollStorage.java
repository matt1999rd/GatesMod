package fr.mattmouss.gates.tollcapability;

import fr.mattmouss.gates.animationboolean.IAnimationBoolean;
import net.minecraft.world.World;

public interface ITollStorage extends IAnimationBoolean {
    int getPrice();
    void setPrice(int price);
    int getId();
    void changeId(World world);
    void setId(int newId,World world);
    void setId(int newId);
}

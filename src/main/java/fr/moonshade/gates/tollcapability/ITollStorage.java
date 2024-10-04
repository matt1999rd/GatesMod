package fr.moonshade.gates.tollcapability;

import fr.moonshade.gates.animationboolean.IAnimationBoolean;
import net.minecraft.world.level.Level;

public interface ITollStorage extends IAnimationBoolean {
    int getPrice();
    void setPrice(int price);
    int getId();
    void changeId(Level world);
    void setId(int newId,Level world);
    void setId(int newId);
}

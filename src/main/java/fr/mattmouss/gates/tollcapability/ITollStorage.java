package fr.mattmouss.gates.tollfuckingcapability;

import fr.mattmouss.gates.animationboolean.IAnimationBoolean;

public interface ITollStorage extends IAnimationBoolean {
    int getPrice();
    void setPrice(int price);
    int getId();
    void changeId();
    void setId(int newId);
}

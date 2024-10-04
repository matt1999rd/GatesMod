package fr.moonshade.gates.tileentity;

public interface IPriceControllingTE {
    void lowerPrice();
    void raisePrice();
    int getPrice();
}

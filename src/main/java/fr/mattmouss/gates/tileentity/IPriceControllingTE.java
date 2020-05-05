package fr.mattmouss.gates.tileentity;

public interface IPriceControllingTE {
    void lowerPrice();
    void raisePrice();
    int getPrice();
}

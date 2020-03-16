package fr.mattmouss.gates.tileentity;

import net.minecraftforge.energy.IEnergyStorage;

public interface IControlIdTE {
    int getId();

    void changeId();

    IEnergyStorage getIdValue();

    void setId(int id);
}

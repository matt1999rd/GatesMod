package fr.mattmouss.gates.energystorage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.EnergyStorage;


public class PriceStorage extends EnergyStorage implements INBTSerializable<CompoundNBT> {

    public PriceStorage(int capacity, int maxTransfer) {
        //the standard price is 1 emerald
        super(capacity, maxTransfer,maxTransfer,1);
    }


    public void raisePrice() {
        this.energy += 1;
        if (this.energy > getMaxEnergyStored()) {
            this.energy = getEnergyStored();
        }
    }

    public void lowerPrice(){
        this.energy -= 1;
        if (this.energy<0){
            this.energy=0;
        }
    }

    public void setValue(int price){
        this.energy = price;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag_price = new CompoundNBT();
        //System.out.println("----------------------writing price :"+getEnergyStored()+"-------------");
        tag_price.putInt("price",getEnergyStored());
        return tag_price;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        //System.out.println("--------------------reading price :"+nbt.getInt("price")+"-------------");
        setValue(nbt.getInt("price"));
    }
}

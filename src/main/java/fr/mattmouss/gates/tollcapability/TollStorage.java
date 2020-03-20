package fr.mattmouss.gates.tollfuckingcapability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Random;

public class TollStorage implements ITollStorage, INBTSerializable<CompoundNBT> {

    private boolean startOpeningAnimation ;
    private boolean startClosingAnimation ;
    private int price;
    private int id;
    private static final Random random = new Random();

    public TollStorage(){
        startClosingAnimation = false;
        startOpeningAnimation = false;
        price = 1;
        id = MathHelper.abs(random.nextInt());
    }

    public void raisePrice(){
        price++;
    }

    public void lowerPrice(){
        price--;
    }

    @Override
    public int getPrice() {
        return price;
    }

    @Override
    public void setPrice(int price_in) {
        price = price_in;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void changeId() {
        System.out.println("changing id...");
        int newId= random.nextInt();
        setId(MathHelper.abs(newId));
    }

    @Override
    public void setId(int newId) {
        id = newId;
    }

    @Override
    public boolean isClosing() {
        return startClosingAnimation;
    }

    @Override
    public boolean isOpening() {
        return startOpeningAnimation;
    }

    @Override
    public void setBoolOpen(Boolean boolOpen) {
        startOpeningAnimation = boolOpen;
    }

    @Override
    public void setBoolClose(Boolean boolClose) {
        startClosingAnimation= boolClose;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        System.out.println("------------price written : "+price);
        System.out.println("------------id written : "+id);
        tag.putBoolean("open",startOpeningAnimation);
        tag.putBoolean("close",startClosingAnimation);
        tag.putInt("price",price);
        tag.putInt("id",id);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (isValidNbt(nbt)) {
            System.out.println("------------price read : "+nbt.getInt("price"));
            System.out.println("------------id read : "+nbt.getInt("id"));
            startOpeningAnimation= nbt.getBoolean("open");
            startClosingAnimation= nbt.getBoolean("close");
            price = nbt.getInt("price");
            id= nbt.getInt("id");
            return;
        }
        System.out.println("nothing has been found");
    }

    private boolean isValidNbt(CompoundNBT nbt) {
        System.out.println("nbt.contains(\"open\") = "+nbt.contains("open"));
        System.out.println("nbt.contains(\"close\") = "+nbt.contains("close"));
        System.out.println("nbt.contains(\"price\") = "+nbt.contains("price"));
        System.out.println("nbt.contains(\"id\") = "+nbt.contains("id"));
        return nbt.contains("open") && nbt.contains("close") && nbt.contains("price") && nbt.contains("id");
    }
}
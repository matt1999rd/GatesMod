package fr.mattmouss.gates.items;


import net.minecraftforge.registries.ObjectHolder;

public class ModItem {
    @ObjectHolder("gates:toll_gate_key")
    public static TollKeyItem TOLL_GATE_KEY = new TollKeyItem();
    @ObjectHolder("gates:card_key")
    public static CardKeyItem CARD_KEY = new CardKeyItem();


}

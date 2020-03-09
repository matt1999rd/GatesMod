package fr.mattmouss.gates.items;

import fr.mattmouss.gates.setup.ModSetup;
import net.minecraft.item.Item;

public class TollKeyItem extends Item {
    public TollKeyItem(){
        super(new Item.Properties().group(ModSetup.itemGroup));
        this.setRegistryName("toll_gate_key");
    }


}

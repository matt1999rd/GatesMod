package fr.mattmouss.gates.network;

import fr.mattmouss.gates.items.ModItem;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGiveCard {
    private int id;
    public PacketGiveCard(int id){
        this.id = id;
    }

    public PacketGiveCard(PacketBuffer buf){
        this.id = buf.readInt();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeInt(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ServerPlayerEntity entity = context.get().getSender();
            int freeSlot = entity.inventory.getFreeSlot();
            ItemStack cardKeyStack = new ItemStack(ModItem.CARD_KEY);
            cardKeyStack.setCount(1);
            CompoundNBT nbt = cardKeyStack.getOrCreateTag();
            nbt.putInt("id",id);
            if (freeSlot != -1){
                entity.inventory.setItem(freeSlot,cardKeyStack);
            }else {
                entity.spawnAtLocation(cardKeyStack);
            }
        });
        context.get().setPacketHandled(true);
    }
}

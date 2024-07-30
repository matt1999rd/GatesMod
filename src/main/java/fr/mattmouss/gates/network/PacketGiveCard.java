package fr.mattmouss.gates.network;

import fr.mattmouss.gates.items.ModItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketGiveCard {
    private final int id;
    public PacketGiveCard(int id){
        this.id = id;
    }

    public PacketGiveCard(FriendlyByteBuf buf){
        this.id = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ServerPlayer entity = context.get().getSender();
            assert entity != null;
            int freeSlot = entity.getInventory().getFreeSlot();
            ItemStack cardKeyStack = new ItemStack(ModItem.CARD_KEY);
            cardKeyStack.setCount(1);
            CompoundTag nbt = cardKeyStack.getOrCreateTag();
            nbt.putInt("id",id);
            if (freeSlot != -1){
                entity.getInventory().setItem(freeSlot,cardKeyStack);
            }else {
                entity.spawnAtLocation(cardKeyStack);
            }
        });
        context.get().setPacketHandled(true);
    }
}

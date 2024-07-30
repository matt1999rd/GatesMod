package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.IPriceControllingTE;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketRaisePrice {
    private final BlockPos pos;

    public PacketRaisePrice(FriendlyByteBuf buf){
        pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
    }

    public PacketRaisePrice(BlockPos pos_in){
        pos = pos_in;
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            IPriceControllingTE ipcte = (IPriceControllingTE) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos);
            assert ipcte != null;
            ipcte.raisePrice();
        });
        context.get().setPacketHandled(true);
    }

}

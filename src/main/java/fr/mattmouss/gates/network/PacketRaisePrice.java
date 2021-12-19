package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.IPriceControllingTE;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRaisePrice {
    private final BlockPos pos;

    public PacketRaisePrice(PacketBuffer buf){
        pos = buf.readBlockPos();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
    }

    public PacketRaisePrice(BlockPos pos_in){
        pos = pos_in;
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            IPriceControllingTE ipcte = (IPriceControllingTE) context.get().getSender().getLevel().getBlockEntity(pos);
            ipcte.raisePrice();
        });
        context.get().setPacketHandled(true);
    }

}

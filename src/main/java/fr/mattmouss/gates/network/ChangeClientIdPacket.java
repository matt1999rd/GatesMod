package fr.mattmouss.gates.network;


import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ChangeIdPacket {
    private final BlockPos pos;


    public ChangeIdPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
    }

    public ChangeIdPacket(BlockPos pos_in){
        pos = pos_in;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        AtomicInteger val = new AtomicInteger(0);
        context.get().enqueueWork(()->{
            TileEntity te =  context.get().getSender().getServerWorld().getTileEntity(pos);
            System.out.println("packet handled");
            if (te instanceof TollGateTileEntity){
                ((TollGateTileEntity)te).changeId();
                val.set(((TollGateTileEntity) te).getId());
            }else if (te instanceof TurnStileTileEntity){
                ((TurnStileTileEntity)te).changeId();
                val.set(((TurnStileTileEntity) te).getId());
            }else {
                return;
            }
        });
        Networking.INSTANCE.send(PacketDistributor.PLAYER.with(()-> {
            return context.get().getSender();
        }),new SetIdPacket(pos,val.get()));
        context.get().setPacketHandled(true);
    }
}

package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.network.PacketBuffer;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class blockTSPacket {
    private final BlockPos pos;


    public blockTSPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
    }

    public blockTSPacket(BlockPos pos_in){
        pos = pos_in;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            System.out.println("packet blockTS handled !");
            TurnStileTileEntity tste = (TurnStileTileEntity)context.get().getSender().getServerWorld().getTileEntity(pos);
            for (BlockPos pos1 : tste.getPositionOfBlockConnected()){
                TurnStileTileEntity te = (TurnStileTileEntity) context.get().getSender().getServerWorld().getTileEntity(pos1);
                te.blockTS();
            }
        });
        context.get().setPacketHandled(true);
    }
}

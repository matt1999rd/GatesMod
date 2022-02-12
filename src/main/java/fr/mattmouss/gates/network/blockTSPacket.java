package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.AbstractTurnStileTileEntity;
import net.minecraft.network.PacketBuffer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
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
            TileEntity te= Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos);
            List<BlockPos> posList;
            AbstractTurnStileTileEntity atste = (AbstractTurnStileTileEntity) te;
            assert atste != null;
            posList=atste.getPositionOfBlockConnected();
            for (BlockPos pos1 : posList){
                AbstractTurnStileTileEntity atste1 = (AbstractTurnStileTileEntity) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos1);
                assert atste1 != null;
                atste1.blockTS();
            }
        });
        context.get().setPacketHandled(true);
    }
}

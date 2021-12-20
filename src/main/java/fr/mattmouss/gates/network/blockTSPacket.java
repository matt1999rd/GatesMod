package fr.mattmouss.gates.network;

import fr.mattmouss.gates.tileentity.RedstoneTurnStileTileEntity;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.network.PacketBuffer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class blockTSPacket {
    private final BlockPos pos;
    private final boolean isRedStoneSpecified;


    public blockTSPacket(PacketBuffer buf) {
        pos = buf.readBlockPos();
        isRedStoneSpecified = buf.readBoolean();
    }

    public blockTSPacket(BlockPos pos_in,boolean isRedStoneSpecified){
        pos = pos_in;
        this.isRedStoneSpecified = isRedStoneSpecified;
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
        buf.writeBoolean(isRedStoneSpecified);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            System.out.println("packet blockTS handled !");
            TileEntity te= Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos);
            List<BlockPos> posList;
            if (!isRedStoneSpecified) {
                TurnStileTileEntity tste = (TurnStileTileEntity) te;
                assert tste != null;
                posList=tste.getPositionOfBlockConnected();
            }else {
                RedstoneTurnStileTileEntity rtste = (RedstoneTurnStileTileEntity) te;
                assert rtste != null;
                posList=rtste.getPositionOfBlockConnected();
            }
            for (BlockPos pos1 : posList){
                if (!isRedStoneSpecified){
                    TurnStileTileEntity tste1 = (TurnStileTileEntity) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos1);
                    assert tste1 != null;
                    tste1.blockTS();
                }else {
                    RedstoneTurnStileTileEntity rtste1 = (RedstoneTurnStileTileEntity) Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos1);
                    assert rtste1 != null;
                    rtste1.blockTS();
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}

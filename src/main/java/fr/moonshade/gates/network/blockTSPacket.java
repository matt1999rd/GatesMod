package fr.moonshade.gates.network;

import fr.moonshade.gates.tileentity.AbstractTurnStileTileEntity;
import net.minecraft.network.FriendlyByteBuf;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class blockTSPacket {
    private final BlockPos pos;


    public blockTSPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    public blockTSPacket(BlockPos pos_in){
        pos = pos_in;
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            System.out.println("packet blockTS handled !");
            BlockEntity te= Objects.requireNonNull(context.get().getSender()).getLevel().getBlockEntity(pos);
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

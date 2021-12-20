package fr.mattmouss.gates.network;

import fr.mattmouss.gates.GatesMod;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketMarkDirty {
    private final BlockPos pos;
    private final boolean isDirty;

    public PacketMarkDirty(BlockPos pos,boolean isDirty){
        this.pos = pos;
        this.isDirty = isDirty;
    }
    public PacketMarkDirty(PacketBuffer buf){
        this.pos = buf.readBlockPos();
        isDirty = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeBoolean(isDirty);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            World world = Objects.requireNonNull(context.get().getSender()).level;
            CardGetterTileEntity cgte = (CardGetterTileEntity)world.getBlockEntity(pos);
            if (cgte == null) GatesMod.logger.warning("NO more of this fucking function that are not working !! ");
            else cgte.markIdDirty(isDirty);
        });
        context.get().setPacketHandled(true);
    }
}

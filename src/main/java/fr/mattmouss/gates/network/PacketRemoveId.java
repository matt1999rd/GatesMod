package fr.mattmouss.gates.network;

import fr.mattmouss.gates.energystorage.IdTracker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class PacketRemoveId {
    private final BlockPos pos;
    private final int id;

    public PacketRemoveId(BlockPos pos,int id){
        this.pos = pos;
        this.id = id;
    }

    public PacketRemoveId(PacketBuffer buf){
        this.pos = buf.readBlockPos();
        this.id = buf.readInt();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(pos);
        buf.writeInt(id);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ServerWorld world = Objects.requireNonNull(context.get().getSender()).getLevel();
            IdTracker idTracker = world.getDataStorage().computeIfAbsent(IdTracker::new, "idgates");
            idTracker.removeId(id);
        });
    }
}

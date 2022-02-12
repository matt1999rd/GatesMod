package fr.mattmouss.gates.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class movePlayerPacket {
    private final BlockPos te_pos;
    private final UUID player_uuid;
    private final boolean fromExit;

    public movePlayerPacket(BlockPos pos, UUID player_uuid, boolean fromExit_in){
        te_pos = pos;
        this.player_uuid = player_uuid;
        fromExit=fromExit_in;
    }

    public movePlayerPacket(PacketBuffer buf){
        te_pos =buf.readBlockPos();
        long lsb = buf.readLong();
        long msb = buf.readLong();
        player_uuid = new UUID(msb,lsb);
        fromExit = buf.readBoolean();
    }

    public void toBytes(PacketBuffer buf){
        buf.writeBlockPos(te_pos);
        buf.writeLong(player_uuid.getLeastSignificantBits());
        buf.writeLong(player_uuid.getMostSignificantBits());
        buf.writeBoolean(fromExit);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ServerWorld serverWorld = Objects.requireNonNull(context.get().getSender()).getLevel();
            TileEntity te = serverWorld.getBlockEntity(te_pos);
            Entity entity =  serverWorld.getEntity(player_uuid);
            if (entity == null){
                System.out.println("no entity found with given id !!");
            }
            if (!(entity instanceof ServerPlayerEntity)){
                assert entity != null;
                System.out.println("error of id !!! the given entity is "+entity.getClass());
                return;
            }
            ServerPlayerEntity player = (ServerPlayerEntity)entity;
            assert te != null;
            Direction facing = te.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            Vector2f rot =player.getRotationVector();
            Direction offsetDirection=(fromExit)? facing : facing.getOpposite();
            BlockPos final_pos = te_pos.relative(offsetDirection) ;
            player.absMoveTo(final_pos.getX(),final_pos.getY(),final_pos.getZ(),rot.y,rot.x);
        });
    }

}

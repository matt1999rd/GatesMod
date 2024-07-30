package fr.mattmouss.gates.network;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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

    public movePlayerPacket(FriendlyByteBuf buf){
        te_pos =buf.readBlockPos();
        long lsb = buf.readLong();
        long msb = buf.readLong();
        player_uuid = new UUID(msb,lsb);
        fromExit = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeBlockPos(te_pos);
        buf.writeLong(player_uuid.getLeastSignificantBits());
        buf.writeLong(player_uuid.getMostSignificantBits());
        buf.writeBoolean(fromExit);
    }

    public void handle(Supplier<NetworkEvent.Context> context){
        context.get().enqueueWork(()->{
            ServerLevel serverWorld = Objects.requireNonNull(context.get().getSender()).getLevel();
            BlockEntity te = serverWorld.getBlockEntity(te_pos);
            Entity entity =  serverWorld.getEntity(player_uuid);
            if (entity == null){
                System.out.println("no entity found with given id !!");
            }
            if (!(entity instanceof ServerPlayer)){
                assert entity != null;
                System.out.println("error of id !!! the given entity is "+entity.getClass());
                return;
            }
            ServerPlayer player = (ServerPlayer)entity;
            assert te != null;
            Direction facing = te.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            Vec2 rot =player.getRotationVector();
            Direction offsetDirection=(fromExit)? facing : facing.getOpposite();
            BlockPos final_pos = te_pos.relative(offsetDirection) ;
            player.absMoveTo(final_pos.getX(),final_pos.getY(),final_pos.getZ(),rot.y,rot.x);
        });
    }

}

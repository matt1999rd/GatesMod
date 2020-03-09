package fr.mattmouss.gates.doors;


import fr.mattmouss.gates.gui.TGContainer;
import fr.mattmouss.gates.tileentity.GarageTileEntity;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.registries.ObjectHolder;

import java.util.List;

public class ModBlock {
    //enregistrement avec ObjectHolder des blocks qui ne sont pas des portes

    @ObjectHolder("gates:toll_gate")
    public static TollGate TOLL_GATE = new TollGate();

    @ObjectHolder("gates:toll_gate")
    public static TileEntityType<TollGateTileEntity> TOLL_GATE_ENTITY_TYPE;

    @ObjectHolder("gates:toll_gate")
    public static ContainerType<TGContainer> TOLLGATE_CONTAINER;

    @ObjectHolder("gates:garage_door")
    public static TileEntityType<GarageTileEntity> GARAGE_TILE_TYPE;



    public static Direction getDirectionFromEntity(LivingEntity placer, BlockPos pos) {
        Vec3d vec = placer.getPositionVec();
        Direction d = Direction.getFacingFromVector(vec.x-pos.getX(),vec.y-pos.getY(),vec.z-pos.getZ());
        if (d==Direction.DOWN || d==Direction.UP){
            return Direction.NORTH;
        }
        return d;
    }

    public static void addConnectedToCtlrUnit(List<BlockPos> posList,Direction extDirection,BlockPos pos,Direction facingDirection){
        posList.add(pos.offset(extDirection));
        posList.add(pos.offset(extDirection,2));
        posList.add(pos.up());
        posList.add(pos.up(2));
        posList.add(pos.offset(facingDirection));
    }

    public static Direction getDirectionOfExtBlock(Direction direction,DoorHingeSide dhs){
        return  (dhs == DoorHingeSide.RIGHT)?direction.rotateYCCW():direction.rotateY();
    }

    public static DoorHingeSide getHingeSideFromEntity(LivingEntity entity, BlockPos pos, Direction direction) {
        switch (direction){
            case DOWN:
            case UP:
            default:
                throw new IllegalArgumentException("No such direction authorised !!");
            case NORTH:
                return (entity.getPositionVec().x<pos.getX()+0.5)?DoorHingeSide.RIGHT:DoorHingeSide.LEFT;
            case SOUTH:
                return (entity.getPositionVec().x<pos.getX()+0.5)?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            case WEST:
                return (entity.getPositionVec().z<pos.getZ()+0.5)?DoorHingeSide.LEFT:DoorHingeSide.RIGHT;
            case EAST:
                return (entity.getPositionVec().z<pos.getZ()+0.5)?DoorHingeSide.RIGHT:DoorHingeSide.LEFT;
        }
    }


}

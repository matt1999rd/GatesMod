package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class TollKeyItem extends KeyItem {

    public TollKeyItem(){
        super();
        this.setRegistryName("toll_gate_key");
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        PlayerEntity entity = context.getPlayer();
        World world = context.getLevel();
        Hand hand = context.getHand();
        assert entity != null;
        ItemStack stack = entity.getItemInHand(hand);
        TileEntity te = world.getBlockEntity(pos);
        if (!(te instanceof TollGateTileEntity)){
            //we exit the function if it is not a TollGateTileEntity
            return super.useOn(context);
        }
        if (!(te.getBlockState().getBlock() instanceof TollGate)){
            throw new IllegalStateException("Corrupted world : a tile entity exists when the block associated did not");
        }
        boolean isControlUnit = te.getBlockState().getValue(TollGate.TG_POSITION) == TollGPosition.CONTROL_UNIT;
        if (!isControlUnit){
            //we don't open the gui if the block clicked is not compared to the control unit part
            return super.useOn(context);
        }

        BlockPos registeredPos = getTGPosition(stack, world);
        //if someone takes the key from the creative tab it will not have any BlockPos, and we set the blockPos to the present tollgate
        //if this key has a blockPos that don't correspond to a tollgate anymore (because of destroyed block) we set the blockPos also
        if (registeredPos == null) {
            setTGPosition(stack, world, pos);
            registeredPos = getTGPosition(stack, world);
        }


        if (!pos.equals(registeredPos)) {
            //System.out.println("the registered pos is not the pos of this block");
            //System.out.println("pos of toll gate key attribute :"+registeredPos);
            //System.out.println("pos of toll gate :"+pos);
            return super.useOn(context);
        }
        TollGateTileEntity tgte = (TollGateTileEntity) te;

        if (isPlayerFacingTheRightFace(tgte, entity, pos)) {
            //System.out.println("the player is a technician ");
            tgte.setSide(false);
            if (!world.isClientSide) {
                NetworkHooks.openGui((ServerPlayerEntity) entity, tgte, tgte.getBlockPos());
            }
        }
        return super.useOn(context);

    }

    //this function checks if the player is facing the key aperture face
    private boolean isPlayerFacingTheRightFace(TollGateTileEntity tgte, PlayerEntity entity,BlockPos pos) {
        BlockState state = tgte.getBlockState();

        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        Direction entity_looking_direction = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = state.getValue(BlockStateProperties.DOOR_HINGE);
        /*for that we need to click on the control-unit part(1) and on the side corresponding to the rotated direction
                     ClockWise if the rotation axe is right(2)
         and CounterClockWise  if the rotation axe is left(3)

         * */
        return (state.getValue(TollGate.TG_POSITION) == TollGPosition.CONTROL_UNIT) &&
                ((entity_looking_direction==facing.getClockWise() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.getCounterClockWise() && (dhs == DoorHingeSide.LEFT)));

    }




}

package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.doors.TollGate;
import fr.mattmouss.gates.enum_door.TollGPosition;
import fr.mattmouss.gates.setup.ModSetup;
import fr.mattmouss.gates.tileentity.TollGateTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.MapItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoorHingeSide;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class TollKeyItem extends KeyItem {

    //TODO : change name of class to make this item usefull for toll gate and turn stile and to open the tech gui -> TechKeyItem

    public TollKeyItem(){
        super();
        this.setRegistryName("toll_gate_key");
    }

    @Override
    public Item asItem() {
        return this;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        PlayerEntity entity = context.getPlayer();
        World world = context.getWorld();
        Hand hand = context.getHand();
        ItemStack stack = entity.getHeldItem(hand);
        if (!(world.getTileEntity(pos) instanceof TollGateTileEntity)){
            //we exit the function if it is not a TollGateTileEntity
            return super.onItemUse(context);
        }
        //if someone takes the key from the creative tab it will not have any BlockPos and we set the blockPos to the present toll gate
        //if this key has a blockPos that don't correspond to a toll gate anymore (because of destroyed block) we set the blockPos also
        BlockPos registeredPos = getTGPosition(stack,world);
        if (registeredPos == null){
            setTGPosition(stack,world,pos);
            registeredPos = getTGPosition(stack,world);
        }


        if (!pos.equals(registeredPos)){
            System.out.println("the registered pos is not the pos of this block");
            System.out.println("pos of toll gate key attribute :"+registeredPos);
            System.out.println("pos of toll gate :"+pos);
            return super.onItemUse(context);
        }
        TollGateTileEntity tgte = (TollGateTileEntity) world.getTileEntity(pos);
        tgte.changeId();

        if (isPlayerFacingTheRightFace(tgte,entity,pos)){
            System.out.println("the player is a technician ");
            tgte.setSide(false);

            if (!world.isRemote) {
                NetworkHooks.openGui((ServerPlayerEntity) entity, tgte, tgte.getPos());
            }

        }
        return super.onItemUse(context);
    }

    //this function checks if the player is facing the key aperture face
    private boolean isPlayerFacingTheRightFace(TollGateTileEntity tgte, PlayerEntity entity,BlockPos pos) {
        BlockState state = tgte.getBlockState();

        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        Direction entity_looking_direction = Functions.getDirectionFromEntity(entity,pos);
        DoorHingeSide dhs = state.get(BlockStateProperties.DOOR_HINGE);
        /*for that we need to click on the control-unit part(1) and on the side corresponding to the rotate direction
                     ClockWise if the rotation axe is right(2)
         and CounterClockWise  if the rotation axe is left(3)

         * */
        return (state.get(TollGate.TG_POSITION) == TollGPosition.CONTROL_UNIT) &&
                ((entity_looking_direction==facing.rotateY() && (dhs == DoorHingeSide.RIGHT))||
                (entity_looking_direction==facing.rotateYCCW() && (dhs == DoorHingeSide.LEFT)));

    }




}

package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.TurnStile;
import fr.mattmouss.gates.tileentity.TurnStileTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class TurnStileKeyItem extends KeyItem {
    public TurnStileKeyItem() {
        super();
        this.setRegistryName("turn_stile_key");
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        BlockPos pos = context.getClickedPos();
        PlayerEntity entity = context.getPlayer();
        World world = context.getLevel();
        Hand hand = context.getHand();
        assert entity != null;
        ItemStack stack = entity.getItemInHand(hand);
        TileEntity te= world.getBlockEntity(pos);
        if (!(te instanceof TurnStileTileEntity)){
            //we exit the function if it is not a TurnStileTileEntity
            return super.useOn(context);
        }

        if (!(te.getBlockState().getBlock() instanceof TurnStile)){
            throw new IllegalStateException("Corrupted world : a tile entity exists when the block associated did not");
        }
        TurnStile clickedTurnStile = (TurnStile) te.getBlockState().getBlock();
        boolean isControlUnit = clickedTurnStile.isControlUnit(te.getBlockState());
        if (!isControlUnit){
            //we don't open the gui if the block clicked is not compared to the control unit part
            return super.useOn(context);
        }

        //if someone takes the key from the creative tab it will not have any BlockPos, and we set the blockPos to the present turn stile
        //if this key has a blockPos that isn't a turn stile anymore, (because of destroyed block) we set the blockPos also
        BlockPos registeredPos = getTSPosition(stack,world);
        if (registeredPos == null){
            setTSPosition(stack,world,pos);
            registeredPos = getTSPosition(stack,world);
        }


        if (!pos.equals(registeredPos)){
            System.out.println("the registered pos is not the pos of this block");
            System.out.println("pos of turn stile key attribute :"+registeredPos);
            System.out.println("pos of turn stile :"+pos);
            // the player try to configure a turn stile which is not his own
            return super.useOn(context);
        }
        TurnStileTileEntity tste = (TurnStileTileEntity) world.getBlockEntity(pos);
        if (!world.isClientSide) {
            assert tste != null;
            NetworkHooks.openGui((ServerPlayerEntity) entity, tste, tste.getBlockPos());
        }
        return super.useOn(context);

    }


}

package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.GarageDoor;
import fr.mattmouss.gates.setup.ModSetup;

import fr.mattmouss.gates.util.Functions;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;


public class GarageDoorItem extends BlockItem {
    public GarageDoorItem(GarageDoor garageDoor) {
        super(garageDoor,new Item.Properties().tab(ModSetup.itemGroup));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (allBlockReplacedAreAir(new BlockPlaceContext(context)) && hasSupportToStay(new BlockPlaceContext(context))){
            return super.useOn(context);
        }
        System.out.println("block not created");
        return InteractionResult.FAIL;
    }

    // the function is not working  because we need to know where the first block will be created it depends on where the player clicked
    // need to check the onItemUse function of blockItem to know where it put the first value
    private boolean allBlockReplacedAreAir(BlockPlaceContext context){
        Level world = context.getLevel();
        Player entity=context.getPlayer();
        BlockPos pos_base =context.getClickedPos();
        assert entity != null;
        Direction facing = Functions.getDirectionFromEntity(entity,pos_base);
        Direction dir_left_section=facing.getClockWise();
        List<BlockPos> posList = new ArrayList<>();
        //block where we put the base block
        posList.add(pos_base);
        //back blocks
        posList.add(pos_base.above().relative(facing.getOpposite()));
        posList.add(pos_base.above()
                .relative(facing.getOpposite())
                .relative(dir_left_section));
        //left block
        posList.add(pos_base.relative(dir_left_section));
        posList.add(pos_base.relative(dir_left_section).above());

        for (BlockPos pos_in : posList){
            //if the block is not air we return false
            if (!(world.getBlockState(pos_in).getBlock() instanceof AirBlock)){
                System.out.println("the block pos that mess it up :"+pos_in);
                System.out.println("Not working block :"+world.getBlockState(pos_in).getBlock());
                return false;
            }
        }
        return true;
    }

    private boolean hasSupportToStay(BlockPlaceContext context){
        Level world = context.getLevel();
        Player entity=context.getPlayer();
        BlockPos pos_base =context.getClickedPos();
        assert entity != null;
        Direction facing = Functions.getDirectionFromEntity(entity,pos_base);
        Direction dir_left_section=facing.getClockWise();
        List<BlockPos> posList = new ArrayList<>();
        //support at the bottom
        posList.add(pos_base.below());
        posList.add(pos_base.relative(dir_left_section).below());
        //support on the right
        posList.add(pos_base.relative(dir_left_section.getOpposite()));
        posList.add(pos_base.relative(dir_left_section.getOpposite()).above());
        //support on the left
        posList.add(pos_base.relative(dir_left_section,2));
        posList.add(pos_base.relative(dir_left_section,2).above());
        //support on the top front
        posList.add(pos_base.above(2).relative(dir_left_section.getOpposite()));
        posList.add(pos_base.above(2));
        posList.add(pos_base.above(2).relative(dir_left_section));
        posList.add(pos_base.above(2).relative(dir_left_section,2));
        //support on the top back
        posList.add(pos_base.above(2).relative(dir_left_section.getOpposite()).relative(facing.getOpposite()));
        posList.add(pos_base.above(2).relative(dir_left_section,2).relative(facing.getOpposite()));
        for (BlockPos pos : posList){
            if (!(world.getBlockState(pos).getMaterial().blocksMotion())){
                System.out.println("the block pos that mess it up :"+pos);
                System.out.println("Not working block :"+world.getBlockState(pos).getBlock());
                return false;
            }
        }
        return true;
    }
}

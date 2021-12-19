package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.GarageDoor;
import fr.mattmouss.gates.setup.ModSetup;

import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.AirBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;

import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;


public class GarageDoorItem extends BlockItem {
    public GarageDoorItem(GarageDoor garageDoor) {
        super(garageDoor,new Item.Properties().tab(ModSetup.itemGroup));
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (allBlockReplacedisAir(new BlockItemUseContext(context)) && hasSupportToStay(new BlockItemUseContext(context))){
            return super.useOn(context);
        }
        System.out.println("block non fabriqué");
        return ActionResultType.FAIL;
    }

    // the function is not working  because we need to know where the first block will be created it depends on where the player clicked
    // need to check the onItemUse function of blockItem to know where it put the first value
    private boolean allBlockReplacedisAir(BlockItemUseContext context){
        World world = context.getLevel();
        PlayerEntity entity=context.getPlayer();
        BlockPos pos_base =context.getClickedPos();
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
            //si le block n'est pas de l'air on retourne false
            if (!(world.getBlockState(pos_in).getBlock() instanceof AirBlock)){
                System.out.println("la blockPos qui fait foirer :"+pos_in);
                System.out.println("Block qui bloque :"+world.getBlockState(pos_in).getBlock());
                return false;
            }
        }
        return true;
    }

    private boolean hasSupportToStay(BlockItemUseContext context){
        World world = context.getLevel();
        PlayerEntity entity=context.getPlayer();
        BlockPos pos_base =context.getClickedPos();
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
                System.out.println("la blockPos qui fait foirer :"+pos);
                System.out.println("Block qui bloque :"+world.getBlockState(pos).getBlock());
                return false;
            }
        }
        return true;
    }
}

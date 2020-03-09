package fr.mattmouss.gates.items;

import fr.mattmouss.gates.doors.GarageDoor;
import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.setup.ModSetup;

import net.minecraft.block.AirBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;

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
        super(garageDoor,new Item.Properties().group(ModSetup.itemGroup));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos =context.getPos();
        World world = context.getWorld();
        PlayerEntity entity=context.getPlayer();
        if (allBlockReplacedisAir(pos.up(),entity,world)){
            return super.onItemUse(context);
        }
        System.out.println("block non fabriqué");
        return ActionResultType.FAIL;
    }

    private boolean allBlockReplacedisAir(BlockPos pos,PlayerEntity entity,World world){
        Direction facing = ModBlock.getDirectionFromEntity(entity,pos);
        Direction dir_left_section=facing.rotateY();
        List<BlockPos> posList = new ArrayList<>();
        posList.add(pos);
        //block du dessus
        posList.add(pos.up());
        //blocks de l'arrière
        posList.add(pos.up().offset(facing.getOpposite()));
        posList.add(pos.up()
                .offset(facing.getOpposite())
                .offset(dir_left_section));
        //block à droite
        posList.add(pos.offset(dir_left_section));
        posList.add(pos.offset(dir_left_section).up());

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
}

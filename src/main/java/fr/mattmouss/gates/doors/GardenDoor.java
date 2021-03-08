package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class GardenDoor extends MultDoor {

    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position",DoorPlacing.class,placing -> {
        return placing.isSide() && !placing.isCenterY();
    });


    public GardenDoor(String name) {
        super(Properties.create(Material.IRON));
        this.setRegistryName(name);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (!state.get(PLACING).isSide() ||state.get(PLACING).isCenterY())return VoxelShapes.empty();
        int meta=state.get(PLACING).getMeta();
        Direction facing=state.get(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        int index=8*meta+2*facing.getHorizontalIndex()+(isOpen?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.gardenDoorShape[index];
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        worldIn.setBlockState(pos.up(), state.with(PLACING, DoorPlacing.LEFT_UP), 3);
        worldIn.setBlockState(pos.offset(facing.rotateYCCW()),state.with(PLACING,DoorPlacing.RIGHT_DOWN),3);
        worldIn.setBlockState(pos.offset(facing.rotateYCCW()).up(),state.with(PLACING,DoorPlacing.RIGHT_UP),3);
    }


    protected List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing){
        List<BlockPos> blockToDestroy = Lists.newArrayList();
        BlockPos offsetPos;
        if (placing.isUp()){
            offsetPos = pos.down();
            blockToDestroy.add(pos.down());
        } else {
            offsetPos = pos.up();
            blockToDestroy.add(pos.up());
        }
        if (placing.isLeft()){
            offsetPos = offsetPos.offset(facing.rotateYCCW());
            blockToDestroy.add(offsetPos);
            blockToDestroy.add(pos.offset(facing.rotateYCCW()));
        }else {
            offsetPos = offsetPos.offset(facing.rotateY());
            blockToDestroy.add(offsetPos);
            blockToDestroy.add(pos.offset(facing.rotateY()));
        }
        return blockToDestroy;
    }

    @Override
    protected EnumProperty<DoorPlacing> getPlacingBSP() {
        return PLACING;
    }

    public boolean isInternUpdate(DoorPlacing placing,Direction facingUpdate,Direction blockFacing){
        return ( placing.isUp() && facingUpdate == Direction.DOWN) ||
               (!placing.isUp() && facingUpdate == Direction.UP)   ||
                (placing.isLeft() && facingUpdate == blockFacing.rotateYCCW()) ||
                (!placing.isLeft() && facingUpdate == blockFacing.rotateY());
    }

}

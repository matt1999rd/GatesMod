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

    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position",DoorPlacing.class,placing -> placing.isSide() && !placing.isCenterY());


    public GardenDoor(String name) {
        super(Properties.of(Material.METAL).noOcclusion());
        this.setRegistryName(name);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (!state.getValue(PLACING).isSide() ||state.getValue(PLACING).isCenterY())return VoxelShapes.empty();
        int meta=state.getValue(PLACING).getMeta();
        Direction facing=state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        int index=8*meta+2*facing.get2DDataValue()+(isOpen?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.gardenDoorShape[index];
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        worldIn.setBlock(pos.above(), state.setValue(PLACING, DoorPlacing.LEFT_UP), 3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()),state.setValue(PLACING,DoorPlacing.RIGHT_DOWN),3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()).above(),state.setValue(PLACING,DoorPlacing.RIGHT_UP),3);
    }


    protected List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing){
        List<BlockPos> blockToDestroy = Lists.newArrayList();
        BlockPos offsetPos;
        if (placing.isUp()){
            offsetPos = pos.below();
            blockToDestroy.add(pos.below());
        } else {
            offsetPos = pos.above();
            blockToDestroy.add(pos.above());
        }
        if (placing.isLeft()){
            offsetPos = offsetPos.relative(facing.getCounterClockWise());
            blockToDestroy.add(offsetPos);
            blockToDestroy.add(pos.relative(facing.getCounterClockWise()));
        }else {
            offsetPos = offsetPos.relative(facing.getClockWise());
            blockToDestroy.add(offsetPos);
            blockToDestroy.add(pos.relative(facing.getClockWise()));
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
                (placing.isLeft() && facingUpdate == blockFacing.getCounterClockWise()) ||
                (!placing.isLeft() && facingUpdate == blockFacing.getClockWise());
    }

}

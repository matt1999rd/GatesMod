package fr.mattmouss.gates.doors;

import com.google.common.collect.Lists;
import fr.mattmouss.gates.enum_door.DoorPlacing;
import fr.mattmouss.gates.voxels.VoxelDefinition;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
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

public class LargeDoor extends MultDoor {
    public static EnumProperty<DoorPlacing> PLACING = EnumProperty.create("position",DoorPlacing.class,DoorPlacing::isSide);

    public LargeDoor(String key,Material material) {
        super(Properties.of(material, MaterialColor.COLOR_BLACK));
        this.setRegistryName(key);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Material material = this.material;
        if (!state.getValue(PLACING).isSide()){
            return VoxelShapes.empty();
        }
        if (material == Material.WOOD){
            return getCircleShape(state);
        }else {
            return getSquareShape(state);
        }
    }

    private VoxelShape getSquareShape(BlockState state) {
        DoorPlacing placing = state.getValue(PLACING);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        int index = placing.getMeta()*8+facing.get2DDataValue()*2+ ((isOpen)?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.largeDoorSquareShape[index];
    }

    private VoxelShape getCircleShape(BlockState state) {
        DoorPlacing placing = state.getValue(PLACING);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);
        int index = placing.getMeta()*8+facing.get2DDataValue()*2+ ((isOpen)?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.largeDoorCircleShape[index];
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        worldIn.setBlock(pos.above(), state.setValue(PLACING, DoorPlacing.LEFT_CENTER), 3);
        worldIn.setBlock(pos.above(2), state.setValue(PLACING, DoorPlacing.LEFT_UP), 3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()),state.setValue(PLACING,DoorPlacing.RIGHT_DOWN),3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()).above(),state.setValue(PLACING,DoorPlacing.RIGHT_CENTER),3);
        worldIn.setBlock(pos.relative(facing.getCounterClockWise()).above(2),state.setValue(PLACING,DoorPlacing.RIGHT_UP),3);
    }

    protected List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing){
        List<BlockPos> blockToDestroy = Lists.newArrayList();
        BlockPos offsetPos;
        BlockPos offsetPos2;
        if (placing.isUp()){
            offsetPos = pos.below();
            offsetPos2 = pos.below(2);
            blockToDestroy.add(pos.below());
            blockToDestroy.add(pos.below(2));
        }else if (placing.isCenterY()){
            offsetPos = pos.above();
            offsetPos2 = pos.below();
            blockToDestroy.add(pos.above());
            blockToDestroy.add(pos.below());
        } else {
            offsetPos = pos.above();
            offsetPos2 = pos.above(2);
            blockToDestroy.add(pos.above());
            blockToDestroy.add(pos.above(2));
        }
        if (placing.isLeft()){
            offsetPos = offsetPos.relative(facing.getCounterClockWise());
            blockToDestroy.add(offsetPos);
            offsetPos2 = offsetPos2.relative(facing.getCounterClockWise());
            blockToDestroy.add(offsetPos2);
            blockToDestroy.add(pos.relative(facing.getCounterClockWise()));
        }else {
            offsetPos = offsetPos.relative(facing.getClockWise());
            blockToDestroy.add(offsetPos);
            offsetPos2 = offsetPos2.relative(facing.getClockWise());
            blockToDestroy.add(offsetPos2);
            blockToDestroy.add(pos.relative(facing.getClockWise()));
        }
        return blockToDestroy;
    }

    @Override
    protected EnumProperty<DoorPlacing> getPlacingBSP() {
        return PLACING;
    }

    protected boolean isInternUpdate(DoorPlacing placing,Direction facingUpdate,Direction blockFacing){
        return ( (placing.isUp()|| placing.isCenterY()) && facingUpdate == Direction.DOWN) ||
                ((!placing.isUp()|| placing.isCenterY()) && facingUpdate == Direction.UP) ||
                (placing.isLeft() && facingUpdate == blockFacing.getCounterClockWise()) ||
                (!placing.isLeft() && facingUpdate == blockFacing.getClockWise());
    }

}

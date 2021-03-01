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
        super(Properties.create(material, MaterialColor.BLACK));
        this.setRegistryName(key);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        Material material = this.material;
        if (!state.get(PLACING).isSide()){
            return VoxelShapes.empty();
        }
        if (material == Material.WOOD){
            return getCircleShape(state);
        }else {
            return getSquareShape(state);
        }
    }

    private VoxelShape getSquareShape(BlockState state) {
        DoorPlacing placing = state.get(PLACING);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        int index = placing.getMeta()*8+facing.getHorizontalIndex()*2+ ((isOpen)?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.largeDoorSquareShape[index];
    }

    private VoxelShape getCircleShape(BlockState state) {
        DoorPlacing placing = state.get(PLACING);
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        boolean isOpen = state.get(BlockStateProperties.OPEN);
        int index = placing.getMeta()*8+facing.getHorizontalIndex()*2+ ((isOpen)?1:0);
        if (!VoxelDefinition.isInit){
            VoxelDefinition.init();
        }
        return VoxelDefinition.largeDoorCircleShape[index];
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
        worldIn.setBlockState(pos.up(), state.with(PLACING, DoorPlacing.LEFT_CENTER), 3);
        worldIn.setBlockState(pos.up(2), state.with(PLACING, DoorPlacing.LEFT_UP), 3);
        worldIn.setBlockState(pos.offset(facing.rotateYCCW()),state.with(PLACING,DoorPlacing.RIGHT_DOWN),3);
        worldIn.setBlockState(pos.offset(facing.rotateYCCW()).up(),state.with(PLACING,DoorPlacing.RIGHT_CENTER),3);
        worldIn.setBlockState(pos.offset(facing.rotateYCCW()).up(2),state.with(PLACING,DoorPlacing.RIGHT_UP),3);
    }

    protected List<BlockPos> getPosOfNeighborBlock(BlockPos pos,DoorPlacing placing,Direction facing){
        List<BlockPos> blockToDestroy = Lists.newArrayList();
        BlockPos offsetPos;
        BlockPos offsetPos2;
        if (placing.isUp()){
            offsetPos = pos.down();
            offsetPos2 = pos.down(2);
            blockToDestroy.add(pos.down());
            blockToDestroy.add(pos.down(2));
        }else if (placing.isCenterY()){
            offsetPos = pos.up();
            offsetPos2 = pos.down();
            blockToDestroy.add(pos.up());
            blockToDestroy.add(pos.down());
        } else {
            offsetPos = pos.up();
            offsetPos2 = pos.up(2);
            blockToDestroy.add(pos.up());
            blockToDestroy.add(pos.up(2));
        }
        if (placing.isLeft()){
            offsetPos = offsetPos.offset(facing.rotateYCCW());
            blockToDestroy.add(offsetPos);
            offsetPos2 = offsetPos2.offset(facing.rotateYCCW());
            blockToDestroy.add(offsetPos2);
            blockToDestroy.add(pos.offset(facing.rotateYCCW()));
        }else {
            offsetPos = offsetPos.offset(facing.rotateY());
            blockToDestroy.add(offsetPos);
            offsetPos2 = offsetPos2.offset(facing.rotateY());
            blockToDestroy.add(offsetPos2);
            blockToDestroy.add(pos.offset(facing.rotateY()));
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
                (placing.isLeft() && facingUpdate == blockFacing.rotateYCCW()) ||
                (!placing.isLeft() && facingUpdate == blockFacing.rotateY());
    }

}

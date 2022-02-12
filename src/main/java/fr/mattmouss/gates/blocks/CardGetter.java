package fr.mattmouss.gates.blocks;

import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class CardGetter extends Block {
    public CardGetter() {
        super(Properties.of(Material.METAL)
        .strength(2f)
        .sound(SoundType.METAL)
        .noOcclusion()
        .lightLevel(value -> 5));
        this.setRegistryName("card_getter");
    }

    private static final VoxelShape SHAPE = Block.box(0,0,0,16,32,16);

    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState p_220053_1_, @Nonnull IBlockReader p_220053_2_, @Nonnull BlockPos p_220053_3_, @Nonnull ISelectionContext p_220053_4_) {
        return SHAPE;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CardGetterTileEntity();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public void setPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity entity, @Nonnull ItemStack stack) {
        if (entity != null){
            world.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.HORIZONTAL_FACING, Functions.getDirectionFromEntity(entity,pos)));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }


    @Nonnull
    @Override
    public ActionResultType use(BlockState state, World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult result) {
        Direction looking_direction = Functions.getDirectionFromEntity(player,pos);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        CardGetterTileEntity cgte = (CardGetterTileEntity) world.getBlockEntity(pos);
        if (facing == looking_direction){
            assert cgte != null;
            cgte.setSide(true); // player is a user
            if (!world.isClientSide) NetworkHooks.openGui((ServerPlayerEntity) player,cgte,cgte.getBlockPos());
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }


}

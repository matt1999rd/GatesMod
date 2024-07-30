package fr.mattmouss.gates.blocks;

import fr.mattmouss.gates.items.KeyItem;
import fr.mattmouss.gates.tileentity.CardGetterTileEntity;
import fr.mattmouss.gates.util.Functions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;



public class CardGetter extends Block implements EntityBlock {
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
    public VoxelShape getShape(@Nonnull BlockState p_220053_1_, @Nonnull BlockGetter p_220053_2_, @Nonnull BlockPos p_220053_3_, @Nonnull CollisionContext p_220053_4_) {
        return SHAPE;
    }

    @Override
    public void setPlacedBy(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity entity, @Nonnull ItemStack stack) {
        if (entity != null){
            world.setBlockAndUpdate(pos,state.setValue(BlockStateProperties.HORIZONTAL_FACING, Functions.getDirectionFromEntity(entity,pos)));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }


    @Nonnull
    @Override
    public InteractionResult use(BlockState state, Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult result) {
        Direction looking_direction = Functions.getDirectionFromEntity(player,pos);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        CardGetterTileEntity cgte = (CardGetterTileEntity) world.getBlockEntity(pos);
        if (facing == looking_direction){
            if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof KeyItem){
                return InteractionResult.PASS;
            }
            assert cgte != null;
            cgte.setSide(true); // player is a user
            if (!world.isClientSide) NetworkHooks.openGui((ServerPlayer) player,cgte,cgte.getBlockPos());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CardGetterTileEntity(blockPos,blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (type == ModBlock.CARD_GETTER_TILE_TYPE) ? (((level1, blockPos, blockState, t) -> {
            if (t instanceof CardGetterTileEntity) {
                ((CardGetterTileEntity) t).tick(level1);
            }
        })) : null;
    }
}

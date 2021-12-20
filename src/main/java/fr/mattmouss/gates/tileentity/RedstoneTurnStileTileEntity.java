package fr.mattmouss.gates.tileentity;


import fr.mattmouss.gates.blocks.ModBlock;
import fr.mattmouss.gates.doors.RedstoneTurnStile;
import fr.mattmouss.gates.doors.TurnStile;
import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.movePlayerPacket;
import fr.mattmouss.gates.setup.ModSound;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class RedstoneTurnStileTileEntity extends TileEntity implements ITickableTileEntity {

    private boolean isAnimationInWork = false;
    private boolean lastPowered = false;
    private boolean initialise = false;

    public RedstoneTurnStileTileEntity() {
        super(ModBlock.REDSTONE_TURNSTILE_TILE_TYPE);
    }

    public List<BlockPos> getPositionOfBlockConnected() {
        List<BlockPos> posList = new ArrayList<>();
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos main_pos = getMainPos();
        posList.add(main_pos);
        posList.add(main_pos.relative(direction.getClockWise()));
        posList.add(main_pos.relative(direction.getCounterClockWise()));
        posList.add(main_pos.relative(Direction.UP));
        return posList;
    }

    public BlockPos getMainPos() {
        TurnSPosition tsp = this.getBlockState().getValue(TurnStile.TS_POSITION);
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch (tsp.getMeta()) {
            case 0:
                return worldPosition;
            case 1:
                return worldPosition.relative(direction.getClockWise());
            case 2:
                return worldPosition.relative(direction.getCounterClockWise());
            case 3:
                return worldPosition.relative(Direction.DOWN);
            default:
                throw new IllegalArgumentException("unknown meta value for tsp :" + tsp.getMeta());
        }
    }

    @Override
    public void tick() {
        BlockState state = this.getBlockState();
        Block block=state.getBlock();
        if (!(block instanceof RedstoneTurnStile))return;
        RedstoneTurnStile turnStile = (RedstoneTurnStile)block;
        if (turnStile.isControlUnit(state)) {
            assert level != null;
            if (!level.isClientSide){
                if (!initialise){
                    initialise = true;
                    lastPowered = state.getValue(BlockStateProperties.POWERED);
                }
                if (lastPowered != state.getValue(BlockStateProperties.POWERED)) {
                    openOrBlockAllTS();
                }
            }
            BlockPos mainPos = this.getMainPos();
            double x = mainPos.getX() + 0.5D;
            double y = mainPos.getY() + 0.5D;
            double z = mainPos.getZ() + 0.5D;
            PlayerEntity player = level.getNearestPlayer(x, y, z, 2, false);
            if (state.getValue(TurnStile.WAY_IS_ON)) {
                if (this.isAnimationInWork()) {
                    movePlayerGoingThrough(player,false);
                    return;
                }
                if (playerIsGoingThrough(player)) {
                    movePlayerGoingThrough(player,false);
                    Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.TURN_STILE_PASS, 1.0F));
                }
            }

            if (this.isAnimationInWork()) {
                movePlayerGoingThrough(player,true);
                return;
            }
            if (playerIsGoingThroughFromExit(player)) {
                movePlayerGoingThrough(player,true);
                Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.TURN_STILE_PASS, 1.0F));
            }
        }
        lastPowered = state.getValue(BlockStateProperties.POWERED);
    }

    //this function check if the player in argument is going into this turn stile with a certain speed
    private boolean playerIsGoingThrough(PlayerEntity entity) {
        Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos mainPos = this.getMainPos();
        double x = mainPos.getX();
        double y = mainPos.getY();
        double z = mainPos.getZ();
        if (entity != null) {
            Vector3d vec3d = entity.getDeltaMovement();
            //the player is going in the direction of the turn stile
            boolean isRightMove = (Direction.getNearest(vec3d.x, vec3d.y, vec3d.z) == facing.getOpposite());
            Vector3d player_pos = entity.position();
            double x_player = player_pos.x;
            double y_player = player_pos.y;
            double z_player = player_pos.z;
            boolean isPlayerInFrontOfMainBlock;
            Direction.Axis axis = facing.getAxis();
            double playerProjection = axis.choose(x_player, y_player, z_player);
            double turnStileProjection = axis.choose(x, y, z);
            int axisDirOffset = facing.getAxisDirection().getStep();
            //it is a very simplified expression which check in each direction for placement of player
            // if NORTH or SOUTH it will check the coordinate z and verify if
            // for NORTH posZ-0.5<z<posZ for SOUTH posZ+1<z<posZ+1.5
            // if EAST or WEST it will check th coordinate x and verify if
            // for WEST posX-0.5<x<posX for EAST posX+1<x<posX+1.5
            isPlayerInFrontOfMainBlock = (playerProjection > turnStileProjection + 0.25 + axisDirOffset * 0.75) && (playerProjection < turnStileProjection + 0.75 + 0.75 * axisDirOffset);
            return isRightMove && isPlayerInFrontOfMainBlock;
        }
        return false;
    }

    //this function check if the player in argument is going into this turn stile from exit with a certain speed
    private boolean playerIsGoingThroughFromExit(PlayerEntity entity) {
        Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos mainPos = this.getMainPos();
        double x = mainPos.getX();
        double y = mainPos.getY();
        double z = mainPos.getZ();
        if (entity != null) {
            Vector3d vec3d = entity.getDeltaMovement();
            //the player is going in the direction of the turn stile
            boolean isRightMove = (Direction.getNearest(vec3d.x, vec3d.y, vec3d.z) == facing);
            Vector3d player_pos = entity.position();
            double x_player = player_pos.x;
            double y_player = player_pos.y;
            double z_player = player_pos.z;
            boolean isPlayerInFrontOfMainBlock;
            Direction.Axis axis = facing.getAxis();
            double playerProjection = axis.choose(x_player, y_player, z_player);
            double turnStileProjection = axis.choose(x, y, z);
            int axisDirOffset = facing.getAxisDirection().getStep();
            //it is a very simplified expression which check in each direction for placement of player
            // if NORTH or SOUTH it will check the coordinate z and verify if
            // for NORTH posZ-0.5<z<posZ for SOUTH posZ+1<z<posZ+1.5
            // if EAST or WEST it will check the coordinate x and verify if
            // for WEST posX-0.5<x<posX for EAST posX+1<x<posX+1.5
            isPlayerInFrontOfMainBlock = (playerProjection > turnStileProjection + 0.25 - axisDirOffset * 0.75) && (playerProjection < turnStileProjection + 0.75 - 0.75 * axisDirOffset);
            return isRightMove && isPlayerInFrontOfMainBlock;
        }
        return false;
    }

    //this function is aimed to move the player into the turn stile
    private void movePlayerGoingThrough(PlayerEntity player,boolean fromExit) {
        if (player == null) {
            return;
        }
        this.changeAllAnim();

        BlockPos pos = getMainPos();
        System.out.println("moving player into turn stile !!");

        if (this.isAnimationInWork()) {
            Networking.INSTANCE.sendToServer(new movePlayerPacket(pos, (ClientPlayerEntity) player, true,fromExit));
            this.endAnimation();
        } else {
            Networking.INSTANCE.sendToServer(new movePlayerPacket(pos, (ClientPlayerEntity) player, false,fromExit));
            //player.moveToBlockPosAndAngles(pos,rot.y,rot.x);
            this.startAnimation();
        }
    }

    //other function for functionality in the te

    public void changeAllAnim() {
        List<BlockPos> posList = getPositionOfBlockConnected();
        for (BlockPos pos1 : posList) {
            assert level != null;
            RedstoneTurnStileTileEntity tste = (RedstoneTurnStileTileEntity) level.getBlockEntity(pos1);
            assert tste != null;
            tste.changeAnim();
        }
    }

    public void changeAnim() {
        BlockState state = this.getBlockState();
        int i = state.getValue(TurnStile.ANIMATION);
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(TurnStile.ANIMATION, 1 - i));
    }

    private void openOrBlockAllTS() {
        List<BlockPos> posList=this.getPositionOfBlockConnected();
        for (BlockPos pos:posList){
            assert level != null;
            TileEntity te=level.getBlockEntity(pos);
            if (te instanceof RedstoneTurnStileTileEntity){
                RedstoneTurnStileTileEntity rtste=(RedstoneTurnStileTileEntity)te;
                if (lastPowered){
                    rtste.blockTS();
                }else {
                    rtste.openTS();
                }
            }
        }
    }


    public void openTS() {
        BlockState state = this.getBlockState();
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(TurnStile.WAY_IS_ON, true));
    }

    public void blockTS() {
        BlockState state = this.getBlockState();
        assert level != null;
        level.setBlockAndUpdate(worldPosition, state.setValue(TurnStile.WAY_IS_ON, false));
    }



    private boolean isAnimationInWork() {
        return isAnimationInWork;
    }

    private void startAnimation() {
        isAnimationInWork = true;
    }

    private void endAnimation() {
        isAnimationInWork = false;
    }



}

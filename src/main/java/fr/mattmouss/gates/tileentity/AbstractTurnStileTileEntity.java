package fr.mattmouss.gates.tileentity;

import fr.mattmouss.gates.doors.TurnStile;
import fr.mattmouss.gates.enum_door.TurnSPosition;
import fr.mattmouss.gates.network.Networking;
import fr.mattmouss.gates.network.blockTSPacket;
import fr.mattmouss.gates.network.movePlayerPacket;
import fr.mattmouss.gates.setup.ModSound;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTurnStileTileEntity extends TileEntity {
    public AbstractTurnStileTileEntity(TileEntityType<?> p_i48289_1_) {
        super(p_i48289_1_);
    }

    protected void managePlayerMovement(){
        BlockState state = this.getBlockState();
        Vector3d vector3d = Vector3d.atCenterOf(getMainPos());
        assert level != null;
        PlayerEntity player = level.getNearestPlayer(vector3d.x, vector3d.y, vector3d.z, 2, false);
        if (state.getValue(TurnStile.WAY_IS_ON)) {
            movePlayer(player,false);
        }
        movePlayer(player,true);
    }

    private void movePlayer(PlayerEntity player, boolean fromExit){
        if (this.isAnimationInWork()) {
            movePlayerGoingThrough(player, fromExit);
            return;
        }
        if (playerIsGoingThrough(player, fromExit)) {
            movePlayerGoingThrough(player, fromExit);
            Minecraft.getInstance().getSoundManager().play(SimpleSound.forUI(ModSound.TURN_STILE_PASS, 1.0F));
        }
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

    public BlockPos getMainPos(){
        TurnSPosition tsp = this.getBlockState().getValue(TurnStile.TS_POSITION);
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        switch (tsp.getMeta()){
            case 0:
                return worldPosition;
            case 1:
                return worldPosition.relative(direction.getClockWise());
            case 2:
                return worldPosition.relative(direction.getCounterClockWise());
            case 3:
                return worldPosition.relative(Direction.DOWN);
            default:
                throw new IllegalArgumentException("unknown meta value for tsp :"+tsp.getMeta());
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

    public void changeAllAnim() {
        List<BlockPos> posList = getPositionOfBlockConnected();
        for (BlockPos pos1 : posList){
            assert level != null;
            AbstractTurnStileTileEntity tste = (AbstractTurnStileTileEntity) level.getBlockEntity(pos1);
            assert tste != null;
            tste.changeAnim();
        }
    }

    public void changeAnim(){
        BlockState state = this.getBlockState();
        int i = state.getValue(TurnStile.ANIMATION);
        assert level != null;
        level.setBlockAndUpdate(worldPosition,state.setValue(TurnStile.ANIMATION,1-i));
    }

    //this function check if the player in argument is going into this turn stile with a certain speed
    protected boolean playerIsGoingThrough(PlayerEntity entity,boolean fromExit) {
        Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        BlockPos mainPos = this.getMainPos();
        double x = mainPos.getX();
        double y = mainPos.getY();
        double z = mainPos.getZ();
        if (entity != null){
            Vector3d vec3d = entity.getDeltaMovement();
            //the player is going in the direction of the turn stile
            boolean isRightMove = (Direction.getNearest(vec3d.x,vec3d.y,vec3d.z) == ((fromExit) ? facing : facing.getOpposite()));
            Vector3d player_pos = entity.position();
            double x_player = player_pos.x;
            double y_player = player_pos.y;
            double z_player = player_pos.z;
            boolean isPlayerInFrontOfMainBlock;
            Direction.Axis axis = facing.getAxis();
            double playerProjection =axis.choose(x_player,y_player,z_player);
            double turnStileProjection = axis.choose(x,y,z);
            int axisDirOffset = facing.getAxisDirection().getStep();
            //it is a very simplified expression which check in each direction for placement of player
            // if NORTH or SOUTH it will check the coordinate z and verify if
            // for NORTH posZ-0.5<z<posZ for SOUTH posZ+1<z<posZ+1.5
            // if EAST or WEST it will check th coordinate x and verify if
            // for WEST posX-0.5<x<posX for EAST posX+1<x<posX+1.5
            isPlayerInFrontOfMainBlock = (playerProjection>turnStileProjection+0.25+axisDirOffset*0.75) && (playerProjection<turnStileProjection+0.75+0.75*axisDirOffset);
            return isRightMove && isPlayerInFrontOfMainBlock;
        }
        return false;
    }



    //this function is aimed to move the player into the turn stile
    protected void movePlayerGoingThrough(PlayerEntity player, boolean fromExit) {
        if (player == null){
            return;
        }
        this.changeAllAnim();

        BlockPos pos = getMainPos();
        System.out.println("moving player into turn stile !!");

        if (this.isAnimationInWork()){
            Networking.INSTANCE.sendToServer(new movePlayerPacket(pos, (ClientPlayerEntity) player,true,fromExit));
            this.endAnimation();
            Networking.INSTANCE.sendToServer(new blockTSPacket(pos));
            for (BlockPos pos1 : this.getPositionOfBlockConnected()) {
                assert level != null;
                TurnStileTileEntity tste = (TurnStileTileEntity) level.getBlockEntity(pos1);
                assert tste != null;
                tste.blockTS();
            }
        }else {
            Networking.INSTANCE.sendToServer(new movePlayerPacket(pos,(ClientPlayerEntity)player,false,fromExit));
            //player.moveToBlockPosAndAngles(pos,rot.y,rot.x);
            this.startAnimation();
        }
    }

    //check if this TE is the control unit tile entity to avoid multiple definition of id storage that will be of no use
    public boolean isControlUnit() {
        Block block=this.getBlockState().getBlock();
        if (!(block instanceof TurnStile)){
            return false;
        }
        TurnStile turnStile = (TurnStile)block;
        return turnStile.isControlUnit(this.getBlockState());
    }

    protected abstract void startAnimation();
    protected abstract void endAnimation();
    protected abstract boolean isAnimationInWork();


}

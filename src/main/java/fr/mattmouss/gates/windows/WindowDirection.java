package fr.mattmouss.gates.windows;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;
import fr.mattmouss.gates.util.ExtendDirection;

public class WindowDirection {
    private final Pair<ExtendDirection,Integer> firstOffset;
    private final Pair<ExtendDirection,Integer> secondOffset;

    public WindowDirection(int nbOffset, ExtendDirection dir){
        firstOffset = new Pair<>(dir,nbOffset);
        secondOffset = null;
    }

    public WindowDirection(int nbOffset,ExtendDirection dir,int nbOffset2,ExtendDirection dir2){
        firstOffset = new Pair<>(dir,nbOffset);
        secondOffset = new Pair<>(dir2,nbOffset2);
    }

    public BlockPos offsetPos(BlockPos originPos){
        BlockPos pos = originPos.immutable();
        pos = firstOffset.getFirst().offset(pos,firstOffset.getSecond());
        if (secondOffset != null){
            pos = secondOffset.getFirst().offset(pos,secondOffset.getSecond());
        }
        return pos;
    }

}

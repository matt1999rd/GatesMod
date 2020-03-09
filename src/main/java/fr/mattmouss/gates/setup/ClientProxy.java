package fr.mattmouss.gates.setup;

import fr.mattmouss.gates.doors.ModBlock;
import fr.mattmouss.gates.gui.TGTechnicianScreen;
import fr.mattmouss.gates.gui.TGUserScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class ClientProxy implements IProxy {

    @Override
    public void init() {
        ScreenManager.registerFactory(ModBlock.TOLLGATE_USER_CONTAINER, TGUserScreen::new);
        ScreenManager.registerFactory(ModBlock.TOLLGATE_TECH_CONTAINER, TGTechnicianScreen::new);
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public PlayerEntity getClientPlayer() {
        return Minecraft.getInstance().player;
    }
}

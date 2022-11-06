package gg.capybara.mod;

import gg.capybara.mod.event.HudRenderCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CapybaraMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("CapybaraGG");

    @Override
    public void onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("capybara").executes(context -> {
                context.getSource().sendFeedback(Component.literal("capybara!"));
                return 0;
            }));
        });

        CapybaraHud hud = new CapybaraHud();
        HudRenderCallback.EVENT.register(hud::render);
    }
}

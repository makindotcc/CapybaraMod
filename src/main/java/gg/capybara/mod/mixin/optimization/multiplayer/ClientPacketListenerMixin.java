package gg.capybara.mod.mixin.optimization.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.searchtree.SearchRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Shadow
    @Final
    private Minecraft minecraft;

    @SuppressWarnings("rawtypes")
    @Redirect(
        method = "handleUpdateTags",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;populateSearchTree(" +
                "Lnet/minecraft/client/searchtree/SearchRegistry$Key;Ljava/util/List;)V")
    )
    public void procrastinateCreativeSearchUpdate(Minecraft instance, SearchRegistry.Key key, List list) {
        // nei takie wazne ze musi kurwa byc w tym samym ticku aktualizowane co wejscie na serwa
        System.out.println("procrastinateCreativeSearchUpdate: " + System.currentTimeMillis());
        this.runIn(() -> {
            this.minecraft.populateSearchTree(key, list);
        }, key == SearchRegistry.CREATIVE_NAMES ? 40 : 60);
    }

    @SuppressWarnings("rawtypes")
    @Redirect(
        method = "handleUpdateRecipes",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;populateSearchTree(" +
                "Lnet/minecraft/client/searchtree/SearchRegistry$Key;Ljava/util/List;)V")
    )
    public void procrastinateRecipesSearchUpdate(Minecraft instance, SearchRegistry.Key key, List list) {
        // nei takie wazne ze musi kurwa byc w tym samym ticku aktualizowane co wejscie na serwa
        System.out.println("procrastinateRecipesSearchUpdate: " + System.currentTimeMillis());
        this.runIn(() -> {
            this.minecraft.populateSearchTree(key, list);
        }, 20);
    }

    // todo scheduler bo nie ma kurwa w tej grze
    private void runIn(Runnable runnable, int ticks) {
        executor.schedule(() -> {
            Minecraft.getInstance().tell(() -> {
                System.out.println("running: " + System.currentTimeMillis());
                runnable.run();
            });
        }, ticks * 50L, TimeUnit.MILLISECONDS);
    }
}
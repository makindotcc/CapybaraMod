**Experimental** Minecraft Fabric mod created with mind of improving performance of Minecraft.

Goals:
- [x] Improve font rendering - no need to recalculate them every frame.
- [x] Optimize packet with array of longs decoding ([FasterFriendlyByteBuf.java](src%2Fmain%2Fjava%2Fgg%2Fcapybara%2Fmod%2Fmixin%2Foptimization%2Fnetwork%2FFasterFriendlyByteBuf.java))
- [x] Lazy recipies & creative search update after server switch (HELPS to reduce stutter on server switch a lot 
on multi-server networks using proxy like Bungee/Velocity) \
Sample video of reduced stutter on server switch https://www.youtube.com/watch?v=Vmzrc46zXpU
Algorithm also could be probably optimized, but this was easier for now...
- [x] Remove 2 sec delay in receiving level (world/terrain) screen.
- [x] Don't release mouse while switching worlds (to prevent accidental clicks on desktop while playing windowed mode)
- [x] Fix resetting key state (is down/up) after changing screen (e.g. ``Loading terrain`` while switching worlds) - \
Before that, while holding "W" on world switch you have to release and press it to make it working again.
- [x] Initialize block state face sturdy (WHATEVER it is) lazily (also helps to reduce stutter on world switch)
- [x] Force block behaviour cache to be initialized in any other thread that rendering.
- [x] Cleaner (transparent) receiving level (world/terrain) screen.
- [ ] ~~Don't render HUD every frame~~ - https://github.com/tr7zw/Exordium
- [ ] Cache entities VBO - no need to recalculate entities geometry every frame.
- [ ] (in progress) Cache entities name tags text.

![Vanilla Minecraft client - 570 fps](img%2Fvanilla_mc.png)
![Modded Minecraft client - 756 fps](img%2Fimproved_mc.png)

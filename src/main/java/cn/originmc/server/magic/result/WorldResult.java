package cn.originmc.server.magic.result;

import cn.origincraft.magic.function.results.ObjectResult;
import net.minestom.server.instance.InstanceContainer;


public class WorldResult extends ObjectResult {
        public WorldResult(InstanceContainer world) {
            super(world);
        }
        public InstanceContainer getWorld() {
            return (InstanceContainer) getObject();
        }
        @Override
        public String getName() {
            return "World";
        }
}

package cn.originmc.server.magic.result;

import cn.origincraft.magic.function.results.ObjectResult;
import net.minestom.server.MinecraftServer;

public class ServerResult extends ObjectResult {
    public ServerResult(MinecraftServer minecraftServer){
        super(minecraftServer);
    }
    public MinecraftServer getServer() {
        return (MinecraftServer) getObject();
    }
    @Override
    public String getName() {
        return "Server";
    }

}

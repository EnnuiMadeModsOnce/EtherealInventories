package io.github.ennuil.etherealinventories.components;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import net.minecraft.util.Identifier;

public class EtherealInventoriesComponents implements EntityComponentInitializer, LevelComponentInitializer {
    public static final ComponentKey<SoulboundComponent> SOULBOUND = ComponentRegistry.getOrCreate(
        new Identifier("etherealinventories", "soulbound"), SoulboundComponent.class
    );

    public static final ComponentKey<EtherinvComponent> ETHERINV = ComponentRegistry.getOrCreate(
        new Identifier("etherealinventories", "etherinv"), EtherinvComponent.class
    );

    public static final ComponentKey<EtherinvStorageComponent> ETHERINV_STORAGE = ComponentRegistry.getOrCreate(
        new Identifier("etherealinventories", "etherinv_storage"), EtherinvStorageComponent.class
    );
    
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(SOULBOUND, player -> new SoulboundComponent(), RespawnCopyStrategy.LOSSLESS_ONLY);
        registry.registerForPlayers(ETHERINV, player -> new EtherinvComponent(), RespawnCopyStrategy.ALWAYS_COPY);
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(ETHERINV_STORAGE, level -> new EtherinvStorageComponent());
    }
}
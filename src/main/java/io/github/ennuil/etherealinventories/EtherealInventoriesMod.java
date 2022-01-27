package io.github.ennuil.etherealinventories;

import eu.pb4.polymer.api.entity.PolymerEntityUtils;
import io.github.ennuil.etherealinventories.entity.EtherinvEntity;
import io.github.ennuil.etherealinventories.entity.EtherinvItemEntity;
import io.github.ennuil.etherealinventories.item.EtherealCompassItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EtherealInventoriesMod implements ModInitializer {
	public static final EntityType<EtherinvEntity> ETHERINV_ENTITY_TYPE = FabricEntityTypeBuilder.create(SpawnGroup.MISC, EtherinvEntity::new).dimensions(EntityDimensions.fixed(1.04F, 1.04F)).build();
	public static final EntityType<EtherinvItemEntity> ETHERINV_ITEM_ENTITY_TYPE = FabricEntityTypeBuilder.<EtherinvItemEntity>create(SpawnGroup.MISC, EtherinvItemEntity::new)
		.dimensions(EntityDimensions.fixed(0.25F, 0.25F))
		.trackRangeChunks(6)
		.trackedUpdateRate(20)
		.build();
	public static final Item ETHERAL_COMPASS_ITEM = new EtherealCompassItem(new Item.Settings().maxDamageIfAbsent(6));

	@Override
	public void onInitialize() {
		Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("etherealinventories", "etherinv"),
			ETHERINV_ENTITY_TYPE
			);
		Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("etherealinventories", "etherinv_item"),
			ETHERINV_ITEM_ENTITY_TYPE
			);

		PolymerEntityUtils.registerType(ETHERINV_ENTITY_TYPE);
		PolymerEntityUtils.registerType(ETHERINV_ITEM_ENTITY_TYPE);
		
		Registry.register(
			Registry.ITEM,
			new Identifier("etherealinventories", "ethereal_compass"),
			ETHERAL_COMPASS_ITEM
		);
	}
}

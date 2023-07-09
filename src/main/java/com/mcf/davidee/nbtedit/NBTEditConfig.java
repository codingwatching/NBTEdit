package com.mcf.davidee.nbtedit;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class NBTEditConfig {
	ForgeConfigSpec.BooleanValue opOnly;
	ForgeConfigSpec.BooleanValue editOtherPlayers;

	public NBTEditConfig(ForgeConfigSpec.Builder builder) {
		this.opOnly = builder.comment("true if only Ops can NBTEdit; false allows users in creative mode to NBTEdit")
				.define("op_only", true);
		this.editOtherPlayers = builder.comment("true if editing players other then your self is allowed. false by default. USE AT YOUR OWN RISK")
				.define("edit_other_players", false);
	}

	@SubscribeEvent
	public static void onConfigLoad(ModConfig.ModConfigEvent event) {
		NBTEdit.opOnly = NBTEdit.configPair.getKey().opOnly();
		NBTEdit.editOtherPlayers = NBTEdit.configPair.getKey().editOtherPlayers();
	}

	public boolean opOnly() {
		return opOnly.get();
	}

	public boolean editOtherPlayers() {
		return editOtherPlayers.get();
	}
}

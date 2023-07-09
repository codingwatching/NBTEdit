package com.mcf.davidee.nbtedit;

import com.mcf.davidee.nbtedit.command.NBTEditCommand;
import com.mcf.davidee.nbtedit.forge.ClientProxy;
import com.mcf.davidee.nbtedit.forge.CommonProxy;
import com.mcf.davidee.nbtedit.nbt.NBTNodeSorter;
import com.mcf.davidee.nbtedit.nbt.NBTTree;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import com.mcf.davidee.nbtedit.nbt.SaveStates;
import com.mcf.davidee.nbtedit.packets.PacketHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;

@Mod(NBTEdit.MODID)
public class NBTEdit {
	public static final String MODID = "nbtedit";
	public static final String NAME = "NBTEdit";
	public static final String VERSION = "1.16.5-2.0.0";

	public static final NBTNodeSorter SORTER = new NBTNodeSorter();
	public static final PacketHandler NETWORK = new PacketHandler();

	public static Logger logger = LogManager.getLogger(NAME);
	public static CommonProxy proxy = new CommonProxy();

	public static NamedNBT clipboard = null;
	public static boolean opOnly = true;
	public static boolean editOtherPlayers = false;

	static Pair<NBTEditConfig, ForgeConfigSpec> configPair;

	private static NBTEdit instance;
	private SaveStates saves;

	public NBTEdit() {
		instance = this;
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(NBTEditConfig.class);

		//Load the config
		configPair = new ForgeConfigSpec.Builder().configure(NBTEditConfig::new);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, configPair.getValue());

		if (FMLEnvironment.dist == Dist.CLIENT) {
			proxy = new ClientProxy();
		}
	}

	public void setup(FMLCommonSetupEvent event) {
		NBTEditConfig config = configPair.getKey();
		opOnly = config.opOnly();
		editOtherPlayers = config.editOtherPlayers();

		org.apache.logging.log4j.core.Logger coreLogger = (org.apache.logging.log4j.core.Logger) logger;
		coreLogger.setAdditive(false); //Sets our logger to not show up in console.
		coreLogger.setLevel(Level.ALL);

		// Set up our file logging.
		PatternLayout layout = PatternLayout.newBuilder().withPattern("[%d{MM-dd HH:mm:ss}] [%level]: %msg%n").build();
		FileAppender appender = FileAppender.newBuilder()
				.withFileName("logs/nbtedit.log")
				.setName("NBTEdit File Appender")
				.setLayout(layout)
				.setIgnoreExceptions(false)
				.build();
		appender.start();
		coreLogger.addAppender(appender);

		NETWORK.initialize();
	}

	public void init(FMLLoadCompleteEvent event) {
		logger.trace("NBTEdit Initialized");
		saves = new SaveStates(new File(new File(proxy.getMinecraftDirectory(), "saves"), "nbtedit.dat"));
		proxy.registerInformation();
	}

	@SubscribeEvent
	public void onRegisterCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
		LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(NBTEditCommand.INSTANCE.commandName());
		NBTEditCommand.INSTANCE.buildCommand(builder);
		dispatcher.register(builder);
		logger.trace("Server Starting -- Added \"/nbtedit\" command");
		PermissionAPI.registerNode("nbtedit.edit", DefaultPermissionLevel.OP, "Allows the user to edit NBT data");
	}

	public static void log(Level l, String s) {
		logger.log(l, s);
	}

	public static void throwing(String cls, String mthd, Throwable thr) {
		logger.warn("class: " + cls + " method: " + mthd, thr);
	}

	static final String SEP = System.getProperty("line.separator");

	public static void logTag(CompoundNBT tag) {
		NBTTree tree = new NBTTree(tag);
		String sb = "";
		for (String s : tree.toStrings()) {
			sb += SEP + "\t\t\t" + s;
		}
		NBTEdit.log(Level.TRACE, sb);
	}

	public static SaveStates getSaveStates() {
		return instance.saves;
	}
}

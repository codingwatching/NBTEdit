package cx.rain.mc.nbtedit;

import cx.rain.mc.nbtedit.command.NBTEditCommand;
import cx.rain.mc.nbtedit.config.NBTEditConfigs;
import cx.rain.mc.nbtedit.utility.nbt.NBTSortHelper;
import cx.rain.mc.nbtedit.utility.nbt.NamedNBT;
import cx.rain.mc.nbtedit.utility.nbt.ClipboardStates;
import cx.rain.mc.nbtedit.networking.NBTEditNetworking;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;

@Mod(value = NBTEdit.MODID)
public class NBTEdit {
	public static final String MODID = "nbtedit";
	public static final String NAME = "In-game NBTEdit";
	public static final String VERSION = "1.17.1-3.0.0";

	public static NamedNBT CLIPBOARD = null;

	private static NBTEdit INSTANCE;

	private final Logger log = LogManager.getLogger("NBTEdit");
	private final org.apache.logging.log4j.core.Logger internalLog =
			(org.apache.logging.log4j.core.Logger) LogManager.getLogger("NBTEditInternal");

	private ClipboardStates clipboardSaves;

	public NBTEdit() {
		INSTANCE = this;

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NBTEditConfigs.CONFIG);

		initInternalLogger();

		final var bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::setup);
		bus.addListener(this::setupClient);

		MinecraftForge.EVENT_BUS.addListener(NBTEditCommand::onRegisterCommands);
	}

	public static NBTEdit getInstance() {
		return INSTANCE;
	}

	public Logger getLog() {
		return log;
	}

	public Logger getInternalLogger() {
		return internalLog;
	}

	private void initInternalLogger() {
		internalLog.setAdditive(false);
		internalLog.setLevel(Level.ALL);

		var layout = PatternLayout.newBuilder()
				.withPattern("[%d{MM-dd HH:mm:ss}][%level] %msg%n")
				.build();

		var appenderBuilder = RollingFileAppender.newBuilder();
		appenderBuilder.withFilePattern("logs/NBTEdit/%d{yyyy-MM-dd}-%i.log")
				.withFileName("logs/NBTEdit/latest.log")
				.withName("NBTEditRollingFileAppender")
				.withLayout(layout)
				.withIgnoreExceptions(false)
				.withPolicy(OnStartupTriggeringPolicy.createPolicy(0));
		var appender = appenderBuilder.build();
		appender.start();

		internalLog.addAppender(appender);
	}

	private void setup(FMLCommonSetupEvent event) {
		log.info("NBTEdit initializing.");
		internalLog.info("Initializing.");

		event.enqueueWork(NBTEditNetworking::new);
	}

	private void setupClient(FMLClientSetupEvent event) {
		log.info("Setup client.");

		clipboardSaves = new ClipboardStates(new File(new File("."), "NBTEdit.dat"));

		ClipboardStates clipboard = getClipboardSaves();
		clipboard.load();
		clipboard.save();

		log.info("Client setup successful.");
	}

	public static ClipboardStates getClipboardSaves() {
		return INSTANCE.clipboardSaves;
	}
}

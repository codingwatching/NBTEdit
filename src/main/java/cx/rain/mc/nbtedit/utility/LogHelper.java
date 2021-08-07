package cx.rain.mc.nbtedit.utility;

import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class LogHelper {
    public static void logNBTTag(Logger logger, Level level, Tag tag) {
        logger.log(level, tag.getAsString());
    }
}

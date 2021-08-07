package cx.rain.mc.nbtedit.data.provider.language;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.data.DataGenerator;
import cx.rain.mc.nbtedit.data.provider.base.LanguageProviderBase;

public class LanguageProviderENUS extends LanguageProviderBase {
    public LanguageProviderENUS(DataGenerator gen) {
        super(gen, NBTEdit.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addKey(TranslateKeys.KEY_CATEGORY, "In-game NBTEdit (Reborn)");
        addKey(TranslateKeys.KEY_NBTEDIT_SHORTCUT, "NBTEdit shortcut");

        addKey(TranslateKeys.MESSAGE_NOT_PLAYER, "Only players can use this command.");
        addKey(TranslateKeys.MESSAGE_NO_PERMISSION, "You have no permission to use NBTEdit.");
        addKey(TranslateKeys.MESSAGE_NO_ANY_TARGET, "There is no any target for editing.");
        addKey(TranslateKeys.MESSAGE_NO_TARGET_TILE, "There is no Tile Entity to edit.");
        addKey(TranslateKeys.MESSAGE_SAVED, "Saved successfully.");
        addKey(TranslateKeys.MESSAGE_SAVE_FAILED_INVALID_NBT, "Save failed, invalid NBT.");
        addKey(TranslateKeys.MESSAGE_SAVE_FAILED_NO_LONGER_HAS_TILE, "Save failed, the Tile Entity is no longer exists.");
        addKey(TranslateKeys.MESSAGE_CANNOT_EDIT_OTHER_PLAYER, "Sorry, but you cannot edit other player.");
        addKey(TranslateKeys.MESSAGE_UNKNOWN_ENTITY_ID, "Unknown Entity ID.");
        addKey(TranslateKeys.MESSAGE_SAVE_FAILED_ENTITY_NOT_EXISTS, "Save failed, the Entity is not exists.");

        addKey(TranslateKeys.TITLE_NBTEDIT_ENTITY_GUI, "Editing Entity with id {0}");
        addKey(TranslateKeys.TITLE_NBTEDIT_TILE_GUI, "Editing Tile Entity in {0} {1} {2}");

        addKey(TranslateKeys.BUTTON_SAVE, "Save");
        addKey(TranslateKeys.BUTTON_QUIT, "Quit");
        addKey(TranslateKeys.BUTTON_LOAD, "Load");
    }
}

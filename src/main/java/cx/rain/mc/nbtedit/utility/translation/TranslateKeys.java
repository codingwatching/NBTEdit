package cx.rain.mc.nbtedit.utility.translation;

import cx.rain.mc.nbtedit.NBTEdit;

public enum TranslateKeys {
    KEY_CATEGORY("key.category." + NBTEdit.MODID),
    KEY_NBTEDIT_SHORTCUT("key." + NBTEdit.MODID),

    MESSAGE_NOT_PLAYER("messages.not_a_player"),
    MESSAGE_NO_PERMISSION("messages.no_permission"),

    MESSAGE_NO_TARGET_TILE("messages.no_target_tile"),
    MESSAGE_SAVED("messages.saved"),
    MESSAGE_SAVE_FAILED_INVALID_NBT("messages.save_failed"),
    MESSAGE_SAVE_FAILED_NO_LONGER_HAS_TILE("messages.no_longer_has_tile"),
    MESSAGE_NO_ANY_TARGET("messages.no_any_target"),
    MESSAGE_CANNOT_EDIT_OTHER_PLAYER("messages.cannot_edit_other_player"),
    MESSAGE_UNKNOWN_ENTITY_ID("messages.unknown_entity_id"),
    MESSAGE_SAVE_FAILED_ENTITY_NOT_EXISTS("messages.entity_not_exists")
    ;

    private String key;

    private TranslateKeys(String keyIn) {
        key = keyIn;
    }

    public String getKey() {
        return key;
    }
}

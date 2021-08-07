package cx.rain.mc.nbtedit.data.provider.language;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.data.provider.base.LanguageProviderBase;
import cx.rain.mc.nbtedit.utility.translation.TranslateKeys;
import net.minecraft.data.DataGenerator;

public class LanguageProviderZHCN extends LanguageProviderBase {
    public LanguageProviderZHCN(DataGenerator gen) {
        super(gen, NBTEdit.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        addKey(TranslateKeys.KEY_CATEGORY, "游戏内NBT修改器 重制版");
        addKey(TranslateKeys.KEY_NBTEDIT_SHORTCUT, "NBT修改器快捷键");

        addKey(TranslateKeys.MESSAGE_NOT_PLAYER, "只有在游戏中的玩家可以使用这个命令！");
        addKey(TranslateKeys.MESSAGE_NO_PERMISSION, "你没有权限使用NBETEdit！");
        addKey(TranslateKeys.MESSAGE_NO_ANY_TARGET, "没有任何目标可供编辑。");
        addKey(TranslateKeys.MESSAGE_NO_TARGET_TILE, "没有目标方块实体可供编辑。");
        addKey(TranslateKeys.MESSAGE_SAVED, "保存成功。");
        addKey(TranslateKeys.MESSAGE_SAVE_FAILED_INVALID_NBT, "保存失败。无效的NBT！");
        addKey(TranslateKeys.MESSAGE_SAVE_FAILED_NO_LONGER_HAS_TILE, "保存失败。目标方块实体已经不存在了！");
        addKey(TranslateKeys.MESSAGE_CANNOT_EDIT_OTHER_PLAYER, "你不能编辑其他玩家！");
        addKey(TranslateKeys.MESSAGE_UNKNOWN_ENTITY_ID, "无效的实体ID！");
        addKey(TranslateKeys.MESSAGE_SAVE_FAILED_ENTITY_NOT_EXISTS, "保存失败。目标实体不存在！");

        addKey(TranslateKeys.TITLE_NBTEDIT_ENTITY_GUI, "正在编辑实体 ID：{0}");
        addKey(TranslateKeys.TITLE_NBTEDIT_TILE_GUI, "正在编辑位于 {0} {1} {2} 的方块实体。");

        addKey(TranslateKeys.BUTTON_SAVE, "保存");
        addKey(TranslateKeys.BUTTON_QUIT, "退出");
        addKey(TranslateKeys.BUTTON_LOAD, "加载");
    }
}

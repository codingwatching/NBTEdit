package cx.rain.mc.nbtedit.utility.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Comparator;

public class NBTSortHelper implements Comparator<NBTNode<NamedNBT>> {
	private static NBTSortHelper INSTANCE;

	public static NBTSortHelper get() {
		if (INSTANCE == null) {
			return new NBTSortHelper();
		}
		return INSTANCE;
	}

	public NBTSortHelper() {
		INSTANCE = this;
	}

	@Override
	public int compare(NBTNode<NamedNBT> a, NBTNode<NamedNBT> b) {
		Tag tag1 = a.get().getTag();
		Tag tag2 = b.get().getTag();
		String name1 = a.get().getName();
		String name2 = b.get().getName();
		if (tag1 instanceof CompoundTag || tag1 instanceof ListTag) {
			if (tag2 instanceof CompoundTag || tag2 instanceof ListTag) {
				int difference = tag1.getId() - tag2.getId();
				return (difference == 0) ? name1.compareTo(name2) : difference;
			}
			return 1;
		}
		if (tag2 instanceof CompoundTag || tag2 instanceof ListTag) {
			return -1;
		}
		int difference = tag1.getId() - tag2.getId();
		return (difference == 0) ? name1.compareTo(name2) : difference;
	}
}

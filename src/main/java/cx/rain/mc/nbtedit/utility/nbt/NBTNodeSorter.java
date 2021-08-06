package cx.rain.mc.nbtedit.utility.nbt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Comparator;

public class NBTNodeSorter implements Comparator<NBTNode<NamedNBT>> {
	@Override
	public int compare(NBTNode<NamedNBT> a, NBTNode<NamedNBT> b) {
		Tag n1 = a.get().getTag(), n2 = b.get().getTag();
		String s1 = a.get().getName(), s2 = b.get().getName();
		if (n1 instanceof CompoundTag || n1 instanceof ListTag) {
			if (n2 instanceof CompoundTag || n2 instanceof ListTag) {
				int dif = n1.getId() - n2.getId();
				return (dif == 0) ? s1.compareTo(s2) : dif;
			}
			return 1;
		}
		if (n2 instanceof CompoundTag || n2 instanceof ListTag)
			return -1;
		int dif = n1.getId() - n2.getId();
		return (dif == 0) ? s1.compareTo(s2) : dif;
	}
}

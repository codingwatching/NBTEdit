package cx.rain.mc.nbtedit.utility.nbt;

import java.util.*;

import cx.rain.mc.nbtedit.utility.NBTHelper;
import net.minecraft.nbt.CompoundTag;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.utility.NBTIOHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NBTTree {
	private CompoundTag baseTag;

	private NBTNode<NamedNBT> root;

	public NBTTree(CompoundTag tag) {
		baseTag = tag;
		construct();
	}

	public NBTNode<NamedNBT> getRoot() {
		return root;
	}

	public boolean canDelete(NBTNode<NamedNBT> NBTNode) {
		return NBTNode != root;
	}

	public boolean delete(NBTNode<NamedNBT> NBTNode) {
		return !(NBTNode == null || NBTNode == root) && deleteNode(NBTNode, root);
	}

	private boolean deleteNode(NBTNode<NamedNBT> toDelete, NBTNode<NamedNBT> cur) {
		for (Iterator<NBTNode<NamedNBT>> it = cur.getChildren().iterator(); it.hasNext(); ) {
			NBTNode<NamedNBT> child = it.next();
			if (child == toDelete) {
				it.remove();
				return true;
			}
			boolean flag = deleteNode(toDelete, child);
			if (flag) {
				return true;
			}
		}
		return false;
	}

	private void construct() {
		root = new NBTNode<>(new NamedNBT("ROOT", baseTag.copy()));
		root.setShowChildren(true);
		addChildrenToTree(root);
		sort(root);
	}

	public void sort(NBTNode<NamedNBT> NBTNode) {
		Collections.sort(NBTNode.getChildren(), NBTSortHelper.get());
		for (NBTNode<NamedNBT> c : NBTNode.getChildren()) {
			sort(c);
		}
	}

	public void addChildrenToTree(NBTNode<NamedNBT> parent) {
		Tag tag = parent.get().getTag();
		if (tag instanceof CompoundTag) {
			Map<String, Tag> map = NBTIOHelper.getMap((CompoundTag) tag);
			for (Map.Entry<String, Tag> entry : map.entrySet()) {
				Tag base = entry.getValue();
				NBTNode<NamedNBT> child = new NBTNode<>(parent, new NamedNBT(entry.getKey(), base));
				parent.addChild(child);
				addChildrenToTree(child);
			}

		} else if (tag instanceof ListTag) {
			ListTag list = (ListTag) tag;
			for (int i = 0; i < list.size(); ++i) {
				Tag base = NBTIOHelper.getTagAt(list, i);
				NBTNode<NamedNBT> child = new NBTNode<>(parent, new NamedNBT(base));
				parent.addChild(child);
				addChildrenToTree(child);
			}
		}
	}

	public CompoundTag toCompound() {
		CompoundTag tag = new CompoundTag();
		addChild(root, tag);
		return tag;
	}

	public void addChild(NBTNode<NamedNBT> parent, CompoundTag tag) {
		for (NBTNode<NamedNBT> child : parent.getChildren()) {
			Tag base = child.get().getTag();
			String name = child.get().getName();
			if (base instanceof CompoundTag) {
				CompoundTag newTag = new CompoundTag();
				addChild(child, newTag);
				tag.put(name, newTag);
			} else if (base instanceof ListTag) {
				ListTag list = new ListTag();
				addChild(child, list);
				tag.put(name, list);
			} else {
				tag.put(name, base.copy());
			}
		}
	}

	public void addChild(NBTNode<NamedNBT> parent, ListTag list) {
		for (NBTNode<NamedNBT> child : parent.getChildren()) {
			Tag base = child.get().getTag();
			if (base instanceof CompoundTag) {
				CompoundTag newTag = new CompoundTag();
				addChild(child, newTag);
				list.add(newTag);
			} else if (base instanceof ListTag) {
				ListTag newList = new ListTag();
				addChild(child, newList);
				list.add(newList);
			} else {
				list.add(base.copy());
			}
		}
	}

	public void print() {
		print(root, 0);
	}

	private void print(NBTNode<NamedNBT> n, int i) {
		System.out.println(repeat("\t", i) + NBTHelper.getNBTName(n.get()));
		for (NBTNode<NamedNBT> child : n.getChildren()) {
			print(child, i + 1);
		}
	}

	public List<String> toStrings() {
		List<String> s = new ArrayList<>();
		toStrings(s, root, 0);
		return s;
	}

	private void toStrings(List<String> s, NBTNode<NamedNBT> n, int i) {
		s.add(repeat("   ", i) + NBTHelper.getNBTName(n.get()));
		for (NBTNode<NamedNBT> child : n.getChildren()) {
			toStrings(s, child, i + 1);
		}
	}

	public static String repeat(String c, int i) {
		StringBuilder b = new StringBuilder(i + 1);
		for (int j = 0; j < i; ++j) {
			b.append(c);
		}
		return b.toString();
	}
}

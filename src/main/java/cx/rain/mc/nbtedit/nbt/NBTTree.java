package cx.rain.mc.nbtedit.nbt;

import java.util.*;

import net.minecraft.nbt.CompoundTag;

import cx.rain.mc.nbtedit.NBTEdit;
import cx.rain.mc.nbtedit.utility.NBTHelper;
import cx.rain.mc.nbtedit.NBTStringHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NBTTree {

	private CompoundTag baseTag;

	private Node<NamedNBT> root;

	public NBTTree(CompoundTag tag) {
		baseTag = tag;
		construct();
	}

	public Node<NamedNBT> getRoot() {
		return root;
	}

	public boolean canDelete(Node<NamedNBT> node) {
		return node != root;
	}

	public boolean delete(Node<NamedNBT> node) {
		return !(node == null || node == root) && deleteNode(node, root);
	}

	private boolean deleteNode(Node<NamedNBT> toDelete, Node<NamedNBT> cur) {
		for (Iterator<Node<NamedNBT>> it = cur.getChildren().iterator(); it.hasNext(); ) {
			Node<NamedNBT> child = it.next();
			if (child == toDelete) {
				it.remove();
				return true;
			}
			boolean flag = deleteNode(toDelete, child);
			if (flag)
				return true;
		}
		return false;
	}


	private void construct() {
		root = new Node<>(new NamedNBT("ROOT", baseTag.copy()));
		root.setDrawChildren(true);
		addChildrenToTree(root);
		sort(root);
	}

	public void sort(Node<NamedNBT> node) {
		Collections.sort(node.getChildren(), NBTEdit.SORTER);
		for (Node<NamedNBT> c : node.getChildren())
			sort(c);
	}

	public void addChildrenToTree(Node<NamedNBT> parent) {
		Tag tag = parent.getObject().getTag();
		if (tag instanceof CompoundTag) {
			Map<String, Tag> map = NBTHelper.getMap((CompoundTag) tag);
			for (Map.Entry<String, Tag> entry : map.entrySet()) {
				Tag base = entry.getValue();
				Node<NamedNBT> child = new Node<>(parent, new NamedNBT(entry.getKey(), base));
				parent.addChild(child);
				addChildrenToTree(child);
			}

		} else if (tag instanceof ListTag) {
			ListTag list = (ListTag) tag;
			for (int i = 0; i < list.size(); ++i) {
				Tag base = NBTHelper.getTagAt(list, i);
				Node<NamedNBT> child = new Node<>(parent, new NamedNBT(base));
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

	public void addChild(Node<NamedNBT> parent, CompoundTag tag) {
		for (Node<NamedNBT> child : parent.getChildren()) {
			Tag base = child.getObject().getTag();
			String name = child.getObject().getName();
			if (base instanceof CompoundTag) {
				CompoundTag newTag = new CompoundTag();
				addChild(child, newTag);
				tag.put(name, newTag);
			} else if (base instanceof ListTag) {
				ListTag list = new ListTag();
				addChild(child, list);
				tag.put(name, list);
			} else
				tag.put(name, base.copy());
		}
	}

	public void addChild(Node<NamedNBT> parent, ListTag list) {
		for (Node<NamedNBT> child : parent.getChildren()) {
			Tag base = child.getObject().getTag();
			if (base instanceof CompoundTag) {
				CompoundTag newTag = new CompoundTag();
				addChild(child, newTag);
				list.add(newTag);
			} else if (base instanceof ListTag) {
				ListTag newList = new ListTag();
				addChild(child, newList);
				list.add(newList);
			} else
				list.add(base.copy());
		}
	}

	public void print() {
		print(root, 0);
	}

	private void print(Node<NamedNBT> n, int i) {
		System.out.println(repeat("\t", i) + NBTStringHelper.getNBTName(n.getObject()));
		for (Node<NamedNBT> child : n.getChildren())
			print(child, i + 1);
	}

	public List<String> toStrings() {
		List<String> s = new ArrayList<>();
		toStrings(s, root, 0);
		return s;
	}

	private void toStrings(List<String> s, Node<NamedNBT> n, int i) {
		s.add(repeat("   ", i) + NBTStringHelper.getNBTName(n.getObject()));
		for (Node<NamedNBT> child : n.getChildren())
			toStrings(s, child, i + 1);
	}

	public static String repeat(String c, int i) {
		StringBuilder b = new StringBuilder(i + 1);
		for (int j = 0; j < i; ++j)
			b.append(c);
		return b.toString();
	}
}

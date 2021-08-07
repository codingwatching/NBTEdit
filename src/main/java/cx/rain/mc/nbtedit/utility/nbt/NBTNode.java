package cx.rain.mc.nbtedit.utility.nbt;

import java.util.ArrayList;
import java.util.List;

public class NBTNode<T> {

	private List<NBTNode<T>> children;

	private NBTNode<T> parent;
	private T obj;

	private boolean showChildren;

	public NBTNode() {
		this((T) null);
	}

	public NBTNode(T obj) {
		children = new ArrayList<>();
		this.obj = obj;
	}

	public boolean shouldShowChildren() {
		return showChildren;
	}

	public void setShowChildren(boolean show) {
		showChildren = show;
	}

	public NBTNode(NBTNode<T> parent) {
		this(parent, null);
	}

	public NBTNode(NBTNode<T> parent, T obj) {
		this.parent = parent;
		children = new ArrayList<>();
		this.obj = obj;
	}

	public void addChild(NBTNode<T> n) {
		children.add(n);
	}

	public boolean removeChild(NBTNode<T> n) {
		return children.remove(n);
	}

	public List<NBTNode<T>> getChildren() {
		return children;
	}

	public NBTNode<T> getParent() {
		return parent;
	}

	public T get() {
		return obj;
	}

	public String toString() {
		return "" + obj;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public boolean hasParent() {
		return parent != null;
	}
}

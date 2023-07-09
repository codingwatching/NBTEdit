package com.mcf.davidee.nbtedit;

import net.minecraft.nbt.*;

import com.google.common.base.Strings;
import com.mcf.davidee.nbtedit.nbt.NamedNBT;
import net.minecraftforge.common.util.Constants;

/**
 * TODO ENUM THE NBT STUFF. -Jay
 */
public class NBTStringHelper {

	public static final char SECTION_SIGN = '\u00A7';

	public static String getNBTName(NamedNBT namedNBT) {
		String name = namedNBT.getName();
		INBT obj = namedNBT.getNBT();

		String s = toString(obj);
		return Strings.isNullOrEmpty(name) ? "" + s : name + ": " + s;
	}

	public static String getNBTNameSpecial(NamedNBT namedNBT) {
		String name = namedNBT.getName();
		INBT obj = namedNBT.getNBT();

		String s = toString(obj);
		return Strings.isNullOrEmpty(name) ? "" + s : name + ": " + s + SECTION_SIGN + 'r';
	}

	public static INBT newTag(byte type) {
		switch (type) {
			case Constants.NBT.TAG_END:
				return EndNBT.INSTANCE;
			case Constants.NBT.TAG_BYTE:
				return ByteNBT.ZERO;
			case Constants.NBT.TAG_SHORT:
				return ShortNBT.valueOf((short) 0);
			case Constants.NBT.TAG_INT:
				return IntNBT.valueOf(0);
			case Constants.NBT.TAG_LONG:
				return LongNBT.valueOf(0);
			case Constants.NBT.TAG_FLOAT:
				return FloatNBT.valueOf(0.0f);
			case Constants.NBT.TAG_DOUBLE:
				return DoubleNBT.valueOf(0.0d);
			case Constants.NBT.TAG_BYTE_ARRAY:
				return new ByteArrayNBT(new byte[0]);
			case Constants.NBT.TAG_STRING:
				return StringNBT.valueOf("");
			case Constants.NBT.TAG_LIST:
				return new ListNBT();
			case Constants.NBT.TAG_COMPOUND:
				return new CompoundNBT();
			case Constants.NBT.TAG_INT_ARRAY:
				return new IntArrayNBT(new int[0]);
			case Constants.NBT.TAG_LONG_ARRAY:
				return new LongArrayNBT(new long[0]);
			default:
				return null;
		}
	}

	public static String toString(INBT base) {
		switch (base.getId()) {
			case Constants.NBT.TAG_BYTE:
			case Constants.NBT.TAG_SHORT:
			case Constants.NBT.TAG_INT:
			case Constants.NBT.TAG_LONG:
			case Constants.NBT.TAG_FLOAT:
			case Constants.NBT.TAG_DOUBLE:
				return ((NumberNBT) base).getAsNumber().toString();
			case Constants.NBT.TAG_BYTE_ARRAY:
				return base.toString();
			case Constants.NBT.TAG_STRING:
				return base.getAsString();
			case Constants.NBT.TAG_LIST:
				return "(TagList)";
			case Constants.NBT.TAG_COMPOUND:
				return "(TagCompound)";
			case Constants.NBT.TAG_INT_ARRAY:
			case Constants.NBT.TAG_LONG_ARRAY:
				return base.toString();
			default:
				return "?";
		}
	}

	public static String getButtonName(byte id) {
		switch (id) {
			case Constants.NBT.TAG_BYTE:
				return "Byte";
			case Constants.NBT.TAG_SHORT:
				return "Short";
			case Constants.NBT.TAG_INT:
				return "Int";
			case Constants.NBT.TAG_LONG:
				return "Long";
			case Constants.NBT.TAG_FLOAT:
				return "Float";
			case Constants.NBT.TAG_DOUBLE:
				return "Double";
			case Constants.NBT.TAG_BYTE_ARRAY:
				return "Byte[]";
			case Constants.NBT.TAG_STRING:
				return "String";
			case Constants.NBT.TAG_LIST:
				return "List";
			case Constants.NBT.TAG_COMPOUND:
				return "Compound";
			case Constants.NBT.TAG_INT_ARRAY:
				return "Int[]";/*
			case Constants.NBT.TAG_LONG_ARRAY:
				return "Int[]";*/
			case 12:
				return "Edit";
			case 13:
				return "Delete";
			case 14:
				return "Copy";
			case 15:
				return "Cut";
			case 16:
				return "Paste";
			default:
				return "Unknown";
		}
	}
}

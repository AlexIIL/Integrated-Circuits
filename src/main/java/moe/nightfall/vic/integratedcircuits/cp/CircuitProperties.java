package moe.nightfall.vic.integratedcircuits.cp;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import moe.nightfall.vic.integratedcircuits.api.gate.ISocket.EnumConnectionType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class CircuitProperties implements Cloneable {
	private String name = "NO_NAME", author = "unknown";
	private int con;
	private Map<UUID, Comment> comments = new LinkedHashMap<UUID, Comment>();

	public void setName(String name) {
		this.name = name;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setCon(int con) {
		this.con = con;
	}

	public String getName() {
		return name;
	}

	public String getAuthor() {
		return author;
	}

	public int getCon() {
		return con;
	}

	public void clearComments() {
		comments.clear();
	}

	public void removeComment(Comment comment) {
		if (comment == null)
			return;
		comments.remove(comment.uuid);
	}

	public void removeComment(UUID uuid) {
		comments.remove(uuid);
	}

	public void addComment(Comment comment) {
		comments.put(comment.uuid, comment);
	}

	public Collection<Comment> getComments() {
		return comments.values();
	}

	public EnumConnectionType getModeAtSide(int side) {
		return getModeAtSide(con, side);
	}
	
	public static EnumConnectionType getModeAtSide(int con, int side) {
		return EnumConnectionType.values()[con >> (side * 2) & 3];
	}

	public int setModeAtSide(int side, EnumConnectionType type) {
		int con = this.con;
		con &= ~(3 << (side * 2));
		con |= type.ordinal() << (side * 2);
		return con;
	}

	public static CircuitProperties readFromNBT(NBTTagCompound comp) {
		CircuitProperties properties = new CircuitProperties();
		if (comp.hasKey("name"))
			properties.name = comp.getString("name");
		if (comp.hasKey("author"))
			properties.author = comp.getString("author");
		properties.con = comp.getInteger("con");
		if (comp.hasKey("comments")) {
			NBTTagList commentList = comp.getTagList("comments", NBT.TAG_COMPOUND);
			for (int i = 0; i < commentList.tagCount(); i++) {
				NBTTagCompound comment = commentList.getCompoundTagAt(i);
				properties.addComment(Comment.readFromNBT(comment));
			}
		}
		return properties;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound comp, boolean pcb) {
		comp.setString("name", name);
		comp.setString("author", author);
		comp.setInteger("con", con);
		if (!pcb) {
			NBTTagList commentList = new NBTTagList();
			for (Comment comment : comments.values()) {
				commentList.appendTag(comment.writeToNBT(new NBTTagCompound()));
			}
			comp.setTag("comments", commentList);
		}
		return comp;
	}
	
	public static class Comment implements Cloneable {
		public String text = "";
		public double xPos, yPos;
		public int id;
		public UUID uuid;
		
		public Comment(double xPos, double yPos) {
			this(xPos, yPos, UUID.randomUUID());
		}

		public Comment(double xPos, double yPos, UUID uuid) {
			this.xPos = xPos;
			this.yPos = yPos;
			this.uuid = uuid;
		}

		public Comment setText(String text) {
			this.text = text;
			return this;
		}

		public static Comment readFromNBT(NBTTagCompound comp) {
			Comment comment = new Comment(comp.getDouble("xPos"), comp.getDouble("yPos"));
			comment.text = comp.getString("text");
			comment.uuid = UUID.fromString(comp.getString("uuid"));
			return comment;
		}

		public NBTTagCompound writeToNBT(NBTTagCompound compound) {
			compound.setDouble("xPos", xPos);
			compound.setDouble("yPos", yPos);
			compound.setString("text", text);
			compound.setString("uuid", uuid.toString());
			return compound;
		}
	}
}

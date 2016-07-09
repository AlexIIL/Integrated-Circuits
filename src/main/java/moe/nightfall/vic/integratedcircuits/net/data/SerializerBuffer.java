package moe.nightfall.vic.integratedcircuits.net.data;


import net.minecraft.network.PacketBuffer;

import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;

public class SerializerBuffer {

    private boolean misWriting = false;
    private SerializerStreamWrapper stream;

    public SerializerBuffer(DataInput dataInput) {
        stream = new SerializerStreamWrapper(dataInput);
    }

    public SerializerBuffer(DataOutput dataOutput) {
        stream = new SerializerStreamWrapper(dataOutput);
    }

    public SerializerBuffer(PacketBuffer buffer, boolean isWriting) {
        if (!isWriting) {
            DataInputStream dataInput = new DataInputStream(new InputStream() {
                @Override
                public int read() throws IOException {
                    return buffer.readByte();
                }
            });
            stream = new SerializerStreamWrapper(dataInput);
        } else {
            DataOutputStream dataOutput = new DataOutputStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    buffer.writeByte(b);
                }
            });
            stream = new SerializerStreamWrapper(dataOutput);
        }
    }

    public boolean serializeBoolean(boolean value) throws IOException {
        if (isReading()) {
            return stream.readBoolean();
        } else {
            stream.writeBoolean(value);
            return value;
        }
    }

    public int serializeInt(int value) throws IOException {
        if (isReading()) {
            return stream.readInt();
        } else {
            stream.writeInt(value);
            return value;
        }
    }

    public boolean isWriting() {
        return stream.isWriting();
    }

    public boolean isReading() {
        return stream.isReading();
    }
}

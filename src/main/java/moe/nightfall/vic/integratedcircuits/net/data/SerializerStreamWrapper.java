package moe.nightfall.vic.integratedcircuits.net.data;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SerializerStreamWrapper {

    private DataInput input;
    private DataOutput output;
    private boolean mIsWriting;

    private void checkOutput() {
        if (output == null || isReading())
            throw new RuntimeException("Trying to read from output serializer");
    }

    private void checkInput() {
        if (input == null || isWriting())
            throw new RuntimeException("Trying to write to input serializer");
    }

    public boolean isWriting() {
        return mIsWriting;
    }

    public boolean isReading() {
        return !mIsWriting;
    }

    public SerializerStreamWrapper(DataInput input) {
        mIsWriting = false;
        this.input = input;
    }

    public SerializerStreamWrapper(DataOutput output) {
        mIsWriting = true;
        this.output = output;
    }

    public void writeBoolean(boolean bool) throws IOException {
        checkOutput();
        output.writeBoolean(bool);
    }

    public boolean readBoolean() throws IOException {
        checkInput();
        return input.readBoolean();
    }

    public void writeInt(int value) throws IOException {
        checkOutput();
        output.writeInt(value);
    }

    public int readInt() throws IOException {
        checkInput();
        return input.readInt();
    }
 }

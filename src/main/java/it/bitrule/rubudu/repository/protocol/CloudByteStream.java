package it.bitrule.rubudu.repository.protocol;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.cloudburstmc.protocol.common.util.VarInts;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public final class CloudByteStream {

    private final @NonNull ByteBuf stream;

    public void writeInt(int value) {
        this.stream.writeInt(value);
    }

    public int readInt() {
        return this.stream.readInt();
    }

    public void writeUnsignedInt(int value) {
        VarInts.writeUnsignedInt(this.stream, value);
    }

    public int readUnsignedInt() {
        return VarInts.readUnsignedInt(this.stream);
    }

    public void writeLong(long value) {
        this.stream.writeLong(value);
    }

    public long readLong() {
        return this.stream.readLong();
    }

    public void writeString(@NonNull String value) {
        this.writeUnsignedInt(value.length());
        this.stream.writeBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    public void writeStringNullable(@Nullable String value) {
        this.writeBoolean(value != null);

        if (value == null) return;

        this.writeString(value);
    }

    public @NonNull String readString() {
        int length = this.readUnsignedInt();
        final byte[] chars = new byte[length];

        for (int i = 0; i < length; ++i) {
            chars[i] = this.stream.readByte();
        }

        return new String(chars);
    }

    public @Nullable String readStringNullable() {
        return this.readBoolean() ? this.readString() : null;
    }

    public void writeBoolean(boolean value) {
        this.stream.writeBoolean(value);
    }

    public boolean readBoolean() {
        return this.stream.readBoolean();
    }

    public byte[] toArray() {
        return this.stream.array();
    }
}
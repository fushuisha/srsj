/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.rtmp.message;

import com.flazr.rtmp.RtmpHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ChunkSize extends AbstractMessage {

    private int chunkSize;

    public ChunkSize(RtmpHeader header, ByteBuf in) {
        super(header, in);
    }

    public ChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CHUNK_SIZE;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    @Override
    public ByteBuf encode() {
        ByteBuf out = Unpooled.buffer(4);
        out.writeInt(chunkSize);
        return out;
    }

    @Override
    public void decode(ByteBuf in) {
        chunkSize = in.readInt();
    }

    @Override
    public String toString() {
        return super.toString() + chunkSize;
    }

}

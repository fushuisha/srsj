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

import com.flazr.amf.Amf0Value;
import com.flazr.rtmp.RtmpHeader;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.List;

public class MetadataAmf0 extends Metadata {        

    public MetadataAmf0(String name, Object... data) {
        super(name, data);
    }

    public MetadataAmf0(RtmpHeader header, ByteBuf in) {
        super(header, in);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.METADATA_AMF0;
    }

    @Override
    public ByteBuf encode() {
        ByteBuf out = Unpooled.buffer();
        Amf0Value.encode(out, name);
        Amf0Value.encode(out, data);
        return out;
    }

    @Override
    public void decode(ByteBuf in) {
        name = (String) Amf0Value.decode(in);
        List<Object> list = new ArrayList<Object>();
        while(in.isReadable()) {
            list.add(Amf0Value.decode(in));
        }
        data = list.toArray();
    }

}

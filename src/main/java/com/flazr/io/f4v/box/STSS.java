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

package com.flazr.io.f4v.box;

import com.flazr.io.f4v.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class STSS implements Payload {

    private static final Logger logger = LoggerFactory.getLogger(STSS.class);
    private List<Integer> sampleNumbers;
    
    public STSS(ByteBuf in) {
        read(in);
    }

    public List<Integer> getSampleNumbers() {
        return sampleNumbers;
    }

    public void setSampleNumbers(List<Integer> sampleNumbers) {
        this.sampleNumbers = sampleNumbers;
    }

    @Override
    public void read(ByteBuf in) {
        in.readInt(); // UI8 version + UI24 flags
        final int count = in.readInt();
        logger.debug("no of sample sync records: {}", count);
        sampleNumbers = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            final Integer sampleNumber = in.readInt();
            // logger.debug("#{} sampleNumber: {}", new Object[]{i, sampleNumber});
            sampleNumbers.add(sampleNumber);
        }
    }

    @Override
    public ByteBuf write() {
        ByteBuf out = Unpooled.buffer();
        out.writeInt(0); // UI8 version + UI24 flags
        out.writeInt(sampleNumbers.size());
        for (Integer sampleNumber : sampleNumbers) {
            out.writeInt(sampleNumber);
        }
        return out;
    }
    
}

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

public class STSZ implements Payload {

    private static final Logger logger = LoggerFactory.getLogger(STSZ.class);
    private List<Integer> sampleSizes;
    private int constantSize;

    public STSZ(ByteBuf in) {
        read(in);
    }

    public List<Integer> getSampleSizes() {
        return sampleSizes;
    }

    public void setConstantSize(int constantSize) {
        this.constantSize = constantSize;
    }

    public void setSampleSizes(List<Integer> sampleSizes) {
        this.sampleSizes = sampleSizes;
    }

    @Override
    public void read(ByteBuf in) {
        in.readInt(); // UI8 version + UI24 flags
        constantSize = in.readInt();
        logger.debug("sample size constant size: {}", constantSize);
        final int count = in.readInt();
        logger.debug("no of sample size records: {}", count);
        sampleSizes = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            final Integer sampleSize = in.readInt();
            // logger.debug("#{} sampleSize: {}", new Object[]{i, sampleSize});
            sampleSizes.add(sampleSize);
        }
    }

    @Override
    public ByteBuf write() {
        ByteBuf out = Unpooled.buffer();
        out.writeInt(0); // UI8 version + UI24 flags
        out.writeInt(constantSize);
        out.writeInt(sampleSizes.size());
        for (Integer sampleSize : sampleSizes) {
            out.writeInt(sampleSize);
        }
        return out;
    }
    
}

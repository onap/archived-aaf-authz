/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.cadi.oauth;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.UUID;

import org.onap.aaf.cadi.Hash;

public class AAFToken {
    private static final int CAPACITY = (Long.SIZE*2+Byte.SIZE*3)/8;
    private static final SecureRandom sr = new SecureRandom();

    public static final String toToken(UUID uuid) {
        long lsb = uuid.getLeastSignificantBits();
        long msb = uuid.getMostSignificantBits();
        int sum=35; // AAF
        for (int i=0;i<Long.SIZE;i+=8) {
            sum+=((lsb>>i) & 0xFF);
        }
        for (int i=0;i<Long.SIZE;i+=8) {
            sum+=((((msb>>i) & 0xFF))<<0xB);
        }
        sum+=(sr.nextInt()&0xEFC00000); // this is just to not leave zeros laying around

        ByteBuffer bb = ByteBuffer.allocate(CAPACITY);
        bb.put((byte)sum);
        bb.putLong(msb);
        bb.put((byte)(sum>>8));
        bb.putLong(lsb);
        bb.put((byte)(sum>>16));
        return Hash.toHexNo0x(bb.array());
    }

    public static final UUID fromToken(String token)  {
        byte[] bytes = Hash.fromHexNo0x(token);
        if (bytes==null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        if (bb.capacity()!=CAPACITY ) {
            return null; // not a CADI Token
        }
        byte b1 = bb.get();
        long msb = bb.getLong();
        byte b2 = bb.get();
        long lsb = bb.getLong();
        byte b3 = (byte)(0x3F&bb.get());
        int sum=35;

        for (int i=0;i<Long.SIZE;i+=8) {
            sum+=((lsb>>i) & 0xFF);
        }
        for (int i=0;i<Long.SIZE;i+=8) {
            sum+=((((msb>>i) & 0xFF))<<0xB);
        }

        if (b1!=((byte)sum) ||
           b2!=((byte)(sum>>8)) ||
           b3!=((byte)((sum>>16)))) {
            return null; // not a CADI Token
        }
        return new UUID(msb, lsb);
    }

}

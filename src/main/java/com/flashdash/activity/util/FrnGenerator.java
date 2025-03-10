package com.flashdash.activity.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

public class FrnGenerator {

    private static final String PREFIX = "frn:flashdash";
    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateFrn(ResourceType resourceType) {
        String uniqueId = generateBase62UUID();
        return String.format("%s:%s:%s", PREFIX, resourceType.getType(), uniqueId);
    }

    private static String generateBase62UUID() {
        UUID uuid = UUID.randomUUID();
        byte[] uuidBytes = ByteBuffer.wrap(new byte[16])
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
        return encodeBase62(new BigInteger(1, uuidBytes));
    }

    private static String encodeBase62(BigInteger value) {
        StringBuilder sb = new StringBuilder();
        while (value.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = value.divideAndRemainder(BigInteger.valueOf(62));
            value = divmod[0];
            sb.insert(0, BASE62.charAt(divmod[1].intValue()));
        }
        return sb.toString();
    }
}

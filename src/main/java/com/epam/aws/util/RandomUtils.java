package com.epam.aws.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomUtils {

    public static int getRandomInt(int upperLimit) {
        if (upperLimit > 0) {
            return ThreadLocalRandom.current().nextInt(0, upperLimit);
        } else {
            return 0;
        }
    }
}

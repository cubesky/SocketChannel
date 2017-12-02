package party.liyin.socketchannel;

import com.google.common.collect.BiMap;

import java.util.Random;

class Utils {
    private static Random random = new Random();

    static long getNewUniqueId(BiMap uniqueMap) {
        long newValue = random.nextLong();
        while (uniqueMap.containsValue(newValue) || newValue == 0) newValue = random.nextLong();
        return newValue;
    }
}


package com.artemlychko;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.artemlychko.CustomHashMap;

import java.util.*;


import static org.junit.jupiter.api.Assertions.*;


class CustomHashMapTest {

    private CustomHashMap<Object, Object> map;
    private static final int TESTS_WITH_BIG_DATA_SIZE = 1_000_000_0;
    private static final String[] keys = new String[TESTS_WITH_BIG_DATA_SIZE];
    private static final Integer[] values = new Integer[TESTS_WITH_BIG_DATA_SIZE];

    @BeforeAll
    static void fillDataArrays() {
        for (int i = 0; i < TESTS_WITH_BIG_DATA_SIZE; i++) {
            values[i] = (int) (Math.random() * 10000);
            keys[i] = "key" + values[i];
        }
    }

    @BeforeEach
    void init() {
        map = new CustomHashMap<>();
    }

    @Test
    void constructor() {
        assertAll(
                () -> assertEquals(0, map.size()),
                () -> assertEquals(16, map.getCapacity()),
                () -> assertEquals(0.75f, map.getLoadFactor())
        );

    }

    @Test
    void constructorWithInitialCapacity() {
        int initialCapacity = 3;
        var newMap = new CustomHashMap<>(initialCapacity);

        assertAll(
                () -> assertEquals(0, newMap.size()),
                () -> assertEquals(initialCapacity, newMap.getCapacity()),
                () -> assertEquals(0.75f, newMap.getLoadFactor())
        );
    }

    @Test
    void constructorWithInitialCapacityAndLoadFactor() {
        int initialCapacity = 3;
        float loadFactor = 0.6f;
        var newMap = new CustomHashMap<>(initialCapacity, loadFactor);

        assertAll(
                () -> assertEquals(0, newMap.size()),
                () -> assertEquals(initialCapacity, newMap.getCapacity()),
                () -> assertEquals(loadFactor, newMap.getLoadFactor())
        );
    }

    @Test
    void constructorWithNegativeInitialCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new CustomHashMap<>(-1));
    }

    @Test
    void constructorWithNegativeLoadFactor() {
        assertThrows(IllegalArgumentException.class, () -> new CustomHashMap<>(1, -0.5f));
    }

    @Test
    void constructorWithNaNLoadFactor() {
        assertThrows(IllegalArgumentException.class, () -> new CustomHashMap<>(10, Float.NaN));
    }

    @Test
    void emptyMap() {
        assertTrue(map.isEmpty());
    }

    @Test
    void clearMap() {
        map.put("One", 1);
        map.put("Two", 2);
        map.put("Three", 3);
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    void putAndGet() {
        map.put("One", 1);
        map.put("Two", 2);

        assertAll(
                () -> assertEquals(1, map.get("One")),
                () -> assertEquals(2, map.get("Two")),
                () -> assertThrows(NoSuchElementException.class, () -> map.get(3))
        );
    }

    @Test
    void putAndGetWithNullKey() {
        map.put(null, 1);

        assertEquals(1, map.get(null));
    }

    @Test
    void containsKey() {
        map.put("One", null);

        assertAll(
                () -> assertTrue(map.containsKey("One")),
                () -> assertFalse(map.containsKey("ONE"))
        );
    }

    @Test
    void containsNullKey() {
        map.put(null, 1);

        assertTrue(map.containsKey(null));
    }

    @Test
    void containsValue() {
        map.put("One", null);

        assertTrue(map.containsValue(null));
    }

    @Test
    void containsValueWithNullKey() {
        map.put(null, 1);

        assertTrue(map.containsValue(1));
    }

    @Test
    void remove() {
        map.put("One", 1);
        map.put("Two", 2);

        assertAll(
                () -> assertEquals(1, map.remove("One")),
                () -> assertThrows(NoSuchElementException.class, () -> map.get("One")),
                () -> assertEquals(2, map.get("Two")),
                () -> assertThrows(NoSuchElementException.class, () -> map.remove("Three"))
        );
    }

    @Test
    void removeWithNullKey() {
        map.put(null, 1);

        assertAll(
                () -> assertEquals(1, map.remove(null)),
                () -> assertThrows(NoSuchElementException.class, () -> map.get(null))
        );
    }

    @Test
    void putWithResize() {
        var newMap = new CustomHashMap<>(2);
        newMap.put("One", 1);
        newMap.put("Two", 2);
        newMap.put("Three", 3);

        assertAll(
                () -> assertEquals(3, newMap.size()),
                () -> assertEquals(1, newMap.get("One")),
                () -> assertEquals(2, newMap.get("Two")),
                () -> assertEquals(3, newMap.get("Three"))
        );
    }

    @Test
    void putWithResizeWithNullKey() {
        var newMap = new CustomHashMap<>(2);
        newMap.put("One", 1);
        newMap.put("Two", 2);
        newMap.put(null, 3);

        assertAll(
                () -> assertEquals(3, newMap.size()),
                () -> assertEquals(1, newMap.get("One")),
                () -> assertEquals(2, newMap.get("Two")),
                () -> assertEquals(3, newMap.get(null))
        );
    }

    @Test
    void uniqueKey() {
        map.put("One", 1);
        map.put("One", 2);
        map.put("One", 3);

        assertAll(
                () -> assertEquals(1, map.size()),
                () -> assertEquals(3, map.get("One"))
        );
    }

    @Test
    void uniqueNullKey() {
        map.put(null, 1);
        map.put(null, 2);
        map.put(null, 3);

        assertAll(
                () -> assertEquals(1, map.size()),
                () -> assertEquals(3, map.get(null))
        );
    }

    @Test
    void putAndGetWithBigData() {
        for (int i = 0; i < TESTS_WITH_BIG_DATA_SIZE; i++) {
            map.put(keys[i], values[i]);
        }

        for (int i = 0; i < map.size(); i++) {
            assertEquals(values[i], map.get(keys[i]));
        }
    }

    @Test
    void removeWithBigData() {
        for (int i = 0; i < TESTS_WITH_BIG_DATA_SIZE; i++) {
            map.put(keys[i], values[i]);
        }
        String[] uniqueKeys = Arrays.stream(keys).distinct().toArray(String[]::new);
        Integer[] uniqueValues = Arrays.stream(values).distinct().toArray(Integer[]::new);

        for (int i = 0; i < map.size(); i++) {
            assertEquals(uniqueValues[i], map.remove(uniqueKeys[i]));
        }
    }

}


package ru.artemlychko;


import java.util.*;
import java.util.stream.Collectors;


/**
 * Hash table based implementation of the {@code Map} interface.  This
 * implementation provides all the optional map operations, and permits
 * {@code null} values and the {@code null} key.
 * <p>
 * The array of lists of entries is used to place elements in the Hash table.
 * An instance of {@code CustomHashMap} has two parameters that affect its
 * performance: <i>capacity</i> and <i>load factor</i>.
 * The <i>capacity</i> is the number of buckets in the hash table, and the <i>initial capacity</i>
 * is simply the capacity at the time the hash table is created.  The
 * <i>load factor</i> is a measure of how full the hash table is allowed to
 * get before its capacity is automatically increased.  When the number of
 * entries in the hash table exceeds the product of the load factor and the
 * current capacity, the hash table is rehashed (that is, internal data
 * structures are rebuilt) so that the hash table has approximately twice the
 * number of buckets.
 * <p>
 *
 * @param <K> the Type of keys in this map
 * @param <V> the Type of values in this map
 * @author Lychko Artem
 */
public class CustomHashMap<K, V> implements Map<K, V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * An Entry maintaining a key and a value.  The value may be
     * changed using the {@code setValue} method. Instances of
     * this class are not associated with any map nor with any
     * map's entry-set view.
     *
     * @param <K> the type of key
     * @param <V> the type of the value
     */
    private static class MyEntry<K, V> implements Entry<K, V> {
        private final int hash;
        private final K key;
        private V value;

        public MyEntry(K key, V value) {
            if (key == null)
                this.hash = 0;
            else
                this.hash = key.hashCode();

            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() throws NullPointerException {
            return this.key;
        }

        @Override
        public V getValue() throws NullPointerException {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            V temp = this.value;
            this.value = value;
            return temp;
        }
    }

    private LinkedList<MyEntry<K, V>>[] bins;

    private int size;

    private final float loadFactor;

    private int capacity;

    /**
     * Constructs an empty map with an initial capacity of DEFAULT_CAPACITY
     * and a default load factor of DEFAULT_LOAD_FACTOR.
     */
    public CustomHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty map with an initial capacity of capacity.
     *
     * @param initialCapacity the initial capacity of the map
     * @throws IllegalArgumentException - if the specified initial capacity is wrong
     */
    @SuppressWarnings("unchecked")
    public CustomHashMap(int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Вместимость не может быть отрицательной");
        this.capacity = initialCapacity;
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.bins = new LinkedList[capacity];
    }

    /**
     * Constructs an empty map with an initial capacity of capacity and load factor.
     *
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor      the initial load factor of the map
     * @throws IllegalArgumentException - if the specified initial capacity or load factor is wrong
     */
    @SuppressWarnings("unchecked")
    public CustomHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Вместимость не может быть отрицательной");
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Неподходящий load factor: " + loadFactor);
        this.loadFactor = loadFactor;
        this.capacity = initialCapacity;
        this.bins = new LinkedList[capacity];
    }

    /**
     * @return {@code true} if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all the elements from this map.
     */
    @Override
    public void clear() {
        Arrays.stream(bins)
                .filter(Objects::nonNull)
                .forEach(LinkedList::clear);
        size = 0;
    }

    /**
     * @return a {@link Set} view of the keys contained in this map.
     */
    @Override
    public Set<K> keySet() {
        return Arrays.stream(bins)
                .filter(Objects::nonNull)
                .flatMap((l) -> l.stream()
                        .map(MyEntry::getKey))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * @return a {@link Collection} view of values contained in this map.
     */
    @Override
    public Collection<V> values() {
        return Arrays.stream(bins)
                .filter(Objects::nonNull)
                .flatMap((l) -> l.stream()
                        .map(MyEntry::getValue))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @return a {@link Set} view of the mappings contained in this map.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return Arrays.stream(bins)
                .filter(Objects::nonNull)
                .flatMap((l) -> l.stream()
                        .map((e) -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue())))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Copies all the mappings from the specified map to this map
     *
     * @param m mappings to be stored in this map
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Appends the specified entry to the specified bucket into the array of buckets of this map.
     *
     * @param key   key to be appended to map.
     * @param value value to be appended to map.
     * @return value to be appended to map.
     */
    @Override
    public V put(K key, V value) {
        int index = getIndex(key);
        if (bins[index] == null)
            bins[index] = new LinkedList<>();

        for (MyEntry<K, V> entry : bins[index]) {
            if (Objects.equals(key, entry.getKey())) {
                entry.setValue(value);
                return value;
            }
        }
        bins[index].add(new MyEntry<>(key, value));
        size++;
        if ((double) size / capacity > loadFactor)
            resize();
        return value;
    }


    /**
     * Returns the value with the specified key form the map.
     *
     * @param key of the entry to be got from the map.
     * @return value to be got from the map if this map contains a mapping for the specified key,
     * {@code null} if map doesn't contain it
     * @throws NoSuchElementException - if entry with this key doesn't exist
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        int index = getIndex((K) key);

        if (bins[index] != null) {
            for (MyEntry<K, V> entry : bins[index]) {
                if (Objects.equals(key, entry.getKey()) && (entry.hash == (key != null ? key.hashCode() : 0))) {
                    return entry.getValue();
                }
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Removes the specified entry to the specified bucket into the array of buckets of this map.
     *
     * @param key of the entry to be removed from the map.
     * @return value to be removed from the map if this map contains a mapping for the specified key,
     * {@code null} if map doesn't contain it
     * @throws NoSuchElementException - if entry with this key doesn't exist
     */
    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        int index = getIndex((K) key);

        if (bins[index] != null) {
            for (MyEntry<K, V> entry : bins[index]) {
                if (Objects.equals(key, entry.getKey())) {
                    V value = entry.getValue();
                    bins[index].remove(entry);
                    size--;
                    return value;
                }
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified key
     *
     * @param key key whose presence in this map
     * @return {@code true} if this map contains a mapping for the specified key,
     * {@code false}  if this map doesn't contain a mapping for the specified key
     */
    @Override
    public boolean containsKey(Object key) {
        for (LinkedList<MyEntry<K, V>> bin : bins) {
            if (bin != null) {
                for (MyEntry<K, V> entry : bin) {
                    if (entry != null && (Objects.equals(key, entry.getKey()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified value
     *
     * @param value key whose presence in this map
     * @return {@code true} if this map contains a mapping for the specified value,
     * {@code false}  if this map doesn't contain a mapping for the specified value
     */
    @Override
    public boolean containsValue(Object value) {
        for (LinkedList<MyEntry<K, V>> bin : bins) {
            if (bin != null) {
                for (MyEntry<K, V> entry : bin) {
                    if (entry != null && (Objects.equals(value, entry.getValue()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * @return the load factor for the hash table in this map
     */
    public float getLoadFactor() {
        return loadFactor;
    }

    /**
     * @return the capacity for the hash table in this map
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @param key whose presence in this map
     * @return the positive index for the bucket in this map
     */
    private int getIndex(K key) {
        return key == null ? 0 : Math.abs(key.hashCode() % capacity);
    }

    /**
     * Resize base array of buckets of the map.
     * Create the new array with new capacity. Copy all buckets to the new array with rehashing.
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        capacity *= 2;
        LinkedList<MyEntry<K, V>>[] newBins = new LinkedList[capacity];

        for (LinkedList<MyEntry<K, V>> bin : bins) {
            if (bin != null) {
                for (MyEntry<K, V> entry : bin) {
                    int newIndex = getIndex(entry.getKey());
                    if (newBins[newIndex] == null) {
                        newBins[newIndex] = new LinkedList<>();
                    }
                    newBins[newIndex].add(entry);
                }
            }
        }
        bins = newBins;
    }
}
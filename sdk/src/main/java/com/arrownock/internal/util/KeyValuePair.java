package com.arrownock.internal.util;

public class KeyValuePair<K, V> {
    private K key;
    private V value;
    private static final String OPEN_BRACE = "{";
    private static final String COMMA = ",";
    private static final String CLOSE_BRACE = "}";

    public KeyValuePair() {
    }

    public KeyValuePair(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public KeyValuePair<K, V> setPair(final K key, final V value) {
        this.key = key;
        this.value = value;
        return this;
    }

    public K getKey() {
        return key;
    }

    public KeyValuePair<K, V> setKey(final K key) {
        this.key = key;
        return this;
    }

    public V getValue() {
        return value;
    }

    public KeyValuePair<K, V> setValue(final V value) {
        this.value = value;
        return this;
    }

    public String toString() {
        return OPEN_BRACE + key + COMMA + value + CLOSE_BRACE;
    }

    public String toTuple() {
        return OPEN_BRACE + key + COMMA + value + CLOSE_BRACE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValuePair<?, ?> pair = (KeyValuePair<?, ?>) o;

        if (key != null ? !key.equals(pair.key) : pair.key != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }
}
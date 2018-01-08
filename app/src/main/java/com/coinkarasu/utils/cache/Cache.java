package com.coinkarasu.utils.cache;

public interface Cache {
    Entry get(String key);

    void put(String key, Entry entry);

    void remove(String key);

    void clear();

    class Entry {
        public String data;

        public Entry(String data) {
            this.data = data;
        }
    }
}

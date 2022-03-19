package com.example.routerdemo;

import android.util.ArrayMap;
import android.util.Log;

import java.util.ConcurrentModificationException;

public class MArrayMap<K, V> {
    private static final boolean DEBUG = false;
    private static final String TAG = "MArrayMap";
    int mSize;
    int[] mHashes;
    private static final boolean CONCURRENT_MODIFICATION_EXCEPTIONS = true;
    final boolean mIdentityHashCode;
    static final int[] EMPTY_IMMUTABLE_INTS = new int[0];
    public static final ArrayMap EMPTY = new ArrayMap<>(-1);
    Object[] mArray;
    private static final int BASE_SIZE = 4;
    static Object[] mBaseCache;
    static int mBaseCacheSize;
    static Object[] mTwiceBaseCache;
    static int mTwiceBaseCacheSize;
    static final Object[] EmptyArray = new Object[0];
    private static final int CACHE_SIZE = 10;
    static boolean sThrowExceptionForUpperArrayOutOfBounds = true;

    public MArrayMap() {
        this(0, false);
    }

    /**
     * Create a new ArrayMap with a given initial capacity.
     */
    public MArrayMap(int capacity) {
        this(capacity, false);
    }

    public MArrayMap(int capacity, boolean identityHashCode) {
        mIdentityHashCode = identityHashCode;

        // If this is immutable, use the sentinal EMPTY_IMMUTABLE_INTS
        // instance instead of the usual EmptyArray.INT. The reference
        // is checked later to see if the array is allowed to grow.
        if (capacity < 0) {
            mHashes = EMPTY_IMMUTABLE_INTS;
            mArray = EmptyArray;//EmptyArray.OBJECT;
        } else if (capacity == 0) {
            mHashes = new int[0];//EmptyArray.INT;
            mArray = EmptyArray;//EmptyArray.OBJECT;
        } else {
            allocArrays(capacity);
        }
        mSize = 0;
    }

    public V put(K key, V value) {
        final int osize = mSize;
        final int hash;
        int index;
        if (key == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = mIdentityHashCode ? System.identityHashCode(key) : key.hashCode();
            index = indexOf(key, hash);
        }
        if (index >= 0) {
            index = (index<<1) + 1;
            final V old = (V)mArray[index];
            mArray[index] = value;
            return old;
        }

        index = ~index;
        if (osize >= mHashes.length) {
            final int n = osize >= (BASE_SIZE*2) ? (osize+(osize>>1))
                    : (osize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);

            if (DEBUG) Log.d(TAG, "put: grow from " + mHashes.length + " to " + n);

            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(n);

            if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
                throw new ConcurrentModificationException();
            }

            if (mHashes.length > 0) {
                if (DEBUG) Log.d(TAG, "put: copy 0-" + osize + " to 0");
                System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, mArray, 0, oarray.length);
            }

            freeArrays(ohashes, oarray, osize);
        }

        if (index < osize) {
            if (DEBUG) Log.d(TAG, "put: move " + index + "-" + (osize-index)
                    + " to " + (index+1));
            System.arraycopy(mHashes, index, mHashes, index + 1, osize - index);
            System.arraycopy(mArray, index << 1, mArray, (index + 1) << 1, (mSize - index) << 1);
        }

        if (CONCURRENT_MODIFICATION_EXCEPTIONS) {
            if (osize != mSize || index >= mHashes.length) {
                throw new ConcurrentModificationException();
            }
        }
        mHashes[index] = hash;
        mArray[index<<1] = key;
        mArray[(index<<1)+1] = value;
        mSize++;
        return null;
    }

    int indexOfNull() {
        final int N = mSize;

        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;//-1
        }

        int index = binarySearchHashes(mHashes, N, 0);

        // If the hash code wasn't found, then we have no entry for this key.
        if (index < 0) {
            return index;
        }

        // If the key at the returned index matches, that's what we want.
        if (null == mArray[index<<1]) {
            return index;
        }

        // Search for a matching key after the index.
        int end;
        for (end = index + 1; end < N && mHashes[end] == 0; end++) {
            if (null == mArray[end << 1]) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == 0; i--) {
            if (null == mArray[i << 1]) return i;
        }

        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }

    private static int binarySearchHashes(int[] hashes, int N, int hash) {
        try {
            return ContainerHelpers.binarySearch(hashes, N, hash);
        } catch (ArrayIndexOutOfBoundsException e) {
            if (CONCURRENT_MODIFICATION_EXCEPTIONS) {
                throw new ConcurrentModificationException();
            } else {
                throw e; // the cache is poisoned at this point, there's not much we can do
            }
        }
    }

    private void allocArrays(final int size) {
        if (mHashes == EMPTY_IMMUTABLE_INTS) {
            throw new UnsupportedOperationException("ArrayMap is immutable");
        }
        if (size == (BASE_SIZE*2)) {
            synchronized (ArrayMap.class) {
                if (mTwiceBaseCache != null) {
                    final Object[] array = mTwiceBaseCache;
                    mArray = array;
                    mTwiceBaseCache = (Object[])array[0];
                    mHashes = (int[])array[1];
                    array[0] = array[1] = null;
                    mTwiceBaseCacheSize--;
                    if (DEBUG) Log.d(TAG, "Retrieving 2x cache " + mHashes
                            + " now have " + mTwiceBaseCacheSize + " entries");
                    return;
                }
            }
        } else if (size == BASE_SIZE) {
            synchronized (ArrayMap.class) {
                if (mBaseCache != null) {
                    final Object[] array = mBaseCache;
                    mArray = array;
                    mBaseCache = (Object[])array[0];
                    mHashes = (int[])array[1];
                    array[0] = array[1] = null;
                    mBaseCacheSize--;
                    if (DEBUG) Log.d(TAG, "Retrieving 1x cache " + mHashes
                            + " now have " + mBaseCacheSize + " entries");
                    return;
                }
            }
        }

        mHashes = new int[size];
        mArray = new Object[size<<1];
    }

    int indexOf(Object key, int hash) {
        final int N = mSize;

        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;//-1
        }

        int index = binarySearchHashes(mHashes, N, hash);

        // If the hash code wasn't found, then we have no entry for this key.
        if (index < 0) {
            return index;
        }

        // If the key at the returned index matches, that's what we want.
        if (key.equals(mArray[index<<1])) {
            return index;
        }

        // Search for a matching key after the index.
        int end;
        for (end = index + 1; end < N && mHashes[end] == hash; end++) {
            if (key.equals(mArray[end << 1])) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
            if (key.equals(mArray[i << 1])) return i;
        }

        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }

    private static void freeArrays(final int[] hashes, final Object[] array, final int size) {
        if (hashes.length == (BASE_SIZE*2)) {
            synchronized (ArrayMap.class) {
                if (mTwiceBaseCacheSize < CACHE_SIZE) {
                    array[0] = mTwiceBaseCache;
                    array[1] = hashes;
                    for (int i=(size<<1)-1; i>=2; i--) {
                        array[i] = null;
                    }
                    mTwiceBaseCache = array;
                    mTwiceBaseCacheSize++;
                    if (DEBUG) Log.d(TAG, "Storing 2x cache " + array
                            + " now have " + mTwiceBaseCacheSize + " entries");
                }
            }
        } else if (hashes.length == BASE_SIZE) {
            synchronized (ArrayMap.class) {
                if (mBaseCacheSize < CACHE_SIZE) {
                    array[0] = mBaseCache;
                    array[1] = hashes;
                    for (int i=(size<<1)-1; i>=2; i--) {
                        array[i] = null;
                    }
                    mBaseCache = array;
                    mBaseCacheSize++;
                    if (DEBUG) Log.d(TAG, "Storing 1x cache " + array
                            + " now have " + mBaseCacheSize + " entries");
                }
            }
        }
    }

    public V get(Object key) {
        final int index = indexOfKey(key);
        return index >= 0 ? (V)mArray[(index<<1)+1] : null;
    }

    /**
     * Returns the index of a key in the set.
     *
     * @param key The key to search for.
     * @return Returns the index of the key if it exists, else a negative integer.
     */
    public int indexOfKey(Object key) {
        return key == null ? indexOfNull()
                : indexOf(key, mIdentityHashCode ? System.identityHashCode(key) : key.hashCode());
    }

    public V remove(Object key) {
        final int index = indexOfKey(key);
        if (index >= 0) {
            return removeAt(index);
        }

        return null;
    }

    /**
     * Remove the key/value mapping at the given index.
     *
     * <p>For indices outside of the range <code>0...size()-1</code>, the behavior is undefined for
     * apps targeting {@link android.os.Build.VERSION_CODES#P} and earlier, and an
     * {@link ArrayIndexOutOfBoundsException} is thrown for apps targeting
     * {@link android.os.Build.VERSION_CODES#Q} and later.</p>
     *
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the value that was stored at this index.
     */
    public V removeAt(int index) {
        if (index >= mSize && sThrowExceptionForUpperArrayOutOfBounds) {
            // The array might be slightly bigger than mSize, in which case, indexing won't fail.
            // Check if exception should be thrown outside of the critical path.
            throw new ArrayIndexOutOfBoundsException(index);
        }

        final Object old = mArray[(index << 1) + 1];
        final int osize = mSize;
        final int nsize;
        if (osize <= 1) {
            // Now empty.
            if (DEBUG) Log.d(TAG, "remove: shrink from " + mHashes.length + " to 0");
            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            mHashes = new int[0];//EmptyArray.INT;
            mArray = EmptyArray;//EmptyArray.OBJECT;
            freeArrays(ohashes, oarray, osize);
            nsize = 0;
        } else {
            nsize = osize - 1;
            if (mHashes.length > (BASE_SIZE*2) && mSize < mHashes.length/3) {
                // Shrunk enough to reduce size of arrays.  We don't allow it to
                // shrink smaller than (BASE_SIZE*2) to avoid flapping between
                // that and BASE_SIZE.
                final int n = osize > (BASE_SIZE*2) ? (osize + (osize>>1)) : (BASE_SIZE*2);

                if (DEBUG) Log.d(TAG, "remove: shrink from " + mHashes.length + " to " + n);

                final int[] ohashes = mHashes;
                final Object[] oarray = mArray;
                allocArrays(n);

                if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
                    throw new ConcurrentModificationException();
                }

                if (index > 0) {
                    if (DEBUG) Log.d(TAG, "remove: copy from 0-" + index + " to 0");
                    System.arraycopy(ohashes, 0, mHashes, 0, index);
                    System.arraycopy(oarray, 0, mArray, 0, index << 1);
                }
                if (index < nsize) {
                    if (DEBUG) Log.d(TAG, "remove: copy from " + (index+1) + "-" + nsize
                            + " to " + index);
                    System.arraycopy(ohashes, index + 1, mHashes, index, nsize - index);
                    System.arraycopy(oarray, (index + 1) << 1, mArray, index << 1,
                            (nsize - index) << 1);
                }
            } else {
                if (index < nsize) {
                    if (DEBUG) Log.d(TAG, "remove: move " + (index+1) + "-" + nsize
                            + " to " + index);
                    System.arraycopy(mHashes, index + 1, mHashes, index, nsize - index);
                    System.arraycopy(mArray, (index + 1) << 1, mArray, index << 1,
                            (nsize - index) << 1);
                }
                mArray[nsize << 1] = null;
                mArray[(nsize << 1) + 1] = null;
            }
        }
        if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
            throw new ConcurrentModificationException();
        }
        mSize = nsize;
        return (V)old;
    }

    public int size() {
        return mSize;
    }
}

/**
 * Copyright 2002-2004 The Apache Software Foundation.
 *
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
 */

/*
 * Note: originally released under the GNU LGPL v2.1,
 * but rereleased by the original author under the ASF license (above).
 */
package libomv.utils;

/**
 * <p>
 * A hash map that uses primitive ints for the value rather than objects.
 * </p>
 * 
 * <p>
 * Note that this class is for internal optimization purposes only, and may not
 * be supported in future releases of Jakarta Commons Lang. Utilities of this
 * sort may be included in future releases of Jakarta Commons Collections.
 * </p>
 * 
 * @author Justin Couch
 * @author Alex Chaffee (alex@apache.org)
 * @author Stephen Colebourne
 * @since 2.0
 * @version $Revision: 1.1 $
 * @see java.util.HashMap
 */
public class HashMapInt <K>
{
    /**
     * The default initial capacity - MUST be a power of two.
     */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The hash table data.
	 */
	private transient Entry<K>[] table;

	/**
	 * The total number of entries in the hash table.
	 */
	private transient int size;

	/**
	 * The table is rehashed when its size exceeds this threshold. (The value of
	 * this field is (int)(capacity * loadFactor).)
	 * 
	 * @serial
	 */
	private int threshold;

	/**
	 * The load factor for the hashtable.
	 * 
	 * @serial
	 */
	private float loadFactor;

	/**
	 * <p>
	 * Innerclass that acts as a datastructure to create a new entry in the
	 * table.
	 * </p>
	 */
	private static class Entry <K>
	{
		int hash;

		K key;

		int value;

		Entry<K> next;

		/**
		 * <p>
		 * Create a new entry with the given values.
		 * </p>
		 * 
		 * @param hash
		 *            The code used to hash the object with
		 * @param key
		 *            The key used to enter this in the table
		 * @param value
		 *            The value for this key
		 * @param next
		 *            A reference to the next entry in the table
		 */
		protected Entry(int hash, K key, int value, Entry<K> next)
		{
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

	/**
	 * <p>
	 * Constructs a new, empty hashtable with a default capacity and load
	 * factor, which is <code>20</code> and <code>0.75</code> respectively.
	 * </p>
	 */
	public HashMapInt()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * <p>
	 * Constructs a new, empty hashtable with the specified initial capacity and
	 * default load factor, which is <code>0.75</code>.
	 * </p>
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hashtable.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero.
	 */
	public HashMapInt(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * <p>
	 * Constructs a new, empty hashtable with the specified initial capacity and
	 * the specified load factor.
	 * </p>
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hashtable.
	 * @param loadFactor
	 *            the load factor of the hashtable.
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero, or if the load
	 *             factor is nonpositive.
	 */
	@SuppressWarnings("unchecked")
	public HashMapInt(int initialCapacity, float loadFactor)
	{
		super();
		if (initialCapacity < 0)
		{
			throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
		}
		if (loadFactor <= 0)
		{
			throw new IllegalArgumentException("Illegal Load: " + loadFactor);
		}
		if (initialCapacity == 0)
		{
			initialCapacity = 1;
		}

		this.loadFactor = loadFactor;
		table = new Entry[initialCapacity];
		threshold = (int) (initialCapacity * loadFactor);
	}

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits. Note: Null keys always map to hash 0, thus index 0.
     */
    static int hash(int h)
    {
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Returns index for hash code h.
     */
    static int indexFor(int h, int length)
    {
        return h & (length-1);
    }

    /**
	 * <p>
	 * Returns the number of keys in this hashtable.
	 * </p>
	 * 
	 * @return the number of keys in this hashtable.
	 */
	public int size()
	{
		return size;
	}

	/**
	 * <p>
	 * Tests if this hashtable maps no keys to values.
	 * </p>
	 * 
	 * @return <code>true</code> if this hashtable maps no keys to values;
	 *         <code>false</code> otherwise.
	 */
	public boolean isEmpty()
	{
		return size == 0;
	}

	/**
	 * <p>
	 * Returns <code>true</code> if this HashMap maps one or more keys to this
	 * value.
	 * </p>
	 * 
	 * <p>
	 * Note that this method is identical in functionality to contains (which
	 * predates the Map interface).
	 * </p>
	 * 
	 * @param value
	 *            value whose presence in this HashMap is to be tested.
	 * @see java.util.Map
	 * @since JDK1.2
	 */
	public boolean containsValue(int value)
	{
		for (int i = table.length; i-- > 0;)
		{
			for (Entry<K> e = table[i]; e != null; e = e.next)
			{
				if (e.value == value)
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * <p>
	 * Tests if the specified object is a key in this hashtable.
	 * </p>
	 * 
	 * @param key
	 *            possible key.
	 * @return <code>true</code> if and only if the specified object is a key in
	 *         this hashtable, as determined by the <tt>equals</tt> method;
	 *         <code>false</code> otherwise.
	 * @see #contains(Object)
	 */
	public boolean containsKey(Object key)
	{
		int hash = (key == null) ? 0 : hash(key.hashCode());
		for (Entry<K> e = table[indexFor(hash, table.length)]; e != null; e = e.next)
		{
            K k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
				return true;
		}
		return false;
	}

	/**
	 * <p>
	 * Returns the value to which the specified key is mapped in this map.
	 * </p>
	 * 
	 * @param key
	 *            a key in the hashtable.
	 * @return the value to which the key is mapped in this hashtable;
	 *         <code>null</code> if the key is not mapped to any value in this
	 *         hashtable.
	 * @see #put(int, Object)
	 */
	public int get(Object key)
	{
		int hash = (key == null) ? 0 : hash(key.hashCode());
		for (Entry<K> e = table[indexFor(hash, table.length)]; e != null; e = e.next)
		{
            K k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
                return e.value;
		}
		return -1;
	}

	/**
	 * <p>
	 * Increases the capacity of and internally reorganizes this hashtable, in
	 * order to accommodate and access its entries more efficiently.
	 * </p>
	 * 
	 * <p>
	 * This method is called automatically when the number of keys in the
	 * hashtable exceeds this hashtable's capacity and load factor.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	protected void resize(int newCapacity)
	{
        Entry<K>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY)
        {
            threshold = Integer.MAX_VALUE;
            return;
        }
		table = new Entry[newCapacity];

		for (int k = 0; k < oldCapacity; k++)
		{
			Entry<K> e = oldTable[k];
			if (e != null)
			{
				oldTable[k] = null;
				do
				{
					Entry<K> next = e.next;
					int i = indexFor(e.hash, newCapacity);
					e.next = table[i];
					table[i] = e;
					e = next;
				} while (e != null);
			}
		}
        threshold = (int)(newCapacity * loadFactor);
	}

    /**
	 * <p>
	 * Maps the specified <code>key</code> to the specified <code>value</code>
	 * in this hashtable. The key cannot be <code>null</code>.
	 * </p>
	 * 
	 * <p>
	 * The value can be retrieved by calling the <code>get</code> method with a
	 * key that is equal to the original key.
	 * </p>
	 * 
	 * @param key
	 *            the hashtable key.
	 * @param value
	 *            the value.
	 * @return the previous value of the specified key in this hashtable, or
	 *         <code>null</code> if it did not have one.
	 * @throws NullPointerException
	 *             if the key is <code>null</code>.
	 * @see #get(int)
	 */
	public int put(K key, int value)
	{
		// Makes sure the key is not already in the hashtable.
		int hash = (key == null) ? 0 : hash(key.hashCode());
		int index = indexFor(hash, table.length);
		for (Entry<K> e = table[index]; e != null; e = e.next)
		{
			K k;
            if (e.hash == hash && ((k = e.key) == key || key.equals(k)))
			{
				int old = e.value;
				e.value = value;
				return old;
			}
		}
		
		// Creates the new entry.
        table[index] = new Entry<K>(hash, key, value, table[index]);
        if (size++ >= threshold)
            resize(2 * table.length);

		return -1;
	}

	/**
	 * <p>
	 * Removes the key (and its corresponding value) from this hashtable.
	 * </p>
	 * 
	 * <p>
	 * This method does nothing if the key is not present in the hashtable.
	 * </p>
	 * 
	 * @param key
	 *            the key that needs to be removed.
	 * @return the value to which the key had been mapped in this hashtable, or
	 *         <code>null</code> if the key did not have a mapping.
	 */
	public int remove(Object key)
	{
        int hash = (key == null) ? 0 : hash(key.hashCode());
        int i = indexFor(hash, table.length);
		for (Entry<K> e = table[i], prev = null; e != null; prev = e, e = e.next)
		{
            Object k;
            if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
            {
				if (prev != null)
				{
					prev.next = e.next;
				}
				else
				{
					table[i] = e.next;
				}
				size--;
				int oldValue = e.value;
				e.value = 0;
				return oldValue;
			}
		}
		return -1;
	}

	/**
	 * <p>
	 * Clears this hashtable so that it contains no keys.
	 * </p>
	 */
	public synchronized void clear()
	{
		Entry<K> tab[] = table;
		for (int index = tab.length; --index >= 0;)
		{
			tab[index] = null;
		}
		size = 0;
	}

}

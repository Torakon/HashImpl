package ci583.htable.impl;

/**
 * A HashTable with no deletions allowed. Duplicates overwrite the existing value. Values are of
 * type V and keys are strings -- one extension is to adapt this class to use other types as keys.
 * 
 * The underlying data is stored in the array 'arr', and the actual values stored are pairs of 
 * (key, value). This is so that we can detect collisions in the hash function and look for the next 
 * location when necessary.
 */

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;

public class Hashtable<T extends Comparable<T>,V> {

	private Object[] arr; //an array of Pair objects, where each pair contains the key and value stored in the hashtable
	private int max; //the size of arr. This should be a prime number
	private int itemCount = 0; //the number of items stored in arr
	private final double maxLoad = 0.6; //the maximum load factor
	private int collTotal = 0;  //DEBUG code
	boolean showColl = false; //DEBUG code
	boolean currentColl = false; //DEBUG code
	public static enum PROBE_TYPE {
		LINEAR_PROBE, QUADRATIC_PROBE, DOUBLE_HASH;
	}
	PROBE_TYPE probeType; //the type of probe to use when dealing with collisions
	private final BigInteger DBL_HASH_K = BigInteger.valueOf(8);

	/**
	 * Create a new Hashtable with a given initial capacity and using a given probe type
	 * @param initialCapacity
	 * @param pt
	 * @param debugInput - true to show collision information in console
	 */
	public Hashtable(int initialCapacity, PROBE_TYPE pt, boolean debugInput) {
		max = nextPrime(initialCapacity);
		arr = new Object[max];
		probeType = pt;
		showColl = debugInput; //DEBUG code
	}
	
	/**
	 * Create a new Hashtable with a given initial capacity and using the default probe type
	 * @param initialCapacity
	 */
	public Hashtable(int initialCapacity) {
		max = nextPrime(initialCapacity);
		arr = new Object[max];
		probeType = PROBE_TYPE.LINEAR_PROBE; //as default probe
	}

	/**
	 * Store the value against the given key. If the loadFactor exceeds maxLoad, call the resize 
	 * method to resize the array. the If key already exists then its value should be overwritten.
	 * Create a new Pair item containing the key and value, then use the findEmpty method to find an unoccupied 
	 * position in the array to store the pair. Call findEmmpty with the hashed value of the key as the starting
	 * position for the search, stepNum of zero and the original key.
	 * containing   
	 * @param key
	 * @param value
	 */
	public void put(T key, V value) {
		if (getLoadFactor() >= maxLoad) {
			itemCount = 0;
			collTotal = 0; //DEBUG code
			resize();
		}
		currentColl = false; //DEBUG code
		itemCount++;
		arr[findEmpty(hash(key), 0, key)] = new Pair(key, value);
	}

	/**
	 * Get the value associated with key, or return null if key does not exists. Use the find method to search the
	 * array, starting at the hashed value of the key, stepNum of zero and the original key.
	 * @param key
	 * @return
	 */
	public V get(T key) {
		if (showColl) { //DEBUG code
			System.out.print(Thread.currentThread().getStackTrace()[2].getMethodName() + " Collisions: ");
			System.out.println(collTotal);
		}
		return find(hash(key), key, 0);
	}

	/**
	 * Return true if the Hashtable contains this key, false otherwise 
	 * @param key
	 * @return
	 */
	public boolean hasKey(T key) {
		return (find(hash(key), key, 0) != null ? true : false); 
	}

	/**
	 * Return all the keys in this Hashtable as a collection
	 * @return
	 */
	public Collection<String> getKeys() {
		ArrayList<String> keysList = new ArrayList<String>();
		
		for (int i = 0; i < max; i++) {
			if (arr[i] != null) {
				@SuppressWarnings("unchecked") //type cast for array, no entry will be of any other type
				Pair entry = (Pair) arr[i];
				
				keysList.add(0, entry.key.toString()); //add key at index 0, add 1 to other allocated indices
			}
		}
		return keysList;
	}

	/**
	 * Return the load factor, which is the ratio of itemCount to max
	 * @return
	 */
	public double getLoadFactor() {
		return ((double) itemCount / (double) max);
	}

	/**
	 * return the maximum capacity of the Hashtable
	 * @return
	 */
	public int getCapacity() {
		return max;
	}
	
	/**
	 * Find the value stored for this key, starting the search at position startPos in the array. If
	 * the item at position startPos is null, the Hashtable does not contain the value, so return null. 
	 * If the key stored in the pair at position startPos matches the key we're looking for, return the associated 
	 * value. If the key stored in the pair at position startPos does not match the key we're looking for, this
	 * is a hash collision so use the getNextLocation method with an incremented value of stepNum to find 
	 * the next location to search (the way that this is calculated will differ depending on the probe type 
	 * being used). Then use the value of the next location in a recursive call to find.
	 * @param startPos
	 * @param key
	 * @param stepNum
	 * @return
	 */
	private V find(int startPos, T key, int stepNum) {
		@SuppressWarnings("unchecked") //type cast for array, no entry will be of any other type
		Pair entry = (Pair)arr[startPos];
		
		if (entry == null) {
			return null;
		} else if (entry != null && key.equals(entry.key)) {
			return entry.value;
		} else {
			startPos = getNextLocation(startPos, ++stepNum, key);
			return find(startPos, key, stepNum);
		}
	}

	/**
	 * Find the first unoccupied location where a value associated with key can be stored, starting the
	 * search at position startPos. If startPos is unoccupied, return startPos. Otherwise use the getNextLocation
	 * method with an incremented value of stepNum to find the appropriate next position to check 
	 * (which will differ depending on the probe type being used) and use this in a recursive call to findEmpty.
	 * @param startPos
	 * @param stepNum
	 * @param key
	 * @return
	 */
	private int findEmpty(int startPos, int stepNum, T key) {
		@SuppressWarnings("unchecked") //type cast for array, no entry will be of any other type
		Pair entry = (Pair) arr[startPos];
		
		if(entry == null||key.equals(entry.key)) {
			return startPos;
		}
		if (!currentColl && showColl) { //DEBUG code
			collTotal++;
			currentColl = true;
		}
		startPos = getNextLocation(startPos, ++stepNum, key);
		return findEmpty(startPos, stepNum, key);
	}

	/**
	 * Finds the next position in the Hashtable array starting at position startPos. If the linear
	 * probe is being used, we just increment startPos. If the double hash probe type is being used, 
	 * add the double hashed value of the key to startPos. If the quadratic probe is being used, add
	 * the square of the step number to startPos.
	 * @param i
	 * @param stepNum
	 * @param key
	 * @return
	 */
	private int getNextLocation(int startPos, int stepNum, T key) {
		int step = startPos;
		
		switch (probeType) {
		case LINEAR_PROBE:
			step++;
			break;
		case DOUBLE_HASH:
			step += doubleHash(key);
			break;
		case QUADRATIC_PROBE:
			step += stepNum * stepNum;
			break;
		default:
			break;
		}
		return step % max;
	}

	/**
	 * A secondary hash function which returns a small value (less than or equal to DBL_HASH_K)
	 * to probe the next location if the double hash probe type is being used
	 * @param key
	 * @return
	 */
	private int doubleHash(T key) {
		String keyString = key.toString();
		BigInteger hashVal = BigInteger.valueOf(keyString.charAt(0) - 33);
		
		for (int i = 0; i < keyString.length(); i++) {
			BigInteger c = BigInteger.valueOf(keyString.charAt(i) - 33);
			hashVal = hashVal.multiply(BigInteger.valueOf(31)).add(c);
		}
		return DBL_HASH_K.subtract(hashVal.mod(DBL_HASH_K)).intValue();
	}

	/**
	 * Return an int value calculated by hashing the key. See the lecture slides for information
	 * on creating hash functions. The return value should be less than max, the maximum capacity 
	 * of the array
	 * @param key
	 * @return
	 */
	private int hash(T key) { 
		String keyString = key.toString();
		int hashVal = keyString.charAt(0) - 33; //-33 brings the ASCII value of the first special char '!' down to 0
		int charSum = 0;
		
		for (int i = 0; i < keyString.length(); i++) {
			charSum += (keyString.charAt(i)-33);
			hashVal = 31 * (hashVal + (keyString.charAt(i) - 33)); //hashVal += char value ; hashVal *= 31 ;
			hashVal += charSum;
		}
		hashVal %= max; //modulus the overflowed integer
		return Math.abs(hashVal); 
		
	} 

	/**
	 * Return true if n is prime
	 * @param n
	 * @return
	 */
	private boolean isPrime(int n) {
		if (n <= 2) return true;
		if ((n % 2) == 0) return false;
		for (int i = 3; i <= (int)Math.sqrt(n); i += 2) {
			if ((n % i) == 0) return false;
		}
		return true;
	}

	/**
	 * Get the smallest prime number which is larger than n
	 * @param n
	 * @return
	 */
	private int nextPrime(int n) {
		return (isPrime(n) ? n : nextPrime(n + 1));
	}

	/**
	 * Resize the hashtable, to be used when the load factor exceeds maxLoad. The new size of
	 * the underlying array should be the smallest prime number which is at least twice the size
	 * of the old array.
	 */
	private void resize() {
		int oldMax = max;
		Object[] oldTable = arr;
		
		max = nextPrime(max * 2);
		arr = new Object[max];
		for (int i = 0; i < oldMax; i++) {
			if (oldTable[i] != null) {
				@SuppressWarnings("unchecked") //type cast for array, no entry will be of any other type
				Pair entry = (Pair)oldTable[i]; //no access to .key without cast.
				
				put(entry.key, entry.value);
			}
		}
	}

	
	/**
	 * Instances of Pair are stored in the underlying array. We can't just store
	 * the value because we need to check the original key in the case of collisions.
	 * @author jb259
	 *
	 */
	private class Pair {
		private T key;
		private V value;

		public Pair(T key, V value) {
			this.key = key;
			this.value = value;
		}
	}
} 
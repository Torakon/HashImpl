package ci583.htable.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Random;

import ci583.htable.impl.Hashtable;
import ci583.htable.impl.Hashtable.PROBE_TYPE;

public class TestHT {
	String character = " abcdefghijklmnopqrstuvwxyz";

	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void testEmpty() {
		Hashtable<String, Boolean> h = new Hashtable<String, Boolean>(13);
		assertNull(h.get("foo"));
	}
	
	@Test
	public void testNotFound() {
		Hashtable<String, Boolean> h = new Hashtable<String, Boolean>(10);
		h.put("yes", true);
		assertNull(h.get("no"));
	}
	
	/*
	 * testInsert() NOTES: currently 794861 collisions or about 20%
	 * This becomes 11% at (200*200) input
	 */
	@Test
	public void testInsert() { 
		Hashtable<String, Boolean> h = new Hashtable<String, Boolean>(1000, PROBE_TYPE.DOUBLE_HASH, false);
		for(int i = 0; i < 2000; i++) {
			for(int j = 2000; j > 0; j--) {
				h.put(i + ":" + j, true);
			}
		}
		for(int i = 0; i < 2000; i++) {
			for(int j = 2000; j > 0; j--) {
				assertTrue(h.hasKey(i + ":" + j));
			} 
		}
		h.get("1:1"); 
	}
	
	/*
	 * TestRndData() NOTES: currently 1112098 collisions or about 28%
	 * This becomes 17% at (200*200) input
	 */
	@Test
	public void testRndData() { 
		Hashtable<String, Boolean> h = new Hashtable<String, Boolean>(100, PROBE_TYPE.DOUBLE_HASH, false);
		Random rand = new Random(59743865);
		for (int i = 0; i < (2000*2000); i++) {
			String charIn = "";
			int randLength = rand.nextInt(10) + 1;
			for ( ; randLength > 0; randLength--) {
				char randLetter = character.charAt(rand.nextInt(27));
				charIn += randLetter;
			}
			while (h.hasKey(charIn)) {
				charIn += character.charAt(rand.nextInt(27));
			}
			h.put(charIn, true);
		}
		assertNull(h.get("stringOver10Char"));
	}
	
	@Test
	public void testGet() {
		Hashtable<String, String> h = new Hashtable<String, String>(9);
		for(int i = 0; i < 10; i++) {
			for(int j = 10; j > 0; j--) {
				h.put(i + ":" + j, j + ":" + i);
			}
		}
		for(int i = 0; i < 10; i++) {
			for(int j = 10; j > 0; j--) {
				assertEquals(h.get(i + ":" + j), j + ":" + i);
			}
		}
	}
	
	@Test
	public void testNull() {
		Hashtable<String, Integer> h = new Hashtable<String, Integer>(20);
		for(int i = 0; i < 10; i++) h.put(Integer.valueOf(i).toString(), Integer.valueOf(i));
		assertNull(h.get(11+""));
	}

	@Test
	public void testCapacity() {
		Hashtable<String, Integer> h = new Hashtable<String, Integer>(20);
		assertEquals(h.getCapacity(), 23);//23 is smallest prime > 20
		for(int i = 0; i < 20; i++) {
			h.put(Integer.valueOf(i).toString(), Integer.valueOf(i));
		}
		assertFalse(h.getCapacity() == 23);//should have resized
		assertFalse(h.getLoadFactor() > 0.6);
	}
	
	@Test
	public void testKeys() {
		Hashtable<String, Integer> h = new Hashtable<String, Integer>(20);
		h.put("bananas", 1);
		h.put("pyjamas", 99);
		h.put("kedgeree", 1);
		for(String k: h.getKeys()) {
			assertTrue(k.equals("bananas") || k.equals("pyjamas") || k.equals("kedgeree"));
		}
	}
	
	@Test
	public void testTypes() {
		//test keys of type Boolean//
		Hashtable<Boolean, Integer> typeBoolean = new Hashtable<Boolean, Integer>(2);
		typeBoolean.put(true, 1);
		assertNull(typeBoolean.get(false));
		assertEquals(typeBoolean.get(true), Integer.valueOf(1));
		//test keys of type Integer//
		Hashtable<Integer, Boolean> typeInteger = new Hashtable<Integer, Boolean>(10);
		for(int i = 0; i < 100; i++) typeInteger.put(Integer.valueOf(i), true);
		for(int i = 0; i < 100; i++) assertTrue(typeInteger.get(i));
		//test keys of type BigInteger//
		Hashtable<BigInteger, Boolean> typeBigInt = new Hashtable<BigInteger, Boolean>(10);
		for(int i = 0; i < 100; i++) typeBigInt.put(BigInteger.valueOf(i), true);
		for(int i = 0; i < 100; i++) assertTrue(typeBigInt.get(BigInteger.valueOf(i)));
		//test keys of type Long//
		Hashtable<Long, Boolean> typeLong = new Hashtable<Long, Boolean>(100);
		for(int i = 0; i < 100; i++) typeLong.put(Long.valueOf(i), true);
		for(int i = 0; i < 100; i++) assertTrue(typeLong.get(Long.valueOf(i)));
		//test keys of type Float//
		Hashtable<Float, Boolean> typeFloat = new Hashtable<Float, Boolean>(100);
		for(int i = 0;i < 100; i++) typeFloat.put(Float.valueOf(i), true);
		for(int i = 0; i < 100; i++) assertTrue(typeFloat.get(Float.valueOf(i)));
	}
	
	/*
	 * testQuad() NOTES: currently around 4138 collisions or about 10%
	 * This increases to 20% at 4,000,000 input)
	 */
	@Test
	public void testQuad() {
		Hashtable<String, Boolean> h = new Hashtable<String, Boolean>(1000, PROBE_TYPE.QUADRATIC_PROBE, true); //true to show collisions
		for(int i = 0; i < 200; i++) {
			for(int j = 200; j > 0; j--) {
				h.put(i + ":" + j, true);
			}
		}
		for(int i = 0; i < 200; i++) {
			for(int j = 200; j > 0; j--) {
				assertTrue(h.hasKey(i + ":" + j));
			}
		}
		h.get("1:1");
	}
}
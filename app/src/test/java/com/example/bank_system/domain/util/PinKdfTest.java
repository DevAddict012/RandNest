package com.example.bank_system.domain.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Pure JUnit tests for PinKdf — no emulator needed.
 * Run with: .\gradlew.bat testDebugUnitTest
 */
public class PinKdfTest {

    // -------------------------------------------------------------------------
    // isValidPin
    // -------------------------------------------------------------------------

    @Test
    public void validPin_6digits_accepted() {
        assertTrue(PinKdf.isValidPin("123456".toCharArray()));
    }

    @Test
    public void validPin_12digits_accepted() {
        assertTrue(PinKdf.isValidPin("123456789012".toCharArray()));
    }

    @Test
    public void invalidPin_5digits_rejected() {
        assertFalse(PinKdf.isValidPin("12345".toCharArray()));
    }

    @Test
    public void invalidPin_13digits_rejected() {
        assertFalse(PinKdf.isValidPin("1234567890123".toCharArray()));
    }

    @Test
    public void invalidPin_letters_rejected() {
        assertFalse(PinKdf.isValidPin("12345a".toCharArray()));
    }

    @Test
    public void invalidPin_null_rejected() {
        assertFalse(PinKdf.isValidPin(null));
    }

    @Test
    public void invalidPin_empty_rejected() {
        assertFalse(PinKdf.isValidPin(new char[0]));
    }

    // -------------------------------------------------------------------------
    // Salt generation
    // -------------------------------------------------------------------------

    @Test
    public void generateSalt_produces16bytes() {
        byte[] salt = PinKdf.generateSalt();
        assertEquals(16, salt.length);
    }

    @Test
    public void generateSalt_eachCallDiffers() {
        byte[] a = PinKdf.generateSalt();
        byte[] b = PinKdf.generateSalt();
        assertFalse("Two salts must not be identical", Arrays.equals(a, b));
    }

    // -------------------------------------------------------------------------
    // Derive and verify
    // -------------------------------------------------------------------------

    @Test
    public void deriveVerifier_produces32bytes() {
        byte[] salt     = PinKdf.generateSalt();
        byte[] verifier = PinKdf.deriveVerifier("123456".toCharArray(), salt);
        assertEquals(32, verifier.length);
    }

    @Test
    public void verify_correctPin_returnsTrue() {
        byte[] salt     = PinKdf.generateSalt();
        char[] pin      = "987654".toCharArray();
        byte[] verifier = PinKdf.deriveVerifier(pin.clone(), salt);
        assertTrue(PinKdf.verify(pin, salt, verifier));
    }

    @Test
    public void verify_wrongPin_returnsFalse() {
        byte[] salt     = PinKdf.generateSalt();
        byte[] verifier = PinKdf.deriveVerifier("111111".toCharArray(), salt);
        assertFalse(PinKdf.verify("222222".toCharArray(), salt, verifier));
    }

    @Test
    public void verify_wrongSalt_returnsFalse() {
        byte[] salt1    = PinKdf.generateSalt();
        byte[] salt2    = PinKdf.generateSalt();
        byte[] verifier = PinKdf.deriveVerifier("123456".toCharArray(), salt1);
        assertFalse(PinKdf.verify("123456".toCharArray(), salt2, verifier));
    }

    @Test
    public void samePinAndSalt_alwaysProducesSameVerifier() {
        byte[] salt = PinKdf.generateSalt();
        char[] pin  = "654321".toCharArray();
        byte[] v1   = PinKdf.deriveVerifier(pin.clone(), salt);
        byte[] v2   = PinKdf.deriveVerifier(pin.clone(), salt);
        assertArrayEquals(v1, v2);
    }

    // -------------------------------------------------------------------------
    // constantTimeEquals
    // -------------------------------------------------------------------------

    @Test
    public void constantTimeEquals_identicalArrays_true() {
        assertTrue(PinKdf.constantTimeEquals(new byte[]{1,2,3}, new byte[]{1,2,3}));
    }

    @Test
    public void constantTimeEquals_differentArrays_false() {
        assertFalse(PinKdf.constantTimeEquals(new byte[]{1,2,3}, new byte[]{1,2,4}));
    }

    @Test
    public void constantTimeEquals_differentLengths_false() {
        assertFalse(PinKdf.constantTimeEquals(new byte[]{1,2}, new byte[]{1,2,3}));
    }
}

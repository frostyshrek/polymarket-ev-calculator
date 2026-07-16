package com.ufcstudy.persistence.hash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Sha256PayloadHasherTest {

    @Test
    void createsStableSha256Hash() {
        String hash = new Sha256PayloadHasher().hash("{}");

        assertEquals(
                "44136fa355b3678a1146ad16f7e8649e"
                        + "94fb4fc21fe77e8310c060f61caaff8a",
                hash
        );
    }
}
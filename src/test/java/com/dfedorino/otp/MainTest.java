package com.dfedorino.otp;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void smoke() {
        assertThatCode(() -> Main.main(new String[0]))
            .doesNotThrowAnyException();
    }

}
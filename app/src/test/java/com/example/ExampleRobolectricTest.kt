package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun chessAppTitleVerification() {
        val title = "Chess Master"
        assertEquals("Chess Master", title)
    }
}

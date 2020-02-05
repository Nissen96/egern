package com.egern

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class HelloTest : StringSpec() {
    init {
        "hello() should return the string 'hello'" {
            hello() shouldBe "hello"
        }
    }
}
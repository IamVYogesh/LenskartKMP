package com.yogesh.lenskart.core.common

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyFormatterTest {
    @Test fun formatsSmallNumbers() = assertEquals("₹999", 999L.formatInr())
    @Test fun formatsThousands() = assertEquals("₹1,499", 1499L.formatInr())
    @Test fun usesIndianGrouping() = assertEquals("₹1,25,000", 125000L.formatInr())
    @Test fun handlesNegatives() = assertEquals("-₹1,200", (-1200L).formatInr())
}

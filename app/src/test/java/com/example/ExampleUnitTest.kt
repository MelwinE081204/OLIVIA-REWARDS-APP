package com.example

import com.example.data.model.MembershipTier
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testMembershipTierCalculation() {
        assertEquals(MembershipTier.BRONZE, MembershipTier.fromPoints(450))
        assertEquals(MembershipTier.SILVER, MembershipTier.fromPoints(1200))
        assertEquals(MembershipTier.GOLD, MembershipTier.fromPoints(2850))
        assertEquals(MembershipTier.PLATINUM, MembershipTier.fromPoints(6200))
        assertEquals(MembershipTier.DIAMOND, MembershipTier.fromPoints(15000))
    }

    @Test
    fun testTierMultipliers() {
        assertEquals(1.0, MembershipTier.BRONZE.multiplier, 0.01)
        assertEquals(1.25, MembershipTier.SILVER.multiplier, 0.01)
        assertEquals(1.5, MembershipTier.GOLD.multiplier, 0.01)
        assertEquals(1.75, MembershipTier.PLATINUM.multiplier, 0.01)
        assertEquals(2.0, MembershipTier.DIAMOND.multiplier, 0.01)
    }
}

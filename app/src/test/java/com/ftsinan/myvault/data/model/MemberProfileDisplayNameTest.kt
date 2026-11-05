package com.ftsinan.myvault.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MemberProfileDisplayNameTest {
    @Test
    fun usesOptionalDisplayNameForListLabel() {
        val member = MemberProfile(name = "Legal Name", displayName = "  Uncle  ")

        assertEquals("Uncle", member.listName)
        assertEquals("Legal Name", member.name)
    }

    @Test
    fun fallsBackToRealNameWhenDisplayNameIsBlankOrMissing() {
        assertEquals("Legal Name", MemberProfile(name = "Legal Name").listName)
        assertEquals("Legal Name", MemberProfile(name = "Legal Name", displayName = "  ").listName)
    }
}

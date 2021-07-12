/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.task

import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import org.junit.Assert
import org.junit.Test

class LocalContactTest {

    @Test
    fun `given a contact with valid phone but no email, then contact has valid email or phone`() {
        // given
        val contact = LocalContact(
            id = "test",
            numbers = setOf("0612345678")
        )

        // then
        Assert.assertTrue(contact.hasValidEmailOrPhone())
    }

    @Test
    fun `given a contact with invalid phone but no email, then contact has valid email or phone`() {
        // given
        val contact = LocalContact(
            id = "test",
            numbers = setOf("068")
        )

        // then
        Assert.assertFalse(contact.hasValidEmailOrPhone())
    }

    @Test
    fun `given a contact with valid email but no phone, then contact has valid email or phone`() {
        // given
        val contact = LocalContact(
            id = "test",
            emails = setOf("test@minvs.nl")
        )

        // then
        Assert.assertTrue(contact.hasValidEmailOrPhone())
    }

    @Test
    fun `given a contact with invalid email but no phone, then contact has valid email or phone`() {
        // given
        val contact = LocalContact(
            id = "test",
            emails = setOf("test")
        )

        // then
        Assert.assertFalse(contact.hasValidEmailOrPhone())
    }

    @Test
    fun `given a contact with no phone numbers, then contact has valid phone numbers`() {
        // given
        val contact = LocalContact(
            id = "test",
        )

        // then
        Assert.assertFalse(contact.hasValidPhoneNumber())
    }

    @Test
    fun `given a contact with at least one valid phone numbers, then contact has valid phone numbers`() {
        // given
        val contact1 = LocalContact(
            id = "test",
            numbers = setOf("0612345678")
        )

        val contact2 = LocalContact(
            id = "test",
            numbers = setOf("0612345678", "0687654321")
        )

        val contact3 = LocalContact(
            id = "test",
            numbers = setOf("0612345678", "06")
        )

        // then
        Assert.assertTrue(contact1.hasValidPhoneNumber())
        Assert.assertTrue(contact2.hasValidPhoneNumber())
        Assert.assertTrue(contact3.hasValidPhoneNumber())
    }

    @Test
    fun `given a contact with no email, then contact has no valid email`() {
        // given
        val contact = LocalContact(
            id = "test",
        )

        // then
        Assert.assertFalse(contact.hasValidEmailAddress())
    }

    @Test
    fun `given a contact with at least one valid email, then contact has valid email`() {
        // given
        val contact1 = LocalContact(
            id = "test",
            emails = setOf("test@minvs.nl")
        )

        val contact2 = LocalContact(
            id = "test",
            emails = setOf("test@minvs.nl", "test2@minvs.nl")
        )

        val contact3 = LocalContact(
            id = "test",
            emails = setOf("test@minvs.nl", "test")
        )

        // then
        Assert.assertTrue(contact1.hasValidEmailAddress())
        Assert.assertTrue(contact2.hasValidEmailAddress())
        Assert.assertTrue(contact3.hasValidEmailAddress())
    }
}
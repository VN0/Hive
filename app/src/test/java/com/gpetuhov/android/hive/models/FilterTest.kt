package com.gpetuhov.android.hive.models

import com.gpetuhov.android.hive.domain.model.Filter
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterTest {

    companion object {
        private const val SHOW_USERS_OFFERS_KEY = "showUsersOffers"
        private const val FREE_OFFERS_ONLY_KEY = "freeOffersOnly"
        private const val OFFERS_WITH_REVIEWS_ONLY_KEY = "offersWithReviewsOnly"
        private const val HAS_PHONE_KEY = "hasPhone"
        private const val HAS_EMAIL_KEY = "hasEmail"
        private const val HAS_SKYPE_KEY = "hasSkype"
        private const val HAS_FACEBOOK_KEY = "hasFacebook"
        private const val HAS_TWITTER_KEY = "hasTwitter"
        private const val HAS_INSTAGRAM_KEY = "hasInstagram"
        private const val HAS_YOUTUBE_KEY = "hasYoutube"
        private const val HAS_WEBSITE_KEY = "hasWebsite"
    }

    @Test
    fun showUsersOffersAll() {
        val filter = Filter()
        assertEquals(true, filter.isShowUsersOffersAll)
        filter.setShowUsersOnly()
        assertEquals(false, filter.isShowUsersOffersAll)
        filter.setShowUsersOffersAll()
        assertEquals(true, filter.isShowUsersOffersAll)
    }

    @Test
    fun showUsersOnly() {
        val filter = Filter()
        assertEquals(false, filter.isShowUsersOnly)
        filter.setShowUsersOnly()
        assertEquals(true, filter.isShowUsersOnly)
    }

    @Test
    fun showOffersOnly() {
        val filter = Filter()
        assertEquals(false, filter.isShowOffersOnly)
        filter.setShowOffersOnly()
        assertEquals(true, filter.isShowOffersOnly)
    }

    @Test
    fun toJson() {
        checkToJson(SHOW_USERS_OFFERS_KEY, Filter.SHOW_USERS_OFFERS_ALL) { it.setShowUsersOffersAll() }
        checkToJson(SHOW_USERS_OFFERS_KEY, Filter.SHOW_USERS_ONLY) { it.setShowUsersOnly() }
        checkToJson(SHOW_USERS_OFFERS_KEY, Filter.SHOW_OFFERS_ONLY) { it.setShowOffersOnly() }
        checkToJson(FREE_OFFERS_ONLY_KEY, true) { it.isFreeOffersOnly = true }
        checkToJson(OFFERS_WITH_REVIEWS_ONLY_KEY, true) { it.isOffersWithReviewsOnly = true }
        checkToJson(HAS_PHONE_KEY, true) { it.hasPhone = true }
        checkToJson(HAS_EMAIL_KEY, true) { it.hasEmail = true }
        checkToJson(HAS_SKYPE_KEY, true) { it.hasSkype = true }
        checkToJson(HAS_FACEBOOK_KEY, true) { it.hasFacebook = true }
        checkToJson(HAS_TWITTER_KEY, true) { it.hasTwitter = true }
        checkToJson(HAS_INSTAGRAM_KEY, true) { it.hasInstagram = true }
        checkToJson(HAS_YOUTUBE_KEY, true) { it.hasYoutube = true }
        checkToJson(HAS_WEBSITE_KEY, true) { it.hasWebsite = true }
    }

    @Test
    fun fromJson() {
        checkFromJson({ it.setShowUsersOffersAll() }, { it.isShowUsersOffersAll })
        checkFromJson({ it.setShowUsersOnly() }, { it.isShowUsersOnly })
        checkFromJson({ it.setShowOffersOnly() }, { it.isShowOffersOnly })
        checkFromJson({ it.isFreeOffersOnly = true }, { it.isFreeOffersOnly })
        checkFromJson({ it.isOffersWithReviewsOnly = true }, { it.isOffersWithReviewsOnly })
        checkFromJson({ it.hasPhone = true }, { it.hasPhone })
        checkFromJson({ it.hasEmail = true }, { it.hasEmail })
        checkFromJson({ it.hasSkype = true }, { it.hasSkype })
        checkFromJson({ it.hasFacebook = true }, { it.hasFacebook })
        checkFromJson({ it.hasTwitter = true }, { it.hasTwitter })
        checkFromJson({ it.hasInstagram = true }, { it.hasInstagram })
        checkFromJson({ it.hasYoutube = true }, { it.hasYoutube })
        checkFromJson({ it.hasWebsite = true }, { it.hasWebsite })
    }

    @Test
    fun defaultFilter() {
        checkIsDefault()
        checkIsNotDefault { it.setShowUsersOnly() }
        checkIsNotDefault { it.isFreeOffersOnly = true }
        checkIsNotDefault { it.isOffersWithReviewsOnly = true }
        checkIsNotDefault { it.hasPhone = true }
        checkIsNotDefault { it.hasEmail = true }
        checkIsNotDefault { it.hasSkype = true }
        checkIsNotDefault { it.hasFacebook = true }
        checkIsNotDefault { it.hasTwitter = true }
        checkIsNotDefault { it.hasInstagram = true }
        checkIsNotDefault { it.hasYoutube = true }
        checkIsNotDefault { it.hasWebsite = true }
    }

    // === Private methods ===

    private fun checkToJson(expectedJsonKey: String, expectedJsonValue: Any, action: (Filter) -> Unit) {
        val filter = Filter()
        action(filter)
        val json = Filter.toJson(filter)
        val expectedJsonString = "\"$expectedJsonKey\":$expectedJsonValue"
        assertEquals(true, json.contains(expectedJsonString))
    }

    private fun checkFromJson(action: (Filter) -> Unit, assertion: (Filter) -> Unit) {
        val sourceFilter = Filter()
        action(sourceFilter)
        val json = Filter.toJson(sourceFilter)
        val resultFilter = Filter.fromJson(json)
        assertEquals(true, resultFilter != null)
        if (resultFilter != null) assertion(resultFilter)
    }

    private fun checkIsDefault() {
        val filter = Filter()
        assertEquals(true, filter.isDefault)
    }

    private fun checkIsNotDefault(action: (Filter) -> Unit) {
        val filter = Filter()
        action(filter)
        assertEquals(false, filter.isDefault)
    }
}
package com.danneu.reddit

import org.junit.Assert.*
import org.junit.Test
import java.net.URI
import java.time.Duration


// These tests hit the reddit API


val alphabet = "abcdefghijklmnopqrstuvwxyz".split("").filter { it.isNotBlank() }


class ApiTests {
    @Test
    fun testNestedThread() {
        // https://www.reddit.com/r/testfixtures/comments/5g3272/loadmore_comment_thread/
        // Note: `limit` doesn't do anything for deep thread pagination in reddit's api, just
        // affects the top-level pagination
        val comments = ApiClient().commentsOf("testfixtures", submissionId = "5g3272").asSequence().toList()
        assertEquals("can crawl 'Continue Thread ->' nodes in a deeply nested thread", alphabet, comments.map { it.text() }.sorted())
    }

    @Test
    fun testPaginatedTopLevel() {
        // https://www.reddit.com/r/testfixtures/comments/5gdn73/alphabet_toplevel/
        val comments = ApiClient().commentsOf("testfixtures", submissionId = "5gdn73", limit = 20).asSequence().toList()
        assertEquals("can paginated top-level comments", alphabet, comments.map { it.text() }.sorted())
    }

    @Test
    fun testPaginatedReplies() {
        // https://www.reddit.com/r/testfixtures/comments/5gdnin/paginated_replies/
        val comments = ApiClient().commentsOf("testfixtures", submissionId = "5gdnin", limit = 20).asSequence().toList()
        assertEquals("can paginate a comment's replies", alphabet, comments.map { it.text() }.sorted())
    }

    @Test
    fun testEndOfSubmissions() {
        // dumb way to get all submissions in one go. should prob allow something like `start = Instant(0)` but
        // gotta think of a more meaningful api.
        val interval = Duration.ofDays(4000)

        // should end after one request
        val submissions = ApiClient().submissionsOf("testfixtures", interval = interval).asSequence().toList()
        assertEquals("fetches all submissions on page 1 newest to oldest",
            listOf("5h8934", "5gdnin", "5gdn73", "5g3272"),
            submissions.map { it.id }
        )
    }


    // URL PARSING


    @Test
    fun testCommentLinks() {
        // https://www.reddit.com/r/testfixtures/comments/5h8934/urls/
        val urls = ApiClient().commentsOf("testfixtures", "5h8934").asSequence().flatMap { it.urls().asSequence() }.toList()
        assertEquals("parses comment links", listOf(URI("https://example.com/")), urls)
    }


    @Test
    fun testSubmissionLinks() {
        val urls = ApiClient().submissionsOf("testfixtures", interval = Duration.ofDays(400)).asSequence().flatMap { it.urls().asSequence() }.toList()
        assertEquals("parses submission links", listOf(URI("https://amzn.com/1491931655")), urls)
    }
}
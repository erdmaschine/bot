package erdmaschine.bot.model

data class RedditListingThing(
    val data: RedditListing
)

data class RedditListing(
    val children: List<RedditLinkThing>
)

data class RedditLinkThing(
    val data: RedditLink
)

data class RedditLink(
    val title: String,
    val author: String,
    val ups: Long,
    val downs: Long,
    val url: String,
    val permalink: String,
)

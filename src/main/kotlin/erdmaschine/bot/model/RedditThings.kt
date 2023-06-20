package erdmaschine.bot.model

data class RedditListingThing(
    val data: RedditListing
)

data class RedditListing(
    val children: Collection<RedditLinkThing>
)

data class RedditLinkThing(
    val data: RedditLink
)

data class RedditLink(
    val id: String,
    val created: Float,
    val title: String,
    val author: String,
    val url: String,
    val permalink: String,
    val preview: RedditPreview?,
    val is_self: Boolean,
    val selftext: String,
    val over_18: Boolean,
)

data class RedditPreview(
    val images: Collection<RedditPreviewImages>
)

data class RedditPreviewImages(
    val resolutions: Collection<RedditPreviewImageResolutions>,
    val variants: RedditPreviewImageVariants?
)

data class RedditPreviewImageVariants(
    val obfuscated: RedditPreviewImages
)

data class RedditPreviewImageResolutions(
    val url: String,
    val width: Int,
    val height: Int,
)

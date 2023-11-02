package ca.ulaval.glo4002.codereview.app.model

data class LineCommentContextIdentifier(
    val filePath: String,
    val lineStart: Int,
    val lineEnd: Int
) {
    companion object {
        fun fromString(identifier: String): LineCommentContextIdentifier {
            val split = identifier.split(",")
            return LineCommentContextIdentifier(split[0], split[1].toInt(), split[2].toInt())
        }
    }

    override fun toString(): String {
        return "$filePath,$lineStart,$lineEnd"
    }
}

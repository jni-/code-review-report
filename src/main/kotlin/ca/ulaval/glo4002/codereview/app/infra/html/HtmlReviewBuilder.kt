package ca.ulaval.glo4002.codereview.app.infra.html

import ca.ulaval.glo4002.codereview.app.model.GeneralComment
import ca.ulaval.glo4002.codereview.app.model.LineComment
import ca.ulaval.glo4002.codereview.app.model.LineCommentContext
import ca.ulaval.glo4002.codereview.app.model.RepeatedComment
import java.time.LocalDateTime
import java.util.*

class HtmlReviewBuilder(
    private var isForTools: Boolean = false
) {
    companion object {
        private const val ASSETS_CSS = "assets/css/"
        private const val ASSETS_JS = "assets/js/"
        private const val ASSETS_IMAGES = "assets/images/"
        private const val SNIPPET_IMAGE = "snippet.png"
    }

    private val bodyOutput: StringBuilder = StringBuilder()
    private val headOutput: StringBuilder = StringBuilder()

    fun setToolMode(): HtmlReviewBuilder {
        isForTools = true
        return this
    }

    fun build(): String {
        val output = StringBuilder()
        output.append("<html>\n")
        output.append("\t<head>\n")
        output.append(headOutput)
        output.append("\t</head>\n")
        output.append("\t<body>\n")
        output.append("\t\t<div id='wrapper'>\n")
        output.append(bodyOutput)
        output.append("\t\t</div>\n\t</body>\n</html>")

        return output.toString()
    }

    fun addHtmlHeaders(projectName: String): HtmlReviewBuilder {
        if (isForTools) {
            addStyleForTools()
        } else {
            addTitle(projectName)
            copyCssFiles()
            copyJsFiles()
            headOutput.append("\t\t<meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />\n")
        }

        return this
    }

    private fun addTitle(projectName: String) {
        headOutput.append("\t\t<title>$projectName</title>\n")
    }

    private fun copyCssFiles() {
        headOutput.append("\t\t<link rel='stylesheet' href='" + ASSETS_CSS + "style.css'>\n")
        headOutput.append("\t\t<link rel='stylesheet' href='" + ASSETS_CSS + "shCoreEclipse.css'>\n")
        headOutput.append("\t\t<link rel='stylesheet' href='" + ASSETS_CSS + "shThemeEclipse.css'>\n")
    }

    private fun copyJsFiles() {
        headOutput.append("\t\t<script type='text/javascript' src='" + ASSETS_JS + "jquery.min.js'></script>\n")
        headOutput.append("\t\t<script type='text/javascript' src='" + ASSETS_JS + "shCore.js'></script>\n")
        headOutput.append("\t\t<script type='text/javascript' src='" + ASSETS_JS + "shBrushJava.js'></script>\n")
        headOutput.append("\t\t<script type='text/javascript' src='" + ASSETS_JS + "shBrushXml.js'></script>\n")
        headOutput.append("\t\t<script type='text/javascript' src='" + ASSETS_JS + "shBrushPlain.js'></script>\n")
        headOutput.append("\t\t<script type='text/javascript' src='" + ASSETS_JS + "content.js'></script>\n")
    }

    fun addH1Title(projectName: String): HtmlReviewBuilder {
        bodyOutput.append("\t\t\t<h1>Revue : $projectName</h1>\n")
        return this
    }

    fun addGeneralComments(generalComments: MutableList<GeneralComment>): HtmlReviewBuilder {
        if (generalComments.isEmpty()) {
            return this
        }

        bodyOutput.append("\t\t\t<div id='general-comments'>\n")
        addH2Header("Commentaires g&eacute;n&eacute;raux")
        addList(generalComments)
        bodyOutput.append("\t\t\t</div>\n\n")

        return this
    }

    private fun addH2Header(title: String) {
        bodyOutput.append("\t\t\t\t<h2>$title</h2>\n")
    }

    private fun addList(comments: List<GeneralComment>) {
        if (comments.isEmpty()) {
            return
        }
        bodyOutput.append("\t\t\t\t<ul>\n")
        comments.forEach {
            val text = if (isForTools) {
                val editLink = "[<a href=\"edit:${it.id}\">Edit</a>]"
                val deleteLink = "[<a href=\"remove:${it.id}\">Delete</a>]"
                "$editLink $deleteLink ${it.comment}"
            } else {
                it.comment
            }
            addListComment(text, null, it.isImportant)
        }
        bodyOutput.append("\t\t\t\t</ul>\n")
    }

    private fun addListComment(comment: String, snippet: String?, isImportant: Boolean = false) {
        bodyOutput.append("\t\t\t\t\t<li class='taggable' data-tag='${if (isImportant) "Important" else ""}'>")

        if (isImportant && !isForTools) {
            bodyOutput.append("<img src='assets/images/Important.png'/>&nbsp;")
        }

        bodyOutput.append(comment)

        if (isImportant && isForTools) {
            bodyOutput.append("&nbsp;(!!)")
        }

        addSnippet(snippet)
        bodyOutput.append("</li>\n")
    }

    private fun addSnippet(snippet: String?) {
        if (!snippet.isNullOrEmpty()) {
            bodyOutput.append(
                "<img src='" + ASSETS_IMAGES + SNIPPET_IMAGE + "' class='show_snippet' /><div class='snippet'>" + snippet
                        + "</div>"
            )
        }
    }

    fun addFileComments(lineComments: List<LineComment>, selectedComment: UUID? = null): HtmlReviewBuilder {
        if (lineComments.isEmpty()) {
            return this
        }
        bodyOutput.append("\t\t\t<div id='file-reviews'>\n")
        lineComments
            .groupBy { it.context.file.name }
            .forEach {
                bodyOutput.append("\t\t\t\t<div class='file-comments'>\n")
                addFoldableHeader(it.key)
                addSnippetedList(it.value, selectedComment)
                bodyOutput.append("\t\t\t\t</div>\n")
            }

        bodyOutput.append("\t\t\t</div>\n\n")

        return this
    }

    private fun addSnippetedList(comments: List<LineComment>, selectedComment: UUID? = null) {
        bodyOutput.append("\t\t\t\t<ul>\n")

        comments
            .sortedBy { it.context.lineStart }
            .forEach {
            bodyOutput.append("\t")
            val comment = if (!isForTools) {
                it.comment
            } else {
                val cssClass = if (it.id == selectedComment) "selected" else ""
                val repeatedLink = "[<a href=\"repeated:${it.id}\">Repeated</a>]"
                val editLink = "[<a href=\"edit:${it.id}\">Edit</a>]"
                val deleteLink = "[<a href=\"remove:${it.id}\">Delete</a>]"
                "$repeatedLink $editLink $deleteLink <a href=\"goto:${it.context.file.path}#${it.context.lineStart}\" class=\"$cssClass\">${it.comment}</a>"
            }
            addListComment(comment, if (!isForTools) createSnippet(it.context) else null, it.isImportant)
        }
    }

    private fun addSnippetedListByFile(id: UUID, comments: List<LineCommentContext>, isImportant: Boolean) {
        bodyOutput.append("\t\t\t\t<ul>\n")

        comments
            .sortedWith(compareBy({ it.file.name }, { it.lineStart }))
            .forEach {
                bodyOutput.append("\t")
                val text = "${it.file.name}:${it.lineStart}${if (it.lineStart != it.lineEnd) "-${it.lineEnd}" else ""}"
                val comment = if (!isForTools) text else {
                    val unrepeatThis = "[<a href=\"unrepeatedOnce:${id}#${it.toIdentifier()}\">Unrepeated</a>]"
                    val remove = "[<a href=\"removeExample:${id}#${it.toIdentifier()}\">Delete</a>]"
                    "$unrepeatThis $remove <a href=\"goto:${it.file.path}#${it.lineStart}\">$text</a>"
                }
                addListComment(comment, if (!isForTools) createSnippet(it) else null, isImportant)
            }
        bodyOutput.append("\t\t\t\t</ul>\n")
    }

    private fun createSnippet(context: LineCommentContext): String {
        if (context.snippet.isBlank()) {
            return "No snippet available"
        }

        val fileLines = context.snippet
            .split("\n")
            .map { it.replace("<", "&lt;").replace(">", "&gt;") }
            .joinToString("\n") { it.ifBlank { "&nbsp;" } }
        val brush = getBrush(context.file.extension)
        val highlight = getHighlightArray(context)

        return ("<pre class='brush: $brush; first-line: ${context.snippetFirstLine}; highlight: $highlight'>$fileLines</pre>")
    }

    private fun getBrush(ext: String?): String = when (ext) {
        "java" -> "java"
        "xml" -> "xml"
        else -> "plain"
    }

    private fun getHighlightArray(context: LineCommentContext): String {
        return "[${(context.lineStart..context.lineEnd).joinToString(", ")}]"
    }

    fun addRepeatedComments(repeatedComments: List<RepeatedComment>, selectedComment: UUID? = null): HtmlReviewBuilder {
        if (repeatedComments.isEmpty()) {
            return this
        }
        bodyOutput.append("\t\t\t<div id='repeated-comments'>\n")
        addH2Header("Commentaires récurrents")
        if (!isForTools) {
            bodyOutput.append("\t\t\t\t<p id=\"repeated-examples-description\">Ces commentaires se retrouvent à plusieurs endroits. Quelques exemples sont listés ici, mais la liste n'est pas exhaustive.</p>\n")
        }

        bodyOutput.append("\t\t\t\t<ul>\n")
        repeatedComments
            .forEach {
                val comment = if (!isForTools) it.comment else {
                    val repeatedLink = "[<a href=\"unrepeated:${it.id}\">Unrepeated</a>]"
                    val editLink = "[<a href=\"edit:${it.id}\">Edit</a>]"
                    val deleteLink = "[<a href=\"remove:${it.id}\">Delete</a>]"
                    val cssClass = if (it.id == selectedComment) "selected" else ""
                    "$repeatedLink $editLink $deleteLink <span class=\"$cssClass\">${it.comment}</span>"
                }
                bodyOutput.append("\t\t\t\t<li class='repeated-comment'>${comment}\n")
                addSnippetedListByFile(it.id, it.examples, it.isImportant)
                bodyOutput.append("\t\t\t\t</li>")
            }

        bodyOutput.append("\t\t\t</div>\n\n")

        return this
    }

    private fun addFoldableHeader(header: String) {
        bodyOutput.append("\t\t\t\t\t<h3 class=foldable>$header</h3>\n")
    }

    fun addFooter(): HtmlReviewBuilder {
        if (!isForTools) {
            bodyOutput.append("\t\t\t<div id='footer'>\n")
            bodyOutput.append("\t\t\t\t<p>Tous les num&eacute;ros entre [crochets] sont des r&eacute;f&eacute;rences aux 'bad smells' du chapitre 17 de Clean Code</p>\n")
            bodyOutput.append("\t\t\t\t<p>Generated with &copy;CodeReviewReport on ${LocalDateTime.now()}</p>\n")
        }

        return this
    }

    private fun addStyleForTools() = """
        <style>
            body {
                background-color: #45494A;
                color: #BBBBBA;
            }
            
            .file-comments h3 {
                color: #9A472C;
                font-weight: bold;
                font-size: 1.2em;
            }
            
            ul {
                margin-left: 25px;
            }
            
            li a {
                color: #BBBBBA;
                text-decoration: none;
            }
            
            li a:hover {
                text-decoration: underline;
            }
            
            .selected {
                background-color: rgba(255, 200, 0, 0.2);
            }
        </style>
    """.trimIndent()
        .also(headOutput::append)

}

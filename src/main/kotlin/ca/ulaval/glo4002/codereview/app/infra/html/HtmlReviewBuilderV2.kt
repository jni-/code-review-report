package ca.ulaval.glo4002.codereview.app.infra.html

import ca.ulaval.glo4002.codereview.app.model.GeneralComment
import ca.ulaval.glo4002.codereview.app.model.LineComment
import ca.ulaval.glo4002.codereview.app.model.LineCommentContext
import ca.ulaval.glo4002.codereview.app.model.RepeatedComment
import ca.ulaval.glo4002.codereview.app.model.Review
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Modern (v2) standalone report: a single self-contained HTML file with inline
 * CSS/JS — no external assets. Same content and features as the v1 report.
 */
class HtmlReviewBuilderV2(private val projectName: String) {

    fun build(review: Review): String {
        val body = StringBuilder()

        body.append(topBar(review))
        body.append("<main class='layout'>\n<div class='content'>\n")
        body.append(generalSection(review.generalComments))
        body.append(repeatedSection(review.repeatedComments))
        body.append(filesSection(review.lineComments))
        body.append(footer())
        body.append("</div>\n")
        body.append(snippetPanel())
        body.append("</main>\n")
        body.append("<button id='to-top' title='Revenir en haut' aria-label='Revenir en haut'>&uarr;</button>\n")

        return """
            |<!DOCTYPE html>
            |<html lang='fr'>
            |<head>
            |<meta charset='UTF-8'>
            |<meta name='viewport' content='width=device-width, initial-scale=1'>
            |<title>Revue : ${esc(projectName)}</title>
            |<style>
            |${css()}
            |</style>
            |</head>
            |<body>
            |$body
            |<script>
            |${js()}
            |</script>
            |</body>
            |</html>
        """.trimMargin()
    }

    private fun esc(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun topBar(review: Review): String {
        val fileCount = (review.lineComments.map { it.context.file.name } +
                review.repeatedComments.flatMap { repeated -> repeated.examples.map { it.file.name } })
            .toSet()
            .size
        val commentCount = review.generalComments.size + review.repeatedComments.size + review.lineComments.size
        return """
            |<header id='topbar'>
            |  <div class='topbar-inner'>
            |    <div class='title-block'>
            |      <h1>Revue <span class='project-name'>${esc(projectName)}</span></h1>
            |      <span class='stats'>$commentCount commentaire${if (commentCount > 1) "s" else ""} &middot; $fileCount fichier${if (fileCount > 1) "s" else ""}</span>
            |    </div>
            |    <div class='controls'>
            |      <div id='filters' role='group' aria-label='Filtres'></div>
            |      <div class='fold-controls'>
            |        <button id='unfold-all' class='ghost-btn'>Tout d&eacute;plier</button>
            |        <button id='fold-all' class='ghost-btn'>Tout replier</button>
            |      </div>
            |    </div>
            |  </div>
            |</header>
            |""".trimMargin()
    }

    private fun generalSection(comments: List<GeneralComment>): String {
        if (comments.isEmpty()) {
            return ""
        }

        val items = comments.joinToString("\n") { comment ->
            commentRow(esc(comment.comment), comment.isImportant, snippetData = null)
        }
        return """
            |<section class='card' id='general-comments'>
            |  <h2>Commentaires g&eacute;n&eacute;raux</h2>
            |  <ul class='comment-list'>
            |$items
            |  </ul>
            |</section>
            |""".trimMargin()
    }

    private fun repeatedSection(comments: List<RepeatedComment>): String {
        if (comments.isEmpty()) {
            return ""
        }

        val items = comments.joinToString("\n") { repeated ->
            val examples = repeated.examples
                .sortedWith(compareBy({ it.file.name }, { it.lineStart }))
                .joinToString("\n") { context ->
                    // No badge and no tag: examples follow their parent comment's importance
                    // and must not be filtered independently of it.
                    commentRow(
                        "<span class='file-ref'>${esc(context.file.name)}:${context.lineRange}</span>",
                        isImportant = false,
                        snippetData = context,
                        tag = null
                    )
                }
            """
            |    <li class='repeated-comment${if (repeated.isImportant) " important" else ""}' data-tag='${if (repeated.isImportant) "Important" else ""}'>
            |      <div class='comment-line'>${importantBadge(repeated.isImportant)}<span class='comment-text'>${esc(repeated.comment)}</span></div>
            |      <ul class='comment-list examples'>
            |$examples
            |      </ul>
            |    </li>
            """.trimMargin()
        }
        return """
            |<section class='card' id='repeated-comments'>
            |  <h2>Commentaires r&eacute;currents</h2>
            |  <p class='section-note'>Ces commentaires se retrouvent &agrave; plusieurs endroits. Quelques exemples sont list&eacute;s ici, mais la liste n'est pas exhaustive.</p>
            |  <ul class='comment-list repeated-list'>
            |$items
            |  </ul>
            |</section>
            |""".trimMargin()
    }

    private fun filesSection(comments: List<LineComment>): String {
        if (comments.isEmpty()) {
            return ""
        }

        val cards = comments
            .groupBy { it.context.file.name }
            .toSortedMap()
            .entries
            .mapIndexed { index, (fileName, fileComments) ->
                val items = fileComments
                    .sortedBy { it.context.lineStart }
                    .joinToString("\n") { comment ->
                        commentRow(
                            "${esc(comment.comment)} <span class='file-ref'>l. ${comment.context.lineRange}</span>",
                            comment.isImportant,
                            snippetData = comment.context
                        )
                    }
                """
                |  <details class='file-card'${if (index == 0) " open" else ""}>
                |    <summary><span class='file-name'>${esc(fileName)}</span><span class='count-badge'>${fileComments.size}</span></summary>
                |    <ul class='comment-list'>
                |$items
                |    </ul>
                |  </details>
                """.trimMargin()
            }
            .joinToString("\n")

        return """
            |<section id='file-reviews'>
            |  <h2>Commentaires par fichier</h2>
            |$cards
            |</section>
            |""".trimMargin()
    }

    private fun commentRow(
        contentHtml: String,
        isImportant: Boolean,
        snippetData: LineCommentContext?,
        tag: String? = if (isImportant) "Important" else ""
    ): String {
        val row = StringBuilder()
        val tagAttribute = if (tag != null) " data-tag='$tag'" else ""
        row.append("<li class='comment${if (isImportant) " important" else ""}${if (snippetData != null) " has-snippet" else ""}'$tagAttribute>")
        row.append("<div class='comment-line'>")
        row.append(importantBadge(isImportant))
        row.append("<span class='comment-text'>$contentHtml</span>")
        row.append("</div>")

        if (snippetData != null) {
            val firstLine = maxOf(1, snippetData.lineStart - LineCommentContext.SNIPPET_RANGE_AROUND)
            row.append(
                "<div class='snip-data' hidden" +
                        " data-file='${esc(snippetData.file.name)}'" +
                        " data-range='${snippetData.lineRange}'" +
                        " data-first-line='$firstLine'" +
                        " data-hl-start='${snippetData.lineStart}'" +
                        " data-hl-end='${snippetData.lineEnd}'" +
                        " data-lang='${langFor(snippetData.file.extension)}'>"
            )
            row.append(esc(snippetData.snippet.ifBlank { "Aucun extrait disponible" }))
            row.append("</div>")
            row.append("<div class='snippet-inline' hidden></div>")
        }

        row.append("</li>")
        return row.toString()
    }

    private fun importantBadge(isImportant: Boolean): String =
        if (isImportant) "<span class='badge-important'>Important</span>" else ""

    private fun langFor(extension: String?): String = when (extension?.lowercase()) {
        "java", "kt", "kts" -> "code"
        "xml", "html", "xhtml", "fxml" -> "xml"
        else -> "plain"
    }

    private fun footer(): String {
        val date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        return """
            |<footer id='footer'>
            |  <p>Tous les num&eacute;ros entre [crochets] sont des r&eacute;f&eacute;rences aux 'bad smells' du chapitre 17 de Clean Code.</p>
            |  <p>Generated with &copy;CodeReviewReport on $date</p>
            |</footer>
            |""".trimMargin()
    }

    private fun snippetPanel(): String = """
        |<aside id='snippet-panel'>
        |  <div class='panel-header'><span id='panel-file'>Extrait de code</span><button id='panel-unpin' class='ghost-btn' hidden>D&eacute;tacher</button></div>
        |  <div id='panel-code'><p class='panel-hint'>Survolez un commentaire pour voir l'extrait de code. Cliquez pour l'&eacute;pingler.</p></div>
        |</aside>
        |""".trimMargin()

    // The report is fully self-contained: styles and behavior are inlined below.

    private fun css(): String = """
        :root {
          color-scheme: light dark;
          --bg: #f6f8fa;
          --card: #ffffff;
          --text: #1f2328;
          --muted: #656d76;
          --border: #d0d7de;
          --accent: #0969da;
          --important: #d1242f;
          --important-bg: #fff1f2;
          --hl-line: rgba(255, 200, 0, 0.18);
          --code-bg: #f6f8fa;
          --kw: #cf222e;
          --str: #0a3069;
          --cmt: #6e7781;
          --ann: #953800;
          --num: #0550ae;
        }
        @media (prefers-color-scheme: dark) {
          :root {
            --bg: #0d1117;
            --card: #161b22;
            --text: #e6edf3;
            --muted: #8b949e;
            --border: #30363d;
            --accent: #4493f8;
            --important: #f85149;
            --important-bg: rgba(248, 81, 73, 0.12);
            --hl-line: rgba(255, 200, 0, 0.12);
            --code-bg: #0d1117;
            --kw: #ff7b72;
            --str: #a5d6ff;
            --cmt: #8b949e;
            --ann: #d2a8ff;
            --num: #79c0ff;
          }
        }
        * { box-sizing: border-box; }
        body {
          margin: 0;
          background: var(--bg);
          color: var(--text);
          font: 15px/1.55 -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
        }
        #topbar {
          position: sticky;
          top: 0;
          z-index: 20;
          background: color-mix(in srgb, var(--card) 88%, transparent);
          backdrop-filter: blur(8px);
          border-bottom: 1px solid var(--border);
        }
        .topbar-inner {
          max-width: 1400px;
          margin: 0 auto;
          padding: 10px 24px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          gap: 16px;
          flex-wrap: wrap;
        }
        .title-block h1 { margin: 0; font-size: 1.15em; font-weight: 600; display: inline; }
        .project-name { color: var(--accent); }
        .stats { color: var(--muted); font-size: 0.85em; margin-left: 12px; }
        .controls { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
        #filters { display: flex; gap: 6px; }
        .chip {
          border: 1px solid var(--border);
          background: var(--card);
          color: var(--text);
          border-radius: 999px;
          padding: 3px 12px;
          font-size: 0.85em;
          cursor: pointer;
        }
        .chip.selected { background: var(--accent); border-color: var(--accent); color: #fff; }
        .ghost-btn {
          border: none;
          background: none;
          color: var(--accent);
          font-size: 0.85em;
          cursor: pointer;
          padding: 3px 6px;
        }
        .ghost-btn:hover { text-decoration: underline; }
        .layout {
          max-width: 1400px;
          margin: 0 auto;
          padding: 24px;
          display: grid;
          grid-template-columns: minmax(0, 1fr) minmax(320px, 560px);
          gap: 24px;
          align-items: start;
        }
        .content { min-width: 0; }
        .card, .file-card {
          background: var(--card);
          border: 1px solid var(--border);
          border-radius: 10px;
          padding: 16px 20px;
          margin-bottom: 20px;
        }
        h2 { font-size: 1.05em; margin: 0 0 10px; }
        #file-reviews > h2 { margin: 4px 0 12px; }
        .section-note {
          color: var(--muted);
          font-style: italic;
          font-size: 0.9em;
          border-left: 3px solid var(--border);
          padding-left: 10px;
          margin: 0 0 12px;
        }
        .comment-list { list-style: none; margin: 0; padding: 0; }
        .comment-list li { padding: 8px; border-radius: 6px; }
        .comment-text { white-space: pre-line; }
        #general-comments .comment-list > li + li,
        .file-card .comment-list > li + li { border-top: 1px solid var(--border); border-top-left-radius: 0; border-top-right-radius: 0; }
        .comment-list li.hidden-by-filter { display: none; }
        li.comment.has-snippet { cursor: pointer; }
        li.comment.has-snippet:hover { background: color-mix(in srgb, var(--accent) 8%, transparent); }
        li.comment.pinned { background: color-mix(in srgb, var(--accent) 14%, transparent); }
        .comment-line { display: flex; align-items: baseline; gap: 8px; }
        .badge-important {
          flex: none;
          background: var(--important-bg);
          color: var(--important);
          border: 1px solid color-mix(in srgb, var(--important) 40%, transparent);
          font-size: 0.72em;
          font-weight: 600;
          border-radius: 999px;
          padding: 1px 8px;
          text-transform: uppercase;
          letter-spacing: 0.03em;
        }
        .file-ref {
          font-family: ui-monospace, SFMono-Regular, 'JetBrains Mono', Consolas, monospace;
          font-size: 0.85em;
          color: var(--muted);
          white-space: nowrap;
        }
        .repeated-list > li { border: 1px solid var(--border); border-radius: 8px; padding: 10px 12px; margin-bottom: 10px; }
        .repeated-list > li.important { border-left: 3px solid var(--important); }
        .examples { margin-top: 6px; padding-left: 10px; border-left: 2px solid var(--border); }
        .file-card { padding: 0; overflow: hidden; }
        .file-card summary {
          cursor: pointer;
          padding: 12px 20px;
          display: flex;
          align-items: center;
          gap: 10px;
          font-weight: 600;
          user-select: none;
          list-style: none;
        }
        .file-card summary::-webkit-details-marker { display: none; }
        .file-card summary::before { content: '\25B8'; color: var(--muted); transition: transform 0.15s; }
        .file-card[open] summary::before { transform: rotate(90deg); }
        .file-name { font-family: ui-monospace, SFMono-Regular, 'JetBrains Mono', Consolas, monospace; font-size: 0.95em; }
        .count-badge {
          background: color-mix(in srgb, var(--accent) 14%, transparent);
          color: var(--accent);
          border-radius: 999px;
          font-size: 0.78em;
          padding: 1px 9px;
        }
        .file-card .comment-list { padding: 4px 12px 12px; }
        #snippet-panel {
          position: sticky;
          top: 76px;
          background: var(--card);
          border: 1px solid var(--border);
          border-radius: 10px;
          overflow: hidden;
          max-height: calc(100vh - 100px);
          display: flex;
          flex-direction: column;
        }
        .panel-header {
          padding: 10px 14px;
          border-bottom: 1px solid var(--border);
          font-family: ui-monospace, SFMono-Regular, 'JetBrains Mono', Consolas, monospace;
          font-size: 0.85em;
          color: var(--muted);
          display: flex;
          justify-content: space-between;
          align-items: center;
          gap: 8px;
        }
        #panel-code { overflow: auto; background: var(--code-bg); flex: 1; }
        .panel-hint { color: var(--muted); font-style: italic; padding: 14px; margin: 0; }
        .code-block {
          font-family: ui-monospace, SFMono-Regular, 'JetBrains Mono', Consolas, monospace;
          font-size: 12.5px;
          line-height: 1.5;
          padding: 8px 0;
          min-width: max-content;
        }
        .cl { display: flex; white-space: pre; }
        .cl.hl { background: var(--hl-line); }
        .ln {
          flex: none;
          width: 3.2em;
          text-align: right;
          padding-right: 12px;
          color: var(--muted);
          user-select: none;
        }
        .lc { padding-right: 16px; }
        .tok-kw { color: var(--kw); font-weight: 600; }
        .tok-str { color: var(--str); }
        .tok-cmt { color: var(--cmt); font-style: italic; }
        .tok-ann { color: var(--ann); }
        .tok-num { color: var(--num); }
        .snippet-inline { margin-top: 8px; border: 1px solid var(--border); border-radius: 8px; overflow: auto; background: var(--code-bg); max-height: 420px; }
        #footer { color: var(--muted); font-size: 0.85em; margin-top: 28px; border-top: 1px solid var(--border); padding-top: 12px; }
        #to-top {
          position: fixed;
          bottom: 22px;
          right: 22px;
          width: 42px;
          height: 42px;
          border-radius: 50%;
          border: 1px solid var(--border);
          background: var(--card);
          color: var(--text);
          font-size: 1.1em;
          cursor: pointer;
          opacity: 0;
          pointer-events: none;
          transition: opacity 0.2s;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
        }
        #to-top.visible { opacity: 1; pointer-events: auto; }
        @media (max-width: 1100px) {
          .layout { grid-template-columns: 1fr; }
          #snippet-panel { display: none; }
        }
        @media print {
          #topbar, #snippet-panel, #to-top { display: none; }
          .layout { display: block; }
        }
    """.trimIndent()

    private fun js(): String = """
        (function () {
          'use strict';

          var KEYWORDS = ('abstract assert boolean break byte case catch char class const continue default do double else enum ' +
            'extends final finally float for goto if implements import instanceof int interface long native new package ' +
            'private protected public return short static strictfp super switch synchronized this throw throws transient ' +
            'try void volatile while fun val var when object companion override data sealed open lateinit suspend inline ' +
            'is in as internal init constructor typealias reified out vararg by null true false').split(' ');

          function escapeHtml(s) {
            return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
          }

          function span(cls, text) {
            return '<span class="tok-' + cls + '">' + escapeHtml(text) + '</span>';
          }

          function highlightCodeLine(line, inBlock) {
            var out = '';
            var i = 0;
            while (i < line.length) {
              if (inBlock) {
                var end = line.indexOf('*/', i);
                if (end === -1) { out += span('cmt', line.slice(i)); i = line.length; }
                else { out += span('cmt', line.slice(i, end + 2)); i = end + 2; inBlock = false; }
                continue;
              }
              var rest = line.slice(i);
              var m;
              if (rest.lastIndexOf('/*', 0) === 0) { inBlock = true; continue; }
              if (rest.lastIndexOf('//', 0) === 0) { out += span('cmt', rest); i = line.length; continue; }
              if ((m = rest.match(/^"(?:[^"\\]|\\.)*(?:"|$)/))) { out += span('str', m[0]); i += m[0].length; continue; }
              if ((m = rest.match(/^'(?:[^'\\]|\\.)*(?:'|$)/))) { out += span('str', m[0]); i += m[0].length; continue; }
              if ((m = rest.match(/^@[A-Za-z_][A-Za-z0-9_]*/))) { out += span('ann', m[0]); i += m[0].length; continue; }
              if ((m = rest.match(/^[A-Za-z_][A-Za-z0-9_]*/))) {
                out += KEYWORDS.indexOf(m[0]) !== -1 ? span('kw', m[0]) : escapeHtml(m[0]);
                i += m[0].length;
                continue;
              }
              if ((m = rest.match(/^[0-9][0-9_.xXbLfa-fA-F]*/))) { out += span('num', m[0]); i += m[0].length; continue; }
              out += escapeHtml(rest.charAt(0));
              i += 1;
            }
            return { html: out, inBlock: inBlock };
          }

          function highlightXmlLine(line, inBlock) {
            var out = '';
            var i = 0;
            while (i < line.length) {
              if (inBlock) {
                var end = line.indexOf('-->', i);
                if (end === -1) { out += span('cmt', line.slice(i)); i = line.length; }
                else { out += span('cmt', line.slice(i, end + 3)); i = end + 3; inBlock = false; }
                continue;
              }
              var rest = line.slice(i);
              var m;
              if (rest.lastIndexOf('<!--', 0) === 0) { inBlock = true; continue; }
              if ((m = rest.match(/^<\/?[A-Za-z0-9:_-]+/))) { out += span('kw', m[0]); i += m[0].length; continue; }
              if ((m = rest.match(/^"[^"]*(?:"|$)/))) { out += span('str', m[0]); i += m[0].length; continue; }
              if ((m = rest.match(/^[A-Za-z0-9:_-]+=/))) { out += span('ann', m[0]); i += m[0].length; continue; }
              out += escapeHtml(rest.charAt(0));
              i += 1;
            }
            return { html: out, inBlock: inBlock };
          }

          function highlightCode(text, lang) {
            var lines = text.split('\n');
            var inBlock = false;
            return lines.map(function (line) {
              if (lang === 'plain') { return escapeHtml(line); }
              var r = lang === 'xml' ? highlightXmlLine(line, inBlock) : highlightCodeLine(line, inBlock);
              inBlock = r.inBlock;
              return r.html;
            });
          }

          function renderSnippet(data) {
            var text = data.textContent;
            var first = parseInt(data.getAttribute('data-first-line'), 10) || 1;
            var hlStart = parseInt(data.getAttribute('data-hl-start'), 10) || -1;
            var hlEnd = parseInt(data.getAttribute('data-hl-end'), 10) || -1;
            var lines = highlightCode(text, data.getAttribute('data-lang'));
            var html = '<div class="code-block">';
            for (var idx = 0; idx < lines.length; idx++) {
              var n = first + idx;
              var cls = (n >= hlStart && n <= hlEnd) ? ' hl' : '';
              html += '<div class="cl' + cls + '"><span class="ln">' + n + '</span><span class="lc">' + (lines[idx] || '&nbsp;') + '</span></div>';
            }
            return html + '</div>';
          }

          var panel = document.getElementById('snippet-panel');
          var panelFile = document.getElementById('panel-file');
          var panelCode = document.getElementById('panel-code');
          var panelUnpin = document.getElementById('panel-unpin');
          var pinned = null;

          function panelVisible() {
            return panel && panel.offsetParent !== null;
          }

          function showInPanel(row) {
            var data = row.querySelector('.snip-data');
            if (!data) { return; }
            panelFile.textContent = data.getAttribute('data-file') + ' : ' + data.getAttribute('data-range');
            panelCode.innerHTML = renderSnippet(data);
            var hl = panelCode.querySelector('.cl.hl');
            if (hl) { panelCode.scrollTop = Math.max(0, hl.offsetTop - panelCode.clientHeight / 2); }
          }

          function unpin() {
            if (pinned) { pinned.classList.remove('pinned'); pinned = null; }
            panelUnpin.hidden = true;
          }

          panelUnpin.addEventListener('click', unpin);

          var rows = document.querySelectorAll('li.has-snippet');
          rows.forEach(function (row) {
            row.addEventListener('mouseenter', function () {
              if (!pinned && panelVisible()) { showInPanel(row); }
            });
            row.addEventListener('click', function (event) {
              if (event.target.closest('a')) { return; }
              if (panelVisible()) {
                if (pinned === row) { unpin(); return; }
                unpin();
                pinned = row;
                row.classList.add('pinned');
                panelUnpin.hidden = false;
                showInPanel(row);
              } else {
                var inline = row.querySelector('.snippet-inline');
                var data = row.querySelector('.snip-data');
                if (!inline || !data) { return; }
                if (inline.hidden) {
                  inline.innerHTML = renderSnippet(data);
                  inline.hidden = false;
                } else {
                  inline.hidden = true;
                }
              }
            });
          });

          // Tag filters (built from the tags present in the document, like v1)
          var filters = document.getElementById('filters');
          var tags = [];
          document.querySelectorAll('[data-tag]').forEach(function (el) {
            var tag = el.getAttribute('data-tag');
            if (tag && tags.indexOf(tag) === -1) { tags.push(tag); }
          });
          if (tags.length > 0) {
            var allChip = document.createElement('button');
            allChip.className = 'chip selected';
            allChip.textContent = 'Tous';
            allChip.setAttribute('data-filter', '');
            filters.appendChild(allChip);
            tags.forEach(function (tag) {
              var chip = document.createElement('button');
              chip.className = 'chip';
              chip.textContent = tag + 's';
              chip.setAttribute('data-filter', tag);
              filters.appendChild(chip);
            });
            filters.addEventListener('click', function (event) {
              var chip = event.target.closest('.chip');
              if (!chip) { return; }
              filters.querySelectorAll('.chip').forEach(function (c) { c.classList.remove('selected'); });
              chip.classList.add('selected');
              var filter = chip.getAttribute('data-filter');
              document.querySelectorAll('[data-tag]').forEach(function (el) {
                el.classList.toggle('hidden-by-filter', filter !== '' && el.getAttribute('data-tag') !== filter);
              });
            });
          }

          // Fold / unfold all file cards
          document.getElementById('fold-all').addEventListener('click', function () {
            document.querySelectorAll('.file-card').forEach(function (d) { d.open = false; });
          });
          document.getElementById('unfold-all').addEventListener('click', function () {
            document.querySelectorAll('.file-card').forEach(function (d) { d.open = true; });
          });

          // Scroll to top
          var toTop = document.getElementById('to-top');
          window.addEventListener('scroll', function () {
            toTop.classList.toggle('visible', window.scrollY > 300);
          });
          toTop.addEventListener('click', function () {
            window.scrollTo({ top: 0, behavior: 'smooth' });
          });
        })();
    """.trimIndent()
}

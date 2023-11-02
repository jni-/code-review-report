package ca.ulaval.glo4002.codereview.app.infra.html

import ca.ulaval.glo4002.codereview.app.model.LineComment
import ca.ulaval.glo4002.codereview.app.model.Review
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.*
import java.nio.charset.Charset
import java.util.*


@Service(Service.Level.PROJECT)
class HtmlReviewGenerator(val project: Project) {

    fun generate(review: Review): String = HtmlReviewBuilder()
        .addHtmlHeaders(project.name)
        .addH1Title(project.name)
        .addGeneralComments(review.generalComments)
        .addRepeatedComments(review.repeatedComments)
        .addFileComments(review.lineComments)
        .addFooter()
        .build()

    fun generateForTools(review: Review, selectedComment: UUID?): String = HtmlReviewBuilder()
        .setToolMode()
        .addHtmlHeaders(project.name)
        .addH1Title(project.name)
        .addGeneralComments(review.generalComments)
        .addRepeatedComments(review.repeatedComments, selectedComment)
        .addFileComments(review.lineComments, selectedComment)
        .addFooter()
        .build()
}

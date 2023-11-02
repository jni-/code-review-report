package ca.ulaval.glo4002.codereview.listeners

import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame

internal class ReloadProjectApplicationActivationListener : ApplicationActivationListener {
    override fun applicationActivated(ideFrame: IdeFrame) {
        thisLogger().info("Projet reloaded: Reloading existing review if present")
        ideFrame.project?.service<CodeReviewService>()?.reload()
    }
}

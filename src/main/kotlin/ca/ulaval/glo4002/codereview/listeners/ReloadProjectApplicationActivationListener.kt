package ca.ulaval.glo4002.codereview.listeners

import ca.ulaval.glo4002.codereview.app.services.CodeReviewService
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame

internal class ReloadProjectApplicationActivationListener : ApplicationActivationListener {
    override fun applicationActivated(ideFrame: IdeFrame) {
        val project = ideFrame.project ?: return
        if (project.isDisposed || project.isDefault || !project.isInitialized) {
            return
        }

        thisLogger().info("Project activated: Reloading existing review if present")
        project.service<CodeReviewService>().reload()
    }
}

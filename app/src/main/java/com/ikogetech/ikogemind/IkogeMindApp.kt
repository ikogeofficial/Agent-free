package com.ikogetech.ikogemind

import android.app.Application
import com.ikogetech.ikogemind.data.local.AppDatabase
import com.ikogetech.ikogemind.data.remote.ModelRouter
import com.ikogetech.ikogemind.data.repository.ChatRepository
import com.ikogetech.ikogemind.data.repository.SettingsRepository
import com.ikogetech.ikogemind.pipeline.ContextStep
import com.ikogetech.ikogemind.pipeline.FormatOutputStep
import com.ikogetech.ikogemind.pipeline.ModelStep
import com.ikogetech.ikogemind.pipeline.PipelineOrchestrator
import com.ikogetech.ikogemind.pipeline.PostProcessStep

/**
 * Simple manual DI. No Hilt/Koin for v1 — one dev, one app, a service-locator
 * singleton is plenty. Revisit if the graph gets messy once agents land.
 */
class IkogeMindApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var chatRepository: ChatRepository
        private set

    lateinit var pipelineOrchestrator: PipelineOrchestrator
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.build(this)
        settingsRepository = SettingsRepository(this)

        val modelRouter = ModelRouter(settingsRepository)

        chatRepository = ChatRepository(
            conversationDao = database.conversationDao(),
            messageDao = database.messageDao()
        )

        // Pipeline order matches architecture.md:
        // ContextStep -> ModelStep -> PostProcessStep -> FormatOutputStep
        pipelineOrchestrator = PipelineOrchestrator(
            steps = listOf(
                ContextStep(messageDao = database.messageDao()),
                ModelStep(modelRouter = modelRouter),
                PostProcessStep(),
                FormatOutputStep()
            )
        )
    }
}

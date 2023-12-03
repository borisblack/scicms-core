package ru.scisolutions.scicmscore.api.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.Part
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.model.input.UploadInput
import ru.scisolutions.scicmscore.persistence.service.MediaService
import java.net.URLEncoder

@RestController
@RequestMapping("/api/media")
class MediaController(
    private val mediaService: MediaService,
    private val engine: Engine
) {
    @PostMapping("/upload")
    fun upload(
        @RequestParam("file") file: Part,
        @RequestParam("label") label: String?,
        @RequestParam("description") description: String?,
        @RequestParam("permission") permission: String?
    ): MediaInfo {
        val uploadInput = UploadInput(
            file = file,
            label = label,
            description = description,
            permissionId = permission
        )
        return engine.uploadData(uploadInput)
    }

    @PostMapping("/upload-multiple")
    fun uploadMultiple(
        @RequestParam("files") files: Array<Part>,
        @RequestParam("labels") labels: Array<String>,
        @RequestParam("descriptions") descriptions: Array<String>,
        @RequestParam("permissions") permissions: Array<String>
    ): List<MediaInfo> {
        val uploadInputList = files.mapIndexed { i, file ->
            UploadInput(
                file = file,
                label = labels[i].ifBlank { null },
                description = descriptions[i].ifBlank { null },
                permissionId = permissions[i].ifBlank { null }
            )
        }

        return engine.uploadDataMultiple(uploadInputList)
    }

    @GetMapping("/{id}/download")
    fun download(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<*> {
        val media = mediaService.findByIdForRead(id) ?: return ResponseEntity.notFound().build<Unit>()
        val resource = engine.downloadById(id)
        val filename = URLEncoder.encode(media.filename.replace(" ", "_"), "UTF-8")

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''$filename")
            .contentType(MediaType.valueOf(media.mimetype))
            .contentLength(media.fileSize)
            .body(resource)
    }
}
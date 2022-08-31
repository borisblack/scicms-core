package ru.scisolutions.scicmscore.api.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.persistence.service.MediaService
import java.net.URLEncoder
import java.util.UUID
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/media")
class MediaController(
    private val mediaService: MediaService,
    private val engine: Engine
) {
    @GetMapping("/{id}/download")
    fun download(@PathVariable id: UUID, request: HttpServletRequest): ResponseEntity<*> {
        val media = mediaService.findByIdForRead(id.toString()) ?: return ResponseEntity.notFound().build<Unit>()
        val resource = engine.downloadById(id)
        val filename = URLEncoder.encode(media.filename.replace(" ", "_"), "UTF-8")

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''$filename")
            .contentType(MediaType.valueOf(media.mimetype))
            .contentLength(media.fileSize)
            .body(resource)
    }
}
package ru.scisolutions.scicmscore.api.controller

import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.service.MediaService
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/media")
class MediaController(
    private val mediaService: MediaService,
    private val dataEngine: DataEngine
) {
    @GetMapping("/{id}/download")
    fun download(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<ByteArrayResource> {
        val media = mediaService.getById(id)
        val resource = dataEngine.download(media)
        val filename = URLEncoder.encode(media.filename.replace(" ", "_"), "UTF-8")

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''$filename")
            .contentType(MediaType.valueOf(media.mimetype))
            .contentLength(media.fileSize)
            .body(resource)
    }
}
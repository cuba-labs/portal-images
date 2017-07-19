package com.company.demo.portal.controllers;

import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileLoader;
import com.haulmont.cuba.core.global.FileTypesHelper;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.security.app.UserSessionService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Controller
public class PngFilesController {

    private final Logger log = LoggerFactory.getLogger(PngFilesController.class);

    @Inject
    protected UserSessionService userSessionService;

    @Inject
    protected DataService dataService;

    @Inject
    protected FileLoader fileLoader; // file loader is used to load file data from middleware to clients

    @GetMapping("/images/{fileId}")
    public void downloadImage(@PathVariable String fileId,
                              @RequestParam(required = false) Boolean attachment,
                              HttpServletResponse response) throws IOException {
        UUID uuid;
        try {
            uuid = UUID.fromString(fileId);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid entity ID");
            return;
        }
        LoadContext<FileDescriptor> ctx = LoadContext.create(FileDescriptor.class).setId(uuid);
        FileDescriptor fd = dataService.load(ctx);

        // We will send only PNG files here
        if (fd == null || !"png".equals(fd.getExtension())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // YOU HAVE TO CHECK SECURITY RULES MANUALLY HERE OR SETUP ACCESS GROUP CONSTRAINTS FOR ALL USERS !

        try {
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Type", getContentType(fd));
            response.setHeader("Content-Disposition", (BooleanUtils.isTrue(attachment) ? "attachment" : "inline")
                    + "; filename=\"" + fd.getName() + "\"");

            ServletOutputStream os = response.getOutputStream();
            try (InputStream is = fileLoader.openStream(fd)) {
                IOUtils.copy(is, os);
                os.flush();
            }
        } catch (Exception e) {
            log.error("Error on downloading the file {}", fileId, e);

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected String getContentType(FileDescriptor fd) {
        if (StringUtils.isEmpty(fd.getExtension())) {
            return FileTypesHelper.DEFAULT_MIME_TYPE;
        }

        return FileTypesHelper.getMIMEType("." + fd.getExtension().toLowerCase());
    }
}
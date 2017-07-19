package com.company.demo.portal.controllers;

import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.LoadContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class PortalController {
    @Inject
    protected DataService dataService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model) {
        // load all .png files from db
        List<FileDescriptor> files = dataService.loadList(LoadContext.create(FileDescriptor.class)
                .setQuery(new LoadContext.Query("select fd from sys$FileDescriptor fd")));
        List<UUID> fileIds = files.stream()
                .filter(fd -> "png".equals(fd.getExtension()))
                .map(BaseUuidEntity::getId)
                .collect(Collectors.toList());

        model.addAttribute("fileIds", fileIds);

        return "index";
    }
}
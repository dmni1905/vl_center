package com.spring.boot.vlt.mvc.controller;

import com.spring.boot.vlt.common.AccessUtils;
import com.spring.boot.vlt.mvc.model.UserContext;
import com.spring.boot.vlt.mvc.service.LaboratoryFrameService;
import com.spring.boot.vlt.mvc.service.UploadFileService;
import com.spring.boot.vlt.mvc.service.VltService;
import com.spring.boot.vlt.security.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api")
public class UploadFileController {
    @Autowired
    private UploadFileService uploadFileService;
    @Autowired
    LaboratoryFrameService laboratoryFrameService;
    @Autowired
    private VltService vltService;

    @RequestMapping(value = "/upload-file/{dir}", method = RequestMethod.POST)
    public ResponseEntity<String> uploadFile(
            JwtAuthenticationToken token,
            @RequestParam("uploadfile") MultipartFile uploadfile,
            @PathVariable("dir") String dir) {
        UserContext userContext = (UserContext) token.getPrincipal();
        if (AccessUtils.isDeveloperOrAdmin(userContext)) {
            boolean upload = uploadFileService.upload(userContext.getUsername(), uploadfile, dir);
            if (upload) {
                if (chekAndSetUrl(dir)) {
                    return new ResponseEntity("\"error:not set url\"", HttpStatus.OK);
                } else {
                    return new ResponseEntity("\"ok\"", HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    private boolean chekAndSetUrl(String dir) {
        laboratoryFrameService.setPreCondition(dir, null);
        String url = laboratoryFrameService.getUrl();
        return vltService.chekAndSaveUrl(vltService.getVl(dir), url);
    }


}

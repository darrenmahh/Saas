package org.getoffer.shortlink.project.controller;

import lombok.RequiredArgsConstructor;
import org.getoffer.shortlink.project.common.convention.result.Result;
import org.getoffer.shortlink.project.common.convention.result.Results;
import org.getoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.project.service.ShortLinkService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO reqDTO) {
        return Results.success(shortLinkService.createShortLink(reqDTO));
    }
}

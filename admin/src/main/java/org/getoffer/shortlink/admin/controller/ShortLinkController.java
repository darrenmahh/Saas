package org.getoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.dto.ShortLinkRemoteService;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.getoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.getoffer.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
    };

    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO reqDTO) {
        return shortLinkRemoteService.createShortLink(reqDTO);
    }

    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }
}

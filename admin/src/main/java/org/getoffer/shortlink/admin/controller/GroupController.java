package org.getoffer.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.common.convention.result.Results;
import org.getoffer.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.getoffer.shortlink.admin.service.GroupService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/api/short-link/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO reqDTO) {
        groupService.saveGroup(reqDTO.getName());
        return Results.success();
    }
}

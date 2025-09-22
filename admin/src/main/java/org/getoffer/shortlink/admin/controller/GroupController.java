package org.getoffer.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import org.getoffer.shortlink.admin.common.convention.result.Result;
import org.getoffer.shortlink.admin.common.convention.result.Results;
import org.getoffer.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.getoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import org.getoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.getoffer.shortlink.admin.dto.resq.ShortLinkGroupSaveRespDTO;
import org.getoffer.shortlink.admin.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/api/short-link/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO reqDTO) {
        groupService.saveGroup(reqDTO.getName());
        return Results.success();
    }

    @GetMapping("/api/short-link/v1/group")
    public Result<List<ShortLinkGroupSaveRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }

    @PutMapping("/api/short-link/v1/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupUpdateReqDTO reqDTO) {
        groupService.update(reqDTO);
        return Results.success();
    }

    @DeleteMapping("/api/short-link/v1/group")
    public Result<Void> updateGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return  Results.success();
    }

    @PostMapping("/api/short-link/v1/group/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> reqDTO) {
        groupService.sortGroup(reqDTO);
        return Results.success();
    }
}

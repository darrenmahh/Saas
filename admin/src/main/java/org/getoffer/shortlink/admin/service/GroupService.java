package org.getoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.getoffer.shortlink.admin.dao.entity.GroupDO;
import org.getoffer.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import org.getoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.getoffer.shortlink.admin.dto.resq.ShortLinkGroupRespDTO;

import java.util.List;

public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分子
     * @param groupName 分组名
     */
    // void saveGroup(String groupName);

    /**
     * 查询短链接分组集合
     * @return 短链接分组集合
     */
    List<ShortLinkGroupRespDTO> listGroup();

    void update(ShortLinkGroupUpdateReqDTO reqDTO);

    void deleteGroup(String gid);

    void sortGroup(List<ShortLinkGroupSortReqDTO> reqDTO);

    void saveGroup(String groupName);

    void saveGroup(String username, String groupName);
}

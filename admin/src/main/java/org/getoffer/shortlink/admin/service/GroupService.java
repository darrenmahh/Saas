package org.getoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.getoffer.shortlink.admin.dao.entity.GroupDO;

public interface GroupService extends IService<GroupDO> {
    /**
     * 新增短链接分子
     * @param groupName 分组名
     */
    void saveGroup(String groupName);
}

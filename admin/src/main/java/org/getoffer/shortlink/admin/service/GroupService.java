package org.getoffer.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.getoffer.shortlink.admin.dao.entity.GroupDO;

public interface GroupService extends IService<GroupDO> {
    void saveGroup(String groupName);
}

package org.getoffer.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.getoffer.shortlink.admin.dao.entity.GroupDO;
import org.getoffer.shortlink.admin.dao.mapper.GroupMapper;
import org.getoffer.shortlink.admin.service.GroupService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
}

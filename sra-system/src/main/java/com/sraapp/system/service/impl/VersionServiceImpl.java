package com.sraapp.system.service.impl;

import cn.hutool.core.convert.Convert;
import com.sraapp.common.model.BusinessException;
import com.sraapp.system.entity.Version;
import com.sraapp.system.param.version.VersionAddParam;
import com.sraapp.system.param.version.VersionPageParam;
import com.sraapp.system.param.version.VersionUpdateParam;
import com.sraapp.system.service.IVersionService;
import com.sraapp.system.vo.VersionVO;
import org.sagacity.sqltoy.dao.SqlToyLazyDao;
import org.sagacity.sqltoy.model.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VersionServiceImpl implements IVersionService {
    @Resource
    private SqlToyLazyDao sqlToyLazyDao;

    @Override
    public boolean add(VersionAddParam param) throws BusinessException {
        Version version = Convert.convert(Version.class, param);
        Object save = sqlToyLazyDao.save(version);
        return save != null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatch(List<String> idList) throws BusinessException {
        for (String s : idList) {
            delete(s);
        }
        return !idList.isEmpty();
    }

    @Override
    public boolean update(VersionUpdateParam param) throws BusinessException {
        Version version = Convert.convert(Version.class, param);
        Long count = sqlToyLazyDao.update(version);
        return count > 0;
    }

    @Override
    public Page<VersionVO> listByPage(VersionPageParam param) throws BusinessException {
        Page<VersionVO> page = sqlToyLazyDao.findPageBySql(param, "system_version_findByPageParam", param.getVersion());
        return page;
    }

    @Override
    public boolean delete(String id) throws BusinessException {
        return sqlToyLazyDao.delete(new Version().setId(id)) > 0;
    }
}

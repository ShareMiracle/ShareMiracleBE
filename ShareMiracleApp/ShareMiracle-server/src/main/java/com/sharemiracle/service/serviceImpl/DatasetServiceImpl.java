package com.sharemiracle.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharemiracle.context.BaseContext;
import com.sharemiracle.dto.DatasetDTO;
import com.sharemiracle.dto.DatasetDeleteDTO;
import com.sharemiracle.dto.DatasetOrganDTO;
import com.sharemiracle.dto.DatasetQueryDTO;
import com.sharemiracle.entity.Dataset;
import com.sharemiracle.entity.Organization;
import com.sharemiracle.exception.DeletionNotAllowedException;
import com.sharemiracle.mapper.DatasetMapper;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.DatasetService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DatasetServiceImpl implements DatasetService {

    @Resource
    private DatasetMapper datasetMapper;

    public void add(DatasetDTO datasetDTO) {
        Dataset dataset = new Dataset();
        BeanUtils.copyProperties(datasetDTO,dataset);

        dataset.setCreateTime(LocalDateTime.now());
        dataset.setUpdateTime(LocalDateTime.now());

        datasetMapper.insert(dataset);

        Long datasetId = dataset.getId();

        List<Organization> shareOrganization = datasetDTO.getShareOrganization();
        if(shareOrganization != null && !shareOrganization.isEmpty()) {
            for (Organization organization : shareOrganization) {
                Long organizationId = organization.getId();
                datasetMapper.insertDatasetOrgan(datasetId, organizationId);
            }
        }
    }

    public Dataset selectById(DatasetQueryDTO datasetQueryDTO){
        Long id = datasetQueryDTO.getId();
        return datasetMapper.selectById(id);
    }

    public List<Long> selectAll() {
        Long userId = BaseContext.getCurrentId();
        Long organID = datasetMapper.selectOrganId(userId);
        int status = datasetMapper.selectStatus(userId);
        if(status == 0){
            throw new DeletionNotAllowedException("查询失败");
        }
        return datasetMapper.selectAll(organID);
    }

    public void updateDatasetOrgan(DatasetOrganDTO datasetOrganDTO) {
        Long userId = BaseContext.getCurrentId();
        Long datasetId = datasetOrganDTO.getDatasetId();
        Long auth = datasetMapper.selectAuthorityById(datasetId);
        if(!Objects.equals(auth, userId)){
            throw new DeletionNotAllowedException("无权修改");
        }
        List<Long> ids = datasetOrganDTO.getIds();
        for(Long id : ids){
            datasetMapper.updateDatasetOrgan(datasetId,id);
        }
    }

    public void deleteBatch(DatasetDeleteDTO datasetDeleteDTO) {
        Long userId = BaseContext.getCurrentId();
        List<Long> ids = datasetDeleteDTO.getIds();

        for (Long id : ids) {
            Long auth = datasetMapper.selectAuthorityById(id);
            if(!Objects.equals(auth, userId)){
                throw new DeletionNotAllowedException("删除失败");
            }else{
                datasetMapper.deletebyId(id);
            }

        }

    }

    public Result<String> update(DatasetDTO datasetDTO) {
        Dataset dataset = new Dataset();
        BeanUtils.copyProperties(datasetDTO,dataset);

        dataset.setUpdateTime(LocalDateTime.now());

        Long userId = BaseContext.getCurrentId();
        Long datasetId = dataset.getId();
        Long auth = datasetMapper.selectAuthorityById(datasetId);

        if(!Objects.equals(auth, userId)){
            return Result.error("您没有权限修改数据集信息");
        }else{
            datasetMapper.update(dataset);
        }
        return Result.success();

    }

    public Result<String> updateStatus(DatasetDTO datasetDTO) {
        Dataset dataset = new Dataset();
        BeanUtils.copyProperties(datasetDTO,dataset);

        dataset.setUpdateTime(LocalDateTime.now());

        Long userId = BaseContext.getCurrentId();
        Long datasetId = dataset.getId();
        Long auth = datasetMapper.selectAuthorityById(datasetId);

        if(!Objects.equals(auth, userId)){
            return Result.error("您没有权限修改数据集私有状态");
        }else{
            datasetMapper.update(dataset);
        }
        return Result.success();
    }

    public void delete(DatasetDeleteDTO datasetDeleteDTO) {
        Long userId = BaseContext.getCurrentId();
        Long id = datasetDeleteDTO.getId();

        Long auth = datasetMapper.selectAuthorityById(id);
        if(!Objects.equals(auth, userId)){
            throw new DeletionNotAllowedException("删除失败");
        }else{
            datasetMapper.deletebyId(id);
        }
    }
}

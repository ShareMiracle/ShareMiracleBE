package com.sharemiracle.service.serviceImpl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharemiracle.constant.FileConstant;
import com.sharemiracle.context.BaseContext;
import com.sharemiracle.dto.*;
import com.sharemiracle.entity.Dataset;
import com.sharemiracle.entity.Organization;
import com.sharemiracle.exception.DeletionNotAllowedException;
import com.sharemiracle.mapper.DatasetMapper;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.DatasetService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset> implements DatasetService {

    @Resource
    private DatasetMapper datasetMapper;

    @Override
    public Result<String> add(DatasetDTO datasetDTO) {
        Dataset dataset = new Dataset();
        BeanUtils.copyProperties(datasetDTO,dataset);

        dataset.setCreateTime(LocalDateTime.now());
        dataset.setUpdateTime(LocalDateTime.now());
        Long userId = BaseContext.getCurrentId();
        dataset.setUserId(userId);

        int isSuccess = datasetMapper.insert(dataset);

        Long datasetId = dataset.getId();

        List<Organization> shareOrganization = datasetDTO.getShareOrganization();
        if(shareOrganization != null && !shareOrganization.isEmpty()) {
            for (Organization organization : shareOrganization) {
                Long organizationId = organization.getId();
                // Long id = datasetId+organizationId;
                datasetMapper.insertDatasetOrgan(datasetId, organizationId);
            }
        }
        // 上传文件
        if(isSuccess>0) return Result.success("新建数据集成功！");
        return Result.error("新建数据集失败！");
    }

    @Override
    public void delete(DatasetDeleteDTO datasetDeleteDTO) {
        Long userId = BaseContext.getCurrentId();
        Long id = datasetDeleteDTO.getId();
        String filename = getById(id).getName();

        Long auth = datasetMapper.selectAuthorityById(id);
        if(!Objects.equals(auth, userId)){
            throw new DeletionNotAllowedException("删除失败");
        }else{
            datasetMapper.deleteById(id);
        }
    }

    @Override
    public void deleteBatch(DatasetDeleteDTO datasetDeleteDTO) {
        Long userId = BaseContext.getCurrentId();
        List<Long> ids = datasetDeleteDTO.getIds();

        for (Long id : ids) {
            Long auth = datasetMapper.selectAuthorityById(id);
            String filename = getById(id).getName();
            if(!Objects.equals(auth, userId)){
                throw new DeletionNotAllowedException("删除失败");
            }else{
                datasetMapper.deleteById(id);
            }
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public Dataset selectById(DatasetQueryDTO datasetQueryDTO){
        Long id = datasetQueryDTO.getId();
        return datasetMapper.selectById(id);
    }

    @Override
    public List<Long> selectAll() {
        Long userId = BaseContext.getCurrentId();

        List<Long> organIDs = datasetMapper.selectOrganId(userId);
        Set<Long> uniqueIds = new HashSet<>();
        uniqueIds.addAll(datasetMapper.selectAllByUserId(userId));
        uniqueIds.addAll(datasetMapper.selectAllisPublic());

        if (organIDs.isEmpty()) {
            return new ArrayList<>(uniqueIds);
        }
        for(Long organID : organIDs) {
            int status = datasetMapper.selectStatus(userId,organID);
            if(status == 0){
                throw new DeletionNotAllowedException("查询失败");
            }
            uniqueIds.addAll(datasetMapper.selectAll(organID));
        }
        return new ArrayList<>(uniqueIds);
    }

//    /**
//     * 上传文件
//     * @param file
//     * @return
//     */
//    public Boolean uploadFile(MultipartFile file) {
//        try {
//            // 获取原始文件名称
//            String originalFilename = file.getOriginalFilename();
//            // 生成新文件名
//            String fileName = createNewFileName(originalFilename);
//            // 保存文件
//            file.transferTo(new File(FileConstant.DATASET_UPLOAD_DIR, fileName));
//            // 返回结果
//            log.debug("文件上传成功，{" + fileName + "}");
//            return true;
//        } catch (IOException e) {
//            return false;
//        }
//    }
//
//    private String createNewFileName(String originalFilename) {
//        // 获取后缀
//        String suffix = StrUtil.subAfter(originalFilename, ".", true);
//        // 生成目录
//        String name = UUID.randomUUID().toString();
//        int hash = name.hashCode();
//        int d1 = hash & 0xF;
//        int d2 = (hash >> 4) & 0xF;
//        // 判断目录是否存在
//        File dir = new File(FileConstant.DATASET_UPLOAD_DIR, StrUtil.format("/dataset/{}/{}", d1, d2));
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        // 生成文件名
//        return StrUtil.format("/dataset/{}/{}/{}.{}", d1, d2, name, suffix);
//    }
//
//    public Boolean deleteDataset(String filename) {
//        File file = new File(FileConstant.DATASET_UPLOAD_DIR, filename);
//        if (file.isDirectory()) {
//            log.error("文件名称错误!");
//            return false;
//        }
//        FileUtil.del(file);
//        return true;
//    }
//
//    private void saveFileToLocal(MultipartFile file) throws IOException {
//        // TODO: 将文件保存到服务器
//    }
//

}

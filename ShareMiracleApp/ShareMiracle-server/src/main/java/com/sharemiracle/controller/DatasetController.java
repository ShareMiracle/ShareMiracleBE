package com.sharemiracle.controller;

import com.sharemiracle.dto.DatasetDTO;
import com.sharemiracle.dto.DatasetDeleteDTO;
import com.sharemiracle.dto.DatasetOrganDTO;
import com.sharemiracle.dto.DatasetQueryDTO;
import com.sharemiracle.entity.Dataset;
import com.sharemiracle.result.Result;
import com.sharemiracle.service.DatasetService;
import com.sharemiracle.utils.FileUtil;
import com.sharemiracle.vo.DatasetOrganVO;
import com.sharemiracle.vo.DatasetQueryAllVO;
import com.sharemiracle.vo.DatasetQueryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/dataset")
public class DatasetController {

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private FileUtil fileUtil;

    /**
     * 新建数据集
     * @param datasetDTO
     * @return
     */
    @PostMapping("/add")
    public Result<String> buildModelController(@RequestBody DatasetDTO datasetDTO) {
        log.info("新建数据集：{}", datasetDTO);
        datasetService.add(datasetDTO);
        return Result.success();
    }

    /**
     * 删除数据集
     * @param datasetDeleteDTO
     * @param filename
     * @return
     */
    @DeleteMapping("/delete")
    public Result delete(@RequestBody DatasetDeleteDTO datasetDeleteDTO, @RequestParam("name") String filename) {
        log.info("删除数据集数据：{}", datasetDeleteDTO.getId());
        datasetService.delete(datasetDeleteDTO);
        return Result.success();
    }

    /**
     * 批量删除数据集
     * @param datasetDeleteDTO
     * @return
     */
    @DeleteMapping("/delete-batch")
    public Result deleteBatch(@RequestBody DatasetDeleteDTO datasetDeleteDTO) {
        log.info("批量删除数据集数据：{}", datasetDeleteDTO.getIds());
        datasetService.deleteBatch(datasetDeleteDTO);
        return Result.success();
    }

    /**
     * 修改数据集信息
     * @param datasetDTO
     * @return
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody DatasetDTO datasetDTO) {
        log.info("修改数据集信息：{}", datasetDTO);
        return datasetService.update(datasetDTO);
    }

    /**
     * 修改数据集私有状态
     * @param datasetDTO
     * @return
     */
    @PutMapping("/status")
    public Result<String> updateStatus(@RequestBody DatasetDTO datasetDTO) {
        log.info("修改数据集私有状态：{}",datasetDTO.getId());
        return datasetService.updateStatus(datasetDTO);
    }


    /**
     * 修改数据集有权使用组织
     * @param datasetOrganDTO
     * @return
     */
    @PutMapping("/organ")
    public Result<DatasetOrganVO> updateDatasetOrgan(@RequestBody DatasetOrganDTO datasetOrganDTO) {
        log.info("修改数据集有权使用组织: {}",datasetOrganDTO);

        datasetService.updateDatasetOrgan(datasetOrganDTO);

        log.info("修改数据集有权使用组织成功");

        return Result.success();
    }


    /**
     * 请求数据集信息
     * @param datasetQueryDTO
     * @return
     */
    @GetMapping("/query-by-id")
    public Result<DatasetQueryVO> selectById(@RequestBody  DatasetQueryDTO datasetQueryDTO) {
        log.info("请求数据集信息: {}",datasetQueryDTO);

        Dataset dataset = datasetService.selectById(datasetQueryDTO);

        DatasetQueryVO datasetqueryVO = DatasetQueryVO.builder()
                .datasetUrl(dataset.getDatasetUrl())
                .build();
        return Result.success(datasetqueryVO);
    }


    /**
     * 查询当前用户有权使用的所有数据集
     * @return
     */
    @GetMapping("/query-all")
    public Result<DatasetQueryAllVO> selectAll() {
        log.info("查询当前用户有权使用的所有数据集");

        List<Long> list = datasetService.selectAll();

        DatasetQueryAllVO datasetqueryallVO = DatasetQueryAllVO.builder()
                .ids(list)
                .build();
        return Result.success(datasetqueryallVO);
    }

    /**
     * 下载文件
     * @return
     * @throws Exception
     */
    public ResponseEntity<Resource> download() throws Exception {
        String filePath = "D:\\test.txt";
        String fileName = "文件下载.txt";
        return fileUtil.download(filePath,fileName);
    }

}

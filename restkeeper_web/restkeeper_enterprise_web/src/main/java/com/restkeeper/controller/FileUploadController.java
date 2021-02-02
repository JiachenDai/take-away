package com.restkeeper.controller;

import com.aliyun.oss.OSSClient;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Api("图片上传通用接口")
@RestController
@RefreshScope
public class FileUploadController {

    @Autowired
    private OSSClient ossClient;

    @Value("${bucketName}")
    private String bucketName;

    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;


    @PostMapping("/fileUpload")
    public Result fileUpload(@RequestParam("file") MultipartFile multipartFile){
        Result result = new Result();
        //定义文件名
        String fileName = System.currentTimeMillis() + "_" + multipartFile.getName();

        //执行图片上传
        try {
            ossClient.putObject(bucketName, fileName, multipartFile.getInputStream());
            //已经上传成功，但是需要返回图片路径给前端
            String logoPath = "https://" + bucketName + "." + endpoint + "/" + fileName;
            result.setData(logoPath);
            result.setStatus(ResultCode.success);
            result.setDesc("上传成功");
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("上传失败");
            return result;
        }
    }

    @PostMapping("/imageUploadResize")
    @ApiImplicitParam(paramType = "form", dataType = "file", name = "fileName", value = "上传文件", required = true)
    public Result imageUploadResize(@RequestParam("fileName") MultipartFile multipartFile){
        Result result = new Result();
        String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
        //图片上传
        try {
            ossClient.putObject(bucketName, fileName, multipartFile.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("图片上传失败");
            return result;
        }

        //设置文件路径,根据这个url访问oss上的图片就会按照设置的调整返回图片大小
        String imagePath = "https://" + bucketName + "." + endpoint + "/" + fileName + "?x-oss-process=image/resize,m_fill,h_100,w_200";
        result.setStatus(ResultCode.success);
        result.setDesc("图片上传成功");
        result.setData(imagePath);
        return result;
    }
}

package com.oxchains.service;

import com.oxchains.bean.dto.ProductDTO;
import com.oxchains.bean.model.ziyun.Goods;
import com.oxchains.bean.model.ziyun.Product;
import com.oxchains.common.RespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 产品信息Service
 * Created by Luo_xuri on 2017/7/5.
 */
@Service
@Slf4j
public class ProductService extends BaseService {

    @Resource
    private ChaincodeService chaincodeService;

    public RespDTO<List<Product>> getProductList(String approvalNumber, String productCode) {
        System.err.println("-->产品批准文号：" + approvalNumber + "\r\n -->产品编码：" + productCode);
        String jsonStr = chaincodeService.query("getProductList", new String[]{approvalNumber, productCode});
        System.err.println("-->产品JSON：" + jsonStr);
        if (StringUtils.isEmpty(jsonStr)) {
            return RespDTO.fail("没有数据");
        }
        ProductDTO productDTO = simpleGson.fromJson(jsonStr, ProductDTO.class);
        System.err.println("-->产品集合：" + productDTO.getList());
        return RespDTO.success(productDTO.getList());
    }

}
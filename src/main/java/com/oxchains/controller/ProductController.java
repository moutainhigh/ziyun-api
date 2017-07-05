package com.oxchains.controller;

import com.google.gson.JsonSyntaxException;
import com.oxchains.bean.model.ziyun.Goods;
import com.oxchains.bean.model.ziyun.Product;
import com.oxchains.common.ConstantsData;
import com.oxchains.common.RespDTO;
import com.oxchains.service.ChaincodeService;
import com.oxchains.service.ProductService;
import com.sun.org.apache.regexp.internal.RESyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 产品信息Controller
 * Created by root on 17-7-3.
 */
@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController extends BaseController {
    @Resource
    private ChaincodeService chaincodeService;

    @Resource
    private ProductService productService;

    @PostMapping
    public RespDTO<String> addProduct(@RequestBody String body){
        try {
            log.debug("===addProduct==="+body);
            Product product = gson.fromJson(body, Product.class);
            String txID = chaincodeService.invoke("addProduct", new String[] { gson.toJson(product) });
            log.debug("===txID==="+txID);
            if(txID == null){
                return RespDTO.fail("操作失败", ConstantsData.RTN_SERVER_INTERNAL_ERROR);
            }
            return RespDTO.success("操作成功");
        }
        catch(JsonSyntaxException e){
            log.error(e.getMessage());
            return RespDTO.fail("操作失败", ConstantsData.RTN_INVALID_ARGS);
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return RespDTO.fail("操作失败", ConstantsData.RTN_SERVER_INTERNAL_ERROR);
        }
    }

    @GetMapping("/{ApprovalNumber}/{ProductCode}")
    public RespDTO<List<Product>> queryProductInfo(@PathVariable String ApprovalNumber, @PathVariable String ProductCode) {
        try {
            return productService.getProductList(ApprovalNumber, ProductCode);
        }catch (Exception e) {
            log.error("query error!", e);
        }
        return RespDTO.fail();
    }

}
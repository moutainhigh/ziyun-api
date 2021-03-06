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
import org.apache.commons.lang.StringUtils;
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
    private ProductService productService;

    @PostMapping
    public RespDTO<String> addProduct(@RequestBody String body,@RequestParam String Token){
        try {
            log.info("===addProduct==="+body);
            if (StringUtils.isBlank(body)) {
                return RespDTO.fail("参数错误");
            }
            Product product = gson.fromJson(body, Product.class);
            product.setToken(Token);
            return productService.addProduct(product);
        } catch(JsonSyntaxException e){
            log.error("addProduct error: ",e);
            return RespDTO.fail("操作失败", ConstantsData.RTN_INVALID_ARGS);
        } catch (Exception e) {
            log.error("addProduct error: ",e);
            return RespDTO.fail("操作失败", ConstantsData.RTN_SERVER_INTERNAL_ERROR);
        }
    }

    @GetMapping("/{Id}")
    public RespDTO<List<Product>> queryProductInfo(@PathVariable String Id, @RequestParam String Token) {
        try {
            log.info("===queryProductInfo===");
            return productService.getProductList(Id,Token);
        }catch (Exception e) {
            log.error("queryProductInfo error: ", e);
        }
        return RespDTO.fail();
    }

}

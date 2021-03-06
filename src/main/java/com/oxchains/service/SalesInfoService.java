package com.oxchains.service;

import com.oxchains.bean.dto.PurchaseInfoDTO;
import com.oxchains.bean.dto.SalesInfoDTO;
import com.oxchains.bean.model.ziyun.Auth;
import com.oxchains.bean.model.ziyun.JwtToken;
import com.oxchains.bean.model.ziyun.PurchaseInfo;
import com.oxchains.bean.model.ziyun.SalesInfo;
import com.oxchains.common.ConstantsData;
import com.oxchains.common.RespDTO;
import com.oxchains.util.TokenUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by root on 17-7-5.
 */
@Service
@Slf4j
public class SalesInfoService extends BaseService  {
    @Resource
    private ChaincodeService chaincodeService;

    public RespDTO<String> addSalesInfo(SalesInfo salesInfo) throws Exception{
        String token = salesInfo.getToken();
        JwtToken jwt = TokenUtils.parseToken(token);
        salesInfo.setToken(jwt.getId());// store username ,not token
        String txID = chaincodeService.invoke("saveSalesInfo", new String[] { gson.toJson(salesInfo) });
        log.debug("===txID==="+txID);
        if(txID == null){
            return RespDTO.fail("操作失败", ConstantsData.RTN_SERVER_INTERNAL_ERROR);
        }
        return RespDTO.success("操作成功");
    }

    public RespDTO<List<SalesInfo>> querySalesInfoList(String Id,String Token ){
        String jsonStr = chaincodeService.getPayloadAndTxid("searchByQuery", new String[]{
                "{\"selector\":{\"Id\" : \""+Id + "\"}}"});
        if (StringUtils.isEmpty(jsonStr) || "null".equals(jsonStr)) {
            return RespDTO.fail("没有数据");
        }
        String txId = jsonStr.split("!#!")[1];
        jsonStr =  jsonStr.split("!#!")[0];
        SalesInfoDTO salesInfoDTO = simpleGson.fromJson(jsonStr, SalesInfoDTO.class);

        JwtToken jwt = TokenUtils.parseToken(Token);
        String username = jwt.getId();
        for (Iterator<SalesInfo> it = salesInfoDTO.getList().iterator(); it.hasNext();) {
            SalesInfo SalesInfo = it.next();
            SalesInfo.setTxId(txId);
            log.debug("===SalesInfo.getToken()==="+SalesInfo.getToken());
            String jsonAuth = chaincodeService.query("query", new String[] { SalesInfo.getToken() });
            log.info("===jsonAuth==="+jsonAuth);
            Auth auth = gson.fromJson(jsonAuth, Auth.class);
            ArrayList<String> authList = auth.getAuthList();
            log.info("===username==="+username);
            if(!authList.contains(username)){
                log.debug("===remove===");
                it.remove();
            }
        }
        if(salesInfoDTO.getList().isEmpty()){
            return RespDTO.fail("操作失败", ConstantsData.RTN_UNAUTH);
        }

        return RespDTO.success(salesInfoDTO.getList());
    }
}

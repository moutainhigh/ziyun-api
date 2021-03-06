package com.oxchains.bean.model.ziyun;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oxchains.common.BaseEntity;

import java.util.List;

/**
 * Created by root on 17-7-3.
 * 采购信息
 */
public class PurchaseInfo extends BaseEntity {
    @JsonProperty("Id")
    private String Id;

    @JsonProperty("PurchaseTitle")
    private String PurchaseTitle;//采购标题

    @JsonProperty("Count")
    private int Count;//采购数量

    @JsonProperty("EnterpriseId")
    private String EnterpriseId;//所属企业id

    @JsonProperty("EnterpriseName")
    private String EnterpriseName;//企业名字

    @JsonProperty("StockDate")
    private long StockDate;//进货日期 时间戳

    @JsonProperty("SupperName")
    private String SupperName;//供货公司名

    @JsonProperty("SupperAddress")
    private String SupperAddress;//供货地址

    @JsonProperty("SupplyName")
    private String SupplyName;//负责人姓名

    @JsonProperty("SupplyPhone")
    private String SupplyPhone;//负责人联系方式


    @JsonProperty("Type")
    private String Type;

    @JsonProperty("UniqueCodes")
    private List<String> UniqueCodes;

    @JsonProperty("CreateTime")
    private long CreateTime;

    @JsonProperty("PreEnterpriseId")
    private String PreEnterpriseId;//上游企业Id

    @JsonProperty("PreEnterpriseName")
    private String PreEnterpriseName;//上游企业名称

    @JsonProperty("Token")
    private String Token;//token

    @JsonIgnore
    public String getToken() {
        return Token;
    }

    public void setToken(String Token) {
        this.Token = Token;
    }

    private String TxId;

    public void setTxId(String txId) {
        TxId = txId;
    }

    public String getTxId() {
        return TxId;
    }
}

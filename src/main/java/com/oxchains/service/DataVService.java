package com.oxchains.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.oxchains.bean.dto.datav.NameValue;
import com.oxchains.bean.dto.datav.ValueContent;
import com.oxchains.bean.dto.datav.XY;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * DataVService
 *
 * @author liuruichao
 * Created on 2017/4/18 17:31
 */
@Service
@Slf4j
public class DataVService extends BaseService {
    @Resource
    private ChaincodeService chaincodeService;

    public NameValue<Long> getChainHeight() throws InvalidProtocolBufferException, ProposalException, InvalidArgumentException {
        BlockchainInfo blockchainInfo = getBlockChain();

        return new NameValue<>("", blockchainInfo.getHeight());
    }

    public NameValue<Long> getChainTxCount() throws InvalidProtocolBufferException, ProposalException, InvalidArgumentException {
        Long count = 0L;
        BlockchainInfo blockchainInfo = getBlockChain();
        //blockchainInfo.
        for (int i = 1; i < blockchainInfo.getHeight(); i++) {
            BlockInfo blockInfo = chaincodeService.queryBlock(i);
            Common.BlockData blockData = blockInfo.getBlock().getData();
            log.debug("height: " + i + ", blockData count: " + blockData.getDataCount());
            count += blockData.getDataCount();
        }
        return new NameValue<>("", count);
    }

    /**
     * 查询最近十天的交易量
     * @return
     */
    public List<XY> getChainTxNum() throws InvalidProtocolBufferException, ProposalException, InvalidArgumentException {
        int num = 10;
        List<XY> list = new ArrayList<>(num);

        BlockchainInfo blockchainInfo = getBlockChain();
        long startIndex = blockchainInfo.getHeight() - num;
        if (startIndex - 10 < 0) {
            startIndex = 0;
        }
        for (int i = (int) (blockchainInfo.getHeight() - 1); i >= startIndex; i--) {
            BlockInfo blockInfo = chaincodeService.queryBlock(i);
            list.add(new XY(i + "", (long) blockInfo.getBlock().getData().getDataCount()));
        }

        return list;
    }

    public List<ValueContent> getChainNewBlock() throws InvalidProtocolBufferException, ProposalException, InvalidArgumentException {
        int num = 25;
        List<ValueContent> list = new ArrayList<>(num);

        BlockchainInfo blockchainInfo = getBlockChain();
        long startIndex = blockchainInfo.getHeight() - num;
        if (startIndex - 10 < 0) {
            startIndex = 0;
        }
        String contentTmpl = "区块号：#{blockNum}；状态哈希值：#{currentHash}上一区块哈希：#{previousHash}";
        for (int i = (int) (blockchainInfo.getHeight() - 1); i >= startIndex; i--) {
            BlockInfo blockInfo = chaincodeService.queryBlock(i);

            String content = contentTmpl.replace("#{blockNum}", blockInfo.getBlockNumber() + "")
                    .replace("#{currentHash}", Hex.encodeHexString(blockInfo.getBlock().getHeader().getDataHash().toByteArray()))
                    .replace("#{previousHash}", Hex.encodeHexString(blockInfo.getPreviousHash()));
            list.add(new ValueContent("100", content));
        }

        return list;
    }

    private BlockchainInfo getBlockChain() throws InvalidProtocolBufferException, ProposalException, InvalidArgumentException {
        // TODO cache
        return chaincodeService.queryChain();
    }
}
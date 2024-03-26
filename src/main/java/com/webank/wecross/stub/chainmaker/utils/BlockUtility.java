package com.webank.wecross.stub.chainmaker.utils;

import com.webank.wecross.stub.Block;
import com.webank.wecross.stub.BlockHeader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.bouncycastle.util.encoders.Hex;
import org.chainmaker.pb.common.ChainmakerBlock;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.fisco.bcos.sdk.utils.ObjectMapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockUtility {

  private static final Logger logger = LoggerFactory.getLogger(BlockUtility.class);

  /**
   * convert chainMaker Block to BlockHeader
   *
   * @param chainmakerBlock
   * @return
   */
  public static BlockHeader convertToBlockHeader(ChainmakerBlock.Block chainmakerBlock) {
    ChainmakerBlock.BlockHeader chainMakerHeader = chainmakerBlock.getHeader();
    BlockHeader blockHeader = new BlockHeader();
    // 获取区块hash
    String blockHash = Hex.toHexString(chainMakerHeader.getBlockHash().toByteArray());
    blockHeader.setHash(blockHash);
    // 获取上一区块hash
    String prevHash =
        ObjectUtils.isEmpty(chainMakerHeader.getPreBlockHash().toByteArray())
            ? null
            : Hex.toHexString(chainMakerHeader.getPreBlockHash().toByteArray());
    blockHeader.setPrevHash(prevHash);
    blockHeader.setNumber(chainMakerHeader.getBlockHeight());
    // TransactionRoot
    String txRoot = Hex.toHexString(chainMakerHeader.getTxRoot().toByteArray());
    blockHeader.setTransactionRoot(txRoot);
    blockHeader.setTimestamp(chainMakerHeader.getBlockTimestamp());
    return blockHeader;
  }

  /**
   * convert chainMaker Tx to TxHashes
   *
   * @param chainmakerBlock
   * @param onlyHeader
   * @return
   * @throws IOException
   */
  public static Block convertToBlock(ChainmakerBlock.Block chainmakerBlock, boolean onlyHeader) {
    Block stubBlock = new Block();
    /** BlockHeader */
    BlockHeader blockHeader = convertToBlockHeader(chainmakerBlock);
    stubBlock.setBlockHeader(blockHeader);
    /** tx list */
    List<String> txs = new ArrayList<>();
    if (!onlyHeader && chainmakerBlock.getTxsCount() > 0) {
      for (int i = 0; i < chainmakerBlock.getTxsList().size(); i++) {
        ChainmakerTransaction.Transaction transaction = chainmakerBlock.getTxsList().get(i);
        txs.add(transaction.getPayload().getTxId());
      }
    }
    stubBlock.setTransactionsHashes(txs);
    return stubBlock;
  }

  /**
   * @param blockBytes
   * @return
   */
  public static Block convertToBlock(byte[] blockBytes, boolean onlyHeader) throws IOException {
    ChainmakerBlock.BlockInfo blockInfo =
        ObjectMapperFactory.getObjectMapper()
            .readValue(blockBytes, ChainmakerBlock.BlockInfo.class);
    ChainmakerBlock.Block chainmakerBlock = blockInfo.getBlock();
    if (logger.isDebugEnabled()) {
      logger.debug(
          "blockNumber: {}, blockHash: {}",
          chainmakerBlock.getHeader().getBlockHeight(),
          chainmakerBlock.getHeader().getBlockHash());
    }
    Block stubBlock = convertToBlock(chainmakerBlock, onlyHeader);
    stubBlock.setRawBytes(blockBytes);
    return stubBlock;
  }
}

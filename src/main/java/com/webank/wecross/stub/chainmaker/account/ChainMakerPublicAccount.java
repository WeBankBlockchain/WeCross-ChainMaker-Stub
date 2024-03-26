package com.webank.wecross.stub.chainmaker.account;

import com.webank.wecross.stub.Account;

public class ChainMakerPublicAccount implements Account {
  private final String name;
  private final String type;
  private final String publicKey;
  private int keyID;
  private boolean isDefault;

  // FIXME: privateKey format
  public ChainMakerPublicAccount(String name, String type, String publicKey) {
    this.name = name;
    this.type = type;
    this.publicKey = publicKey;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getIdentity() {
    // TODO: get address
    return publicKey;
  }

  @Override
  public int getKeyID() {
    return keyID;
  }

  @Override
  public boolean isDefault() {
    return isDefault;
  }
}

#!/bin/bash
set -e
LOG_INFO() {
    local content=${1}
    echo -e "\033[32m ${content}\033[0m"
}

get_sed_cmd()
{
  local sed_cmd="sed -i"
  if [ "$(uname)" == "Darwin" ];then
        sed_cmd="sed -i .bkp"
  fi
  echo "$sed_cmd"
}

check_basic()
{
# build
bash gradlew build assemble
}

#LOG_INFO "------ download_build_chain---------"
#download_build_chain
LOG_INFO "------ check_basic---------"
check_basic
#LOG_INFO "------ check_ecdsa_evm_node---------"
#check_ecdsa_evm_node
#LOG_INFO "------ check_sm_evm_node---------"
#check_sm_evm_node

#bash <(curl -s https://codecov.io/bash)

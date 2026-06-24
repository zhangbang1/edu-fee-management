package com.edufee.fee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edufee.fee.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 缴费记录 Mapper 接口
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

}

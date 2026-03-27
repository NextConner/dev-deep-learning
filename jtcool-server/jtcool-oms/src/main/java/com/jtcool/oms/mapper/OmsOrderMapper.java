package com.jtcool.oms.mapper;

import com.jtcool.oms.domain.OmsOrder;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OmsOrderMapper {
    List<OmsOrder> selectOmsOrderList(OmsOrder omsOrder);
    OmsOrder selectOmsOrderById(Long orderId);
    int insertOmsOrder(OmsOrder omsOrder);
    int updateOmsOrder(OmsOrder omsOrder);
    int deleteOmsOrderByIds(Long[] orderIds);

    List<OmsOrder> selectOrderListPaginated(@Param("order") OmsOrder order, @Param("pageSize") int pageSize, @Param("offset") int offset);
    long countOrderList(OmsOrder order);
}

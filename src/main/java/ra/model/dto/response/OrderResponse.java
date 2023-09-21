package ra.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.model.domain.OrderDetail;
import ra.model.domain.OrderStatusName;
import ra.model.domain.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String receiverName;
    private String address;
    private String phone;
    private Date date;
    @JsonIgnore
    private Users users;
    @JsonIgnore
    private List<OrderDetail> orderDetails= new ArrayList<>();
    private double totalAmount;
    private OrderStatusName orderStatusName;
}
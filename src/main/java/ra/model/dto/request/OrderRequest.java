package ra.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.model.domain.OrderStatusName;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String receiverName;
    private String address;
    private String phone;
     private Long orderStatusName = 1L;
     //1L, nghĩa là nó chứa số nguyên 1 dưới dạng kiểu Long
}

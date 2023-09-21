package ra.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ra.model.domain.*;
import ra.model.dto.request.OrderRequest;
import ra.model.dto.response.OrderResponse;
import ra.repository.IOrderDetailRepository;
import ra.repository.IOrderRepository;
import ra.repository.IOrderStatusNameRepository;
import ra.repository.IProductRepository;
import ra.service.IOrderService;
import ra.service.mapper.OrderMapper;

import javax.persistence.EntityExistsException;
import ra.exception.OrderException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService implements IOrderService<Order> {
    @Autowired
    private IOrderRepository orderRepository;
     @Autowired
    private IOrderStatusNameRepository orderStatusNameRepository;
    @Autowired
    private CartItemService cartItemService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private IProductRepository productRepository;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> findById(Long id) throws EntityExistsException {
        if (!orderRepository.findById(id).isPresent()) {
            throw new EntityExistsException("ID Order not found");
        }
        return orderRepository.findById(id);
    }

    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public OrderResponse checkOut(Users users, List<CartItem> cartItems, OrderRequest orderRequest) throws OrderException {
        // Kiểm tra xem giỏ hàng có sản phẩm không
        if (cartItems.isEmpty()) {
            // Nếu giỏ hàng rỗng, hiển thị thông báo và không thực hiện thanh toán
            throw new EntityExistsException("Giỏ hàng của bạn đang rỗng");
        }

        // Tạo đơn hàng và thiết lập thông tin từ OrderRequest
        Order order = createOrder(users, cartItems);
        order.setReceiverName(orderRequest.getReceiverName());
        order.setPhone(orderRequest.getPhone());
        order.setAddress(orderRequest.getAddress());
        order.setDate(new Date());
        order.setUsers(users);
        order.setOrderStatusNames(findByIdStatusOder(orderRequest.getOrderStatusName()).get());
        order = save(order);

        // Xóa tất cả sản phẩm trong giỏ hàng khi đã checkOut
        cartItemService.deleteAll(users.getId());

        // Sử dụng mapper để chuyển đổi Order thành OrderResponse và trả về
        return orderMapper.toResponse(order);
    }


    public Order cancelled(Users users, Long id) {
          Optional<Order> o = orderRepository.findById(id);
          if (o.isPresent()) {
            Order order = o.get();
            if (order.getOrderStatusNames().getId() == 2) {
                throw new EntityExistsException("Đơn hàng đang giao không thể huỷ");
            } else if (order.getOrderStatusNames().getId() == 4) {
                throw new EntityExistsException("Đơn hàng đã huỷ");
            } else if (order.getOrderStatusNames().getId() == 3) {
                throw new EntityExistsException("Đơn hàng đã giao thành công  không thể huỷ");
            }
             for (OrderDetail orderDetail : order.getOrderDetails()) {
                 Product product = orderDetail.getProduct();
                 int orderQuantity = orderDetail.getQuantity();
                 product.setStock(product.getStock() + orderQuantity);
                 productRepository.save(product);
              }
            order.setOrderStatusNames(findByIdStatusOder(4L).get());
            return orderRepository.save(order);

        } else {
            throw new EntityExistsException("Id not found");
        }
    }

    public Order confirmOrder(Users users, Long id) {
        Optional<Order> o = orderRepository.findById(id);
        if (o.isPresent()) {
            Order order = o.get();
            Long  StatusId = order.getOrderStatusNames().getId();
            if (StatusId == 1) {
                order.setOrderStatusNames(findByIdStatusOder(2L).get());

            } else if (StatusId == 2) {

                order.setOrderStatusNames(findByIdStatusOder(3L).get());
            } else if (StatusId == 3) {
                for (OrderDetail orderDetail : order.getOrderDetails()) {
                    Product product = orderDetail.getProduct();
                    int orderQuantity = orderDetail.getQuantity();
                    product.setStock(product.getStock() + orderQuantity);
                    productRepository.save(product);
                }
                order.setOrderStatusNames(findByIdStatusOder(4L).get());
            } else {
                throw new EntityExistsException("Invalid order status");
            }

            // Lưu lại đơn hàng đã cập nhật
            return orderRepository.save(order);
        } else {
            throw new EntityExistsException("Id not found");
        }
    }


    public Order createOrder(Users users, List<CartItem> cartItems) throws EntityExistsException {
        Order order = new Order();
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int orderQuantity = cartItem.getQuantity();
            if (product.getStock() >= orderQuantity) {
                product.setStock(product.getStock() - orderQuantity);
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrder(order);
                orderDetail.setProduct(cartItem.getProduct());
                orderDetail.setQuantity(cartItem.getQuantity());
                orderDetails.add(orderDetail);
            } else {
                throw new EntityExistsException("số lượng sản phẩm " + product.getProductName() + " chỉ còn " + product.getStock() + "sản phẩm");
            }
        }
        order.setOrderDetails(orderDetails);
        double totalAmount = calculateTotalAmount(orderDetails);
        order.setTotalAmount(totalAmount);
        return order;
    }


    private double calculateTotalAmount(List<OrderDetail> orderDetails) {
        double totalAmount = 0;
        for (OrderDetail od : orderDetails) {
            double itemTotal = od.getQuantity() * od.getProduct().getPrice();
            totalAmount += itemTotal;
        }
        return totalAmount;
    }

    @Override
    public Optional<OrderStatusName> findByIdStatusOder(Long id) {
        return orderStatusNameRepository.findById(id);
    }
}




